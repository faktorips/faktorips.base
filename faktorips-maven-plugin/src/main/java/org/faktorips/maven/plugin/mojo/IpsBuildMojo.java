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
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.toolchain.ToolchainManager;
import org.codehaus.plexus.logging.Logger;
import org.eclipse.sisu.equinox.EquinoxServiceFactory;
import org.eclipse.sisu.equinox.launching.EquinoxInstallationFactory;
import org.eclipse.sisu.equinox.launching.EquinoxLauncher;
import org.eclipse.tycho.core.maven.ToolchainProvider;
import org.faktorips.maven.plugin.mojo.internal.EclipseRunMojo;
import org.faktorips.maven.plugin.mojo.internal.Repository;

/**
 * Builds the Faktor-IPS project.
 * <p>
 * By default, the latest Faktor-IPS is used with an Eclipse 2019-03 runtime, all installed from
 * <a href="https://faktorzehn.org">faktorzehn.org</a> update sites.
 */
@Mojo(name = "faktorips-build", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class IpsBuildMojo extends AbstractMojo {

    /**
     * Whether to add default dependencies to bundles org.eclipse.equinox.launcher, org.eclipse.osgi
     * and org.eclipse.core.runtime.
     */
    // @Parameter(defaultValue = "true")
    private boolean addDefaultDependencies = true;

    /**
     * Execution environment profile name used to resolve dependencies.
     */
    // @Parameter(defaultValue = "JavaSE-11")
    private String executionEnvironment = "JavaSE-11";

    /**
     * Whether the workspace should be cleared before running eclipse.
     * <p>
     * If {@code false} and a workspace from a previous run exists, that workspace is reused.
     * </p>
     */
    // @Parameter(defaultValue = "true")
    private boolean clearWorkspaceBeforeLaunch = true;

    /**
     * Whether to skip mojo execution.
     */
    // @Parameter(property = "eclipserun.skip", defaultValue = "false")
    private boolean skip = false;

    /**
     * Dependencies which will be resolved transitively to make up the eclipse runtime. Example:
     * 
     * <pre>
     * &lt;dependencies&gt;
     *  &lt;dependency&gt;
     *   &lt;artifactId&gt;org.eclipse.ant.core&lt;/artifactId&gt;
     *   &lt;type&gt;eclipse-plugin&lt;/type&gt;
     *  &lt;/dependency&gt;
     * &lt;/dependencies&gt;
     * </pre>
     */
    // @Parameter
    private List<Dependency> dependencies = new ArrayList<>();

    /**
     * List of JVM arguments set on the command line. Example:
     * 
     * <pre>
     * &lt;jvmArgs&gt;
     *   &lt;args&gt;-Xdebug&lt;/args&gt;
     *   &lt;args&gt;-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=1044&lt;/args&gt;
     * &lt;/jvmArgs&gt;
     * </pre>
     */
    // @Parameter
    private List<String> jvmArgs = new ArrayList<>();

    /**
     * List of applications arguments set on the command line. Example:
     *
     * <pre>
     * &lt;applicationsArgs&gt;
     * &lt;args&gt;-buildfile&lt;/args&gt;
     * &lt;args&gt;build-test.xml&lt;/args&gt;
     * &lt;/applicationsArgs&gt;
     * </pre>
     */
    // @Parameter
    private List<String> applicationsArgs = new ArrayList<>();

    /**
     * p2 repositories which will be used to resolve dependencies. Example:
     * 
     * <pre>
     * &lt;repositories&gt;
     *  &lt;repository&gt;
     *   &lt;id&gt;juno&lt;/id&gt;
     *   &lt;layout&gt;p2&lt;/layout&gt;
     *   &lt;url&gt;https://download.eclipse.org/releases/juno&lt;/url&gt;
     *  &lt;/repository&gt;
     * &lt;/repositories&gt;
     * </pre>
     */
    // @Parameter
    private List<Repository> repositories = new ArrayList<>();

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
    private Map<String, String> environmentVariables;

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
     * Path to the JDK 8.
     */
    @Parameter(defaultValue = "${jdk8.dir}")
    private String jdk8dir;

    /**
     * The version of Faktor-IPS to be installed.
     */
    @Parameter(property = "faktorips.repository.version", defaultValue = "latest")
    private String fipsRepositoryVersion;

    /**
     * Path to the update site to install Faktor-IPS.
     */
    @Parameter(property = "repository.fips", defaultValue = "https://update.faktorzehn.org/faktorips/${faktorips.repository.version}/")
    private String fipsRepository;

    /**
     * Path to the third-party repository.
     */
    @Parameter(property = "repository.thirdparty", defaultValue = "https://drone.faktorzehn.de/p2/thirdparty-1.6")
    private String thirdpartyRepository;

    /**
     * Path to the update site to install Eclipse.
     */
    @Parameter(property = "repository.eclipse", defaultValue = "http://update.faktorzehn.org/p2repositories/2019-03/")
    private String eclipseRepository;

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

    public String getFipsRepository() {
        return fipsRepository.replace("${faktorips.repository.version}", fipsRepositoryVersion);
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        // default values for parameter repositories
        addRepository(eclipseRepository);
        addRepository(getFipsRepository());
        addRepository(thirdpartyRepository);

        // default values for parameter dependencies
        addDependency("org.faktorips.devtools.core");
        addDependency("org.faktorips.devtools.stdbuilder");
        addDependency("org.faktorips.runtime.groovy");
        addDependency("org.faktorips.valuetypes.joda");
        addDependency("org.faktorips.devtools.htmlexport");
        addDependency("org.faktorips.devtools.ant");
        addDependency("org.eclipse.jdt.junit");
        addDependency("org.eclipse.jdt.junit5.runtime");

        // default values for parameter applicationArgs
        applicationsArgs.add("-consoleLog");
        applicationsArgs.add("-application");
        applicationsArgs.add("org.eclipse.ant.core.antRunner");
        applicationsArgs.add("-buildfile");
        applicationsArgs.add(project.getBasedir().getAbsolutePath() + "/build/importProjects.xml");
        applicationsArgs.add("import");

        // default values for parameter jvmArgs
        jvmArgs.add("-Xmx1024m");
        jvmArgs.add("-XX:+HeapDumpOnOutOfMemoryError");
        jvmArgs.add("-DjavacFailOnError=true");
        jvmArgs.add("-Djdk8.dir=" + jdk8dir);
        jvmArgs.add("-Dsourcedir=" + project.getBasedir().getAbsolutePath());

        EclipseRunMojo eclipseRunMojo = new EclipseRunMojo(work,
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
        eclipseRunMojo.execute();
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
}
