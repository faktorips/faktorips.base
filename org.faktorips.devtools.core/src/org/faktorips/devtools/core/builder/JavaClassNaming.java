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

package org.faktorips.devtools.core.builder;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.util.QNameUtil;
import org.faktorips.util.StringUtil;

/**
 * Provides class and package names for {@link IIpsSrcFile IPS source files}.
 * 
 * {@link #getPackageName(IIpsSrcFile)} will only return package names of a specific type of
 * artifacts, e.g. internal and mergable, or published and mergable. The flags published and
 * mergable must be specified when creating a {@link JavaClassNaming}.
 * 
 * TODO vielleicht wäre es sinnvoll die Package-Methoden in eine extra Klasse (zB PackageNaming)
 * auszulagern?
 * 
 * @author widmaier
 */
public class JavaClassNaming {

    private static final String INTERFACE_CLASS_NAME_PREFIX = "I"; //$NON-NLS-1$
    private final static String INTERNAL_PACKAGE = "internal"; //$NON-NLS-1$
    private final boolean publishedArtifacts;
    private final boolean mergableArtifacts;

    public JavaClassNaming(boolean published, boolean mergable) {
        this.publishedArtifacts = published;
        this.mergableArtifacts = mergable;
    }

    /**
     * Returns the name of the package the generated artifacts for the given IPS source file will be
     * placed in.
     * 
     * @param ipsSrcFile The source file to get the package from
     * 
     */
    public String getPackageName(IIpsSrcFile ipsSrcFile) {
        return getPackageNameForGeneratedArtefacts(ipsSrcFile, publishedArtifacts, mergableArtifacts);
    }

    /**
     * Returns the qualified name of the Java class generated for the given IPS source file.
     * 
     * @param ipsSrcFile the IPS source file.
     * 
     */
    public String getQualifiedClassName(IIpsSrcFile ipsSrcFile) {
        StringBuffer buf = new StringBuffer();
        String packageName = getPackageName(ipsSrcFile);
        if (packageName != null) {
            buf.append(packageName);
            buf.append('.');
        }
        buf.append(getUnqualifiedClassName(ipsSrcFile));
        return buf.toString();
    }

    /**
     * Returns the qualified name of the Java class generated for the given IPS object.
     * 
     * 
     * @param ipsObject the IPS object.
     */
    public String getQualifiedClassName(IIpsObject ipsObject) {
        return getQualifiedClassName(ipsObject.getIpsSrcFile());
    }

    /**
     * Returns the qualified name of the Java class generated for the given IPS object.
     * 
     * 
     * @param ipsObject the IPS object.
     */
    public String getQualifiedClassNameForInterface(IIpsObject ipsObject) {
        return INTERFACE_CLASS_NAME_PREFIX + getQualifiedClassName(ipsObject.getIpsSrcFile());
    }

    /**
     * Returns the unqualified name for Java class generated for the given IPS source file.
     * 
     * 
     * @param ipsSrcFile the IPS source file
     */
    public String getUnqualifiedClassName(IIpsSrcFile ipsSrcFile) {
        return StringUtil.getFilenameWithoutExtension(ipsSrcFile.getName());
    }

    /**
     * Returns the name of the (Java) package that contains the artefacts specified by the
     * parameters generated for the given ips source file.
     * 
     * @param publishedArtefact <code>true</code> if the artefacts are published (usable by
     *            clients), <code>false</code> if they are internal.
     * @param mergableArtefact <code>true</code> if the generated artefact is mergable (at the
     *            moment this applies to Java Source files only). <code>false</code) if the artefact
     *            is 100% generated and can't be modified by the user.
     */
    protected String getPackageNameForGeneratedArtefacts(IIpsSrcFile ipsSrcFile,
            boolean publishedArtefact,
            boolean mergableArtefact) {

        try {
            String basePackName = mergableArtefact ? ipsSrcFile.getBasePackageNameForMergableArtefacts() : ipsSrcFile
                    .getBasePackageNameForDerivedArtefacts();
            String packageFragName = ipsSrcFile.getIpsPackageFragment().getName().toLowerCase();
            if (!publishedArtefact) {
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
     * Returns the name of the (Java) package name that contains the published artefacts that are
     * generated for the given IPS source file that (the artefacts) are also mergable.
     */
    public static String getPackageNameForMergablePublishedArtefacts(IIpsSrcFile ipsSrcFile) {
        return new JavaClassNaming(true, true).getPackageName(ipsSrcFile);
    }

    /**
     * Returns the name of the (Java) package name that contains the internal artefacts that are
     * generated for the given IPS source file that (the artefacts) are also mergable.
     */
    public static String getPackageNameForMergableInternalArtefacts(IIpsSrcFile ipsSrcFile) {
        return new JavaClassNaming(false, true).getPackageName(ipsSrcFile);
    }

}
