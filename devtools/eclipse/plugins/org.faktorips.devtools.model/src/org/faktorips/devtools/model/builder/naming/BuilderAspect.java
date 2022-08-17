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

import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;

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
