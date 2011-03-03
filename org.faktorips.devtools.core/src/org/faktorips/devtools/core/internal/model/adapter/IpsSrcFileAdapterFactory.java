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

import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmpt;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.type.IType;

public class IpsSrcFileAdapterFactory extends AbstractIpsAdapterFactory {

    @SuppressWarnings("rawtypes")
    // The Eclipse API uses raw type
    @Override
    public Object getAdapter(Object adaptableObject, Class adapterType) {
        if (adaptableObject instanceof IIpsSrcFile) {
            IIpsSrcFile ipsSrcFile = (IIpsSrcFile)adaptableObject;
            if (IProductCmpt.class.equals(adapterType)) {
                return adaptToProductCmpt(ipsSrcFile);
            }

            if (IType.class.equals(adapterType)) {
                return adaptToType(ipsSrcFile);
            }

            if (IIpsObject.class.equals(adapterType)) {
                return adaptToIpsObject(ipsSrcFile);
            }

            return null;
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    // The Eclipse API uses raw type
    @Override
    public Class[] getAdapterList() {
        return new Class[] { ProductCmpt.class, IType.class, IIpsObject.class };
    }

}
