/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.util;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.faktorips.devtools.core.FaktorIpsClasspathVariableInitializer;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.Util;
import org.faktorips.devtools.core.internal.model.productcmpt.DateBasedProductCmptNamingStrategy;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetConfigModel;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetInfo;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.ipsproject.IIpsSrcFolderEntry;

/**
 * Utilties for the creation and modification of projects.
 * 
 * @author Thorsten Günther, Faktor Zehn AG
 */
public class ProjectUtil {

    /**
     * Adds the Faktor-IPS nature to the project.
     * 
     * @param project A platform project.
     * 
     * @throws NullPointerException if project is <code>null</code>.
     * @throws CoreException
     */
    public final static void addIpsNature(IProject project) throws CoreException {
        Util.addNature(project, IIpsProject.NATURE_ID);
    }

    /**
     * Returns <code>true</code> if the project has the Faktor-IPS Nature, otherwise
     * <code>false</code>.
     * 
     * @param project A platform project.
     * 
     * @throws CoreException
     */
    public final static boolean hasIpsNature(IJavaProject project) throws CoreException {
        return hasIpsNature(project.getProject());
    }

    /**
     * Returns <code>true</code> if the project has the Faktor-IPS Nature, otherwise
     * <code>false</code>.
     * 
     * @param project A platform project.
     * 
     * @throws CoreException
     */
    public final static boolean hasIpsNature(IProject project) throws CoreException {
        return project.getDescription().hasNature(IIpsProject.NATURE_ID);
    }

    /**
     * Disable a feature of the default IPS builder.
     * 
     * @param ipsProject The project to disable the feature at.
     * @param featureName The name of the feature to disable.
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
     * Create an IPS-Project based on the given Java-Project.
     * 
     * @param javaProject The Java-Project to use as base for the IPS-Project
     * @param runtimeIdPrefix The prefix for the runtime-IDs to be used in the new project.
     * @param mergableFolder The source folder for mergable java files.
     * @param derivedFolder The source folder for derived java files.
     * @param srcFolder The source folder for IPS-Objects.
     * @param isProductDefinitionProject <code>true</code> to create a project which is capable of
     *            product definitions.
     * @param isModelProject <code>true</code> to create a project which is capabel of model
     *            objects.
     * 
     * @return The new IPS-Project.
     * 
     * @throws CoreException In case of any Errors.
     */
    public static IIpsProject createIpsProject(IJavaProject javaProject,
            boolean isProductDefinitionProject,
            boolean isModelProject,
            String runtimeIdPrefix,
            IFolder mergableFolder,
            IFolder derivedFolder,
            IFolder srcFolder) throws CoreException {
        IIpsProject ipsProject = createIpsProject(javaProject, runtimeIdPrefix, isProductDefinitionProject,
                isModelProject);

        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        path.setOutputDefinedPerSrcFolder(true);

        if (path.containsSrcFolderEntry(srcFolder)) {
            path.removeSrcFolderEntry(srcFolder);
        }

        IIpsSrcFolderEntry entry = path.newSourceFolderEntry(srcFolder);
        entry.setSpecificBasePackageNameForMergableJavaClasses(javaProject.getProject().getName()
                + "." + srcFolder.getName()); //$NON-NLS-1$
        entry.setSpecificOutputFolderForMergableJavaFiles(mergableFolder);
        entry.setSpecificBasePackageNameForDerivedJavaClasses(javaProject.getProject().getName()
                + "." + srcFolder.getName()); //$NON-NLS-1$
        entry.setSpecificOutputFolderForDerivedJavaFiles(derivedFolder);

        ipsProject.setIpsObjectPath(path);
        return ipsProject;
    }

