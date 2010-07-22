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

package org.faktorips.devtools.core.ui.workbenchadapters;

import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;

public abstract class IpsObjectPartWorkbenchAdapter extends IpsElementWorkbenchAdapter {

    public IpsObjectPartWorkbenchAdapter() {
        super();
    }

    @Override
    protected final ImageDescriptor getImageDescriptor(IIpsElement ipsElement) {
        if (ipsElement instanceof IIpsObjectPart) {
            IIpsObjectPart ipsObjectPart = (IIpsObjectPart)ipsElement;
            return getImageDescriptor(ipsObjectPart);
        }
        return null;
    }

    protected abstract ImageDescriptor getImageDescriptor(IIpsObjectPart ipsObjectPart);

    @Override
    protected final String getLabel(IIpsElement ipsElement) {
        if (ipsElement instanceof IIpsObjectPart) {
            IIpsObjectPart ipsObjectPart = (IIpsObjectPart)ipsElement;
            return getLabel(ipsObjectPart);
        }
        return null;
    }

    protected String getLabel(IIpsObjectPart ipsObjectPart) {
        return ipsObjectPart.getName();
    }

}