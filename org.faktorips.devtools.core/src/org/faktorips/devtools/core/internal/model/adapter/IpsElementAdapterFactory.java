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

package org.faktorips.devtools.core.internal.model.adapter;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdapterFactory;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;

/**
 * This {@link IAdapterFactory} is able to adapt {@link IIpsElement}s to other objects like
 * {@link IResource}
 * 
 * @author dirmeier
 */
public class IpsElementAdapterFactory implements IAdapterFactory {

    @SuppressWarnings("rawtypes")
    // the eclipse API uses raw type
    @Override
    public Object getAdapter(Object adaptableObject, Class adapterType) {
        if (!(adaptableObject instanceof IIpsElement)) {
            return null;
        }

        IIpsElement ipsElement = (IIpsElement)adaptableObject;

        try {
            IResource enclosingResource = ipsElement.getEnclosingResource();
            if (adapterType.isInstance(enclosingResource)) {
                return enclosingResource;
            }
        } catch (Exception e) {
            IpsPlugin.log(e);
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    // the eclipse API uses raw type
    @Override
    public Class[] getAdapterList() {
        return new Class[] { IResource.class, IProject.class, IFolder.class, IFile.class };
    }

}
