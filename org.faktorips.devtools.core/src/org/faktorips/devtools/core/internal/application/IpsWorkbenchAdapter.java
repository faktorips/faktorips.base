/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.application;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeMatcher;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.model.WorkbenchAdapter;
import org.faktorips.devtools.core.IpsPlugin;

/**
 * WorkbenchAdapter which is only capable of <code>IResource</code>s
 * 
 * @author Thorsten Guenther
 */
class IpsWorkbenchAdapter extends WorkbenchAdapter {

    @Override
    public Object[] getChildren(Object object) {
        if (object instanceof IContainer) {
            try {
                return ((IContainer)object).members();
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
        }
        return new IResource[0];
    }

    @Override
    public ImageDescriptor getImageDescriptor(Object object) {
        String key = null;
        ImageDescriptor image = null;
        if (object instanceof IProject) {
            if (((IProject)object).isOpen()) {
                key = IDE.SharedImages.IMG_OBJ_PROJECT;
            } else {
                key = IDE.SharedImages.IMG_OBJ_PROJECT_CLOSED;
            }
        } else if (object instanceof IFile) {
            key = ISharedImages.IMG_OBJ_FILE;
            IFile file = (IFile)object;
            IContentType contentType = null;
            try {
                IContentTypeMatcher matcher = file.getProject().getContentTypeMatcher();
                contentType = matcher.findContentTypeFor(file.getName());
            } catch (CoreException e) {
            }
            image = PlatformUI.getWorkbench().getEditorRegistry().getImageDescriptor(file.getName(), contentType);

        } else if (object instanceof IFolder) {
            key = ISharedImages.IMG_OBJ_FOLDER;
        }

        if (image != null) {
            return image;
        }

        if (key != null) {
            return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(key);
        }

        return super.getImageDescriptor(object);
    }

    @Override
    public String getLabel(Object object) {
        if (object instanceof IResource) {
            return ((IResource)object).getName();
        }
        return super.getLabel(object);
    }

    @Override
    public Object getParent(Object object) {
        if (object instanceof IResource) {
            return ((IResource)object).getParent();
        }
        return super.getParent(object);
    }

}
