/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.refactor;

import org.eclipse.core.resources.IFile;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.ILibraryIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.ILibraryIpsPackageFragmentRoot;

/**
 * Helper class for drag&amp;drop move operations
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
            if (!isSelfOrRefProject(sources, target)) {
                return false;
            }
        }
        return isValidTargetType(target);
    }

    /**
     * Returns <code>true</code> if the target project is same as source project or a target
     * referencing source project or a target is referenced by source project. Indirect references
     * are taken into account, too.
     */
    private static boolean isSelfOrRefProject(Object[] sources, Object target) {
        IIpsProject targetIpsProject = ((IIpsElement)target).getIpsProject();
        for (Object source : sources) {
            if (source instanceof IIpsElement) {
                IIpsProject ipsProject = ((IIpsElement)source).getIpsProject();
                if (!(ipsProject.equals(targetIpsProject) || ipsProject.isReferencedBy(targetIpsProject, true)
                        || ipsProject
                                .isReferencing(targetIpsProject))) {
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
                & !(target instanceof IIpsSrcFile) & !(target instanceof ILibraryIpsPackageFragment)
                & !(target instanceof ILibraryIpsPackageFragmentRoot);
    }

    /**
     * Returns true for allowed move operations containing packages.
     * <p>
     * The current implementation returns <code>false</code> if the given target is element of the
     * given array of sources, e.g. moving an Object in itself. If the given target is a package,
     * this method returns <code>false</code> if the package is a subpackage of the given sources.
     * <code>true</code> otherwise. If the corresponding resource of the target is null return
     * <code>false</code> e.g. target is inside an ips archive. Also returns <code>false</code> if
     * at least one of the sources to move is a default package.
     */
    public static boolean canMovePackages(Object[] sources, Object target) {
        for (Object source : sources) {
            if (source.equals(target)) {
                return false;
            }
            if (representsFolder(source)) {
                if (isDefaultPackageFragement(source)) {
                    return false;
                }
                if (representsFolder(target)) {
                    AFolder sourceFolder = (AFolder)((IIpsElement)source).getCorrespondingResource();
                    AResource targetResource = ((IIpsElement)target).getCorrespondingResource();
                    if (!(targetResource instanceof AFolder)) {
                        return false;
                    }
                    AFolder targetFolder = (AFolder)targetResource;
                    if (targetFolder.getWorkspaceRelativePath().startsWith(sourceFolder.getWorkspaceRelativePath())) {
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
