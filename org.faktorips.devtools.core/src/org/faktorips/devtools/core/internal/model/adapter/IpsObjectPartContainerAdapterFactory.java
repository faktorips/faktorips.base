/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.adapter;

import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.type.IType;

/**
 * Adapts {@link IIpsObjectPartContainer}s to all classes listed in {@link #getAdapterList()}
 * 
 * @author Thorsten Günther
 */
public class IpsObjectPartContainerAdapterFactory extends AbstractIpsAdapterFactory {

    @Override
    @SuppressWarnings("rawtypes")
    // The Eclipse API uses raw type
    public Object getAdapter(Object adaptableObject, Class adapterType) {
        if (!(adaptableObject instanceof IIpsObjectPartContainer)) {
            return null;
        }

        if (IIpsSrcFile.class.equals(adapterType)) {
            return adaptToIpsSrcFile(adaptableObject);
        }

        if (IIpsObject.class.equals(adapterType)) {
            return adaptToIpsObject(adaptToIpsSrcFile(adaptableObject));
        }

        if (IProductCmpt.class.equals(adapterType)) {
            return adaptToProductCmpt(adaptToIpsSrcFile(adaptableObject));
        }

        if (IType.class.equals(adapterType)) {
            return adaptToType(adaptToIpsSrcFile(adaptableObject));
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
