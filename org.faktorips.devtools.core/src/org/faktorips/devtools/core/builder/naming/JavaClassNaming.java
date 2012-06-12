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

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;

/**
 * Provides class and package names for {@link IIpsSrcFile IPS source files}. The name is
 * concatenated by the package name provided by {@link JavaPackageStructure} and the unqualified
 * name. Clients may overwrite the unqualified class name.
 * 
 * Qualified names will only contain package names of a specific type of artifacts, e.g. internal
 * and mergable, or published and mergable. The flags published and mergable must be specified when
 * creating a {@link JavaClassNaming}.
 * 
 * @author widmaier
 */
public class JavaClassNaming {

    public final static String JAVA_EXTENSION = ".java"; //$NON-NLS-1$

    private final boolean publishedArtifacts;
    private final boolean mergableArtifacts;
    private final boolean buildsInterface;

    public JavaClassNaming(boolean buildsInterface, boolean publishedArtifacts, boolean mergableArtifacts) {
        this.buildsInterface = buildsInterface;
        this.publishedArtifacts = publishedArtifacts;
        this.mergableArtifacts = mergableArtifacts;
    }

    /**
     * Returns the qualified name of the Java class generated for the given IPS source file.
     * 
     * @param ipsSrcFile the IPS source file.
     * 
     */
    public String getQualifiedClassName(IIpsSrcFile ipsSrcFile) {
        return getQualifiedName(JavaPackageStructure.getPackageName(ipsSrcFile, publishedArtifacts, mergableArtifacts),
                getUnqualifiedClassName(ipsSrcFile));
    }

    private String getQualifiedName(String packageName, String className) {
        StringBuffer buf = new StringBuffer();
        if (packageName != null) {
            buf.append(packageName);
            buf.append('.');
        }
        buf.append(className);
        return buf.toString();
    }

    /**
     * Returns the qualified name of the Java class generated for the given IPS object.
     * 
     * 
     * @param ipsObject the IPS object.
     */
    public String getQualifiedClassName(IIpsObject ipsObject) {
        if (ipsObject == null) {
            return null;
        }
        return getQualifiedClassName(ipsObject.getIpsSrcFile());
    }

    /**
     * Provides the unqualified default name for the given {@link IIpsSrcFile}
     * 
     * @param ipsSrcFile the {@link IIpsSrcFile} you want to have the default name for
     * 
     * @return the unqualified default name
     */
    protected String getDefaultUnqualifiedName(IIpsSrcFile ipsSrcFile) {
        return ipsSrcFile.getIpsObjectName();
    }

    /**
     * Returns the unqualified name for Java class generated for the given IPS source file.
     * 
     * 
     * @param ipsSrcFile the IPS source file
     */
    public String getUnqualifiedClassName(IIpsSrcFile ipsSrcFile) {
        if (buildsInterface) {
            return ipsSrcFile.getIpsProject().getJavaNamingConvention()
                    .getPublishedInterfaceName(getDefaultUnqualifiedName(ipsSrcFile));
        } else {
            return ipsSrcFile.getIpsProject().getJavaNamingConvention()
                    .getImplementationClassName(getDefaultUnqualifiedName(ipsSrcFile));
        }
    }

    /**
     * Return the path to the java file relative to the destination folder.
     * <p>
     * If the java class name is org.example.MyExample this method would return an {@link IPath} of
     * <em>/org/example/MyExample.java</em>
     */
    public IPath getRelativeJavaFile(IIpsSrcFile ipsSrcFile) {
        String name = getQualifiedClassName(ipsSrcFile);

        int index = name.lastIndexOf('.');
        if (index == name.length()) {
            throw new RuntimeException("The qualified class name is not a valid java class name"); //$NON-NLS-1$
        }
        IPath javaFile = new Path(name.replace('.', '/') + JAVA_EXTENSION);
        return javaFile;
    }

}
