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

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.faktorips.devtools.model.IIpsProjectConfigurator;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.plugin.ExtensionPoints;
import org.faktorips.devtools.model.plugin.IpsModelActivator;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.util.IpsProjectCreationProperties;
import org.faktorips.devtools.model.util.PersistenceSupportNames;
import org.faktorips.m2e.version.MavenVersionFormatter;
import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * Concrete implementation of {@link IIpsProjectConfigurator}.
 * <p>
 * This class provides an extension for the IPS model extension point
 * {@link ExtensionPoints#ADD_IPS_NATURE}.
 * <p>
 * Use this in order to configure an existent Maven project for the usage of Faktor-IPS.
 * 
 * @author Florian Orendi
 */
public class MavenIpsProjectConfigurator implements IIpsProjectConfigurator {

    private static final String META_INF_FOLDER = "META-INF";
    private static final String MANIFEST_FILE = "MANIFEST.MF";

    private static final String DEPENDENCY_IPS_GROUP_ID = "org.faktorips";

    private static final String MAVEN_PROPERTY_IPS_VERSION = "faktorips.version";

    @Override
    public boolean canConfigure(IProject project) {
        return MavenPlugin.getMavenProjectRegistry().getProject(project) != null;
    }

    @Override
    public boolean isGroovySupported() {
        return true;
    }

    @Override
    public void configureIpsProject(IIpsProject ipsProject, IpsProjectCreationProperties creationProperties)
            throws CoreException {
        IProject project = ipsProject.getProject();
        IIpsObjectPath ipsObjectPath = ipsProject.getIpsObjectPath();

        String errorMessage = creationProperties.checkForRequiredProperties()
                .concat(checkForRequiredIpsObjectPathProperties(ipsObjectPath));

        if (IpsStringUtils.isNotEmpty(errorMessage)) {
            throw new CoreException(new IpsStatus(errorMessage));
        }

        addIpsProjectProperties(ipsProject);

        createManifestFile(ipsProject, creationProperties);

        String resourceFolder = ipsObjectPath.getOutputFolderForDerivedSources().getProjectRelativePath().toString();
        configureMaven(project, resourceFolder, creationProperties);

        // Updating the project is required in order to synchronize the project with the new POM
        MavenPlugin.getProjectConfigurationManager().updateProjectConfiguration(project, new NullProgressMonitor());
    }

    /**
     * Checks whether the {@link IIpsObjectPath} contains the required information for configuring
     * the Maven project.
     * <p>
     * Returns an error message containing the missing properties. The message is empty if all
     * required properties exist.
     * 
     * @param ipsObjectPath The IPS object path
     * @return the error message
     */
    private String checkForRequiredIpsObjectPathProperties(IIpsObjectPath ipsObjectPath) {
        boolean existsOutputFolderForMergableSources = ipsObjectPath.getOutputFolderForMergableSources() != null;
        boolean existsOutputFolderForDerivedSources = ipsObjectPath.getOutputFolderForMergableSources() != null;

        if (existsOutputFolderForMergableSources && existsOutputFolderForDerivedSources) {
            return IpsStringUtils.EMPTY;
        }

        StringBuilder errorMessage = new StringBuilder(
                "The following required IPS-Object-Path properties are missing:\n");
        if (!existsOutputFolderForMergableSources) {
            errorMessage.append("OutputFolderForMergableSources;\n");
        }
        if (!existsOutputFolderForDerivedSources) {
            errorMessage.append("OutputFolderForDerivedSources;\n");
        }

        return errorMessage.toString();
    }

    /**
     * Adds all properties to the .ipsproject file which are required for using Maven.
     * 
     * @param ipsProject The created {@link IIpsProject}
     * @throws CoreException If setting the properties failed
     */
    private void addIpsProjectProperties(IIpsProject ipsProject) throws CoreException {
        IIpsProjectProperties properties = ipsProject.getProperties();
        properties.setVersionProviderId("org.faktorips.maven.mavenVersionProvider");
        IIpsObjectPath ipsObjectPath = ipsProject.getIpsObjectPath();
        IIpsObjectPathEntry entry = ipsObjectPath.newContainerEntry("JDTClasspathContainer",
                "org.eclipse.m2e.MAVEN2_CLASSPATH_CONTAINER");
        entry.setReexported(true);
        properties.setIpsObjectPath(ipsObjectPath);
        ipsProject.setProperties(properties);
    }

    /**
     * Creates a manifest file with the required attributes for using Faktor-IPS together with
     * Maven.
     * <p>
     * Already existing attributes stay untouched.
     * 
     * @param ipsProject The created {@link IIpsProject}
     * @param creationProperties The required properties {@link IpsProjectCreationProperties} for
     *            creating a Faktor-IPS project
     * @throws CoreException If creating the manifest file failed
     */
    private void createManifestFile(IIpsProject ipsProject, IpsProjectCreationProperties creationProperties)
            throws CoreException {
        IProject project = ipsProject.getProject();
        IFolder metaInfFolder = project.getFolder(META_INF_FOLDER);
        if (!metaInfFolder.exists()) {
            metaInfFolder.create(true, true, new NullProgressMonitor());
        }
        try {
            IFile manifestFile = metaInfFolder.getFile(MANIFEST_FILE);
            Manifest manifest = null;
            if (manifestFile.exists()) {
                manifest = new Manifest(manifestFile.getContents());
            } else {
                manifestFile.create(null, true, new NullProgressMonitor());
                manifest = new Manifest();
            }
            addIpsManifestFileAttributes(manifest, ipsProject.getProperties().getIpsObjectPath(), creationProperties);

            FileOutputStream fileOutputStream = new FileOutputStream(manifestFile.getLocation().toFile());
            manifest.write(fileOutputStream);
            fileOutputStream.close();
        } catch (IOException e) {
            throw new CoreException(new IpsStatus(e.getMessage(), e));
        }
    }

    /**
     * Adds all attributes required by Faktor-IPS to a passed manifest file.
     * 
     * @param manifest The used manifest file
     * @param ipsObjectPath The {@link IIpsObjectPath}
     * @param creationProperties The required properties {@link IpsProjectCreationProperties} for
     *            creating a Faktor-IPS project
     */
    private void addIpsManifestFileAttributes(Manifest manifest,
            IIpsObjectPath ipsObjectPath,
            IpsProjectCreationProperties creationProperties) {
        Attributes mainAttributes = manifest.getMainAttributes();
        mainAttributes.putIfAbsent(Attributes.Name.MANIFEST_VERSION, "1.0");
        mainAttributes.putValue("Fips-BasePackage", creationProperties.getBasePackageName());

        mainAttributes.putValue("Fips-SourcecodeOutput",
                ipsObjectPath.getOutputFolderForMergableSources().getProjectRelativePath().toString());
        mainAttributes.putValue("Fips-ResourceOutput",
                ipsObjectPath.getOutputFolderForDerivedSources().getProjectRelativePath().toString());

        StringBuilder objectDirAttributeBuilder = new StringBuilder();
        objectDirAttributeBuilder
                .append(creationProperties.getSourceFolderName()).append(";")
                .append("toc=\"faktorips-repository-toc.xml\"").append(";")
                .append("validation-messages=\"message-validation\"");
        mainAttributes.putValue("Fips-ObjectDir", objectDirAttributeBuilder.toString());
    }

    /**
     * Configures the existent Maven project for using it together with Faktor-IPS.
     * <p>
     * Already existing configurations stay untouched.
     * 
     * @param project The currently selected Maven project
     * @param resourcesPath The path to the resources folder
     * @param creationProperties The required properties {@link IpsProjectCreationProperties} for
     *            creating a Faktor-IPS project
     * @throws CoreException If configuring the project failed
     */
    private void configureMaven(IProject project, String resourcesPath, IpsProjectCreationProperties creationProperties)
            throws CoreException {
        IMavenProjectFacade facade = MavenPlugin.getMavenProjectRegistry().getProject(project);
        if (facade == null) {
            String message = String.format("The project \"%s\" is not a Maven project", project.getName());
            IpsStatus status = new IpsStatus(message);
            throw new CoreException(status);
        }

        // The lazy load of the Maven project using a progress monitor is required here in case that
        // the cached projects have not been updated yet. Then, getMavenProject() would return null.
        MavenProject mavenProject = facade.getMavenProject(new NullProgressMonitor());
        Model mavenModel = mavenProject.getOriginalModel();
        if (mavenModel == null) {
            String message = String.format("The project \"%s\" is not a valid Maven project", project.getName());
            IpsStatus status = new IpsStatus(message);
            throw new CoreException(status);
        }

        Build build = mavenModel.getBuild();
        if (build == null) {
            build = new Build();
        }

        addMavenProperties(mavenModel);
        addMavenDependencies(mavenModel, creationProperties);
        addMavenResources(build, resourcesPath, creationProperties);
        addMavenPlugins(build);

        mavenModel.setBuild(build);

        writePom(mavenModel);
    }

    /**
     * Updates an existing POM file.
     * 
     * @implNote Only using the setter for the model within the Maven project is not enough since it
     *           does not update the POM file itself.
     * 
     * @param mavenModel The model to be written to the POM file
     * @throws CoreException If updating the POM file failed
     */
    private void writePom(Model mavenModel) throws CoreException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(mavenModel.getPomFile())) {
            MavenPlugin.getMaven().writeModel(mavenModel, fileOutputStream);
        } catch (IOException e) {
            throw new CoreException(new IpsStatus(e.getMessage(), e));
        }
    }

    /**
     * Adds all properties to Maven required for using Faktor-IPS.
     * 
     * @param mavenModel The model of the selected Maven project
     */
    private void addMavenProperties(Model mavenModel) {
        String version = getIpsVersionForMaven();
        mavenModel.addProperty(MAVEN_PROPERTY_IPS_VERSION, version);
    }

    /**
     * Adds all dependencies to Maven required for using Faktor-IPS.
     * <p>
     * Existent dependencies are not touched.
     * 
     * @param mavenModel The model of the selected Maven project
     * @param creationProperties The {@link IpsProjectCreationProperties} containing information
     *            about the required dependencies
     */
    private void addMavenDependencies(Model mavenModel, IpsProjectCreationProperties creationProperties)
            throws CoreException {
        String ipsVersion = String.format("${%s}", MAVEN_PROPERTY_IPS_VERSION);

        Set<String> dependencies = mavenModel.getDependencies().stream()
                .map(Dependency::getArtifactId)
                .collect(Collectors.toSet());

        String runtimeArtifactId = "faktorips-runtime";
        if (!dependencies.contains(runtimeArtifactId)) {
            Dependency ipsRuntimeDependency = new Dependency();
            ipsRuntimeDependency.setGroupId(DEPENDENCY_IPS_GROUP_ID);
            ipsRuntimeDependency.setArtifactId(runtimeArtifactId);
            ipsRuntimeDependency.setVersion(ipsVersion);
            mavenModel.addDependency(ipsRuntimeDependency);
        }

        if (creationProperties.isGroovySupport()) {
            String groovyArtifactId = "faktorips-runtime-groovy";
            if (!dependencies.contains(groovyArtifactId)) {
                Dependency ipsGroovyDependency = new Dependency();
                ipsGroovyDependency.setGroupId(DEPENDENCY_IPS_GROUP_ID);
                ipsGroovyDependency.setArtifactId(groovyArtifactId);
                ipsGroovyDependency.setVersion(ipsVersion);
                mavenModel.addDependency(ipsGroovyDependency);
            }
        }

        if (creationProperties.isPersistentProject()) {
            String persistenceSupport = creationProperties.getPersistenceSupport();
            Dependency persistenceDependency = new Dependency();
            persistenceDependency.setGroupId("org.eclipse.persistence");
            if (persistenceSupport.equals(PersistenceSupportNames.ID_ECLIPSE_LINK_1_1)) {
                persistenceDependency.setArtifactId("eclipselink");
                persistenceDependency.setVersion("1.1.0");
            } else if (persistenceSupport.equals(PersistenceSupportNames.ID_ECLIPSE_LINK_2_5)) {
                persistenceDependency.setArtifactId("eclipselink");
                persistenceDependency.setVersion("2.5.0");
            } else if (persistenceSupport.equals(PersistenceSupportNames.ID_GENERIC_JPA_2)) {
                persistenceDependency.setArtifactId("javax.persistence");
                persistenceDependency.setVersion("2.0.0");
            } else if (persistenceSupport.equals(PersistenceSupportNames.ID_GENERIC_JPA_2_1)) {
                persistenceDependency.setArtifactId("javax.persistence");
                persistenceDependency.setVersion("2.1.0");
            } else {
                throw new CoreException(new IpsStatus(
                        String.format("The selected persistence support \"%s\" is not supported.",
                                creationProperties.getPersistenceSupport())));
            }
            mavenModel.addDependency(persistenceDependency);
        }
    }

    /**
     * Adds all resources to Maven required for using Faktor-IPS.
     * <p>
     * Existent resources are not touched.
     * 
     * @param mavenBuild The build of the selected Maven project
     * @param creationProperties The required properties {@link IpsProjectCreationProperties} for
     *            creating a Faktor-IPS project
     */
    private void addMavenResources(Build mavenBuild,
            String resourcesPath,
            IpsProjectCreationProperties creationProperties) {
        Resource resourceFolder = new Resource();
        resourceFolder.setDirectory(resourcesPath);
        resourceFolder.addInclude("**/*.xml");
        resourceFolder.addInclude("**/*.properties");
        mavenBuild.addResource(resourceFolder);

        Resource sourceFolder = new Resource();
        String sourcFolderName = creationProperties.getSourceFolderName();
        sourceFolder.setDirectory(sourcFolderName);
        sourceFolder.setTargetPath(sourcFolderName);
        mavenBuild.addResource(sourceFolder);
    }

    /**
     * Adds all plugins to Maven required for using Faktor-IPS.
     * <p>
     * The version is defined in {@code PluginManagement}.
     * <p>
     * Existent plugins are not touched.
     * 
     * @param mavenBuild The build of the selected Maven project
     */
    private void addMavenPlugins(Build mavenBuild) {
        PluginManagement pluginManagement = mavenBuild.getPluginManagement();
        if (pluginManagement == null) {
            pluginManagement = new PluginManagement();
        }

        // Plugin management

        Map<String, Plugin> managementPlugins = pluginManagement.getPlugins().stream()
                .collect(Collectors.toMap(Plugin::getArtifactId, Function.identity()));

        String mavenPluginsGroupId = "org.apache.maven.plugins";

        // Jar plugin
        Plugin jarPlugin;
        String jarPluginArtifactId = "maven-jar-plugin";
        if (!managementPlugins.containsKey(jarPluginArtifactId)) {
            jarPlugin = new Plugin();
            jarPlugin.setGroupId(mavenPluginsGroupId);
            jarPlugin.setArtifactId(jarPluginArtifactId);
            jarPlugin.setVersion("3.2.0");
            pluginManagement.addPlugin(jarPlugin);
        }

        // Source plugin
        Plugin sourcePlugin;
        String sourcePluginArtifactId = "maven-source-plugin";
        if (!managementPlugins.containsKey(jarPluginArtifactId)) {
            sourcePlugin = new Plugin();
            sourcePlugin.setGroupId(mavenPluginsGroupId);
            sourcePlugin.setArtifactId(sourcePluginArtifactId);
            sourcePlugin.setVersion("3.2.1");
            pluginManagement.addPlugin(sourcePlugin);
        }

        mavenBuild.setPluginManagement(pluginManagement);

        // Build plugins

        Map<String, Plugin> buildPlugins = mavenBuild.getPlugins().stream()
                .collect(Collectors.toMap(Plugin::getArtifactId, Function.identity()));

        // Jar plugin
        if (buildPlugins.containsKey(jarPluginArtifactId)) {
            jarPlugin = buildPlugins.get(jarPluginArtifactId);
            Xpp3Dom configuration = (Xpp3Dom)jarPlugin.getConfiguration();
            configuration.addChild(createJarPluginSkipConfiguration());
            configuration.addChild(createJarPluginArchiveConfiguration());
        } else {
            jarPlugin = new Plugin();
            jarPlugin.setGroupId(mavenPluginsGroupId);
            jarPlugin.setArtifactId(jarPluginArtifactId);
            jarPlugin.setConfiguration(createJarPluginConfiguration());
            mavenBuild.addPlugin(jarPlugin);
        }

        // Source plugin
        if (buildPlugins.containsKey(sourcePluginArtifactId)) {
            sourcePlugin = buildPlugins.get(sourcePluginArtifactId);
        } else {
            sourcePlugin = new Plugin();
            sourcePlugin.setGroupId(mavenPluginsGroupId);
            sourcePlugin.setArtifactId(sourcePluginArtifactId);
            mavenBuild.addPlugin(sourcePlugin);
        }
        String id = "attach-sources";
        String goal = "jar";
        Optional<PluginExecution> pluginExecution = sourcePlugin.getExecutions().stream()
                .filter(execution -> execution.getId().equals(id))
                .findAny();
        if (pluginExecution.isPresent()) {
            pluginExecution.get().addGoal(goal);
        } else {
            PluginExecution execution = new PluginExecution();
            execution.setId(id);
            execution.addGoal(goal);
            sourcePlugin.addExecution(execution);
        }
    }

    /**
     * Creates the configuration required by the Maven-Jar-Plugin.
     * <p>
     * The configuration has to be the following DOM object:<br>
     * {@code org.codehaus.plexus.util.xml.Xpp3Dom}
     * 
     * @return The creates configuration
     */
    private Xpp3Dom createJarPluginConfiguration() {
        Xpp3Dom configuration = new Xpp3Dom("configuration");

        Xpp3Dom skip = createJarPluginSkipConfiguration();
        configuration.addChild(skip);

        Xpp3Dom archive = createJarPluginArchiveConfiguration();
        configuration.addChild(archive);

        return configuration;
    }

    /**
     * Creates a part of the Maven-Jar-Plugin configuration.
     * <p>
     * {@code skip} should be set to {@code false}.
     * 
     * @return the created configuration element
     */
    private Xpp3Dom createJarPluginSkipConfiguration() {
        Xpp3Dom skip = new Xpp3Dom("skip");
        skip.setValue("false");
        return skip;
    }

    /**
     * Creates a part of the Maven-Jar-Plugin configuration.
     * <p>
     * {@code archive} should point to the manifest file.
     * 
     * @return the created configuration element
     */
    private Xpp3Dom createJarPluginArchiveConfiguration() {
        Xpp3Dom archive = new Xpp3Dom("archive");
        Xpp3Dom manifestFile = new Xpp3Dom("manifestFile");
        String manifestFilePath = META_INF_FOLDER.concat("/").concat(MANIFEST_FILE);
        manifestFile.setValue(manifestFilePath);
        archive.addChild(manifestFile);
        return archive;
    }

    /**
     * Provides the current Faktor-IPS version using a valid Maven format.
     * 
     * @return The Faktor-IPS version
     */
    private String getIpsVersionForMaven() {
        String versionWithQualifier = IpsModelActivator.getInstalledFaktorIpsVersion();
        try {
            return MavenVersionFormatter.formatVersion(versionWithQualifier);
        } catch (IllegalArgumentException e) {
            throw new CoreRuntimeException(e.getMessage());
        }
    }
}
