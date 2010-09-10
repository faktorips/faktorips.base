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

package org.faktorips.devtools.core.ui;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdapterFactory;
import org.faktorips.devtools.core.model.IIpsElement;

public class IpsElementResourceAdapterFactory implements IAdapterFactory {

    @SuppressWarnings("unchecked")
    // eclipse api is not type safe
    @Override
    public Object getAdapter(Object adaptableObject, Class adapterType) {
        if (adaptableObject instanceof IIpsElement) {
            IIpsElement ipsElement = (IIpsElement)adaptableObject;
            IResource corrResource = ipsElement.getCorrespondingResource();
            if (corrResource != null && adapterType.isAssignableFrom(corrResource.getClass())) {
                return corrResource;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    // eclipse api is not type safe
    @Override
    public Class[] getAdapterList() {
        return new Class[] { IResource.class };
    }

}
