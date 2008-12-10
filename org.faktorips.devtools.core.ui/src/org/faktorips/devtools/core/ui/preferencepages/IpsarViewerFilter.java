/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
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
 * @author Roman Grutza
 */
public class IpsarViewerFilter extends ViewerFilter {

    private List excluded;
    private boolean recursive;

    
    /**
     * @param alreadyRefArchives
     * @param recursive
     */
    public IpsarViewerFilter(List excluded, boolean recursive) {
        this.excluded  = excluded;
        this.recursive = recursive;
    }

    /**
     * {@inheritDoc}
     */
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        
        if (element instanceof IFile) {
            if (excluded != null && excluded.contains(element))
                return false;
            return isArchiveFile(((IFile) element).getFullPath());
        } else if (element instanceof IContainer) { // IProject, IFolder
            if (!recursive) {
                return true;
            }
            // Ignore closed projects
            if (element instanceof IProject && !((IProject)element).isOpen())
                return false;

            try {
                IResource[]resources = ((IContainer)element).members();
                // recursive! Only show containers that contain an archive
                for (int i= 0; i < resources.length; i++) {
                    if (select(viewer, parentElement, resources[i])) {
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
        boolean isArchive;
        
        String fileExtension = fullPath.getFileExtension();
        isArchive = (fileExtension != null)
            && (fileExtension.equals("ipsar") || fileExtension.equals("jar") || fileExtension.equals("zip")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        
        return isArchive;
    }

}
