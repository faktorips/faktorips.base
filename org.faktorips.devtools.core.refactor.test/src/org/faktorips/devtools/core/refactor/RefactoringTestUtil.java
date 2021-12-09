/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.refactor;

import org.eclipse.core.resources.IFolder;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.faktorips.devtools.model.builder.naming.JavaClassNaming;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsSrcFolderEntry;

public abstract class RefactoringTestUtil {

    /**
     * Returns the Java {@link IType} corresponding to the indicated package name, type name and
     * internal flag.
     * 
     * @param packageName The package where the {@link IType} is located
     * @param typeName The name of the {@link IType}
     * @param publishedSource Flag indicating whether a published interface or an implementation
     *            type is searched
     * @param derivedSource Flag indicating whether the Java source file is a derived resource or
     *            not
     * @param ipsProject The {@link IIpsProject} to search
     */
    public static IType getJavaType(String packageName,
            String typeName,
            boolean publishedSource,
            boolean derivedSource,
            IIpsProject ipsProject) {

        String newPackageName = packageName;
        IIpsSrcFolderEntry srcFolderEntry = ipsProject.getIpsObjectPath().getSourceFolderEntries()[0];
        IFolder javaSrcFolder = derivedSource ? srcFolderEntry.getOutputFolderForDerivedJavaFiles().unwrap()
                : srcFolderEntry
                        .getOutputFolderForMergableJavaFiles().unwrap();
        IPackageFragmentRoot javaRoot = ((IJavaProject)ipsProject.getJavaProject().unwrap())
                .getPackageFragmentRoot(javaSrcFolder);

        String basePackageName = derivedSource ? srcFolderEntry.getBasePackageNameForDerivedJavaClasses()
                : srcFolderEntry.getBasePackageNameForMergableJavaClasses();
        if (!(publishedSource)) {
            basePackageName += ".internal";
        }
        if (newPackageName.length() > 0) {
            newPackageName = "." + newPackageName;
        }
        IPackageFragment javaPackage = javaRoot.getPackageFragment(basePackageName + newPackageName);

        return javaPackage.getCompilationUnit(typeName + JavaClassNaming.JAVA_EXTENSION).getType(typeName);
    }

    public static String getPublishedInterfaceName(String originalName, IIpsProject ipsProject) {
        return ipsProject.getJavaNamingConvention().getPublishedInterfaceName(originalName);
    }

    public static String getGenerationConceptNameAbbreviation(IIpsProject ipsProject) {
        return ipsProject.getChangesInTimeNamingConventionForGeneratedCode().getGenerationConceptNameAbbreviation();
    }

}