    /**
     * Create a new java project based on the given data.
     * 
     * @param project The platform project to be used as base for the java project.
     * @param srcFolder The first source folder
     * @param derivedFolder The second source folder
     * @return The new java project
     * @throws CoreException if the project nature can not be set
     * @throws JavaModelException if the classpath could not be set
     */
    public static IJavaProject createJavaProject(IProject project, IFolder srcFolder, IFolder derivedFolder)
            throws CoreException {

        Util.addNature(project, JavaCore.NATURE_ID);

        IClasspathEntry srcEntry = JavaCore.newSourceEntry(srcFolder.getFullPath());
        IClasspathEntry derivedEntry = JavaCore.newSourceEntry(derivedFolder.getFullPath());

        IClasspathEntry[] entries = PreferenceConstants.getDefaultJRELibrary();
        IClasspathEntry[] newEntries = new IClasspathEntry[entries.length + 2];
        System.arraycopy(entries, 0, newEntries, 0, entries.length);
        newEntries[newEntries.length - 1] = srcEntry;
        newEntries[newEntries.length - 2] = derivedEntry;

        IJavaProject javaProject = JavaCore.create(project);
        IFolder destFolder = project.getFolder(PreferenceConstants.getPreferenceStore().getString(
                PreferenceConstants.SRCBIN_BINNAME));

        if (!destFolder.exists()) {
            destFolder.create(IResource.FORCE | IResource.DERIVED, true, new NullProgressMonitor());
        }
        destFolder.setDerived(true);

        project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());

