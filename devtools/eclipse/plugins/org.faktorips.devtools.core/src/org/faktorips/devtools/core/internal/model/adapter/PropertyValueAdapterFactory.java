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
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IPropertyValue;

public class PropertyValueAdapterFactory extends AbstractIpsAdapterFactory {

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
        if (!(adaptableObject instanceof IPropertyValue propertyValue)) {
            return null;
        }

        if (IIpsSrcFile.class.equals(adapterType)) {
            return (T)propertyValue.getIpsSrcFile();
        }

        if (IIpsObject.class.equals(adapterType)) {
            return (T)propertyValue.getIpsObject();
        }

        if (IProductCmptGeneration.class.equals(adapterType)) {
            if (propertyValue.getPropertyValueContainer() instanceof IProductCmptGeneration) {
                return (T)propertyValue.getPropertyValueContainer();
            }
        }

        if (IProductCmpt.class.equals(adapterType)) {
            return (T)propertyValue.getPropertyValueContainer().getProductCmpt();
        }

        return null;
    }

    @Override
    public Class<?>[] getAdapterList() {
        return new Class[] { IIpsSrcFile.class, IIpsObject.class, IProductCmptGeneration.class, IProductCmpt.class };
    }

}
