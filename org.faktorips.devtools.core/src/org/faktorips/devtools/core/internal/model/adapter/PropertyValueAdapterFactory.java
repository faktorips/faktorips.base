/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
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
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;

public class PropertyValueAdapterFactory extends AbstractIpsAdapterFactory {

    @Override
    @SuppressWarnings("rawtypes")
    // The Eclipse API uses raw type
    public Object getAdapter(Object adaptableObject, Class adapterType) {
        if (!(adaptableObject instanceof IPropertyValue)) {
            return null;
        }

        IPropertyValue propertyValue = (IPropertyValue)adaptableObject;

        if (IIpsSrcFile.class.equals(adapterType)) {
            return propertyValue.getIpsSrcFile();
        }

        if (IIpsObject.class.equals(adapterType)) {
            return propertyValue.getIpsObject();
        }

        if (IProductCmptGeneration.class.equals(adapterType)) {
            if (propertyValue.getPropertyValueContainer() instanceof IProductCmptGeneration) {
                return propertyValue.getPropertyValueContainer();
            }
        }

        if (IProductCmpt.class.equals(adapterType)) {
            return propertyValue.getPropertyValueContainer().getProductCmpt();
        }

        return null;
    }

    @SuppressWarnings("rawtypes")
    // eclipse adapters are not type safe
    @Override
    public Class[] getAdapterList() {
        return new Class[] { IIpsSrcFile.class, IIpsObject.class, IProductCmptGeneration.class, IProductCmpt.class };
    }

}
