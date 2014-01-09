/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.refactor;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.faktorips.devtools.core.builder.naming.JavaClassNaming;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsSrcFolderEntry;

final class RefactoringTestUtil {

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
            IIpsProject ipsProject) throws CoreException {

        IIpsSrcFolderEntry srcFolderEntry = ipsProject.getIpsObjectPath().getSourceFolderEntries()[0];
        IFolder javaSrcFolder = derivedSource ? srcFolderEntry.getOutputFolderForDerivedJavaFiles() : srcFolderEntry
                .getOutputFolderForMergableJavaFiles();
        IPackageFragmentRoot javaRoot = ipsProject.getJavaProject().getPackageFragmentRoot(javaSrcFolder);

        String basePackageName = derivedSource ? srcFolderEntry.getBasePackageNameForDerivedJavaClasses()
                : srcFolderEntry.getBasePackageNameForMergableJavaClasses();
        if (!(publishedSource)) {
            basePackageName += ".internal";
        }
        if (packageName.length() > 0) {
            packageName = "." + packageName;
        }
        IPackageFragment javaPackage = javaRoot.getPackageFragment(basePackageName + packageName);

        return javaPackage.getCompilationUnit(typeName + JavaClassNaming.JAVA_EXTENSION).getType(typeName);
    }

    public static String getPublishedInterfaceName(String originalName, IIpsProject ipsProject) {
        return ipsProject.getJavaNamingConvention().getPublishedInterfaceName(originalName);
    }

    public static String getGenerationConceptNameAbbreviation(IIpsProject ipsProject) {
        return ipsProject.getChangesInTimeNamingConventionForGeneratedCode().getGenerationConceptNameAbbreviation();
    }

}
