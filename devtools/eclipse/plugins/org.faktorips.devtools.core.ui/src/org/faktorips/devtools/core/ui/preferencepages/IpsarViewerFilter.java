/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.preferencepages;

import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.faktorips.devtools.core.IpsPlugin;

/**
 * Viewer filter for IPS archives
 *
 * @author Roman Grutza
 */
public class IpsarViewerFilter extends ViewerFilter {

    private List<String> excluded;
    private boolean recursive;

    public IpsarViewerFilter(List<String> excluded, boolean recursive) {
        this.excluded = excluded;
        this.recursive = recursive;
    }

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        if (element instanceof IFile file) {
            IPath fullPath = file.getLocation();
            if (excluded != null && excluded.contains(fullPath.toOSString())) {
                return false;
            }
            return isArchiveFile(fullPath);
        } else if (element instanceof IContainer container) {
            // IProject, IFolder
            if (!recursive) {
                return true;
            }
            // Ignore closed projects
            if (element instanceof IProject project && !project.isOpen()) {
                return false;
            }

            try {
                IResource[] resources = container.members();
                // recursive! Only show containers that contain an archive
                for (IResource resource : resources) {
                    if (select(viewer, parentElement, resource)) {
                        return true;
                    }
                }
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
        }
        return false;
    }

    private boolean isArchiveFile(IPath fullPath) {
        String fileExtension = fullPath.getFileExtension();
        return (fileExtension != null)
                && ("ipsar".equals(fileExtension) || "jar".equals(fileExtension) || "zip".equals(fileExtension)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

}
