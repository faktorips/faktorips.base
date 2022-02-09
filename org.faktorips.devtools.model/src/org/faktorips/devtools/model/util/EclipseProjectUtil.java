/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.util;

import static org.faktorips.devtools.abstraction.Wrappers.wrap;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.AJavaProject;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.eclipse.AEclipseFolder;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.IIpsModelExtensions;
import org.faktorips.devtools.model.IIpsProjectConfigurator;
import org.faktorips.devtools.model.builder.AbstractBuilderSet;
import org.faktorips.devtools.model.internal.builder.JavaNamingConvention;
import org.faktorips.devtools.model.internal.ipsproject.IpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetConfigModel;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetInfo;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.model.ipsproject.ISupportedLanguage;
import org.faktorips.devtools.model.plugin.ExtensionPoints;
import org.faktorips.devtools.model.plugin.IpsLog;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.productcmpt.DateBasedProductCmptNamingStrategy;
import org.faktorips.runtime.MessageList;

/**
 * Utilities for the creation and modification of projects.
 * 
 * @author Thorsten Günther
 */
public class EclipseProjectUtil {

    private EclipseProjectUtil() {
        // Utility class not to be instantiated.
    }

    /**
     * Adds the Faktor-IPS nature to the given project.
     * 
     * @param project A platform project.
     * 
     * @throws NullPointerException If project is <code>null</code>.
     */
    public static final void addIpsNature(IProject project) {
        try {
            IpsProjectUtil.addNature(project, IIpsProject.NATURE_ID);
        } catch (CoreException e) {
            throw new IpsException(e);
        }
    }

    /**
     * Returns <code>true</code> if the given Java project has the Faktor-IPS Nature, otherwise
     * <code>false</code>.
     * 
     * @param project A platform project.
     */
    public static final boolean hasIpsNature(IJavaProject project) {
        return hasIpsNature(project.getProject());
    }

    /**
     * Returns <code>true</code> if the given project has the Faktor-IPS Nature, otherwise
     * <code>false</code>.
     * 
     * @param project A platform project.
     */
    public static final boolean hasIpsNature(IProject project) {
        try {
            return project.getDescription().hasNature(IIpsProject.NATURE_ID);
        } catch (CoreException e) {
            throw new IpsException(e);
        }
    }

    /**
     * Disable a feature of the default IPS builder.
     * 
     * @param ipsProject The project to disable the feature at.
     * @param featureName The name of the feature to disable.
     * 
     * @throws IpsException if an error occurs while saving the properties to the file.
     */
    public static void disableBuilderFeature(IIpsProject ipsProject, String featureName) {
        IIpsProjectProperties props = ipsProject.getProperties();
        IIpsArtefactBuilderSetConfigModel configModel = props.getBuilderSetConfig();
        String desc = configModel.getPropertyDescription(featureName);
        configModel.setPropertyValue(featureName, Boolean.FALSE.toString(), desc);
        props.setBuilderSetConfig(configModel);
        ipsProject.setProperties(props);
    }

    /**
     * @deprecated since 21.6; use
     *             {@link EclipseProjectUtil#createIpsProject(IJavaProject, IpsProjectCreationProperties)}
     *             instead
     */
    @Deprecated
    // CSOFF: ParameterNumber
    public static IIpsProject createIpsProject(IJavaProject javaProject,
            boolean isProductDefinitionProject,
            boolean isModelProject,
            boolean isPersistentProject,
            String runtimeIdPrefix,
            AFolder mergableFolder,
            AFolder derivedFolder,
            AFolder srcFolder) {

        List<Locale> supportedLocales = new ArrayList<>(1);
        supportedLocales.add(IIpsModel.get().getMultiLanguageSupport().getLocalizationLocale());
        IIpsProject ipsProject = createIpsProject(javaProject, runtimeIdPrefix, isProductDefinitionProject,
                isModelProject,
                isPersistentProject, supportedLocales);

        createIpsSourceFolderEntry(ipsProject, srcFolder.getName(), mergableFolder, derivedFolder);

        return ipsProject;
    }

    // CSON: TooManyParameters

