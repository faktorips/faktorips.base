/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.workbenchadapters;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;

public class IpsObjectWorkbenchAdapter extends IpsElementWorkbenchAdapter {

    private final ImageDescriptor imageDescriptor;

    public IpsObjectWorkbenchAdapter(ImageDescriptor imageDescriptor) {
        this.imageDescriptor = imageDescriptor;
    }

    @Override
    protected final ImageDescriptor getImageDescriptor(IIpsElement ipsElement) {
        if (ipsElement instanceof IIpsSrcFile) {
            IIpsSrcFile ipsSrcFile = (IIpsSrcFile)ipsElement;
            return getImageDescriptor(ipsSrcFile);
        } else if (ipsElement instanceof IIpsObject) {
            IIpsObject ipsObject = (IIpsObject)ipsElement;
            return getImageDescriptor(ipsObject);
        }
        return null;
    }

    protected ImageDescriptor getImageDescriptor(IIpsSrcFile ipsSrcFile) {
        return imageDescriptor;
    }

    protected ImageDescriptor getImageDescriptor(IIpsObject ipsObject) {
        return imageDescriptor;
    }

    @Override
    protected final String getLabel(IIpsElement ipsElement) {
        if (ipsElement instanceof IIpsSrcFile) {
            IIpsSrcFile ipsSrcFile = (IIpsSrcFile)ipsElement;
            return getLabel(ipsSrcFile);
        } else if (ipsElement instanceof IIpsObject) {
            IIpsObject ipsObject = (IIpsObject)ipsElement;
            return getLabel(ipsObject);
        }
        return ipsElement.getName();
    }

    protected String getLabel(IIpsSrcFile ipsSrcFile) {
        try {
            return ipsSrcFile.getPropertyValue(IIpsElement.PROPERTY_NAME);
        } catch (CoreException e) {
            IpsPlugin.log(e);
            return ipsSrcFile.getName();
        }
    }

    protected String getLabel(IIpsObject ipsObject) {
        return ipsObject.getName();
    }

}
