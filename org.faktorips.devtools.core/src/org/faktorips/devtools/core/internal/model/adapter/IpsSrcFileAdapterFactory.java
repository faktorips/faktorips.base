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
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.type.IType;

public class IpsSrcFileAdapterFactory extends AbstractIpsAdapterFactory {

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
        if (adaptableObject instanceof IIpsSrcFile) {
            IIpsSrcFile ipsSrcFile = (IIpsSrcFile)adaptableObject;
            if (!ipsSrcFile.exists()) {
                return null;
            }
            if (IProductCmpt.class.equals(adapterType)) {
                return (T)adaptToProductCmpt(ipsSrcFile);
            }

            if (IType.class.equals(adapterType)) {
                return (T)adaptToType(ipsSrcFile);
            }

            if (IIpsObject.class.equals(adapterType)) {
                return (T)adaptToIpsObject(ipsSrcFile);
            }
        }
        return null;
    }

    @Override
    public Class<?>[] getAdapterList() {
        return new Class[] { IProductCmpt.class, IType.class, IIpsObject.class };
    }

}
