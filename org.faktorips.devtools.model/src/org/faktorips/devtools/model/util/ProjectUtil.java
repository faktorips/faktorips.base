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

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.IIpsModelExtensions;
import org.faktorips.devtools.model.internal.builder.JavaNamingConvention;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetConfigModel;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetInfo;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.model.ipsproject.ISupportedLanguage;
import org.faktorips.devtools.model.plugin.IpsClasspathContainerInitializer;
import org.faktorips.devtools.model.plugin.IpsLog;
import org.faktorips.devtools.model.productcmpt.DateBasedProductCmptNamingStrategy;

/**
 * Utilities for the creation and modification of projects.
 * 
 * @author Thorsten Günther
 */
public class ProjectUtil {

    private ProjectUtil() {
        // Utility class not to be instantiated.
    }

    /**
     * Adds the Faktor-IPS nature to the given project.
     * 
     * @param project A platform project.
     * 
     * @throws NullPointerException If project is <code>null</code>.
     */
    public static final void addIpsNature(IProject project) throws CoreException {
        IpsProjectUtil.addNature(project, IIpsProject.NATURE_ID);
    }

    /**
     * Returns <code>true</code> if the given Java project has the Faktor-IPS Nature, otherwise
     * <code>false</code>.
     * 
     * @param project A platform project.
     */
    public static final boolean hasIpsNature(IJavaProject project) throws CoreException {
        return hasIpsNature(project.getProject());
    }

    /**
     * Returns <code>true</code> if the given project has the Faktor-IPS Nature, otherwise
     * <code>false</code>.
     * 
     * @param project A platform project.
     */
    public static final boolean hasIpsNature(IProject project) throws CoreException {
        return project.getDescription().hasNature(IIpsProject.NATURE_ID);
    }

    /**
     * Disable a feature of the default IPS builder.
     * 
     * @param ipsProject The project to disable the feature at.
     * @param featureName The name of the feature to disable.
     * 
     * @throws CoreException if an error occurs while saving the properties to the file.
     */
    public static void disableBuilderFeature(IIpsProject ipsProject, String featureName) throws CoreException {
        IIpsProjectProperties props = ipsProject.getProperties();
        IIpsArtefactBuilderSetConfigModel configModel = props.getBuilderSetConfig();
        String desc = configModel.getPropertyDescription(featureName);
        configModel.setPropertyValue(featureName, Boolean.FALSE.toString(), desc);
        props.setBuilderSetConfig(configModel);
        ipsProject.setProperties(props);
    }