        javaProject.setRawClasspath(newEntries, new NullProgressMonitor());
        return javaProject;
    }

    /**
     * Create a new project.
     * 
     * @param projectName The name for the new project.
     * 
     * @return The new project.
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
     * Create a new folder root folder.
     * 
     * @param project The project to create the folder in.
     * @param folderName The name of the folder.
     * @return The handle to the new folder.
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
     * Create a new folder and add it as additional source folder entry to the IPS-project.
     * 
     * @param ipsProject The project to create the folder in and to add the source folder entry to.
     * @param folderName The name of the new folder.
     * @param outputFolderForMergableJavaFiles The Folder for mergable java files.
     * @param outputFolderForDerivedJavaFiles The folder for derived java files.
     * @throws CoreException if the creation of the folder fails or if the IPSObjectPath could not
     *             be set.
     */
    public static void createIpsSourceFolderEntry(IIpsProject ipsProject,
            String folderName,
            IFolder outputFolderForMergableJavaFiles,
            IFolder outputFolderForDerivedJavaFiles) throws CoreException {
        IFolder srcFolder = createFolder(ipsProject.getProject(), folderName);

        IIpsObjectPath path = ipsProject.getIpsObjectPath();

        if (path.containsSrcFolderEntry(srcFolder)) {
            path.removeSrcFolderEntry(srcFolder);
        }

        String packageName = ipsProject.getName() + "." + folderName;

        IIpsSrcFolderEntry entry = path.newSourceFolderEntry(srcFolder);
        entry.setSpecificBasePackageNameForMergableJavaClasses(packageName);
        entry.setSpecificOutputFolderForMergableJavaFiles(outputFolderForMergableJavaFiles);
        entry.setSpecificBasePackageNameForDerivedJavaClasses(packageName);
        entry.setSpecificOutputFolderForDerivedJavaFiles(outputFolderForDerivedJavaFiles);

        ipsProject.setIpsObjectPath(path);

    }

    /**
     * Add a referrence between the two projects.
     * 
     * @param referringProject The project referring the other.
     * @param referencedProject The project refferde by the first one.
     * @throws CoreException if the IpsObjectPath could not be set.
     */
    public static void addProjectReference(IIpsProject referringProject, IIpsProject referencedProject)
            throws CoreException {
        IIpsObjectPath ipsObjPath = referringProject.getIpsObjectPath();
        ipsObjPath.newIpsProjectRefEntry(referencedProject);
        referringProject.setIpsObjectPath(ipsObjPath);
    }

    /**
     * Create an IpsProject based on the given JavaProject.
     * 
     * @param javaProject The JavaProject which is to be extended with IPS-Capabilities
     * @param runtimeIdPrefix The prefix for RuntimeIDs to be used in this project
     * @param isProductDefinitionProject <code>true</code> if this is a product definition project.
     * @param isModelProject <code>true</code> if this is a model project.
     * @return The new IpsProject.
     * 
     * @throws CoreException In case of any Errors.
     */
    public static IIpsProject createIpsProject(IJavaProject javaProject,
            String runtimeIdPrefix,
            boolean isProductDefinitionProject,
            boolean isModelProject) throws CoreException {
        addIpsRuntimeLibraries(javaProject);
        IIpsProject ipsProject = IpsPlugin.getDefault().getIpsModel().createIpsProject(javaProject);
        IIpsProjectProperties props = ipsProject.getProperties();
        props.setRuntimeIdPrefix(runtimeIdPrefix);
        props.setProductDefinitionProject(isProductDefinitionProject);
        props.setModelProject(isModelProject);

        // use the first registered builder set info as default
        IIpsArtefactBuilderSetInfo[] builderSetInfos = IpsPlugin.getDefault().getIpsModel()
                .getIpsArtefactBuilderSetInfos();
        props.setBuilderSetId(builderSetInfos.length > 0 ? builderSetInfos[0].getBuilderSetId() : ""); //$NON-NLS-1$

        props.setPredefinedDatatypesUsed(IpsPlugin.getDefault().getIpsModel().getPredefinedValueDatatypes());
        DateBasedProductCmptNamingStrategy namingStrategy = new DateBasedProductCmptNamingStrategy(" ", "yyyy-MM", true); //$NON-NLS-1$ //$NON-NLS-2$
        props.setProductCmptNamingStrategy(namingStrategy);
        props
                .setMinRequiredVersionNumber(
                        "org.faktorips.feature", (String)Platform.getBundle("org.faktorips.devtools.core").getHeaders().get("Bundle-Version")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        props.setChangesOverTimeNamingConventionIdForGeneratedCode(IpsPlugin.getDefault().getIpsPreferences()
                .getChangesOverTimeNamingConvention().getId());
        IIpsArtefactBuilderSetInfo builderSetInfo = IpsPlugin.getDefault().getIpsModel().getIpsArtefactBuilderSetInfo(
                props.getBuilderSetId());
        if (builderSetInfo != null) {
            props.setBuilderSetConfig(builderSetInfo.createDefaultConfiguration(ipsProject));
        }
        ipsProject.setProperties(props);
        return ipsProject;
    }

    private static void addIpsRuntimeLibraries(IJavaProject javaProject) throws JavaModelException {
        IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
        if (targetVersionIsAtLeast5(javaProject)) {
            int numOfJars = FaktorIpsClasspathVariableInitializer.IPS_VARIABLES_JAVA5_BIN.length;
            IClasspathEntry[] entries = new IClasspathEntry[oldEntries.length + numOfJars];
            System.arraycopy(oldEntries, 0, entries, 0, oldEntries.length);
            for (int i = 0; i < numOfJars; i++) {
                Path jarPath = new Path(FaktorIpsClasspathVariableInitializer.IPS_VARIABLES_JAVA5_BIN[i]);
                Path srcZipPath = null;
                if (StringUtils.isNotEmpty(FaktorIpsClasspathVariableInitializer.IPS_VARIABLES_JAVA5_SRC[i])) {
                    srcZipPath = new Path(FaktorIpsClasspathVariableInitializer.IPS_VARIABLES_JAVA5_SRC[i]);
                }
                entries[oldEntries.length + i] = JavaCore.newVariableEntry(jarPath, srcZipPath, null);
            }
            javaProject.setRawClasspath(entries, null);
        } else {
            int numOfJars = FaktorIpsClasspathVariableInitializer.IPS_VARIABLES_BIN.length;
            IClasspathEntry[] entries = new IClasspathEntry[oldEntries.length + numOfJars];
            System.arraycopy(oldEntries, 0, entries, 0, oldEntries.length);
            for (int i = 0; i < numOfJars; i++) {
                Path jarPath = new Path(FaktorIpsClasspathVariableInitializer.IPS_VARIABLES_BIN[i]);
                Path srcZipPath = null;
                if (StringUtils.isNotEmpty(FaktorIpsClasspathVariableInitializer.IPS_VARIABLES_SRC[i])) {
                    srcZipPath = new Path(FaktorIpsClasspathVariableInitializer.IPS_VARIABLES_SRC[i]);
                }
                entries[oldEntries.length + i] = JavaCore.newVariableEntry(jarPath, srcZipPath, null);
            }
            javaProject.setRawClasspath(entries, null);
        }
    }

    private static boolean targetVersionIsAtLeast5(IJavaProject javaProject) {

        String[] targetVersion = javaProject.getOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, true).split("\\."); //$NON-NLS-1$
        return (Integer.parseInt(targetVersion[0]) == 1 && Integer.parseInt(targetVersion[1]) >= 5)
                || Integer.parseInt(targetVersion[0]) > 1;
    }

}
