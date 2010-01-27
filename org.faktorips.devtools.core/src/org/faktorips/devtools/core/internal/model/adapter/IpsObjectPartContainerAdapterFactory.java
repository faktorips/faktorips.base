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

package org.faktorips.devtools.core.internal.model.adapter;

import org.eclipse.core.runtime.IAdapterFactory;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;

public class IpsObjectPartContainerAdapterFactory implements IAdapterFactory {

    // required because the signature of this method is fixed by IAdapterFactory
    @SuppressWarnings("unchecked")
    public Object getAdapter(Object adaptableObject, Class adapterType) {
        if (!IIpsSrcFile.class.equals(adapterType)) {
            return null;
        }

        if (adaptableObject instanceof IIpsObjectPartContainer) {
            return ((IIpsObjectPartContainer)adaptableObject).getIpsSrcFile();
        }
        return null;
    }

    public Class<?>[] getAdapterList() {
        return new Class[] { IIpsObjectPartContainer.class };
    }

}
