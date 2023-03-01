/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.naming;

import org.faktorips.devtools.model.builder.IJavaPackageStructure;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.model.util.QNameUtil;
import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * Implements the old interface {@link IJavaPackageStructure} containing a lot of static util
 * methods to get the correct package name for generated artifacts
 * 
 * @author widmaier
 */
public class JavaPackageStructure implements IJavaPackageStructure {

    private static final String INTERNAL_PACKAGE = "internal"; //$NON-NLS-1$

    /**
     * Returns the name of the package the generated artifacts for the given IPS source file will be
     * placed in.
     * 
     * @param ipsSrcFile The source file to get the package from
     * 
     */
    @Override
    public String getPackageName(IIpsSrcFile ipsSrcFile, boolean internalArtifacts, boolean mergableArtifacts) {
        return getPackageNameForGeneratedArtefacts(ipsSrcFile, internalArtifacts, mergableArtifacts);
    }

    @Override
    public String getBasePackageName(IIpsSrcFolderEntry entry, boolean internalArtifacts, boolean mergableArtifacts) {
        String basePackName = mergableArtifacts ? entry.getBasePackageNameForMergableJavaClasses()
                : entry
                        .getBasePackageNameForDerivedJavaClasses();
        return getPackageName(internalArtifacts, basePackName, IpsStringUtils.EMPTY);
    }

    /**
     * Returns the name of the (Java) package that contains the artifacts specified by the
     * parameters generated for the given ips source file.
     * 
     * @param internalArtifacts <code>true</code> if the artifacts are internal <code>false</code>
     *            if they are published (usable by clients).
     * @param mergableArtifacts <code>true</code> if the generated artifact is mergable (at the
     *            moment this applies to Java Source files only). <code>false</code>) if the
     *            artifact is 100% generated and can't be modified by the user.
     */
    protected String getPackageNameForGeneratedArtefacts(IIpsSrcFile ipsSrcFile,
            boolean internalArtifacts,
            boolean mergableArtifacts) {
        String basePackName = mergableArtifacts ? ipsSrcFile.getBasePackageNameForMergableArtefacts()
                : ipsSrcFile
                        .getBasePackageNameForDerivedArtefacts();
        String packageFragName = ipsSrcFile.getIpsPackageFragment().getName().toLowerCase();
        return getPackageName(internalArtifacts, basePackName, packageFragName);
    }

    private String getPackageName(boolean internalArtifacts, String basePackName, String packageFragName) {
        if (internalArtifacts) {
            return getInternalPackage(basePackName, packageFragName);
        } else {
            return QNameUtil.concat(basePackName, packageFragName);
        }
    }

    String getInternalPackage(final String basePackName, final String subPackageFragment) {
        String internalBasePack = QNameUtil.concat(basePackName, INTERNAL_PACKAGE);
        return QNameUtil.concat(internalBasePack, subPackageFragment);
    }

}