    /**
     * Creates and returns a new project.
     * 
     * @param projectName The name for the new project.
     * 
     * @throws IpsException if the creation of the project fails. See
     *             {@link IProject#create(org.eclipse.core.runtime.IProgressMonitor)} for details.
     */
    public static IProject createPlatformProject(String projectName) throws CoreException {
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
        project.create(new NullProgressMonitor());
        project.open(new NullProgressMonitor());
        return project;
    }

    /**
     * Creates a new folder root folder and returns a handle to it.
     * 
     * @param project The project to create the folder in.
     * @param folderName The name of the folder.
     * 
     * @throws IpsException If the creation of the folder fails. See
     *             {@link IFolder#create(boolean, boolean, org.eclipse.core.runtime.IProgressMonitor)}
     *             for details.
     */
    public static AFolder createFolder(AProject project, String folderName) {
        AFolder folder = project.getFolder(folderName);
        if (!folder.exists()) {
            folder.create(new NullProgressMonitor());
        }
        return folder;
    }

    /**
     * Create a new folder and adds it as additional source folder entry to the IPS project.
     * 
     * @param ipsProject The project to create the folder in and to add the source folder entry to.
     * @param folderName The name of the new folder.
     * @param outputFolderForMergableJavaFiles The folder for mergable Java files.
     * @param outputFolderForDerivedJavaFiles The folder for derived Java files.
     * 
     * @throws IpsException if the creation of the folder fails or if the IPSObjectPath
     *             could not be set.
     */
    public static AFolder createIpsSourceFolderEntry(IIpsProject ipsProject,
            String folderName,
            AFolder outputFolderForMergableJavaFiles,
            AFolder outputFolderForDerivedJavaFiles) {

        AFolder srcFolder = createFolder(ipsProject.getProject(), folderName);

        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        path.setOutputDefinedPerSrcFolder(true);

        if (path.containsSrcFolderEntry(srcFolder)) {
            path.removeSrcFolderEntry(srcFolder);
        }
        String packageName = new JavaNamingConvention().getValidJavaIdentifier(ipsProject.getName()) + "." + folderName; //$NON-NLS-1$

        IIpsSrcFolderEntry entry = path.newSourceFolderEntry(srcFolder);
        entry.setSpecificBasePackageNameForMergableJavaClasses(packageName);
        entry.setSpecificOutputFolderForMergableJavaFiles(outputFolderForMergableJavaFiles);
        entry.setSpecificBasePackageNameForDerivedJavaClasses(packageName);
        entry.setSpecificOutputFolderForDerivedJavaFiles(outputFolderForDerivedJavaFiles);

        ipsProject.setIpsObjectPath(path);

        return srcFolder;
    }

    /**
     * Add a reference between the two projects.
     * 
     * @param referringProject The project referring the other.
     * @param referencedProject The project referred by the first one.
     * 
     * @throws IpsException If the IPS object path could not be set accordingly.
     */
    public static void addProjectReference(IIpsProject referringProject, IIpsProject referencedProject)
            {

        IIpsObjectPath ipsObjPath = referringProject.getIpsObjectPath();
        ipsObjPath.newIpsProjectRefEntry(referencedProject);
        referringProject.setIpsObjectPath(ipsObjPath);
    }

    /**
     * Creates and returns an {@link IIpsProject} based on the given <code>IJavaProject</code> and
     * the inserted properties {@link IpsProjectCreationProperties} required for creating an IPS
     * project.
     * <p>
     * Uses {@link IIpsProjectConfigurator IIpsProjectConfigurators} provided by the extension point
     * {@value ExtensionPoints#ADD_IPS_NATURE} to add Faktor-IPS-dependencies in a way matching the
     * used dependency management, defaulting to Eclipse Java project libraries.
     * 
     * @param javaProject the selected java project
     * @param creationProperties the required properties {@link IpsProjectCreationProperties} for
     *            creating a Faktor-IPS project
     * @return the created and configured Faktor-IPS project
     */
    public static IIpsProject createIpsProject(IJavaProject javaProject,
            IpsProjectCreationProperties creationProperties)
            throws CoreException {

        MessageList errorMessages = creationProperties.validate(wrap(javaProject).as(AJavaProject.class));
        if (errorMessages.containsErrorMsg()) {
            throw new CoreException(IpsStatus.of(errorMessages));
        }

        IFolder javaSrcFolder = javaProject.getProject().getFolder("src"); //$NON-NLS-1$
        IPackageFragmentRoot[] roots = javaProject.getPackageFragmentRoots();
        for (IPackageFragmentRoot root : roots) {
            if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
                if (root.getCorrespondingResource() instanceof AProject) {
                    throw new CoreException(
                            new IpsStatus(Messages.ProjectUtil_msgSourceInProjectImpossible));
                }
                javaSrcFolder = (IFolder)root.getCorrespondingResource();
                break;
            }
        }

