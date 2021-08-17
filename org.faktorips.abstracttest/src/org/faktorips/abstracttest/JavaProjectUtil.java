/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.abstracttest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.JavaRuntime;
import org.faktorips.devtools.model.util.IpsProjectUtil;

public class JavaProjectUtil {
    private static final String MODULE_INFO_JAVA_FILENAME = "module-info.java";

    private JavaProjectUtil() {
        // Utility class
    }

    /**
     * Creates a new Java Project for the given platform project.
     */
    public static IJavaProject addJavaCapabilities(IProject project) throws CoreException {
        IJavaProject javaProject = JavaCore.create(project);
        // add Java nature
        IpsProjectUtil.addNature(project, JavaCore.NATURE_ID);
        javaProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
        // create bin folder and set as output folder.
        IFolder binFolder = project.getFolder("bin");
        if (!binFolder.exists()) {
            binFolder.create(true, true, null);
        }
        IFolder srcFolder = project.getFolder(AbstractIpsPluginTest.OUTPUT_FOLDER_NAME_MERGABLE);
        javaProject.setOutputLocation(binFolder.getFullPath(), null);
        if (!srcFolder.exists()) {
            srcFolder.create(true, true, null);
        }
        IFolder extFolder = project.getFolder(AbstractIpsPluginTest.OUTPUT_FOLDER_NAME_DERIVED);
        if (!extFolder.exists()) {
            extFolder.create(true, true, null);
        }
        IPackageFragmentRoot srcRoot = javaProject.getPackageFragmentRoot(srcFolder);
        IPackageFragmentRoot extRoot = javaProject.getPackageFragmentRoot(extFolder);
        IClasspathEntry[] entries = new IClasspathEntry[2];
        entries[0] = JavaCore.newSourceEntry(srcRoot.getPath());
        entries[1] = JavaCore.newSourceEntry(extRoot.getPath());
        javaProject.setRawClasspath(entries, null);
        addSystemLibraries(javaProject);
        return javaProject;
    }

    private static void addSystemLibraries(IJavaProject javaProject) throws JavaModelException {
        IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
        IClasspathEntry[] newEntries = new IClasspathEntry[oldEntries.length + 1];
        System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
        newEntries[oldEntries.length] = JavaRuntime.getDefaultJREContainerEntry();
        javaProject.setRawClasspath(newEntries, null);
    }

