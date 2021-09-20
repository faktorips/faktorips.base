/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.maven.plugin.mojo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.toolchain.Toolchain;
import org.apache.maven.toolchain.ToolchainManager;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.FileUtils;
import org.eclipse.sisu.equinox.EquinoxServiceFactory;
import org.eclipse.sisu.equinox.launching.EquinoxInstallationFactory;
import org.eclipse.sisu.equinox.launching.EquinoxLauncher;
import org.eclipse.tycho.core.maven.ToolchainProvider;
import org.eclipse.tycho.extras.eclipserun.EclipseRunMojo;
import org.eclipse.tycho.extras.eclipserun.LoggingEclipseRunMojo;
import org.eclipse.tycho.plugins.p2.extras.Repository;
import org.faktorips.maven.plugin.mojo.internal.GitStatusPorcelain;
import org.faktorips.maven.plugin.mojo.internal.LoggingMode;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Builds the Faktor-IPS project.
 * <p>
 * By default, the latest Faktor-IPS is used with an Eclipse 2019-03 runtime, all installed from
 * <a href="https://faktorzehn.org">faktorzehn.org</a> update sites.
 * <p>
 * To change from where the plugins are installed, see {@link #additionalRepositories},
 * {@link #repositories}, {@link #fipsRepository}/{@link #fipsRepositoryVersion} and
 * {@link #eclipseRepository}.
 * <p>
 * Additional plugins (like the Faktor-IPS Product Variant Plugin) can be configured with
 * {@link #additionalPlugins}.
 */
@Mojo(name = "faktorips-build", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true)
public class IpsBuildMojo extends AbstractMojo {

    private static final Pattern MAJOR_MINOR_VERSION_PATTERN = Pattern.compile("(\\d+)\\.(\\d+).*");

    /**
     * Whether to add default dependencies to bundles org.eclipse.equinox.launcher, org.eclipse.osgi
     * and org.eclipse.core.runtime.
     */
    // @Parameter(defaultValue = "true")
    private boolean addDefaultDependencies = true;

    /**
     * Execution environment profile name used to resolve dependencies and run Faktor-IPS. Must be
     * at least JavaSE-11.
     */
    @Parameter(defaultValue = "JavaSE-11")
    private String executionEnvironment;

    /**
     * Whether to skip mojo execution.
     */
    @Parameter(property = "faktorips.skip", defaultValue = "false")
    private boolean skip;

    /**
     * Dependencies which will be resolved transitively to make up the eclipse runtime.
     */
    // Not a parameter, for internal use only
    private List<Dependency> dependencies = new ArrayList<>();

    /**
     * Additional Eclipse-plugins which will be resolved transitively to make up the eclipse
     * runtime.
     * <p>
     * Example:
     * 
     * <pre>
     * {@code
     * <additionalPlugins>
     *  <dependency>
     *   <artifactId>org.faktorips.productvariant.core</artifactId>
     *   <type>eclipse-plugin</type>
     *  </dependency>
     * </additionalPlugins>
     * }
     * </pre>
     * 
     * If JUnit is integrated as an Eclipse library, one of the following two dependencies must be
     * added:
     *
     * <pre>
     * {@code
     * <additionalPlugins>
     *  <dependency>
     *   <artifactId>org.eclipse.jdt.junit</artifactId>
     *   <type>eclipse-plugin</type>
     *  </dependency>
     * </additionalPlugins>
     * }
     * </pre>
     *
     * or
     * 
     * <pre>
     * {@code
     * <additionalPlugins>
     *  <dependency>
     *   <artifactId>org.eclipse.jdt.junit5.runtime</artifactId>
     *   <type>eclipse-plugin</type>
     *  </dependency>
     * </additionalPlugins>
     * }
     * </pre>
     */
    @Parameter
    private List<Dependency> additionalPlugins = new ArrayList<>();

    /**
     * List of JVM arguments set on the command line.
     * <p>
     * Example:
     * 
     * <pre>
     * {@code
     * <jvmArgs>
     *   <args>-javaagent:lombok.jar</args>
     * </jvmArgs>
     * }
     * </pre>
     */
    @Parameter
    private List<String> jvmArgs = new ArrayList<>();

    /**
     * Path to a local repository.
     */
    @Parameter(property = "maven.repo.local")
    private String localRepository;

    /**
     * List of applications arguments set on the command line.
     * <p>
     * Example:
     *
     * <pre>
     * {@code
     * <applicationsArgs>
     *  <args>-buildfile</args>
     *  <args>build-test.xml</args>
     * </applicationsArgs>
     * }
     * </pre>
     */
    // @Parameter
    private List<String> applicationsArgs = new ArrayList<>();

    /**
     * p2 repositories which will be used to resolve dependencies. If the default values should be
     * used this parameter must remain unused. Additional repositories can then be defined using the
     * {@link #additionalRepositories} parameter. The paths of the default repositories can be
     * changed individually using the parameters {@link #fipsRepository} and
     * {@link #eclipseRepository} or the properties {@code repository.fips},
     * {@code repository.eclipse}.
     * <p>
     * Example:
     * 
     * <pre>
     * {@code
     * <repositories>
     *  <repository>
     *   <id>faktor-ips-21-6</id>
     *   <layout>p2</layout>
     *   <url>https://update.faktorzehn.org/faktorips/v21_6/</url>
     *  </repository>
     *  <repository>
     *   <id>eclipse-2020-12</id>
     *   <layout>p2</layout>
     *   <url>http://download.eclipse.org/eclipse/updates/4.18/</url>
     *  </repository>
     * </repositories>
     * }
     * </pre>
     * 
     * @see #additionalRepositories
     */
    @Parameter
    private List<Repository> repositories = new ArrayList<>();
    /**
     * This parameter makes it possible to define additional repositories while using the default
     * repositories (for which the {@link #repositories} parameter must remain unused).
     * <p>
     * Example:
     * 
     * <pre>
     * {@code
     * <additionalRepositories>
     *  <repository>
     *   <id>productvariants</id>
     *   <layout>p2</layout>
     *   <url>https://update.faktorzehn.org/faktorips/productvariants/21.6</url>
     *  </repository>
     * </additionalRepositories>
     * }
     * </pre>
     */
    @Parameter
    private List<Repository> additionalRepositories = new ArrayList<>();

    /**
     * Kill the forked process after a certain number of seconds. If set to 0, wait forever for the
     * process, never timing out.
     */
    // @Parameter(property = "eclipserun.timeout")
    private int forkedProcessTimeoutInSeconds;

    /**
     * Additional environments to set for the forked JVM.
     */
    // @Parameter
    private Map<String, String> environmentVariables = Collections.emptyMap();

    /**
     * Work area. This includes:
     * <ul>
     * <li><b>&lt;work&gt;/configuration</b>: The configuration area (<b>-configuration</b>)
     * <li><b>&lt;work&gt;/data</b>: The data ('workspace') area (<b>-data</b>)
     * </ul>
     */
    @Parameter(defaultValue = "${java.io.tmpdir}/${project.name}/eclipserun-work")
    private File work;

    @Parameter(property = "session", readonly = true, required = true)
    private MavenSession session;

    /**
     * Path to an ant build file. If no path is specified, a new script is generated.
     */
    @Parameter(property = "ant.script")
    private String antScriptPath;

    /**
     * Name of the ant target to call. If no target name is specified, import is used.
     */
    @Parameter(property = "ant.target", defaultValue = "import")
    private String antTarget;

    /**
     * Whether to include the HTML export. It will be generated in {@code target/html}.
     * <p>
     * <em>UI-libraries are required for the HTML export to work, so if you run this build on a CI
     * server like Jenkins, make sure to install for example the Xvfb fake X server.</em>
     * <p>
     * To package the generated HTML export in its own JAR, configure the maven-jar-plugin as
     * follows:
     *
     * <pre>
     * &lt;plugin&gt;
     *   &lt;artifactId&gt;maven-jar-plugin&lt;/artifactId&gt;
     *   &lt;executions&gt;
     *     &lt;execution&gt;
     *       &lt;configuration&gt;
     *         &lt;classifier&gt;html&lt;/classifier&gt;
     *         &lt;classesDirectory&gt;${project.build.directory}/html&lt;/classesDirectory&gt;
     *         &lt;includes&gt;**&#47;*&lt;/includes&gt;
     *       &lt;/configuration&gt;
     *       &lt;id&gt;pack-html&lt;/id&gt;
     *       &lt;phase&gt;package&lt;/phase&gt;
     *       &lt;goals&gt;
     *         &lt;goal&gt;jar&lt;/goal&gt;
     *       &lt;/goals&gt;
     *     &lt;/execution&gt;
     *   &lt;/executions&gt;
     * &lt;/plugin&gt;
     * </pre>
     */
    // we can't use @code for this Javadoc, because it includes } and **/
    @Parameter(defaultValue = "false")
    private boolean exportHtml;

    /**
     * Path to the JDK the project should build against. If no path is specified, the parameter
     * {@link #jdkId} will be evaluated. If both parameters do not contain a value, the
     * {@link #executionEnvironment} will be used as default.
     */
    @Parameter(property = "jdk.dir")
    private String jdkDir;

    /**
     * ID of the JDK the project should build against. The corresponding JDK must be configured in
     * the Maven {@code toolchains.xml} This parameter is only evaluated if {@link #jdkDir} is not
     * specified. If this parameter does not contain a value either, the
     * {@link #executionEnvironment} will be used as default.
     */
    @Parameter(property = "jdk.id")
    private String jdkId;

    /**
     * Whether to import the Faktor-IPS project as a Maven project. If set to {@code true} the m2e
     * plugin will be used to import the pom.xml of the Faktor-IPS project.
     */
    @Parameter(defaultValue = "true")
    private boolean importAsMavenProject;

    /**
     * The version of the Faktor-IPS repository to be used. This parameter is used in the default
     * {@link #fipsRepository} property.
     */
    @Parameter(property = "faktorips.repository.version")
    private String fipsRepositoryVersion;

    /**
     * Path to the update site to install Faktor-IPS. The default uses the official Faktor-IPS
     * repository for the {@link #fipsRepositoryVersion}.
     */
    @Parameter(property = "repository.fips", defaultValue = "https://update.faktorzehn.org/faktorips/${faktorips.repository.version}/")
    private String fipsRepository;

    /**
     * Path to the update site to install Eclipse.
     */
    @Parameter(property = "repository.eclipse", defaultValue = "https://download.eclipse.org/eclipse/updates/4.11/")
    private String eclipseRepository;

    /**
     * Starts the build in debug mode and pauses it until a remote debugger has been connected. The
     * default debug port is 8000; a different port can be configured with {@link #debugPort}.
     */
    @Parameter(property = "faktorips.debug")
    private boolean debug;

    /**
     * The port on which the started Faktor-IPS build will listen for a remote debugger.
     *
     * @see #debug
     */
    @Parameter(property = "faktorips.debug.port", defaultValue = "8000")
    private int debugPort;

    /**
     * The full path to a custom trace-options file. This option can significantly increase the
     * logging output. Per default an extensive list of trace-options is added when maven is started
     * with {@code -X} or {@code -debug}.
     * <p>
     * For more information see
     * <a href="https://wiki.eclipse.org/FAQ_How_do_I_use_the_platform_debug_tracing_facility">FAQ
     * How do I use the platform debug tracing facility</a>
     */
    @Parameter(property = "faktorips.debuglog.options")
    private String debugLogOptions;

    /**
     * Runs a Git diff on the project to see if any files where changed. Can be configured to only
     * print warnings or to fail the build for uncommitted changes.
     *
     * <pre>
     * {@code
     * <gitStatusPorcelain>
     *     <failBuild>false</failBuild>
     *     <verbosity>VERBOSE</verbosity>
     * </gitStatusPorcelain>
     * }
     * </pre>
     */
    @Parameter
    private GitStatusPorcelain gitStatusPorcelain;

    /**
     * Whether to only start the {@code IpsBuilder} or all configured builders. The default is to
     * use only the {@code IpsBuilder} as it creates the Java source files before the Maven compiler
     * plugin compiles the entire project.
     */
    @Parameter(defaultValue = "true")
    private boolean buildIpsOnly;

    /**
     * There are three different logging modes available for this plugin.
     * <ul>
     * <li>original</li>
     * <li>perThread</li>
     * <li>perThreadFiltered</li>
     * </ul>
     * Each mode will only affect the output of the Faktor-IPS build e.g. importing, creating java
     * classes or do a HTML model export.
     * <p>
     * <strong>original:</strong> will redirect everything to STDOUT.
     * <p>
     * <strong>perThread:</strong> will redirect the STDOUT of every thread to it's own file, and
     * output one file after the other.
     * <p>
     * <strong>perThreadFiltered:</strong> additionally to the {@code perThread} mode this mode will
     * filter the thread's output for well known exceptions e.g. FileNotFoundException of the junit
     * eclipse plugin.
     * 
     * <p>
     * The fastest mode is obviously the {@code original} mode while the slowest is the
     * {@code perThreadFiltered} one. The overhead added to the build time by the
     * {@code perThreadFiltered} is about 5% but will produce a clean and continuous log output even
     * in multi-threaded builds.
     * <p>
     * When maven is started with {@code -X} or {@code -debug}, extensive log output is created and
     * therefore the {@code original} mode is chosen automatically.
     */
    @Parameter(defaultValue = "perThreadFiltered")
    private LoggingMode loggingMode;

    /**
     * maybe a parameter in the future
     */
    private List<String> exceptionTexts;

    @Component
    private MavenProject project;

    @Component
    private EquinoxInstallationFactory installationFactory;

    @Component
    private EquinoxLauncher launcher;

    @Component
    private ToolchainProvider toolchainProvider;

    @Component
    private EquinoxServiceFactory equinox;

    @Component
    private Logger logger;

    @Component
    private ToolchainManager toolchainManager;

    @Component
    private PluginDescriptor pluginDescriptor;

    /**
     * Returns the name of the project in a way that it can be used in the ant script. It should be
     * the same as seen in eclipse. For example in the project or package explorer.
     * <p>
     * The variable used in the ant script is {@code -DprojectName=Project_Name}
     * 
     * @return the name of the project
     */
    public String getProjectName() {
        File eclipseProjectFile = new File(project.getBasedir().getAbsolutePath(), ".project");
        if (eclipseProjectFile.exists() && !importAsMavenProject) {
            try {
                DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document doc = documentBuilder.parse(eclipseProjectFile);
                return doc.getElementsByTagName("name").item(0).getTextContent();
            } catch (SAXException | IOException | ParserConfigurationException e) {
                getLog().error("Can't read Eclipse .project file to find project name", e);
            }
        }
        if (importAsMavenProject) {
            return project.getName();
        }
        return project.getBasedir().getName();
    }

    /**
     * Gets the version of the Faktor-IPS repository to be used.
     *
     * @return the version of the Faktor-IPS repository to be used.
     */
    public String getFipsRepositoryVersion() {
        if (StringUtils.isBlank(fipsRepositoryVersion)) {
            PluginDescriptor descriptor = (PluginDescriptor)getPluginContext().get("pluginDescriptor");
            if (descriptor != null) {
                String version = descriptor.getVersion();
                Matcher versionMatcher = MAJOR_MINOR_VERSION_PATTERN.matcher(version);
                if (versionMatcher.matches()) {
                    return "v" + versionMatcher.group(1) + "_" + versionMatcher.group(2) + "/" + version;
                }
                return version;
            }
        }
        return fipsRepositoryVersion;
    }

    /**
     * Gets the P2-repository containing the Faktor-IPS plugins.
     * 
     * @return the Faktor-IPS repository
     */
    public String getFipsRepository() {
        return fipsRepository.replace("${faktorips.repository.version}", getFipsRepositoryVersion());
    }

    /**
     * Either returns the custom path to the ant script or the standard ant script configured with
     * the correct default values.
     * 
     * @return the path to the ant script
     */
    public String getPathToAntScript() {
        if (antScriptPath == null) {
            createAntScript();
        }
        return antScriptPath;

    }

    private void createAntScript() {
        try {
            antScriptPath = project.getBuild().getDirectory() + "/importProjects.xml";
            Files.deleteIfExists(Paths.get(antScriptPath));
            FileUtils.forceMkdir(new File(project.getBuild().getDirectory()));

            List<String> lines = new ArrayList<>();

            lines.add("<project name=\"importProjects\" default=\"import\">");
            addTaskDefs(lines);

            lines.add("  <target name=\"import\">");
            lines.add("    <echo message=\"Building ${projectName}\" />");
            addTasks(lines);
            lines.add("  </target>");
            lines.add("</project>");

            Files.write(Paths.get(antScriptPath), lines, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        } catch (IOException e) {
            getLog().error("Can't create ant script in " + antScriptPath, e);
        }
    }

    private void addTaskDefs(List<String> lines) {
        lines.add("  <taskdef name=\"faktorips.build\" classname=\"org.faktorips.devtools.ant.BuildTask\" />");
        if (importAsMavenProject) {
            lines.add(
                    "  <taskdef name=\"faktorips.mavenImport\" classname=\"org.faktorips.devtools.ant.MavenProjectImportTask\" />");
            lines.add(
                    "  <taskdef name=\"faktorips.mavenRefresh\" classname=\"org.faktorips.devtools.ant.MavenProjectRefreshTask\" />");
        } else {
            lines.add(
                    "  <taskdef name=\"faktorips.import\" classname=\"org.faktorips.devtools.ant.ProjectImportTask\" />");
        }
        if (usesCustomJdk()) {
            lines.add(
                    "  <taskdef name=\"faktorips.configureJdk\" classname=\"org.faktorips.devtools.ant.ConfigureJdkTask\" />");
        }
        if (exportHtml) {
            lines.add(
                    "  <taskdef name=\"faktorips.exportHtml\" classname=\"org.faktorips.devtools.ant.ExportHtmlTask\" />");
        }
        if (isGitStatusPorcelain()) {
            lines.add(
                    "  <taskdef name=\"faktorips.gitStatus\" classname=\"org.faktorips.devtools.ant.GitStatusPorcelainTask\" />");
        }
    }

    private void addTasks(List<String> lines) {
        if (usesCustomJdk()) {
            lines.add("    <faktorips.configureJdk dir=\"${jdk.dir}\" statusFile=\"${status.file}\" />");
        }
        if (importAsMavenProject) {
            importUpstreamProjects(lines);
            lines.add("    <faktorips.mavenImport dir=\"${sourcedir}\" statusFile=\"${status.file}\" />");
            lines.add(
                    "    <faktorips.mavenRefresh updateSnapshots=\"true\" cleanProjects=\"${fullBuild}\"  statusFile=\"${status.file}\"/>");
        } else {
            lines.add("    <faktorips.import dir=\"${sourcedir}\" copy=\"false\" statusFile=\"${status.file}\" />");
        }

        lines.add(
                "    <faktorips.build fullBuild=\"${fullBuild}\" ipsOnly=\"${build.ipsOnly}\" statusFile=\"${status.file}\">");
        lines.add("      <EclipseProject name=\"${projectName}\" />");
        lines.add("    </faktorips.build>");
        if (exportHtml) {
            lines.add(
                    "    <faktorips.exportHtml ipsProjectName=\"${projectName}\" showValidationErrors=\"true\" showInheritedObjectPartsInTable=\"true\" locale=\"de\" destination=\"${sourcedir}/target/html/\" ipsObjectTypes=\"ALL\" />");
        }
        if (isGitStatusPorcelain()) {
            lines.add(
                    "    <faktorips.gitStatus failBuild=\"${fail.build}\" verbosity=\"${verbosity}\" statusFile=\"${status.file}\" />");
        }
    }

    private void importUpstreamProjects(List<String> lines) {
        List<MavenProject> upstreamIpsProjects = findUpstreamIpsProjectsNotInstalled();
        if (!upstreamIpsProjects.isEmpty()) {
            getLog().info("The project " + project
                    + " is built without the 'install' goal but depends on the following upstream Faktor-IPS projects:"
                    + upstreamIpsProjects.stream().map(MavenProject::toString)
                            .collect(Collectors.joining("\n\t", "\t", "\n"))
                    + "They will also be imported into the Faktor-IPS workspace.");
        }
        upstreamIpsProjects.forEach(p -> lines
                .add("    <faktorips.mavenImport dir=\"" + p.getBasedir() + "\" statusFile=\"${status.file}\" />"));
    }

    private List<MavenProject> findUpstreamIpsProjectsNotInstalled() {
        if (session.getRequest().getGoals().stream().noneMatch(Predicate.isEqual("install"))) {
            List<MavenProject> upstreamProjects = session.getProjectDependencyGraph().getUpstreamProjects(
                    project,
                    true);
            return upstreamProjects.stream()
                    .filter(p -> p.getPackaging().equalsIgnoreCase("jar"))
                    .filter(p -> new File(p.getBasedir().getAbsoluteFile(), ".ipsproject").exists())
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private boolean usesCustomJdk() {
        return jdkDir != null || jdkId != null;
    }

    /**
     * Either returns the custom path to the JDK, the JDK with the {@link #jdkId} configured in the
     * ~/.m2/toolchains.xml or {@code null} if nothing is found.
     * 
     * @return the path to the JDK or {@code null}
     * @throws MojoExecutionException - if the toolchains are misconfigured
     */
    public String getPathToJdk() throws MojoExecutionException {
        if (jdkDir != null) {
            return jdkDir;
        } else {
            if (jdkId != null) {
                Toolchain tc = toolchainProvider.findMatchingJavaToolChain(session, jdkId);
                if (tc != null) {
                    getLog().info("Toolchain in faktorips-maven-plugin: " + tc);
                    return new File(tc.findTool("java")).getParentFile().getParent();
                } else if (Objects.equals(jdkId, "JavaSE-" + Runtime.version().feature())) {
                    getLog().debug("Using current Java runtime to build project as it matches the configured JDK ID.");
                } else {
                    getLog().warn("No toolchain was found in faktorips-maven-plugin for " + jdkId
                            + ". Current Java runtime will be used to build the project.");
                }
            }
        }
        return null;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().info("skipping mojo execution");
            return;
        }

        @SuppressWarnings("unchecked")
        boolean alreadyBuilt = getPluginContext().put("BUILT" + getProjectName(), Boolean.TRUE) != null;

        if (!alreadyBuilt) {
            // add default repositories if no repositories are specified in the pom.xml
            if (repositories.isEmpty()) {
                addRepository(eclipseRepository);
                addRepository(getFipsRepository());
            }
            repositories.addAll(additionalRepositories);

            addDependencies();

            // default values for parameter applicationArgs
            applicationsArgs.add("-consoleLog");
            applicationsArgs.add("-application");
            applicationsArgs.add("org.eclipse.ant.core.antRunner");
            applicationsArgs.add("-buildfile");
            applicationsArgs.add("\"" + getPathToAntScript() + "\"");
            applicationsArgs.add(antTarget);

            // default values for parameter jvmArgs
            jvmArgs.add("-Xmx1024m");
            jvmArgs.add("-XX:+HeapDumpOnOutOfMemoryError");
            jvmArgs.add("-DjavacFailOnError=true");
            if (usesCustomJdk()) {
                jvmArgs.add("-Djdk.dir=" + getPathToJdk());
            }
            jvmArgs.add("-DprojectName=" + getProjectName());
            jvmArgs.add("-Dsourcedir=" + project.getBasedir().getAbsolutePath());

            boolean fullBuild = session.getGoals().contains("clean");
            jvmArgs.add("-DfullBuild=" + Boolean.toString(fullBuild));
            jvmArgs.add("-Dbuild.ipsOnly=" + Boolean.toString(buildIpsOnly));

            String statusFile = createStatusFile();

            if (isGitStatusPorcelain()) {
                jvmArgs.add("-Dfail.build=" + gitStatusPorcelain.getFailBuild());
                jvmArgs.add("-Dverbosity=" + gitStatusPorcelain.getVerbosity().getName());
            }
            if (debug) {
                jvmArgs.add("-Xdebug");
                jvmArgs.add("-Xnoagent");
                jvmArgs.add("-Xrunjdwp:transport=dt_socket,address=" + debugPort + ",server=y,suspend=y");
            }

            if (isInDebugMode()) {
                jvmArgs.add("-Declipse.log.level=DEBUG");
                applicationsArgs.add("-debug");
                applicationsArgs.add(writeDebugLogSettings());
            }

            copyMavenSettings();

            executePlatform();

            failBuildforAntStatusError(statusFile);
        }
    }

    private String createStatusFile() {
        String statusFile = null;
        try {
            statusFile = File.createTempFile("IpsBuild_", ".status").getPath();
            jvmArgs.add("-Dstatus.file=" + statusFile);
        } catch (IOException e) {
            getLog().error("Could not create status file");
            getLog().error(e);
        }
        return statusFile;
    }

    private void failBuildforAntStatusError(String statusFile) throws MojoFailureException {
        if (StringUtils.isNotBlank(statusFile)) {
            Path statusFilePath = Path.of(statusFile);
            try {
                String status = Files.readString(statusFilePath);
                if (StringUtils.isNotBlank(status)) {
                    throw new MojoFailureException(status);
                }
            } catch (IOException e) {
                getLog().error("Could not read status file " + statusFile);
                getLog().error(e);
            } finally {
                try {
                    Files.deleteIfExists(statusFilePath);
                } catch (IOException e) {
                    getLog().error("Could not delete status file " + statusFile);
                    getLog().error(e);
                }
            }
        }
    }

    private boolean isInDebugMode() {
        return session.getRequest().getLoggingLevel() == Logger.LEVEL_DEBUG || debugLogOptions != null;
    }

    private void addDependencies() {
        // default values for parameter dependencies
        addDependency("org.faktorips.devtools.core");
        addDependency("org.faktorips.devtools.stdbuilder");
        addDependency("org.faktorips.runtime.groovy");
        addDependency("org.faktorips.valuetypes.joda");
        addDependency("org.faktorips.devtools.ant");
        if (exportHtml) {
            addDependency("org.faktorips.devtools.htmlexport");
        }
        if (importAsMavenProject) {
            addDependency("org.eclipse.m2e.core");
            addDependency("org.eclipse.m2e.maven.runtime");
            addDependency("org.faktorips.m2e");
        }
        if (isGitStatusPorcelain()) {
            addDependency("org.eclipse.egit.core");
        }
        dependencies.addAll(additionalPlugins);
    }

    private void executePlatform() throws MojoExecutionException, MojoFailureException {

        // no need to clean as the IpsCleanMojo deleted the parent directory in the clean phase
        boolean clearWorkspaceBeforeLaunch = false;
        EclipseRunMojo eclipseRunMojo = null;

        if (isInDebugMode() || LoggingMode.original == loggingMode) {
            eclipseRunMojo = createOriginalEclipseRunMojo(clearWorkspaceBeforeLaunch);
        } else {
            eclipseRunMojo = createLoggingEclipseRunMojo(clearWorkspaceBeforeLaunch);
        }

        eclipseRunMojo.execute();
    }

    private EclipseRunMojo createLoggingEclipseRunMojo(boolean clearWorkspaceBeforeLaunch) {

        getLog().info("Per thread output is active.");
        if (LoggingMode.perThreadFiltered == loggingMode) {
            getLog().info("Well known exceptions that do not affect the build are filtered.");

            exceptionTexts = List.of(
                    "java\\.io\\.FileNotFoundException: org\\.eclipse\\.equinox\\.simpleconfigurator/bundles\\.info \\(No such file or directory\\)");
        } else {
            // perThread
            exceptionTexts = List.of();
        }

        return new LoggingEclipseRunMojo(work,
                clearWorkspaceBeforeLaunch,
                project,
                dependencies,
                addDefaultDependencies,
                executionEnvironment,
                repositories,
                session,
                jvmArgs,
                skip,
                applicationsArgs,
                forkedProcessTimeoutInSeconds,
                environmentVariables,
                installationFactory,
                launcher,
                toolchainProvider,
                equinox,
                logger,
                toolchainManager,
                getProjectName(),
                getLog(),
                exceptionTexts);
    }

    private EclipseRunMojo createOriginalEclipseRunMojo(boolean clearWorkspaceBeforeLaunch) {
        getLog().info("No log filtering or per thread output.");
        return new EclipseRunMojo(work,
                clearWorkspaceBeforeLaunch,
                project,
                dependencies,
                addDefaultDependencies,
                executionEnvironment,
                repositories,
                session,
                jvmArgs,
                skip,
                applicationsArgs,
                forkedProcessTimeoutInSeconds,
                environmentVariables,
                installationFactory,
                launcher,
                toolchainProvider,
                equinox,
                logger,
                toolchainManager);
    }

    private boolean isGitStatusPorcelain() {
        return gitStatusPorcelain != null;
    }

    private void copyMavenSettings() {
        String userSettingsPath = session.getRequest().getUserSettingsFile().getAbsolutePath();

        // add path to localRepository to user settings if maven.repo.local is set
        if (localRepository != null) {
            String copyUserSettingsDir = work.getAbsolutePath() + "\\data\\.metadata\\.plugins";
            String copyUserSettingsPath = copyUserSettingsDir + "\\settings.xml";
            try {
                FileUtils.forceMkdir(new File(copyUserSettingsDir));

                String userSettings = FileUtils.fileRead(session.getRequest().getUserSettingsFile(), "UTF-8");
                if (userSettings.contains("<localRepository>")) {
                    userSettings = userSettings.replaceAll("<localRepository>.+<\\/localRepository>",
                            "<localRepository>" + localRepository + "<\\/localRepository>");
                } else {
                    userSettings = userSettings.replaceAll("<settings>", "<settings>"
                            + System.getProperty("line.separator")
                            + "<localRepository>" + localRepository + "<\\/localRepository>");
                }

                Files.writeString(Paths.get(copyUserSettingsPath), userSettings, StandardOpenOption.CREATE,
                        StandardOpenOption.WRITE);

                userSettingsPath = copyUserSettingsPath;

            } catch (IOException e) {
                getLog().error("Can't create a copy of the user settings" + copyUserSettingsPath, e);
            }
        }

        try {
            File settingsDir = new File(work,
                    "data/.metadata/.plugins/org.eclipse.core.runtime/.settings")
                            .getAbsoluteFile();
            FileUtils.forceMkdir(settingsDir);

            final String finalUserSettings = userSettingsPath;

            writeProperties(settingsDir, "org.eclipse.m2e.core.prefs", p -> {
                p.put("eclipse.m2.userSettingsFile", finalUserSettings);
                p.put("eclipse.m2.globalSettingsFile", session.getRequest().getGlobalSettingsFile().getAbsolutePath());
            });
            writeProperties(settingsDir, "org.eclipse.core.resources.prefs", p -> {
                p.put("refresh.enabled", "true");
                p.put("description.autobuilding", "false");
            });

        } catch (IOException e) {
            getLog().error("Error while copying the maven settings into the workspace", e);
        }
    }

    private void writeProperties(File settingsDir, String propertyFileName, Consumer<Properties> properties) {
        try {
            Properties p = new Properties();
            properties.accept(p);
            p.put("eclipse.preferences.version", "1");
            try (FileOutputStream settings = new FileOutputStream(
                    new File(settingsDir, propertyFileName))) {
                p.store(settings, "IpsBuildMojo");
            }
        } catch (IOException e) {
            getLog().error("Error writing settings file " + propertyFileName + " into the workspace metadata dir "
                    + settingsDir, e);
        }
    }

    private String writeDebugLogSettings() {
        if (debugLogOptions == null) {
            try {
                File settingsDir = new File(work,
                        "data")
                                .getAbsoluteFile();
                FileUtils.forceMkdir(settingsDir);
                String path = settingsDir.getAbsolutePath() + "/.options";

                List<String> loggingOptions = IOUtils.readLines(getClass().getResourceAsStream("/debug_trace"),
                        StandardCharsets.UTF_8);

                Files.write(Paths.get(path), loggingOptions,
                        StandardOpenOption.CREATE, StandardOpenOption.WRITE);

                return path;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return debugLogOptions;
    }

    private void addRepository(String url) {
        Repository repo = new Repository(URI.create(url));
        repo.setLayout("p2");
        repositories.add(repo);
    }

    private void addDependency(String artifactId) {
        Dependency dependency = new Dependency();
        dependency.setArtifactId(artifactId);
        dependency.setType("eclipse-plugin");
        dependencies.add(dependency);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " building " + project;
    }
}
