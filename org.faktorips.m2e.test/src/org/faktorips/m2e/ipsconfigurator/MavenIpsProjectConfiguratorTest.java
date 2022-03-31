/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.m2e.ipsconfigurator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.PluginManagement;
import org.apache.maven.model.Resource;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.ResolverConfiguration;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.internal.ipsproject.IpsContainerEntry;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.plugin.IpsModelActivator;
import org.faktorips.devtools.model.util.IpsProjectCreationProperties;
import org.faktorips.devtools.model.util.PersistenceSupportNames;
import org.faktorips.m2e.version.MavenVersionFormatter;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MavenIpsProjectConfiguratorTest extends AbstractIpsPluginTest {

    private static final String POM_SCENARIO_1 = "pom_scenario1.xml";
    private static final String POM_SCENARIO_2 = "pom_scenario2.xml";
    private static final String POM_EMPTY = "pom_empty.xml";
    private static final String POM_NAME = "pom.xml";
    private static final String MANIFEST_NAME = "MANIFEST.MF";
    private static final String MANIFEST_PATH = "META-INF/" + MANIFEST_NAME;
    private static final String OBJECT_PATH_FOLDER = "testFolder";

    private static MavenIpsProjectConfigurator mavenIpsProjectConfigurator;
    private static IpsProjectCreationProperties projectCreationProperties;
    private static String pomScenario1;
    private static String pomScenario2;
    private static String pomEmpty;
    private static String faktorIpsVersion;

    private IIpsProject ipsProject;

    @BeforeClass
    public static void init() throws Exception {
        mavenIpsProjectConfigurator = new MavenIpsProjectConfigurator();
        pomScenario1 = readResource(POM_SCENARIO_1);
        pomScenario2 = readResource(POM_SCENARIO_2);
        pomEmpty = readResource(POM_EMPTY);
        faktorIpsVersion = MavenVersionFormatter.formatVersion(IpsModelActivator.getInstalledFaktorIpsVersion());
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        createIpsProjectCreationProperties();
        initIpsProject(ipsProject);
        initMaven(ipsProject, pomEmpty);
    }

    @Test
    public void testCanConfigure() {
        assertThat(mavenIpsProjectConfigurator.canConfigure(ipsProject.getJavaProject()), is(true));
    }

    @Test
    public void testCanNotConfigure() throws Exception {
        ipsProject = newIpsProject();
        assertThat(mavenIpsProjectConfigurator.canConfigure(ipsProject.getJavaProject()), is(false));
    }

    @Test
    public void testIsGroovySupported() throws Exception {
        ipsProject = newIpsProject();
        assertThat(mavenIpsProjectConfigurator.isGroovySupported(ipsProject.getJavaProject()), is(true));
    }

    @Test(expected = IpsException.class)
    public void testConfigureIpsProjectMissingMergableOutputFolder() throws Exception {
        IIpsObjectPath objectPath = ipsProject.getProperties().getIpsObjectPath();
        objectPath.setOutputFolderForMergableSources(null);
        ipsProject.setIpsObjectPath(objectPath);

        mavenIpsProjectConfigurator.configureIpsProject(ipsProject, projectCreationProperties);
    }

    @Test(expected = IpsException.class)
    public void testConfigureIpsProject_missingDerivedOutputFolder() throws Exception {
        IIpsObjectPath objectPath = ipsProject.getProperties().getIpsObjectPath();
        objectPath.setOutputFolderForDerivedSources(null);
        ipsProject.setIpsObjectPath(objectPath);

        mavenIpsProjectConfigurator.configureIpsProject(ipsProject, projectCreationProperties);
    }

    @Test(expected = IpsException.class)
    public void testConfigureIpsProject_noMavenProject() throws Exception {
        ipsProject = newIpsProject();
        mavenIpsProjectConfigurator.configureIpsProject(ipsProject, projectCreationProperties);
    }

    @Test
    public void testConfigureIpsProject_groovyDisabled() throws Exception {
        projectCreationProperties.setGroovySupport(false);
        mavenIpsProjectConfigurator.configureIpsProject(ipsProject, projectCreationProperties);
        MavenProject mavenProject = MavenPlugin.getMavenProjectRegistry().getProject(ipsProject.getProject().unwrap())
                .getMavenProject(new NullProgressMonitor());

        boolean containsGroovyDependency = mavenProject.getDependencies().stream()
                .anyMatch(dependency -> dependency.getArtifactId().equals("faktorips-runtime-groovy"));
        assertThat(containsGroovyDependency, is(false));
    }

    @Test
    public void testConfigureIpsProject_persistenceEclipseLink11() throws Exception {
        projectCreationProperties.setPersistentProject(true);
        projectCreationProperties.setPersistenceSupport(PersistenceSupportNames.ID_ECLIPSE_LINK_1_1);
        mavenIpsProjectConfigurator.configureIpsProject(ipsProject, projectCreationProperties);
        MavenProject mavenProject = MavenPlugin.getMavenProjectRegistry().getProject(ipsProject.getProject().unwrap())
                .getMavenProject(new NullProgressMonitor());

        Optional<Dependency> eclipseLink11 = mavenProject.getDependencies().stream()
                .filter(dependency -> dependency.getArtifactId().equals("eclipselink"))
                .findAny();
        assertThat(eclipseLink11.isPresent(), is(true));
        assertThat(eclipseLink11.get().getGroupId(), is("org.eclipse.persistence"));
        assertThat(eclipseLink11.get().getVersion(), is("1.1.0"));
    }

    @Test
    public void testConfigureIpsProject_persistenceEclipseLink25() throws Exception {
        projectCreationProperties.setPersistentProject(true);
        projectCreationProperties.setPersistenceSupport(PersistenceSupportNames.ID_ECLIPSE_LINK_2_5);
        mavenIpsProjectConfigurator.configureIpsProject(ipsProject, projectCreationProperties);
        MavenProject mavenProject = MavenPlugin.getMavenProjectRegistry().getProject(ipsProject.getProject().unwrap())
                .getMavenProject(new NullProgressMonitor());

        Optional<Dependency> eclipseLink11 = mavenProject.getDependencies().stream()
                .filter(dependency -> dependency.getArtifactId().equals("eclipselink"))
                .findAny();
        assertThat(eclipseLink11.isPresent(), is(true));
        assertThat(eclipseLink11.get().getGroupId(), is("org.eclipse.persistence"));
        assertThat(eclipseLink11.get().getVersion(), is("2.5.0"));
    }

    @Test
    public void testConfigureIpsProject_persistenceGenericJpa20() throws Exception {
        projectCreationProperties.setPersistentProject(true);
        projectCreationProperties.setPersistenceSupport(PersistenceSupportNames.ID_GENERIC_JPA_2);
        mavenIpsProjectConfigurator.configureIpsProject(ipsProject, projectCreationProperties);
        MavenProject mavenProject = MavenPlugin.getMavenProjectRegistry().getProject(ipsProject.getProject().unwrap())
                .getMavenProject(new NullProgressMonitor());

        Optional<Dependency> eclipseLink11 = mavenProject.getDependencies().stream()
                .filter(dependency -> dependency.getArtifactId().equals("javax.persistence"))
                .findAny();
        assertThat(eclipseLink11.isPresent(), is(true));
        assertThat(eclipseLink11.get().getGroupId(), is("org.eclipse.persistence"));
        assertThat(eclipseLink11.get().getVersion(), is("2.0.0"));
    }

    @Test
    public void testConfigureIpsProject_persistenceGenericJpa21() throws Exception {
        projectCreationProperties.setPersistentProject(true);
        projectCreationProperties.setPersistenceSupport(PersistenceSupportNames.ID_GENERIC_JPA_2_1);
        mavenIpsProjectConfigurator.configureIpsProject(ipsProject, projectCreationProperties);
        MavenProject mavenProject = MavenPlugin.getMavenProjectRegistry().getProject(ipsProject.getProject().unwrap())
                .getMavenProject(new NullProgressMonitor());

        Optional<Dependency> eclipseLink11 = mavenProject.getDependencies().stream()
                .filter(dependency -> dependency.getArtifactId().equals("javax.persistence"))
                .findAny();
        assertThat(eclipseLink11.isPresent(), is(true));
        assertThat(eclipseLink11.get().getGroupId(), is("org.eclipse.persistence"));
        assertThat(eclipseLink11.get().getVersion(), is("2.1.0"));
    }

    @Test
    public void testConfigureIpsProject_persistenceJakartaPersistence22() throws Exception {
        projectCreationProperties.setPersistentProject(true);
        projectCreationProperties.setPersistenceSupport(PersistenceSupportNames.ID_JAKARTA_PERSISTENCE_2_2);
        mavenIpsProjectConfigurator.configureIpsProject(ipsProject, projectCreationProperties);
        MavenProject mavenProject = MavenPlugin.getMavenProjectRegistry().getProject(ipsProject.getProject().unwrap())
                .getMavenProject(new NullProgressMonitor());

        Optional<Dependency> eclipseLink11 = mavenProject.getDependencies().stream()
                .filter(dependency -> dependency.getArtifactId().equals("jakarta.persistence-api"))
                .findAny();
        assertThat(eclipseLink11.isPresent(), is(true));
        assertThat(eclipseLink11.get().getGroupId(), is("jakarta.persistence"));
        assertThat(eclipseLink11.get().getVersion(), is("2.2.3"));
    }

    @Test(expected = IpsException.class)
    public void testConfigureIpsProject_persistenceMalformed() throws Exception {
        projectCreationProperties.setPersistentProject(true);
        projectCreationProperties.setPersistenceSupport("Malformed name");
        mavenIpsProjectConfigurator.configureIpsProject(ipsProject, projectCreationProperties);
    }

    @Test
    public void testConfigureIpsProject_emptyPom() throws Exception {
        mavenIpsProjectConfigurator.configureIpsProject(ipsProject, projectCreationProperties);
        MavenProject mavenProject = MavenPlugin.getMavenProjectRegistry().getProject(ipsProject.getProject().unwrap())
                .getMavenProject(new NullProgressMonitor());

        checkIpsProjectProperties();
        checkManifestFile();
        checkMavenProperties(mavenProject.getModel());
        checkMavenDependencies(mavenProject);
        checkMavenResources(mavenProject.getBuild());
        checkMavenPluginManagement(mavenProject.getBuild().getPluginManagement());
        checkMavenPlugins(mavenProject.getBuild());
    }

    /**
     * Checks most possible scenarios when adding the IPS nature to a Maven project with existing
     * POM entries.
     */
    @Test
    public void testConfigureIpsProject_scenario1() throws Exception {
        ipsProject = newIpsProject();
        initIpsProject(ipsProject);
        initMaven(ipsProject, pomScenario1);
        AFolder metaInf = ipsProject.getProject().getFolder("META-INF");
        metaInf.create(new NullProgressMonitor());
        AFile manifest = metaInf.getFile(MANIFEST_NAME);
        try (InputStream in = getResourceInputStream(MANIFEST_NAME)) {
            manifest.create(in, new NullProgressMonitor());
        }

        mavenIpsProjectConfigurator.configureIpsProject(ipsProject, projectCreationProperties);
        MavenProject mavenProject = MavenPlugin.getMavenProjectRegistry().getProject(ipsProject.getProject().unwrap())
                .getMavenProject(new NullProgressMonitor());

        checkIpsProjectProperties();
        checkExistingManifestFile();
        checkMavenPropertiesContentPom(mavenProject.getModel());
        checkMavenDependenciesContentPom(mavenProject);
        checkMavenResourcesContentPom(mavenProject.getBuild());
        checkMavenPluginManagementContentPom(mavenProject.getBuild().getPluginManagement());
        checkMavenPluginsContentPom(mavenProject.getBuild());
    }

    /**
     * Checks the remaining cases not covered by the empty POM and the scenario1 POM:
     * <ul>
     * <li>Resources already configured correctly</li>
     * <li>Source-Plugin already configured in plugin management</li>
     * <li>Source-Plugin already configured in plugins</li>
     * </ul>
     */
    @Test
    public void testConfigureIpsProject_scenario2() throws Exception {
        ipsProject = newIpsProject();
        initIpsProject(ipsProject);
        initMaven(ipsProject, pomScenario2);

        mavenIpsProjectConfigurator.configureIpsProject(ipsProject, projectCreationProperties);
        MavenProject mavenProject = MavenPlugin.getMavenProjectRegistry().getProject(ipsProject.getProject().unwrap())
                .getMavenProject(new NullProgressMonitor());

        checkMavenResources(mavenProject.getBuild());
        checkMavenPluginManagement(mavenProject.getBuild().getPluginManagement());
        checkMavenPlugins(mavenProject.getBuild());
    }

    //////////////////
    // Initialization
    //////////////////

    private static InputStream getResourceInputStream(String fileName) {
        return MavenIpsProjectConfiguratorTest.class.getResourceAsStream(fileName);
    }

    private static String readResource(String fileName) throws Exception {
        InputStream inputStream = getResourceInputStream(fileName);
        StringBuilder result = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                result.append(line).append("\n");
            }
        }
        return result.toString();
    }

    private static void createIpsProjectCreationProperties() {
        projectCreationProperties = new IpsProjectCreationProperties();
        projectCreationProperties.getLocales().add(new Locale("en"));
    }

    private void initIpsProject(IIpsProject ipsProject) throws Exception {
        AFolder folder = ipsProject.getProject().getFolder(OBJECT_PATH_FOLDER);
        folder.create(null);
        IIpsObjectPath objectPath = ipsProject.getProperties().getIpsObjectPath();
        objectPath.setOutputFolderForMergableSources(folder);
        objectPath.setOutputFolderForDerivedSources(folder);
        ipsProject.setIpsObjectPath(objectPath);
    }

    private void initMaven(IIpsProject ipsProject, String pomContent) throws Exception {
        ipsProject.getProject().getFile(POM_NAME).create(
                new ByteArrayInputStream(pomContent.getBytes()), null);
        MavenPlugin.getProjectConfigurationManager().enableMavenNature(ipsProject.getProject().unwrap(),
                new ResolverConfiguration(),
                new NullProgressMonitor());
    }

    ////////////////
    // Test checks
    ////////////////

    private void checkIpsProjectProperties() {
        IIpsProjectProperties props = ipsProject.getProperties();
        assertThat(props.getVersionProviderId(), is("org.faktorips.maven.mavenVersionProvider"));
        String mavenContainerId = "JDTClasspathContainer";
        String optionalPath = "org.eclipse.m2e.MAVEN2_CLASSPATH_CONTAINER";
        IIpsObjectPath path = props.getIpsObjectPath();
        boolean containsMavenContainer = Arrays.stream(path.getEntries())
                .filter(IpsContainerEntry.class::isInstance)
                .map(entry -> (IpsContainerEntry)entry)
                .anyMatch(entry -> mavenContainerId.equals(entry.getContainerTypeId())
                        && optionalPath.equals(entry.getOptionalPath()));
        assertThat(containsMavenContainer, is(true));
    }

    private void checkManifestFile() throws Exception {
        AFile file = ipsProject.getProject().getFile(MANIFEST_PATH);
        try (InputStream in = file.getContents()) {
            Manifest manifest = new Manifest(in);
            Attributes mainAttributes = manifest.getMainAttributes();
            String basePackageName = projectCreationProperties.getBasePackageName();
            String sourcecodeOutput = ipsProject.getIpsObjectPath().getOutputFolderForMergableSources()
                    .getProjectRelativePath().toString();
            String resourceOutput = ipsProject.getIpsObjectPath().getOutputFolderForDerivedSources()
                    .getProjectRelativePath().toString();
            StringBuilder objectDirAttributeBuilder = new StringBuilder();
            objectDirAttributeBuilder
                    .append(projectCreationProperties.getSourceFolderName()).append(";")
                    .append("toc=\"faktorips-repository-toc.xml\"").append(";")
                    .append("validation-messages=\"message-validation\"");
            assertThat(mainAttributes.getValue(Attributes.Name.MANIFEST_VERSION), is("1.0"));
            assertThat(mainAttributes.getValue("Fips-BasePackage"), is(basePackageName));
            assertThat(mainAttributes.getValue("Fips-SourcecodeOutput"), is(sourcecodeOutput));
            assertThat(mainAttributes.getValue("Fips-ResourceOutput"), is(resourceOutput));
            assertThat(mainAttributes.getValue("Fips-ObjectDir"), is(objectDirAttributeBuilder.toString()));
        }
    }

    private void checkMavenProperties(Model mavenModel) {
        String version = mavenModel.getProperties().getProperty("faktorips.version");
        assertThat(version, is(faktorIpsVersion));
    }

    private void checkMavenDependencies(MavenProject mavenProject) {
        Map<String, Dependency> dependencies = mavenProject.getDependencies()
                .stream()
                .collect(Collectors.toMap(Dependency::getArtifactId, Function.identity()));
        assertThat(dependencies.size(), is(2));
        Dependency runtime = dependencies.get("faktorips-runtime");
        assertNotNull(runtime);
        assertThat(runtime.getGroupId(), is("org.faktorips"));
        assertThat(runtime.getVersion(), is(faktorIpsVersion));
        Dependency groovy = dependencies.get("faktorips-runtime-groovy");
        assertNotNull(groovy);
        assertThat(groovy.getGroupId(), is("org.faktorips"));
        assertThat(groovy.getVersion(), is(faktorIpsVersion));
    }

    private void checkMavenResources(Build build) {
        String sourceFolderName = projectCreationProperties.getSourceFolderName();
        String resourceDirectory = ipsProject.getIpsObjectPath().getOutputFolderForDerivedSources()
                .getProjectRelativePath().toString();
        List<Resource> resources = build.getResources();
        List<Resource> sourceFolderResources = resources.stream()
                .filter(r -> r.getDirectory() != null && r.getDirectory().endsWith(sourceFolderName))
                .collect(Collectors.toList());
        assertThat(sourceFolderResources.size(), is(1));
        Resource sourceFolderResource = sourceFolderResources.get(0);
        assertThat(sourceFolderResource.getTargetPath(), is(sourceFolderName));

        List<Resource> resourcesFolderResources = resources.stream()
                .filter(r -> r.getDirectory() != null && r.getDirectory().endsWith(resourceDirectory))
                .collect(Collectors.toList());
        assertThat(resourcesFolderResources.size(), is(1));
        Resource resourcesFolderResource = resourcesFolderResources.get(0);
        List<String> includes = resourcesFolderResource.getIncludes();
        assertThat(includes.size(), is(2));
        assertThat(includes.contains("**/*.xml"), is(true));
        assertThat(includes.contains("**/*.properties"), is(true));
    }

    private void checkMavenPluginManagement(PluginManagement pluginManagement) {
        String mavenPluginsGroupId = "org.apache.maven.plugins";
        Map<String, Plugin> plugins = pluginManagement.getPlugins().stream()
                .collect(Collectors.toMap(Plugin::getArtifactId, Function.identity()));
        Plugin sourcePlugin = plugins.get("maven-source-plugin");
        assertNotNull(sourcePlugin);
        assertThat(sourcePlugin.getGroupId(), is(mavenPluginsGroupId));
        assertThat(sourcePlugin.getVersion(), is("3.2.1"));

        Plugin jarPlugin = plugins.get("maven-jar-plugin");
        assertNotNull(jarPlugin);
        assertThat(jarPlugin.getGroupId(), is(mavenPluginsGroupId));
        assertThat(jarPlugin.getVersion(), is("3.2.0"));

        Plugin ipsPlugin = plugins.get("faktorips-maven-plugin");
        assertNotNull(ipsPlugin);
        assertThat(ipsPlugin.getGroupId(), is("org.faktorips"));
        assertThat(ipsPlugin.getVersion(), is(faktorIpsVersion));
    }

    private void checkMavenPlugins(Build build) {
        String mavenPluginsGroupId = "org.apache.maven.plugins";
        Map<String, Plugin> plugins = build.getPlugins().stream()
                .collect(Collectors.toMap(Plugin::getArtifactId, Function.identity()));

        Plugin sourcePlugin = plugins.get("maven-source-plugin");
        assertNotNull(sourcePlugin);
        assertThat(sourcePlugin.getGroupId(), is(mavenPluginsGroupId));
        assertThat(sourcePlugin.getVersion(), is("3.2.1"));
        Map<String, PluginExecution> pluginExecutions = sourcePlugin.getExecutions().stream()
                .collect(Collectors.toMap(PluginExecution::getId, Function.identity()));
        PluginExecution attachExecution = pluginExecutions.get("attach-sources");
        assertNotNull(attachExecution);
        assertThat(attachExecution.getGoals().contains("jar"), is(true));

        Plugin jarPlugin = plugins.get("maven-jar-plugin");
        assertNotNull(jarPlugin);
        assertThat(jarPlugin.getGroupId(), is(mavenPluginsGroupId));
        assertThat(jarPlugin.getVersion(), is("3.2.0"));

        Xpp3Dom configuration = (Xpp3Dom)jarPlugin.getConfiguration();
        assertNotNull(configuration);
        checkJarPluginRequiredConfiguration(configuration);

        Plugin ipsPlugin = plugins.get("faktorips-maven-plugin");
        assertNotNull(ipsPlugin);
        assertThat(ipsPlugin.getGroupId(), is("org.faktorips"));
        PluginExecution ipsPluginExecution = ipsPlugin.getExecutions().get(0);
        assertNotNull(ipsPluginExecution);
        assertThat(ipsPluginExecution.getGoals().contains("faktorips-clean"), is(true));
        assertThat(ipsPluginExecution.getGoals().contains("faktorips-build"), is(true));
    }

    private void checkExistingManifestFile() throws Exception {
        AFile file = ipsProject.getProject().getFile(MANIFEST_PATH);
        try (InputStream in = file.getContents()) {
            Manifest manifest = new Manifest(in);
            Attributes mainAttributes = manifest.getMainAttributes();
            assertThat(mainAttributes.getValue("TestEntry"), is("stay"));

            String basePackageName = projectCreationProperties.getBasePackageName();
            String sourcecodeOutput = ipsProject.getIpsObjectPath().getOutputFolderForMergableSources()
                    .getProjectRelativePath().toString();
            String resourceOutput = ipsProject.getIpsObjectPath().getOutputFolderForDerivedSources()
                    .getProjectRelativePath().toString();
            StringBuilder objectDirAttributeBuilder = new StringBuilder();
            objectDirAttributeBuilder
                    .append(projectCreationProperties.getSourceFolderName()).append(";")
                    .append("toc=\"faktorips-repository-toc.xml\"").append(";")
                    .append("validation-messages=\"message-validation\"");
            assertThat(mainAttributes.getValue(Attributes.Name.MANIFEST_VERSION), is("1.0"));
            assertThat(mainAttributes.getValue("Fips-BasePackage"), is(basePackageName));
            assertThat(mainAttributes.getValue("Fips-SourcecodeOutput"), is(sourcecodeOutput));
            assertThat(mainAttributes.getValue("Fips-ResourceOutput"), is(resourceOutput));
            assertThat(mainAttributes.getValue("Fips-ObjectDir"), is(objectDirAttributeBuilder.toString()));
        }
    }

    private void checkMavenPropertiesContentPom(Model mavenModel) {
        String stayProperty = mavenModel.getProperties().getProperty("stay");
        String versionProperty = mavenModel.getProperties().getProperty("faktorips.version");
        assertThat(stayProperty, is("stay"));

        assertThat(versionProperty, is("20.6.0"));
    }

    private void checkMavenDependenciesContentPom(MavenProject mavenProject) {
        Map<String, Dependency> dependencies = mavenProject.getDependencies()
                .stream()
                .collect(Collectors.toMap(Dependency::getArtifactId, Function.identity()));
        assertThat(dependencies.size(), is(3));
        Dependency stay = dependencies.get("stayArtifact");
        assertNotNull(stay);
        assertThat(stay.getGroupId(), is("stayGroup"));
        assertThat(stay.getVersion(), is("1.0.0"));
        Dependency runtime = dependencies.get("faktorips-runtime");
        assertNotNull(runtime);
        assertThat(runtime.getGroupId(), is("org.faktorips"));
        assertThat(runtime.getVersion(), is("20.6.0"));
        Dependency groovy = dependencies.get("faktorips-runtime-groovy");
        assertNotNull(groovy);
        assertThat(groovy.getGroupId(), is("org.faktorips"));
        assertThat(groovy.getVersion(), is("20.6.0"));
    }

    private void checkMavenResourcesContentPom(Build build) {
        Optional<Resource> resource = build.getResources().stream()
                .filter(r -> r.getTargetPath().equals("stay"))
                .findAny();
        assertThat(resource.isPresent(), is(true));
        assertThat(resource.get().getDirectory().endsWith("stay"), is(true));
        assertThat(resource.get().getTargetPath(), is("stay"));

        checkMavenResources(build);
    }

    private void checkMavenPluginManagementContentPom(PluginManagement pluginManagement) {
        String mavenPluginsGroupId = "org.apache.maven.plugins";
        Map<String, Plugin> plugins = pluginManagement.getPlugins().stream()
                .collect(Collectors.toMap(Plugin::getArtifactId, Function.identity()));
        Plugin stayPlugin = plugins.get("stayArtifact");
        assertNotNull(stayPlugin);
        assertThat(stayPlugin.getGroupId(), is("stayGroup"));
        assertThat(stayPlugin.getVersion(), is("1.0.0"));

        Plugin sourcePlugin = plugins.get("maven-source-plugin");
        assertNotNull(sourcePlugin);
        assertThat(sourcePlugin.getGroupId(), is(mavenPluginsGroupId));
        assertThat(sourcePlugin.getVersion(), is("3.2.0"));

        Plugin jarPlugin = plugins.get("maven-jar-plugin");
        assertNotNull(jarPlugin);
        assertThat(jarPlugin.getGroupId(), is(mavenPluginsGroupId));
        assertThat(jarPlugin.getVersion(), is("3.1.2"));

        Plugin ipsPlugin = plugins.get("faktorips-maven-plugin");
        assertNotNull(ipsPlugin);
        assertThat(ipsPlugin.getGroupId(), is("org.faktorips"));
        assertThat(ipsPlugin.getVersion(), is("20.6.0"));
    }

    private void checkMavenPluginsContentPom(Build build) {
        String mavenPluginsGroupId = "org.apache.maven.plugins";
        Map<String, Plugin> plugins = build.getPlugins().stream()
                .collect(Collectors.toMap(Plugin::getArtifactId, Function.identity()));
        Plugin stayPlugin = plugins.get("stayArtifact");
        assertNotNull(stayPlugin);
        assertThat(stayPlugin.getGroupId(), is("stayGroup"));
        assertThat(stayPlugin.getVersion(), is("1.0.0"));

        Plugin sourcePlugin = plugins.get("maven-source-plugin");
        assertNotNull(sourcePlugin);
        assertThat(sourcePlugin.getGroupId(), is(mavenPluginsGroupId));
        assertThat(sourcePlugin.getVersion(), is("3.2.0"));
        Map<String, PluginExecution> pluginExecutions = sourcePlugin.getExecutions().stream()
                .collect(Collectors.toMap(PluginExecution::getId, Function.identity()));
        PluginExecution stayExecution = pluginExecutions.get("stay");
        assertNotNull(stayExecution);
        assertThat(stayExecution.getGoals().contains("jar"), is(true));
        PluginExecution attachExecution = pluginExecutions.get("attach-sources");
        assertNotNull(attachExecution);
        long jarGoalCount = attachExecution.getGoals().stream()
                .filter(goal -> goal.equals("jar"))
                .count();
        assertThat(jarGoalCount, is(1L));
        assertThat(attachExecution.getGoals().contains("jar"), is(true));
        assertThat(attachExecution.getGoals().contains("aggregate"), is(true));

        Plugin jarPlugin = plugins.get("maven-jar-plugin");
        assertNotNull(jarPlugin);
        assertThat(jarPlugin.getGroupId(), is(mavenPluginsGroupId));
        assertThat(jarPlugin.getVersion(), is("3.1.2"));

        Xpp3Dom configuration = (Xpp3Dom)jarPlugin.getConfiguration();
        assertNotNull(configuration);
        // Check required configuration before already existent configuration
        checkJarPluginRequiredConfiguration(configuration);
        Xpp3Dom stayOutputDir = configuration.getChild("outputDirectory");
        assertNotNull(stayOutputDir);
        assertThat(stayOutputDir.getValue(), is("stay"));
        Xpp3Dom stayArchiveForce = configuration.getChild("archive").getChild("forced");
        assertNotNull(stayArchiveForce);
        assertThat(stayArchiveForce.getValue(), is("true"));

        Plugin ipsPlugin = plugins.get("faktorips-maven-plugin");
        assertNotNull(ipsPlugin);
        assertThat(ipsPlugin.getGroupId(), is("org.faktorips"));
        PluginExecution ipsPluginExecution = ipsPlugin.getExecutions().get(0);
        assertNotNull(ipsPluginExecution);
        assertThat(ipsPluginExecution.getGoals().contains("faktorips-clean"), is(true));
        assertThat(ipsPluginExecution.getGoals().contains("faktorips-build"), is(true));
    }

    private void checkJarPluginRequiredConfiguration(Xpp3Dom configuration) {
        long skipCount = Arrays.stream(configuration.getChildren())
                .filter(child -> child.getName().equals("skip"))
                .count();
        assertThat(skipCount, is(1L));
        Xpp3Dom skip = configuration.getChild("skip");
        assertNotNull(skip);
        assertThat(skip.getValue(), is("false"));

        long archiveCount = Arrays.stream(configuration.getChildren())
                .filter(child -> child.getName().equals("archive"))
                .count();
        assertThat(archiveCount, is(1L));
        Xpp3Dom archive = configuration.getChild("archive");
        assertNotNull(archive);

        long manifestFileCount = Arrays.stream(archive.getChildren())
                .filter(child -> child.getName().equals("manifestFile"))
                .count();
        assertThat(manifestFileCount, is(1L));
        Xpp3Dom manifestFile = archive.getChild("manifestFile");
        assertNotNull(manifestFile);
        assertThat(manifestFile.getValue(), is(MANIFEST_PATH));
    }
}
