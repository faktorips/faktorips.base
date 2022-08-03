/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.abstraction.eclipse.internal;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.ILog;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.faktorips.devtools.abstraction.AAbstraction;
import org.faktorips.devtools.abstraction.AContainer;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.AJavaElement;
import org.faktorips.devtools.abstraction.AJavaProject;
import org.faktorips.devtools.abstraction.ALog;
import org.faktorips.devtools.abstraction.AMarker;
import org.faktorips.devtools.abstraction.APackageFragmentRoot;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.abstraction.AResourceDelta;
import org.faktorips.devtools.abstraction.AWorkspace;
import org.faktorips.devtools.abstraction.AWorkspaceRoot;
import org.faktorips.devtools.abstraction.Wrappers.WrapperBuilder;

public class EclipseWrapperBuilder extends WrapperBuilder {

    protected EclipseWrapperBuilder(Object original) {
        super(original);
    }

    // CSOFF: CyclomaticComplexity
    @SuppressWarnings("unchecked")
    @Override
    protected <A extends AAbstraction> A wrapInternal(Object original, Class<A> aClass) {
        if (ALog.class.isAssignableFrom(aClass)) {
            return (A)new EclipseLog((ILog)original);
        }
        if (AResourceDelta.class.isAssignableFrom(aClass)) {
            return (A)new EclipseResourceDelta((IResourceDelta)original);
        }
        if (AFolder.class.isAssignableFrom(aClass)) {
            return (A)new EclipseFolder((IFolder)original);
        }
        if (AFile.class.isAssignableFrom(aClass)) {
            return (A)new EclipseFile((IFile)original);
        }
        if (AProject.class.isAssignableFrom(aClass)) {
            return (A)new EclipseProject((IProject)original);
        }
        if (AWorkspaceRoot.class.isAssignableFrom(aClass)) {
            return (A)new EclipseWorkspaceRoot((IWorkspaceRoot)original);
        }
        if (AContainer.class.isAssignableFrom(aClass)) {
            IContainer container = (IContainer)original;
            if (container instanceof IWorkspaceRoot) {
                return (A)new EclipseWorkspaceRoot((IWorkspaceRoot)container);
            }
            if (container instanceof IProject) {
                return (A)new EclipseProject((IProject)container);
            }
            if (container instanceof IFolder) {
                return (A)new EclipseFolder((IFolder)container);
            }
        }
        if (AResource.class.isAssignableFrom(aClass)) {
            IResource resource = (IResource)original;
            if (resource instanceof IWorkspaceRoot) {
                return (A)new EclipseWorkspaceRoot((IWorkspaceRoot)resource);
            } else if (resource instanceof IProject) {
                return (A)new EclipseProject((IProject)resource);
            } else if (resource instanceof IFolder) {
                return (A)new EclipseFolder((IFolder)resource);
            } else if (resource instanceof IFile) {
                return (A)new EclipseFile((IFile)resource);
            } else {
                throw new IllegalArgumentException(
                        "Don't know how to wrap a " + resource.getClass().getName() + " as a " + aClass.getName()); //$NON-NLS-1$//$NON-NLS-2$
            }
        }
        if (AJavaProject.class.isAssignableFrom(aClass)) {
            if (original instanceof EclipseProject) {
                return (A)new EclipseJavaProject(JavaCore.create(ResourcesPlugin.getWorkspace().getRoot())
                        .getJavaProject(((EclipseProject)original).getName()));
            } else {
                return (A)new EclipseJavaProject((IJavaProject)original);
            }
        }
        if (APackageFragmentRoot.class.isAssignableFrom(aClass)) {
            return (A)new EclipsePackageFragmentRoot((IPackageFragmentRoot)original);
        }
        if (AJavaElement.class.isAssignableFrom(aClass)) {
            return (A)new EclipseJavaElement((IJavaElement)original);
        }
        if (AMarker.class.isAssignableFrom(aClass)) {
            return (A)new EclipseMarker((IMarker)original);
        }
        if (AWorkspace.class.isAssignableFrom(aClass)) {
            return (A)new EclipseWorkspace((IWorkspace)original);
        }
        throw new IllegalArgumentException("Unknown wrapper class: " + aClass); //$NON-NLS-1$
    }
    // CSON: CyclomaticComplexity

}
