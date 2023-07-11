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
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.AJavaProject;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.abstraction.util.PathUtil;
import org.faktorips.devtools.model.IIpsProjectConfigurator;
import org.faktorips.devtools.model.builder.JaxbSupportVariant;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.plugin.IpsModelActivator;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.plugin.extensions.IpsProjectConfigurerExtension;
import org.faktorips.devtools.model.util.IpsProjectCreationProperties;
import org.faktorips.devtools.model.util.PersistenceSupportNames;
import org.faktorips.m2e.version.MavenVersionFormatter;
import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * Concrete implementation of {@link IIpsProjectConfigurator}.
 * <p>
 * This class provides an extension for the IPS model extension point
 * {@link IpsProjectConfigurerExtension#EXTENSION_POINT_ID_ADD_IPS_NATURE}.
 * <p>
 * Use this in order to configure an existent Maven project for the usage of Faktor-IPS.
 *
 * @author Florian Orendi
 */
public class MavenIpsProjectConfigurator implements IIpsProjectConfigurator {

    private static final String META_INF_FOLDER = "META-INF";
    private static final String MANIFEST_FILE = "MANIFEST.MF";

    private static final String IPS_GROUP_ID = "org.faktorips";
    private static final String IPS_PLUGIN_ARTIFACT_ID = "faktorips-maven-plugin";
    private static final String MAVEN_PROPERTY_IPS_VERSION = "faktorips.version";

    private static final String MAVEN_PLUGINS_GROUP_ID = "org.apache.maven.plugins";
    private static final String MAVEN_JAR_PLUGIN_ARTIFACT_ID = "maven-jar-plugin";
    private static final String MAVEN_SOURCE_PLUGIN_ARTIFACT_ID = "maven-source-plugin";

    @Override
    public boolean canConfigure(AJavaProject javaProject) {
        return MavenPlugin.getMavenProjectRegistry().getProject(javaProject.getProject().unwrap()) != null;
    }

    @Override
    public boolean isGroovySupported(AJavaProject javaProject) {
        return true;
    }

    @Override
    public void configureIpsProject(IIpsProject ipsProject, IpsProjectCreationProperties creationProperties) {
        IProject project = ipsProject.getProject().unwrap();
        IIpsObjectPath ipsObjectPath = ipsProject.getIpsObjectPath();

        String errorMessage = checkForRequiredIpsObjectPathProperties(ipsObjectPath);

        if (IpsStringUtils.isNotEmpty(errorMessage)) {
            throw new IpsException(new IpsStatus(errorMessage));
        }

        addIpsProjectProperties(ipsProject);

        createManifestFile(ipsProject, creationProperties);

        String resourceFolder = PathUtil
                .toPortableString(ipsObjectPath.getOutputFolderForDerivedSources().getProjectRelativePath());
        try {
            configureMaven(project, resourceFolder, creationProperties);

            // Updating the project is required in order to synchronize the project with the new POM
            MavenPlugin.getProjectConfigurationManager().updateProjectConfiguration(project, new NullProgressMonitor());
        } catch (CoreException e) {
            throw new IpsException(e);
        }
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
        boolean existsOutputFolderForDerivedSources = ipsObjectPath.getOutputFolderForDerivedSources() != null;

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
     * @throws IpsException If setting the properties failed
     */
    private void addIpsProjectProperties(IIpsProject ipsProject) {
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
     * @throws IpsException If creating the manifest file failed
     */
    private void createManifestFile(IIpsProject ipsProject, IpsProjectCreationProperties creationProperties) {
        AProject project = ipsProject.getProject();
        AFolder metaInfFolder = project.getFolder(META_INF_FOLDER);
        if (!metaInfFolder.exists()) {
            metaInfFolder.create(new NullProgressMonitor());
        }
        try {
            AFile manifestFile = metaInfFolder.getFile(MANIFEST_FILE);
            Manifest manifest = null;
            if (manifestFile.exists()) {
                manifest = new Manifest(manifestFile.getContents());
            } else {
                manifestFile.create(InputStream.nullInputStream(), new NullProgressMonitor());
                manifest = new Manifest();
            }
            addIpsManifestFileAttributes(manifest, ipsProject.getProperties().getIpsObjectPath(), creationProperties);

            try (FileOutputStream fileOutputStream = new FileOutputStream(manifestFile.getLocation().toFile())) {
                manifest.write(fileOutputStream);
            }
        } catch (IOException e) {
            throw new IpsException(new IpsStatus(e.getMessage(), e));
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
                PathUtil.toPortableString(ipsObjectPath.getOutputFolderForMergableSources().getProjectRelativePath()));
        mainAttributes.putValue("Fips-ResourceOutput",
                PathUtil.toPortableString(ipsObjectPath.getOutputFolderForDerivedSources().getProjectRelativePath()));

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
     * @throws IpsException If configuring the project failed
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
        addMavenPluginManagement(build);
        addMavenPlugins(build);

        mavenModel.setBuild(build);

        writePom(mavenModel);
    }

    /**
     * Updates an existing POM file.
     *
     * @implNote Only using the setter for the model within the Maven project is not enough since it
     *               does not update the POM file itself.
     *
     * @param mavenModel The model to be written to the POM file
     * @throws IpsException If updating the POM file failed
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
        if (!mavenModel.getProperties().containsKey(MAVEN_PROPERTY_IPS_VERSION)) {
            mavenModel.addProperty(MAVEN_PROPERTY_IPS_VERSION, version);
        }
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
    private void addMavenDependencies(Model mavenModel, IpsProjectCreationProperties creationProperties) {
        String ipsVersion = String.format("${%s}", MAVEN_PROPERTY_IPS_VERSION);

        Set<String> dependencies = mavenModel.getDependencies().stream()
                .map(Dependency::getArtifactId)
                .collect(Collectors.toSet());

        addFaktorIpsRuntime(mavenModel, ipsVersion, dependencies);

        addGroovySupport(mavenModel, creationProperties, ipsVersion, dependencies);

        addPersistenceSupport(mavenModel, creationProperties);

        addJaxbSupport(mavenModel, creationProperties, ipsVersion);
    }

    private void addFaktorIpsRuntime(Model mavenModel, String ipsVersion, Set<String> dependencies) {
        String runtimeArtifactId = "faktorips-runtime";
        if (!dependencies.contains(runtimeArtifactId)) {
            Dependency ipsRuntimeDependency = new Dependency();
            ipsRuntimeDependency.setGroupId(IPS_GROUP_ID);
            ipsRuntimeDependency.setArtifactId(runtimeArtifactId);
            ipsRuntimeDependency.setVersion(ipsVersion);
            mavenModel.addDependency(ipsRuntimeDependency);
        }
    }

    private void addGroovySupport(Model mavenModel,
            IpsProjectCreationProperties creationProperties,
            String ipsVersion,
            Set<String> dependencies) {
        if (creationProperties.isGroovySupport()) {
            String groovyArtifactId = "faktorips-runtime-groovy";
            if (!dependencies.contains(groovyArtifactId)) {
                Dependency ipsGroovyDependency = new Dependency();
                ipsGroovyDependency.setGroupId(IPS_GROUP_ID);
                ipsGroovyDependency.setArtifactId(groovyArtifactId);
                ipsGroovyDependency.setVersion(ipsVersion);
                mavenModel.addDependency(ipsGroovyDependency);
            }
        }
    }

    private void addJaxbSupport(Model mavenModel, IpsProjectCreationProperties creationProperties, String ipsVersion) {
        if (creationProperties.isJaxbEnabled()) {
            JaxbSupportVariant jaxbSupport = creationProperties.getJaxbSupport();
            switch (jaxbSupport) {
                case ClassicJAXB:
                    addDependency(mavenModel, IPS_GROUP_ID, "faktorips-runtime-javax-xml", ipsVersion);
                    break;
                case JakartaXmlBinding:
                    addDependency(mavenModel, IPS_GROUP_ID, "faktorips-runtime-jakarta-xml", ipsVersion);
                    break;
                default:
                    break;
            }
        }
    }

    private void addPersistenceSupport(Model mavenModel, IpsProjectCreationProperties creationProperties) {
        if (creationProperties.isPersistentProject()) {
            String persistenceSupport = creationProperties.getPersistenceSupport();
            switch (persistenceSupport) {
                case PersistenceSupportNames.ID_ECLIPSE_LINK_1_1:
                    addDependency(mavenModel, "org.eclipse.persistence", "eclipselink", "1.1.0");
                    break;
                case PersistenceSupportNames.ID_ECLIPSE_LINK_2_5:
                    addDependency(mavenModel, "org.eclipse.persistence", "eclipselink", "2.5.0");
                    break;
                case PersistenceSupportNames.ID_ECLIPSE_LINK_3_0:
                    addDependency(mavenModel, "org.eclipse.persistence", "eclipselink", "3.0.2");
                    addDependency(mavenModel, "jakarta.persistence", "jakarta.persistence-api", "3.0.0");
                    break;
                case PersistenceSupportNames.ID_GENERIC_JPA_2:
                    addDependency(mavenModel, "org.eclipse.persistence", "javax.persistence", "2.0.0");
                    break;
                case PersistenceSupportNames.ID_GENERIC_JPA_2_1:
                    addDependency(mavenModel, "org.eclipse.persistence", "javax.persistence", "2.1.0");
                    break;
                case PersistenceSupportNames.ID_JAKARTA_PERSISTENCE_2_2:
                    addDependency(mavenModel, "jakarta.persistence", "jakarta.persistence-api", "2.2.3");
                    break;
                case PersistenceSupportNames.ID_JAKARTA_PERSISTENCE_3_0:
                    addDependency(mavenModel, "jakarta.persistence", "jakarta.persistence-api", "3.0.0");
                    break;
                default:
                    throw new IpsException(new IpsStatus(
                            String.format("The selected persistence support \"%s\" is not supported.",
                                    creationProperties.getPersistenceSupport())));
            }
        }
    }

    private static void addDependency(Model mavenModel, String groupId, String artifactId, String version) {
        Dependency dependency = new Dependency();
        dependency.setGroupId(groupId);
        dependency.setArtifactId(artifactId);
        dependency.setVersion(version);
        mavenModel.addDependency(dependency);
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

        // Since resources does not have IDs, the directory is taken as identifier
        Map<String, Resource> resources = mavenBuild.getResources().stream()
                .collect(Collectors.toMap(Resource::getDirectory, Function.identity()));

        String includeXML = "**/*.xml";
        String includeProperties = "**/*.properties";
        String includeIpsFiles = "**/*.ips*";
        Resource resourcesFolderResource;
        if (resources.containsKey(resourcesPath)) {
            resourcesFolderResource = resources.get(resourcesPath);
            if (!resourcesFolderResource.getIncludes().contains(includeXML)) {
                resourcesFolderResource.addInclude(includeXML);
            }
            if (!resourcesFolderResource.getIncludes().contains(includeProperties)) {
                resourcesFolderResource.addInclude(includeProperties);
            }
            if (!resourcesFolderResource.getIncludes().contains(includeIpsFiles)) {
                resourcesFolderResource.addInclude(includeIpsFiles);
            }
        } else {
            resourcesFolderResource = new Resource();
            resourcesFolderResource.setDirectory(resourcesPath);
            resourcesFolderResource.addInclude(includeXML);
            resourcesFolderResource.addInclude(includeProperties);
            resourcesFolderResource.addInclude(includeIpsFiles);
            mavenBuild.addResource(resourcesFolderResource);
        }

        Resource sourceFolderResource;
        String sourceFolderName = creationProperties.getSourceFolderName();
        if (resources.containsKey(sourceFolderName)) {
            sourceFolderResource = resources.get(sourceFolderName);
            if (IpsStringUtils.isEmpty(sourceFolderResource.getTargetPath())) {
                sourceFolderResource.setTargetPath(sourceFolderName);
            }
        } else {
            sourceFolderResource = new Resource();
            sourceFolderResource.setDirectory(sourceFolderName);
            sourceFolderResource.setTargetPath(sourceFolderName);
            mavenBuild.addResource(sourceFolderResource);
        }

    }

    /**
     * Adds all plugins to the Maven Plugin-Management required for using Faktor-IPS.
     * <p>
     * Here, the versions of the plugins are defined.
     *
     * @param mavenBuild The build of the selected Maven project
     *
     */
    private void addMavenPluginManagement(Build mavenBuild) {
        PluginManagement pluginManagement = mavenBuild.getPluginManagement();
        if (pluginManagement == null) {
            pluginManagement = new PluginManagement();
        }

        Map<String, Plugin> managementPlugins = pluginManagement.getPlugins().stream()
                .collect(Collectors.toMap(Plugin::getArtifactId, Function.identity()));

        // Jar plugin
        Plugin jarPlugin;
        if (!managementPlugins.containsKey(MAVEN_JAR_PLUGIN_ARTIFACT_ID)) {
            jarPlugin = new Plugin();
            jarPlugin.setGroupId(MAVEN_PLUGINS_GROUP_ID);
            jarPlugin.setArtifactId(MAVEN_JAR_PLUGIN_ARTIFACT_ID);
            jarPlugin.setVersion("3.2.0");
            pluginManagement.addPlugin(jarPlugin);
        }

        // Source plugin
        Plugin sourcePlugin;
        if (!managementPlugins.containsKey(MAVEN_SOURCE_PLUGIN_ARTIFACT_ID)) {
            sourcePlugin = new Plugin();
            sourcePlugin.setGroupId(MAVEN_PLUGINS_GROUP_ID);
            sourcePlugin.setArtifactId(MAVEN_SOURCE_PLUGIN_ARTIFACT_ID);
            sourcePlugin.setVersion("3.2.1");
            pluginManagement.addPlugin(sourcePlugin);
        }

        // IPS plugin
        Plugin ipsPlugin;
        if (!managementPlugins.containsKey(IPS_PLUGIN_ARTIFACT_ID)) {
            ipsPlugin = new Plugin();
            ipsPlugin.setGroupId(IPS_GROUP_ID);
            ipsPlugin.setArtifactId(IPS_PLUGIN_ARTIFACT_ID);
            ipsPlugin.setVersion("${" + MAVEN_PROPERTY_IPS_VERSION + "}");
            pluginManagement.addPlugin(ipsPlugin);
        }

        mavenBuild.setPluginManagement(pluginManagement);
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
        Map<String, Plugin> buildPlugins = mavenBuild.getPlugins().stream()
                .collect(Collectors.toMap(Plugin::getArtifactId, Function.identity()));

        // Jar plugin
        Plugin jarPlugin;
        if (buildPlugins.containsKey(MAVEN_JAR_PLUGIN_ARTIFACT_ID)) {
            jarPlugin = buildPlugins.get(MAVEN_JAR_PLUGIN_ARTIFACT_ID);
        } else {
            jarPlugin = new Plugin();
            jarPlugin.setGroupId(MAVEN_PLUGINS_GROUP_ID);
            jarPlugin.setArtifactId(MAVEN_JAR_PLUGIN_ARTIFACT_ID);
            mavenBuild.addPlugin(jarPlugin);
        }
        addJarPluginConfiguration(jarPlugin);

        // Source plugin
        Plugin sourcePlugin;
        if (buildPlugins.containsKey(MAVEN_SOURCE_PLUGIN_ARTIFACT_ID)) {
            sourcePlugin = buildPlugins.get(MAVEN_SOURCE_PLUGIN_ARTIFACT_ID);
        } else {
            sourcePlugin = new Plugin();
            sourcePlugin.setGroupId(MAVEN_PLUGINS_GROUP_ID);
            sourcePlugin.setArtifactId(MAVEN_SOURCE_PLUGIN_ARTIFACT_ID);
            mavenBuild.addPlugin(sourcePlugin);
        }
        String id = "attach-sources";
        String goal = "jar";
        Optional<PluginExecution> pluginExecution = sourcePlugin.getExecutions().stream()
                .filter(execution -> execution.getId().equals(id))
                .findAny();
        if (pluginExecution.isPresent()) {
            if (!pluginExecution.get().getGoals().contains(goal)) {
                pluginExecution.get().addGoal(goal);
            }
        } else {
            PluginExecution execution = new PluginExecution();
            execution.setId(id);
            execution.addGoal(goal);
            sourcePlugin.addExecution(execution);
        }

        // IPS plugin
        Plugin ipsPlugin;
        if (buildPlugins.containsKey(IPS_PLUGIN_ARTIFACT_ID)) {
            ipsPlugin = buildPlugins.get(IPS_PLUGIN_ARTIFACT_ID);
        } else {
            ipsPlugin = new Plugin();
            ipsPlugin.setGroupId(IPS_GROUP_ID);
            ipsPlugin.setArtifactId(IPS_PLUGIN_ARTIFACT_ID);
            mavenBuild.addPlugin(ipsPlugin);
        }
        addIpsPluginConfiguration(ipsPlugin);
    }

    /**
     * Creates the configuration required by the Maven-Jar-Plugin.
     * <p>
     * The configuration has to be the following DOM object:<br>
     * {@code org.codehaus.plexus.util.xml.Xpp3Dom}
     *
     * @param jarPlugin The Maven-Jar-Plugin to be configured
     */
    private void addJarPluginConfiguration(Plugin jarPlugin) {
        Xpp3Dom configuration = (Xpp3Dom)jarPlugin.getConfiguration();
        if (configuration == null) {
            configuration = new Xpp3Dom("configuration");
            jarPlugin.setConfiguration(configuration);
        }
        addJarPluginSkipConfiguration(configuration);
        addJarPluginArchiveConfiguration(configuration);
    }

    /**
     * Creates the configuration required by the Faktor-IPS-Plugin.
     * <p>
     * The configuration has to be the following DOM object:<br>
     * {@code org.codehaus.plexus.util.xml.Xpp3Dom}
     *
     * @param ipsPlugin The Faktor-IPS-Plugin to be configured
     */
    private void addIpsPluginConfiguration(Plugin ipsPlugin) {
        Optional<PluginExecution> maybePluginExecution = ipsPlugin.getExecutions().stream()
                .findAny();
        PluginExecution pluginExecution = null;
        if (maybePluginExecution.isPresent()) {
            pluginExecution = maybePluginExecution.get();
        } else {
            pluginExecution = new PluginExecution();
            ipsPlugin.addExecution(pluginExecution);
        }
        String cleanGoal = "faktorips-clean";
        String buildGoal = "faktorips-build";
        if (pluginExecution.getGoals().stream().noneMatch(Predicate.isEqual(cleanGoal))) {
            pluginExecution.addGoal(cleanGoal);
        }
        if (pluginExecution.getGoals().stream().noneMatch(Predicate.isEqual(buildGoal))) {
            pluginExecution.addGoal(buildGoal);
        }
    }

    /**
     * Creates a part of the Maven-Jar-Plugin configuration.
     * <p>
     * {@code skip} should be set to {@code false}.
     *
     * @param configuration The configuration of the plugin
     */
    private void addJarPluginSkipConfiguration(Xpp3Dom configuration) {
        String skipConfig = "skip";
        Xpp3Dom skip = configuration.getChild(skipConfig);
        if (skip == null) {
            skip = new Xpp3Dom(skipConfig);
            configuration.addChild(skip);
        }
        skip.setValue("false");
    }

    /**
     * Creates a part of the Maven-Jar-Plugin configuration.
     * <p>
     * {@code archive} should point to the manifest file.
     *
     * @param configuration The configuration of the plugin
     */
    private void addJarPluginArchiveConfiguration(Xpp3Dom configuration) {
        String archiveConfig = "archive";
        Xpp3Dom archive = configuration.getChild(archiveConfig);
        if (archive == null) {
            archive = new Xpp3Dom(archiveConfig);
            configuration.addChild(archive);
        }
        String manifestConfig = "manifestFile";
        Xpp3Dom manifestFile = archive.getChild(manifestConfig);
        if (manifestFile == null) {
            manifestFile = new Xpp3Dom(manifestConfig);
            archive.addChild(manifestFile);
        }
        String manifestFilePath = META_INF_FOLDER.concat("/").concat(MANIFEST_FILE);
        manifestFile.setValue(manifestFilePath);
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
            throw new IpsException(e.getMessage());
        }
    }
}