    /**
     * Creates and returns an IPS project based on the given Java project.
     * 
     * @param javaProject The Java project to use as base for the IPS project.
     * @param runtimeIdPrefix The prefix for the runtime IDs to be used in the new project.
     * @param mergableFolder The source folder for mergable Java files.
     * @param derivedFolder The source folder for derived Java files.
     * @param srcFolder The source folder for IPS objects.
     * @param isProductDefinitionProject <code>true</code> to create a project which is capable of
     *            product definitions.
     * @param isModelProject <code>true</code> to create a project which is capable of model
     *            objects.
     * 
     * @throws CoreException In case of any errors.
     * 
     * @since 2.6
     */
    // CSOFF: ParameterNumber
    public static IIpsProject createIpsProject(IJavaProject javaProject,
            boolean isProductDefinitionProject,
            boolean isModelProject,
            boolean isPersistentProject,
            String runtimeIdPrefix,
            IFolder mergableFolder,
            IFolder derivedFolder,
            IFolder srcFolder) throws CoreException {

        List<Locale> supportedLocales = new ArrayList<Locale>(1);
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
     * @throws CoreException if the creation of the project fails. See
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
     * @throws CoreException If the creation of the folder fails. See
     *             {@link IFolder#create(boolean, boolean, org.eclipse.core.runtime.IProgressMonitor)}
     *             for details.
     */
    public static IFolder createFolder(IProject project, String folderName) throws CoreException {
        IFolder folder = project.getFolder(folderName);
        if (!folder.exists()) {
            folder.create(true, true, new NullProgressMonitor());
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
     * @throws CoreException if the creation of the folder fails or if the IPSObjectPath could not
     *             be set.
     */
    public static IFolder createIpsSourceFolderEntry(IIpsProject ipsProject,
            String folderName,
            IFolder outputFolderForMergableJavaFiles,
            IFolder outputFolderForDerivedJavaFiles) throws CoreException {

        IFolder srcFolder = createFolder(ipsProject.getProject(), folderName);

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
     * @throws CoreException If the IPS object path could not be set accordingly.
     */
    public static void addProjectReference(IIpsProject referringProject, IIpsProject referencedProject)
            throws CoreException {

        IIpsObjectPath ipsObjPath = referringProject.getIpsObjectPath();
        ipsObjPath.newIpsProjectRefEntry(referencedProject);
        referringProject.setIpsObjectPath(ipsObjPath);
    }

    /**
     * Creates and returns an <code>IIpsProject</code> based on the given <code>IJavaProject</code>.
     * 
     * @param javaProject The <code>IJavaProject</code> which is to be extended with IPS
     *            capabilities.
     * @param runtimeIdPrefix The prefix for runtime IDs to be used in this project.
     * @param isProductDefinitionProject Must be <code>true</code> if this is a product definition
     *            project.
     * @param isModelProject Must be <code>true</code> if this is a model project.
     * @param isPersistentProject Must be true if persistence support should be enabled for the
     *            project
     * @param supportedLocales List of locales that will reflect the languages supported by the
     *            project
     * 
     * @throws CoreException In case of any Errors.
     * 
     * @since 3.1
     */
    public static IIpsProject createIpsProject(IJavaProject javaProject,
            String runtimeIdPrefix,
            boolean isProductDefinitionProject,
            boolean isModelProject,
            boolean isPersistentProject,
            List<Locale> supportedLocales) throws CoreException {

        addIpsRuntimeLibraries(javaProject);
        IIpsModel ipsModel = IIpsModel.get();
        IIpsProject ipsProject = ipsModel.createIpsProject(javaProject);
        IIpsProjectProperties props = ipsProject.getProperties();
        props.setRuntimeIdPrefix(runtimeIdPrefix);
        props.setProductDefinitionProject(isProductDefinitionProject);
        props.setModelProject(isModelProject);
        props.setPersistenceSupport(isPersistentProject);

        // use the first registered builder set info as default
        IIpsArtefactBuilderSetInfo[] builderSetInfos = ipsModel.getIpsArtefactBuilderSetInfos();
        props.setBuilderSetId(builderSetInfos.length > 0 ? builderSetInfos[0].getBuilderSetId() : ""); //$NON-NLS-1$

        props.setPredefinedDatatypesUsed(ipsModel.getPredefinedValueDatatypes());
        DateBasedProductCmptNamingStrategy namingStrategy = new DateBasedProductCmptNamingStrategy(" ", "yyyy-MM", //$NON-NLS-1$ //$NON-NLS-2$
                true);
        props.setProductCmptNamingStrategy(namingStrategy);
        props.setMinRequiredVersionNumber(
                "org.faktorips.feature", //$NON-NLS-1$
                Platform.getBundle("org.faktorips.devtools.model").getHeaders().get("Bundle-Version")); //$NON-NLS-1$ //$NON-NLS-2$
        props.setChangesOverTimeNamingConventionIdForGeneratedCode(IIpsModelExtensions.get().getModelPreferences().getChangesOverTimeNamingConvention().getId());
        IIpsArtefactBuilderSetInfo builderSetInfo = ipsModel.getIpsArtefactBuilderSetInfo(props.getBuilderSetId());
        if (builderSetInfo != null) {
            props.setBuilderSetConfig(builderSetInfo.createDefaultConfiguration(ipsProject));
        }

        for (int i = 0; i < supportedLocales.size(); i++) {
            Locale locale = supportedLocales.get(i);
            props.addSupportedLanguage(locale);
            if (i == 0) {
                props.setDefaultLanguage(locale);
            }
        }

        setDefaultFunctionsLanguageLocale(props);

        ipsProject.setProperties(props);

        return ipsProject;
    }

    private static void setDefaultFunctionsLanguageLocale(IIpsProjectProperties properties) {
        ISupportedLanguage defaultLanguage = properties.getDefaultLanguage();
        if (defaultLanguage.getLocale().getLanguage().equals(Locale.GERMAN.getLanguage())) {
            properties.setFormulaLanguageLocale(Locale.GERMAN);
        } else {
            properties.setFormulaLanguageLocale(Locale.ENGLISH);
        }
    }

    private static void addIpsRuntimeLibraries(IJavaProject javaProject) throws JavaModelException {
        IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
        if (targetVersionIsAtLeast5(javaProject)) {
            IClasspathEntry[] entries = new IClasspathEntry[oldEntries.length + 1];
            System.arraycopy(oldEntries, 0, entries, 0, oldEntries.length);
            entries[oldEntries.length] = JavaCore.newContainerEntry(IpsClasspathContainerInitializer
                    .newDefaultEntryPath());
            javaProject.setRawClasspath(entries, null);
        }
    }

    private static boolean targetVersionIsAtLeast5(IJavaProject javaProject) {
        String[] targetVersion = javaProject.getOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, true).split("\\."); //$NON-NLS-1$
        return (Integer.parseInt(targetVersion[0]) == 1 && Integer.parseInt(targetVersion[1]) >= 5)
                || Integer.parseInt(targetVersion[0]) > 1;
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

}
