/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.builder.naming;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.builder.IJavaPackageStructure;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilder;
import org.faktorips.devtools.core.util.QNameUtil;

/**
 * Implements the old interface {@link IJavaPackageStructure} containing a lot of static util
 * methods to get the correct package name for generated artifacts
 * 
 * @author widmaier
 */
public class JavaPackageStructure implements IJavaPackageStructure {

    private final static String INTERNAL_PACKAGE = "internal"; //$NON-NLS-1$

    @Override
    public String getPackage(IIpsArtefactBuilder builder, IIpsSrcFile ipsSrcFile) throws CoreException {
        if (builder instanceof JavaSourceFileBuilder) {
            JavaSourceFileBuilder javaBuilder = (JavaSourceFileBuilder)builder;
            return getPackageName(ipsSrcFile, javaBuilder.isBuildingPublishedSourceFile(),
                    !builder.buildsDerivedArtefacts());
        }
        return getPackageName(ipsSrcFile, false, !builder.buildsDerivedArtefacts());
    }

    /**
     * Returns the name of the package the generated artifacts for the given IPS source file will be
     * placed in.
     * 
     * @param ipsSrcFile The source file to get the package from
     * 
     */
    public static String getPackageName(IIpsSrcFile ipsSrcFile, boolean publishedArtifacts, boolean mergableArtifacts) {
        return getPackageNameForGeneratedArtefacts(ipsSrcFile, publishedArtifacts, mergableArtifacts);
    }

    /**
     * Returns the name of the (Java) package that contains the artifacts specified by the
     * parameters generated for the given ips source file.
     * 
     * @param publishedArtifacts <code>true</code> if the artifacts are published (usable by
     *            clients), <code>false</code> if they are internal.
     * @param mergableArtifacts <code>true</code> if the generated artifact is mergable (at the
     *            moment this applies to Java Source files only). <code>false</code) if the artifact
     *            is 100% generated and can't be modified by the user.
     */
    protected static String getPackageNameForGeneratedArtefacts(IIpsSrcFile ipsSrcFile,
            boolean publishedArtifacts,
            boolean mergableArtifacts) {
        try {
            String basePackName = mergableArtifacts ? ipsSrcFile.getBasePackageNameForMergableArtefacts() : ipsSrcFile
                    .getBasePackageNameForDerivedArtefacts();
            String packageFragName = ipsSrcFile.getIpsPackageFragment().getName().toLowerCase();
            if (!publishedArtifacts) {
                return getInternalPackage(basePackName, packageFragName);
            } else {
                return QNameUtil.concat(basePackName, packageFragName);
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    public static String getInternalPackage(final String basePackName, final String subPackageFragment) {
        String internalBasePack = QNameUtil.concat(basePackName, INTERNAL_PACKAGE);
        return QNameUtil.concat(internalBasePack, subPackageFragment);
    }

    /**
     * Returns the name of the (Java) package name that contains the published artifacts that are
     * generated for the given IPS source file that (the artifacts) are also mergable.
     */
    public static String getPackageNameForMergablePublishedArtefacts(IIpsSrcFile ipsSrcFile) {
        return getPackageName(ipsSrcFile, true, true);
    }

    /**
     * Returns the name of the (Java) package name that contains the internal artifacts that are
     * generated for the given IPS source file that (the artifacts) are also mergable.
     */
    public static String getPackageNameForMergableInternalArtefacts(IIpsSrcFile ipsSrcFile) {
        return getPackageName(ipsSrcFile, false, true);
    }

}
