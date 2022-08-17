/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.adapter;

import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.type.IType;

/**
 * Adapts {@link IIpsObjectPartContainer}s to all classes listed in {@link #getAdapterList()}
 * 
 * @author Thorsten Günther
 */
public class IpsObjectPartContainerAdapterFactory extends AbstractIpsAdapterFactory {

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
        if (!(adaptableObject instanceof IIpsObjectPartContainer)) {
            return null;
        }

        if (IIpsSrcFile.class.equals(adapterType)) {
            return (T)adaptToIpsSrcFile(adaptableObject);
        }

        if (IIpsObject.class.equals(adapterType)) {
            return (T)adaptToIpsObject(adaptToIpsSrcFile(adaptableObject));
        }

        if (IProductCmpt.class.equals(adapterType)) {
            return (T)adaptToProductCmpt(adaptToIpsSrcFile(adaptableObject));
        }

        if (IType.class.equals(adapterType)) {
            return (T)adaptToType(adaptToIpsSrcFile(adaptableObject));
        }

        return null;
    }

    private IIpsSrcFile adaptToIpsSrcFile(Object adaptableObject) {
        return ((IIpsObjectPartContainer)adaptableObject).getIpsSrcFile();
    }

    @Override
    public Class<?>[] getAdapterList() {
        return new Class[] { IIpsSrcFile.class, IIpsObject.class, IProductCmpt.class, IType.class };
    }

}
