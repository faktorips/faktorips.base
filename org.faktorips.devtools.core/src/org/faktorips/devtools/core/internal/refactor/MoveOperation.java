/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.refactor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.ipsproject.LibraryIpsPackageFragment;
import org.faktorips.devtools.core.internal.model.ipsproject.LibraryIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * Helper Class for Drag&Drop Move Operations
 */
public abstract class MoveOperation {

    public static boolean canMove(Object[] sources, Object target) {
        return canMoveToTarget(sources, target) && canMoveSources(sources) && canMovePackages(sources, target);
    }

    /**
     * Returns true if the given IIpsElement array contains at least one IIpsProject, false
     * otherwise.
     */
    private static boolean canMoveSources(Object[] sources) {
        for (Object source : sources) {
            if (source instanceof IIpsProject) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the move operation is applicable for the given source to target.
     */
    private static boolean canMoveToTarget(Object[] sources, Object target) {
        if (target instanceof IIpsElement) {
            if (!isSelfOrReferencedProject(sources, target)) {
                return false;
            }
        }
        return isValidTargetType(target);
    }

    /**
     * Returns <code>true</code> if the target project is same as source project or a target
     * referencing source project.
     */
    private static boolean isSelfOrReferencedProject(Object[] sources, Object target) {
        IIpsProject targetIpsProject = ((IIpsElement)target).getIpsProject();
        for (Object source : sources) {
            if (source instanceof IIpsElement) {
                try {
                    IIpsProject ipsProject = ((IIpsElement)source).getIpsProject();
                    if (!(ipsProject.equals(targetIpsProject) || ipsProject.isReferencedBy(targetIpsProject, true))) {
                        return false;
                    }
                } catch (CoreException e) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * If target object is of type:
     * <ul>
     * <li><code>IIpsObject</code>
     * <li><code>IIpsObjectPart</code>
     * <li><code>IFile</code>
     * <li><code>ArchiveIpsPackageFragment</code>
     * <li><code>ArchiveIpsPackageFragmentRoot</code>
     * </ul>
     * false is returned.
     */
    private static boolean isValidTargetType(Object target) {
        return !(target instanceof IIpsObject) & !(target instanceof IIpsObjectPart) & !(target instanceof IFile)
                & !(target instanceof IIpsSrcFile) & !(target instanceof LibraryIpsPackageFragment)
                & !(target instanceof LibraryIpsPackageFragmentRoot);
    }

    /**
     * Returns true for allowed move operations containing packages.
     * <p>
     * The current implementation returns <code>false</code> if the given target is element of the
     * given array of sources, e.g. moving an Object in itself. If the given target is a package,
     * this method returns <code>false</code> if the package is a subpackage of the given sources.
     * <code>true</code> otherwise. If the corresponding resource of the target is null return
     * <code>false</code> e.g. target is inside an ips archive. Also returns <tt>false</tt> if at
     * least one of the sources to move is a default package.
     */
    private static boolean canMovePackages(Object[] sources, Object target) {
        for (Object source : sources) {
            if (source.equals(target)) {
                return false;
            }
            if (representsFolder(source)) {
                if (isDefaultPackageFragement(source)) {
                    return false;
                }
                if (representsFolder(target)) {
                    IFolder sourceFolder = (IFolder)((IIpsElement)source).getCorrespondingResource();
                    IResource targetResource = ((IIpsElement)target).getCorrespondingResource();
                    if (!(targetResource instanceof IFolder)) {
                        return false;
                    }
                    IFolder targetFolder = (IFolder)targetResource;
                    if (sourceFolder.getFullPath().isPrefixOf(targetFolder.getFullPath())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static boolean representsFolder(Object object) {
        return object instanceof IIpsPackageFragment || object instanceof IIpsPackageFragmentRoot;
    }

    private static boolean isDefaultPackageFragement(Object source) {
        if (source instanceof IIpsPackageFragment) {
            IIpsPackageFragment packageFragment = (IIpsPackageFragment)source;
            if (packageFragment.isDefaultPackage()) {
                return true;
            }
        }
        return false;
    }
}
