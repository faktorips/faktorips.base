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
            if (!ipsSrcFile.exists()) {
                return null;
            }
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
        return new Class[] { IProductCmpt.class, IType.class, IIpsObject.class };
    }

}
