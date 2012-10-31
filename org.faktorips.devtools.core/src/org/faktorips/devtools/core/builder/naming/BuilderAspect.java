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

import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;

/**
 * An enumeration specifying the different aspect of a builder: the builder could generate
 * implementation classes or interfaces. Depending on these aspects the the name and package may
 * differ.
 */
public enum BuilderAspect {

    IMPLEMENTATION {
        @Override
        public String getJavaClassName(IIpsSrcFile ipsSrcFile, IJavaClassNameProvider javaClassNameProvider) {
            return javaClassNameProvider.getImplClassName(ipsSrcFile);
        }

        @Override
        public boolean isInternalArtifact(IJavaClassNameProvider javaClassNameProvider) {
            return javaClassNameProvider.isImplClassInternalArtifact();
        }
    },

    INTERFACE {
        @Override
        public String getJavaClassName(IIpsSrcFile ipsSrcFile, IJavaClassNameProvider javaClassNameProvider) {
            return javaClassNameProvider.getInterfaceName(ipsSrcFile);
        }

        @Override
        public boolean isInternalArtifact(IJavaClassNameProvider javaClassNameProvider) {
            return javaClassNameProvider.isInterfaceInternalArtifact();
        }
    };

    /**
     * Returning the fully qualified java class name for the specified source file using the
     * specified {@link IJavaClassNameProvider} depending on the current builder aspect.
     */
    public abstract String getJavaClassName(IIpsSrcFile ipsSrcFile, IJavaClassNameProvider javaClassNameProvider);

    /**
     * Returns true if the specified {@link IJavaClassNameProvider} would provide internal artifact
     * names for the current builder aspect.
     * 
     * @param javaClassNameProvider The {@link IJavaClassNameProvider} of your builder
     * 
     * @return <code>true</code> if the artifact has an internal name, false if not
     */
    public abstract boolean isInternalArtifact(IJavaClassNameProvider javaClassNameProvider);

    public static BuilderAspect getValue(boolean isInterface) {
        if (isInterface) {
            return INTERFACE;
        } else {
            return IMPLEMENTATION;
        }
    }
}
