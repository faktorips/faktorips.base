/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.util;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

/**
 * A collection of static utility methods for the IpsModelPlugin.
 * 
 * @author Jan Ortmann
 */
public final class IpsProjectUtil {

    private IpsProjectUtil() {
        // Utility class not to be instantiated.
    }

    /**
     * Adds the nature to the project.
     * 
     * @param project A platform project.
     * @param natureId The id of a nature.
     */
    public static void addNature(IProject project, String natureId) throws CoreException {
        IProjectDescription description = project.getDescription();
        String[] natures = description.getNatureIds();
        String[] newNatures = new String[natures.length + 1];
        System.arraycopy(natures, 0, newNatures, 1, natures.length);
        newNatures[0] = natureId;
        description.setNatureIds(newNatures);
        project.setDescription(description, null);
    }

    public static IPackageFragmentRoot addFolderAsPackageFragmentRoot(IJavaProject project, IFolder folder)
            throws JavaModelException {

        IPackageFragmentRoot root = project.getPackageFragmentRoot(folder);
        IClasspathEntry[] oldEntries = project.getRawClasspath();
        IClasspathEntry[] newEntries = new IClasspathEntry[oldEntries.length + 1];
        System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
        newEntries[oldEntries.length] = JavaCore.newSourceEntry(root.getPath());
        project.setRawClasspath(newEntries, null);
        return root;
    }

}