        IIpsProject ipsProject = createDefaultIpsProject(javaProject, creationProperties);

        IFolder ipsModelFolder = ((AEclipseFolder)ipsProject.getProject()
                .getFolder(creationProperties.getSourceFolderName())).unwrap();
        if (!ipsModelFolder.exists()) {
            ipsModelFolder.create(true, true, new NullProgressMonitor());
        }

        initializeDefaultIpsObjectPath(creationProperties, ipsProject, javaSrcFolder, ipsModelFolder);

        executeProjectConfigurators(ipsProject, creationProperties);

        return ipsProject;
    }

    /**
     * @deprecated since 21.6; use
     *             {@link EclipseProjectUtil#createIpsProject(IJavaProject, IpsProjectCreationProperties)}
     *             instead
     */
    @Deprecated
    public static IIpsProject createIpsProject(IJavaProject javaProject,
            String runtimeIdPrefix,
            boolean isProductDefinitionProject,
            boolean isModelProject,
            boolean isPersistentProject,
            List<Locale> supportedLocales) {

        IpsProjectCreationProperties creationProperties = new IpsProjectCreationProperties();
        creationProperties.setRuntimeIdPrefix(runtimeIdPrefix);
        creationProperties.setProductDefinitionProject(isProductDefinitionProject);
        creationProperties.setModelProject(isModelProject);
        creationProperties.setPersistentProject(isPersistentProject);
        creationProperties.setLocales(supportedLocales);

        IIpsProject ipsProject = createDefaultIpsProject(javaProject, creationProperties);

        StandardJavaProjectConfigurator.configureDefaultIpsProject(javaProject);

        return ipsProject;
    }

    /**
     * Creates a default {@link IIpsProject} and sets all properties which are definitely required
     * for the usage of Faktor-IPS in any cases.
     * 
     * @param javaProject The selected java project
     * @param creationProperties The properties required for creating an {@link IIpsProject}
     * @return The created IPS project
     * @throws IpsException If creating or configuring the IPS project failed
     */
    private static IIpsProject createDefaultIpsProject(IJavaProject javaProject,
            IpsProjectCreationProperties creationProperties) {

        IIpsModel ipsModel = IIpsModel.get();
        IIpsProject ipsProject = ipsModel.createIpsProject(wrap(javaProject.getProject()).as(AProject.class));
        IIpsProjectProperties ipsProjectProperties = ipsProject.getProperties();

        initializeIpsProjectPropertiesDefaults(ipsProjectProperties, creationProperties, ipsModel, ipsProject);

        initializeBuilderSetConfigModel(ipsProjectProperties, creationProperties);

        initializesLocales(ipsProjectProperties, creationProperties);

        setDefaultFunctionsLanguageLocale(ipsProjectProperties);

        ipsProject.setProperties(ipsProjectProperties);

        return ipsProject;
    }

    /**
     * Initializes the default {@link IIpsObjectPath}.
     * 
     * @param creationProperties The {@link IIpsProjectProperties} of the created
     *            {@link IIpsProject}
     * @param ipsProject The created {@link IIpsProject}
     * @param javaSrcFolder The java source folder
     * @param ipsModelFolder The model folder
     * @throws IpsException If initializing the object path failed
     */
    private static void initializeDefaultIpsObjectPath(IpsProjectCreationProperties creationProperties,
            IIpsProject ipsProject,
            IFolder javaSrcFolder,
            IFolder ipsModelFolder) throws CoreException {
        IJavaProject javaProject = ipsProject.getJavaProject().unwrap();
        IIpsObjectPath path = new IpsObjectPath(ipsProject);
        path.setOutputDefinedPerSrcFolder(false);
        path.setBasePackageNameForMergableJavaClasses(creationProperties.getBasePackageName());
        path.setBasePackageNameForDerivedJavaClasses(creationProperties.getBasePackageName());
        path.setOutputFolderForMergableSources(wrap(javaSrcFolder).as(AFolder.class));
        if (javaSrcFolder.exists()) {
            String derivedsrcFolderName = creationProperties.isModelProject() ? "resources" : "derived"; //$NON-NLS-1$//$NON-NLS-2$
            IFolder derivedsrcFolder = javaSrcFolder.getParent().getFolder(new Path(derivedsrcFolderName));
            if (!derivedsrcFolder.exists()) {
                derivedsrcFolder.create(true, true, new NullProgressMonitor());
                IClasspathEntry derivedsrc = JavaCore.newSourceEntry(derivedsrcFolder.getFullPath());
                IClasspathEntry[] rawClassPath = javaProject.getRawClasspath();
                IClasspathEntry[] newClassPath = new IClasspathEntry[rawClassPath.length + 1];
                System.arraycopy(rawClassPath, 0, newClassPath, 0, rawClassPath.length);
                newClassPath[newClassPath.length - 1] = derivedsrc;
                javaProject.setRawClasspath(newClassPath, new NullProgressMonitor());
            }
            path.setOutputFolderForDerivedSources(wrap(derivedsrcFolder).as(AFolder.class));
        }
        path.newSourceFolderEntry(wrap(ipsModelFolder).as(AFolder.class));
        ipsProject.setIpsObjectPath(path);
    }

    /**
     * Initializes the default {@link IIpsProjectProperties}.
     * 
     * @param ipsProjectProperties The {@link IIpsProjectProperties} of the created
     *            {@link IIpsProject}
     * @param creationProperties The properties required for creating an {@link IIpsProject}
     * @param ipsModel The {@link IIpsModel}
     * @param ipsProject The created {@link IIpsProject}
     */
    private static void initializeIpsProjectPropertiesDefaults(IIpsProjectProperties ipsProjectProperties,
            IpsProjectCreationProperties creationProperties,
            IIpsModel ipsModel,
            IIpsProject ipsProject) {
        ipsProjectProperties.setRuntimeIdPrefix(creationProperties.getRuntimeIdPrefix());
        ipsProjectProperties.setProductDefinitionProject(creationProperties.isProductDefinitionProject());
        ipsProjectProperties.setModelProject(creationProperties.isModelProject());
        ipsProjectProperties.setPersistenceSupport(creationProperties.isPersistentProject());

        // use the first registered builder set info as default
        IIpsArtefactBuilderSetInfo[] builderSetInfos = ipsModel.getIpsArtefactBuilderSetInfos();
        ipsProjectProperties.setBuilderSetId(builderSetInfos.length > 0 ? builderSetInfos[0].getBuilderSetId() : ""); //$NON-NLS-1$

        ipsProjectProperties.setPredefinedDatatypesUsed(ipsModel.getPredefinedValueDatatypes());
        DateBasedProductCmptNamingStrategy namingStrategy = new DateBasedProductCmptNamingStrategy(" ", "yyyy-MM", //$NON-NLS-1$ //$NON-NLS-2$
                true);
        ipsProjectProperties.setProductCmptNamingStrategy(namingStrategy);
        ipsProjectProperties.setChangesOverTimeNamingConventionIdForGeneratedCode(
                IIpsModelExtensions.get().getModelPreferences().getChangesOverTimeNamingConvention().getId());
        ipsProjectProperties.setMinRequiredVersionNumber(
                "org.faktorips.feature", //$NON-NLS-1$
                Platform.getBundle("org.faktorips.devtools.model").getHeaders().get("Bundle-Version")); //$NON-NLS-1$ //$NON-NLS-2$
        IIpsArtefactBuilderSetInfo builderSetInfo = ipsModel
                .getIpsArtefactBuilderSetInfo(ipsProjectProperties.getBuilderSetId());
        if (builderSetInfo != null) {
            ipsProjectProperties.setBuilderSetConfig(builderSetInfo.createDefaultConfiguration(ipsProject));
        }
    }

    /**
     * Initializes the default {@link IIpsArtefactBuilderSetConfigModel} of an {@link IIpsProject}.
     * 
     * @param ipsProjectProperties The {@link IIpsProjectProperties} of the created
     *            {@link IIpsProject}
     * @param creationProperties The properties required for creating an {@link IIpsProject}
     */
    private static void initializeBuilderSetConfigModel(IIpsProjectProperties ipsProjectProperties,
            IpsProjectCreationProperties creationProperties) {
        IIpsArtefactBuilderSetConfigModel builderSetConfig = ipsProjectProperties.getBuilderSetConfig();

        String persistenceDescription = builderSetConfig
                .getPropertyDescription(PersistenceSupportNames.STD_BUILDER_PROPERTY_PERSISTENCE_PROVIDER);
        builderSetConfig.setPropertyValue(PersistenceSupportNames.STD_BUILDER_PROPERTY_PERSISTENCE_PROVIDER,
                creationProperties.getPersistenceSupport(), persistenceDescription);

        if (creationProperties.isModelProject()) {
            builderSetConfig.setPropertyValue(AbstractBuilderSet.CONFIG_MARK_NONE_MERGEABLE_RESOURCES_AS_DERIVED,
                    "false", //$NON-NLS-1$
                    null);
        }
    }

    /**
     * Initializes the default {@link Locale Locales} of an {@link IIpsProject}.
     * 
     * @param ipsProjectProperties The {@link IIpsProjectProperties} of the created
     *            {@link IIpsProject}
     * @param creationProperties The properties required for creating an {@link IIpsProject}
     */
    private static void initializesLocales(IIpsProjectProperties ipsProjectProperties,
            IpsProjectCreationProperties creationProperties) {
        List<Locale> supportedLocales = creationProperties.getLocales();
        for (int i = 0; i < supportedLocales.size(); i++) {
            Locale locale = supportedLocales.get(i);
            ipsProjectProperties.addSupportedLanguage(locale);
            if (i == 0) {
                ipsProjectProperties.setDefaultLanguage(locale);
            }
        }
    }

    /**
     * Initializes the default formula language of an {@link IIpsProject}.
     * 
     * @param properties The {@link IIpsProjectProperties} of the created {@link IIpsProject}
     */
    private static void setDefaultFunctionsLanguageLocale(IIpsProjectProperties properties) {
        ISupportedLanguage defaultLanguage = properties.getDefaultLanguage();
        if (defaultLanguage.getLocale().getLanguage().equals(Locale.GERMAN.getLanguage())) {
            properties.setFormulaLanguageLocale(Locale.GERMAN);
        } else {
            properties.setFormulaLanguageLocale(Locale.ENGLISH);
        }
    }

    /**
     * Creates a hidden file <code>.keepme</code> in the given folder. Returns <code>true</code> if
     * done successfully, <code>false</code> otherwise.
     * 
     * @param folder parent folder
     */
    public static boolean createKeepMeFile(IFolder folder) {
        IFile outFile = folder.getFile(".keepme"); //$NON-NLS-1$
        String content = "// force e.g. CVS to keep empty folders"; //$NON-NLS-1$
        byte[] stringBytes = content.getBytes();
        ByteArrayInputStream bais = new ByteArrayInputStream(stringBytes);
        try {
            outFile.create(bais, true, new NullProgressMonitor());
        } catch (CoreException e) {
            IpsLog.log(e);
            return false;
        }
        return true;
    }

    /**
     * Executes all responsible {@link IIpsProjectConfigurator project-configurators}.
     * <p>
     * The configurators are provided by the extension point {@link ExtensionPoints#ADD_IPS_NATURE}.
     * 
     * @implNote If no extension is responsible for configuring the project, use the
     *           {@link StandardJavaProjectConfigurator}
     * 
     * @param ipsProject The created {@link IIpsProject}
     * @param creationProperties The {@link IpsProjectCreationProperties} required for creating an
     *            {@link IIpsProject}
     * @throws IpsException If the execution of one of the configurators failed
     */
    private static void executeProjectConfigurators(IIpsProject ipsProject,
            IpsProjectCreationProperties creationProperties)
            {
        boolean isExtensionResponsible = false;
        for (IIpsProjectConfigurator configurator : IpsProjectConfigurators
                .applicableTo(ipsProject.getJavaProject())
                .collect(Collectors.toList())) {
            isExtensionResponsible = true;
            configurator.configureIpsProject(ipsProject, creationProperties);
        }
        if (!isExtensionResponsible) {
            new StandardJavaProjectConfigurator().configureIpsProject(ipsProject, creationProperties);
        }
    }
}