    /*
     * Copied from org.eclipse.jdt.internal.ui.actions.CreateModuleInfoAction and
     * org.eclipse.jdt.internal.ui.wizards.NewModuleInfoWizard
     */
    public static void convertToModuleProject(IJavaProject javaProject) {
        javaProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_11);
        try {
            IPackageFragmentRoot[] packageFragmentRoots = javaProject.getPackageFragmentRoots();
            List<IPackageFragmentRoot> packageFragmentRootsAsList = new ArrayList<>(
                    Arrays.asList(packageFragmentRoots));
            for (IPackageFragmentRoot packageFragmentRoot : packageFragmentRoots) {
                IResource res = packageFragmentRoot.getCorrespondingResource();
                if (res == null || res.getType() != IResource.FOLDER
                        || packageFragmentRoot.getKind() != IPackageFragmentRoot.K_SOURCE) {
                    packageFragmentRootsAsList.remove(packageFragmentRoot);
                }
            }
            packageFragmentRoots = packageFragmentRootsAsList
                    .toArray(new IPackageFragmentRoot[packageFragmentRootsAsList.size()]);

            IPackageFragmentRoot targetPkgFragmentRoot = null;

            for (IPackageFragmentRoot packageFragmentRoot : packageFragmentRoots) {
                if (packageFragmentRoot.getPackageFragment("").getCompilationUnit(MODULE_INFO_JAVA_FILENAME).exists()) { //$NON-NLS-1$
                    targetPkgFragmentRoot = packageFragmentRoot;
                    break;
                }
            }
            if (targetPkgFragmentRoot == null && packageFragmentRoots != null && packageFragmentRoots.length > 0) {
                targetPkgFragmentRoot = packageFragmentRoots[0];
            }

            createModuleInfoJava(javaProject, targetPkgFragmentRoot, packageFragmentRoots);
            convertClasspathToModulePath(javaProject, null);

        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    private static void createModuleInfoJava(IJavaProject javaProject,
            IPackageFragmentRoot targetPkgFragmentRoot,
            IPackageFragmentRoot[] packageFragmentRoots) throws CoreException {
        String fileContent = getModuleInfoFileContent(javaProject, packageFragmentRoots);
        IPackageFragment defaultPkg = targetPkgFragmentRoot.getPackageFragment(""); //$NON-NLS-1$
        defaultPkg.createCompilationUnit(MODULE_INFO_JAVA_FILENAME, fileContent, true, null);
    }

    private static String getModuleInfoFileContent(IJavaProject javaProject,
            IPackageFragmentRoot[] packageFragmentRoots)
            throws CoreException {
        HashSet<String> exportedPackages = new HashSet<>();
        for (IPackageFragmentRoot packageFragmentRoot : packageFragmentRoots) {
            for (IJavaElement child : packageFragmentRoot.getChildren()) {
                if (child instanceof IPackageFragment) {
                    IPackageFragment pkgFragment = (IPackageFragment)child;
                    if (!pkgFragment.isDefaultPackage() && pkgFragment.getCompilationUnits().length != 0) {
                        exportedPackages.add(pkgFragment.getElementName());
                    }
                }
            }
        }

        String[] requiredModules = JavaCore.getReferencedModules(javaProject);
        String moduleName = javaProject.getProject().getName();
        StringBuilder fileContent = new StringBuilder();
        fileContent.append("module "); //$NON-NLS-1$
        fileContent.append(moduleName);
        fileContent.append(" {"); //$NON-NLS-1$

        for (String exportedPkg : exportedPackages) {
            fileContent.append("exports "); //$NON-NLS-1$
            fileContent.append(exportedPkg);
            fileContent.append(";"); //$NON-NLS-1$
        }

        for (String requiredModule : requiredModules) {
            fileContent.append("requires "); //$NON-NLS-1$
            fileContent.append(requiredModule);
            fileContent.append(';');
        }

        fileContent.append('}');

        return fileContent.toString();
    }

    private static void convertClasspathToModulePath(IJavaProject javaProject, IProgressMonitor monitor)
            throws JavaModelException {
        boolean changed = false;
        IClasspathEntry[] rawClasspath = javaProject.getRawClasspath();
        for (int i = 0; i < rawClasspath.length; i++) {
            IClasspathEntry entry = rawClasspath[i];
            switch (entry.getEntryKind()) {
                case IClasspathEntry.CPE_CONTAINER:
                case IClasspathEntry.CPE_LIBRARY:
                case IClasspathEntry.CPE_PROJECT:
                    IClasspathAttribute[] newAttributes = addModuleAttributeIfNeeded(entry.getExtraAttributes());
                    if (newAttributes != null) {
                        rawClasspath[i] = addAttributes(entry, newAttributes);
                        changed = true;
                    }
                    break;
                default:
                    // other kinds are not handled
            }
        }
        if (changed) {
            javaProject.setRawClasspath(rawClasspath, monitor);
        }
    }

    private static IClasspathAttribute[] addModuleAttributeIfNeeded(IClasspathAttribute[] extraAttributes) {
        for (int j = 0; j < extraAttributes.length; j++) {
            IClasspathAttribute classpathAttribute = extraAttributes[j];
            if (IClasspathAttribute.MODULE.equals(classpathAttribute.getName())) {
                if ("true".equals(classpathAttribute.getValue())) {
                    return null; // no change required
                }
                extraAttributes[j] = JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true");
                return extraAttributes;
            }
        }
        extraAttributes = Arrays.copyOf(extraAttributes, extraAttributes.length + 1);
        extraAttributes[extraAttributes.length - 1] = JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE,
                "true");
        return extraAttributes;
    }

    private static IClasspathEntry addAttributes(IClasspathEntry entry, IClasspathAttribute[] extraAttributes) {
        switch (entry.getEntryKind()) {
            case IClasspathEntry.CPE_CONTAINER:
                return JavaCore.newContainerEntry(entry.getPath(), entry.getAccessRules(), extraAttributes,
                        entry.isExported());
            case IClasspathEntry.CPE_LIBRARY:
                return JavaCore.newLibraryEntry(entry.getPath(), entry.getSourceAttachmentPath(),
                        entry.getSourceAttachmentRootPath(), entry.getAccessRules(), extraAttributes,
                        entry.isExported());
            case IClasspathEntry.CPE_PROJECT:
                return JavaCore.newProjectEntry(entry.getPath(), entry.getAccessRules(), entry.combineAccessRules(),
                        extraAttributes, entry.isExported());
            default:
                return entry; // other kinds are not handled
        }
    }

}
