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

import java.nio.file.Path;

import org.eclipse.core.runtime.IPath;
import org.faktorips.devtools.model.builder.IJavaPackageStructure;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;

/**
 * Provides class and package names for {@link IIpsSrcFile IPS source files}. The name is
 * concatenated by the package name provided by {@link JavaPackageStructure} and the unqualified
 * name. Clients may overwrite the unqualified class name.
 * <p>
 * Qualified names will only contain package names of a specific type of artifacts, e.g. internal
 * and mergable, or published and mergable. The flags published and mergable must be specified when
 * creating a {@link JavaClassNaming}.
 * 
 * @author widmaier
 */
public class JavaClassNaming {

    public static final String JAVA_EXTENSION = ".java"; //$NON-NLS-1$

    private final boolean mergableArtifacts;

    private final IJavaPackageStructure javaPackageStructure;

    public JavaClassNaming(IJavaPackageStructure javaPackageStructure, boolean mergableArtifacts) {
        this.javaPackageStructure = javaPackageStructure;
        this.mergableArtifacts = mergableArtifacts;
    }

    /**
     * Returns the qualified name of the Java class generated for the given IPS source file.
     * 
     * @param ipsSrcFile the IPS source file.
     * 
     */
    public String getQualifiedClassName(IIpsSrcFile ipsSrcFile,
            BuilderAspect aspect,
            IJavaClassNameProvider javaClassNameProvider) {
        return getQualifiedName(getPackageName(ipsSrcFile, aspect, javaClassNameProvider),
                getUnqualifiedClassName(ipsSrcFile, aspect, javaClassNameProvider));
    }

    private String getQualifiedName(String packageName, String className) {
        StringBuilder sb = new StringBuilder();
        if (packageName != null) {
            sb.append(packageName);
            sb.append('.');
        }
        sb.append(className);
        return sb.toString();
    }

    /**
     * Returns the qualified name of the Java class generated for the given IPS object.
     * 
     * 
     * @param ipsObject the IPS object.
     */
    public String getQualifiedClassName(IIpsObject ipsObject,
            BuilderAspect aspect,
            IJavaClassNameProvider javaClassNameProvider) {
        if (ipsObject == null) {
            return null;
        }
        return getQualifiedClassName(ipsObject.getIpsSrcFile(), aspect, javaClassNameProvider);
    }

    /**
     * Returns the unqualified name for Java class generated for the given IPS source file.
     * 
     * 
     * @param ipsSrcFile the IPS source file
     */
    public String getUnqualifiedClassName(IIpsSrcFile ipsSrcFile,
            BuilderAspect aspect,
            IJavaClassNameProvider javaClassNameProvider) {
        return aspect.getJavaClassName(ipsSrcFile, javaClassNameProvider);
    }

    public String getPackageName(IIpsSrcFile ipsSrcFile,
            BuilderAspect aspect,
            IJavaClassNameProvider javaClassNameProvider) {
        return javaPackageStructure.getPackageName(ipsSrcFile, aspect.isInternalArtifact(javaClassNameProvider),
                mergableArtifacts);
    }

    /**
     * Return the path to the java file relative to the destination folder.
     * <p>
     * If the java class name is org.example.MyExample this method would return an {@link IPath} of
     * <em>/org/example/MyExample.java</em>
     */
    public Path getRelativeJavaFile(IIpsSrcFile ipsSrcFile,
            BuilderAspect aspect,
            IJavaClassNameProvider javaClassNameProvider) {
        String name = getQualifiedClassName(ipsSrcFile, aspect, javaClassNameProvider);

        int index = name.lastIndexOf('.');
        if (index == name.length()) {
            throw new RuntimeException("The qualified class name is not a valid java class name"); //$NON-NLS-1$
        }
        Path javaFile = Path.of(name.replace('.', '/') + JAVA_EXTENSION);
        return javaFile;
    }

}
