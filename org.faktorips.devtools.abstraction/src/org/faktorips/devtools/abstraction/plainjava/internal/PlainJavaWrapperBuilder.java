/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.abstraction.plainjava.internal;

import java.io.File;

import org.faktorips.devtools.abstraction.AAbstraction;
import org.faktorips.devtools.abstraction.AContainer;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.AJavaElement;
import org.faktorips.devtools.abstraction.AJavaProject;
import org.faktorips.devtools.abstraction.AMarker;
import org.faktorips.devtools.abstraction.APackageFragmentRoot;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.abstraction.AResourceDelta;
import org.faktorips.devtools.abstraction.AWorkspace;
import org.faktorips.devtools.abstraction.AWorkspaceRoot;
import org.faktorips.devtools.abstraction.Abstractions;
import org.faktorips.devtools.abstraction.Wrappers.WrapperBuilder;

public class PlainJavaWrapperBuilder extends WrapperBuilder {

    protected PlainJavaWrapperBuilder(Object original) {
        super(original);
    }

    // CSOFF: CyclomaticComplexity
    @SuppressWarnings("unchecked")
    @Override
    protected <A extends AAbstraction> A wrapInternal(Object original, Class<A> aClass) {
        if (AResourceDelta.class.isAssignableFrom(aClass)) {
            throw new UnsupportedOperationException(
                    "Resource deltas are currently not supported in plain Java mode"); //$NON-NLS-1$
        }
        if (AFolder.class.isAssignableFrom(aClass)) {
            return (A)new PlainJavaFolder((java.io.File)original);
        }
        if (AFile.class.isAssignableFrom(aClass)) {
            return (A)new PlainJavaFile((java.io.File)original);
        }
        if (AProject.class.isAssignableFrom(aClass)) {
            return (A)new PlainJavaProject((java.io.File)original);
        }
        if (AWorkspaceRoot.class.isAssignableFrom(aClass)
                || AContainer.class.isAssignableFrom(aClass)
                || AResource.class.isAssignableFrom(aClass)) {
            java.io.File originalFolder = (File)original;
            return (A)((PlainJavaWorkspaceRoot)Abstractions.getWorkspace().getRoot())
                    .get(originalFolder.toPath());
        }
        if (AJavaProject.class.isAssignableFrom(aClass)) {
            if (original instanceof PlainJavaProject) {
                return (A)new PlainJavaJavaProject(((PlainJavaProject)original).unwrap());
            } else {
                return (A)new PlainJavaJavaProject((java.io.File)original);
            }
        }
        if (APackageFragmentRoot.class.isAssignableFrom(aClass)) {
            if (original instanceof AFolder) {
                return (A)new PlainJavaPackageFragmentRoot(((AFolder)original).unwrap());
            } else {
                return (A)new PlainJavaPackageFragmentRoot((java.io.File)original);
            }
        }
        if (AJavaElement.class.isAssignableFrom(aClass)) {
            return (A)new PlainJavaJavaElement((java.io.File)original);
        }
        if (AMarker.class.isAssignableFrom(aClass)) {
            return (A)new PlainJavaMarker((PlainJavaMarkerImpl)original);
        }
        if (AWorkspace.class.isAssignableFrom(aClass)) {
            return (A)new PlainJavaWorkspace((java.io.File)original);
        }
        throw new IllegalArgumentException("Unknown wrapper class: " + aClass); //$NON-NLS-1$
    }
    // CSON: CyclomaticComplexity

}