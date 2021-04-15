/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.IPropertyValue;

public class ProductPartCollection {

    private final PropertyValueCollection propertyValueCollection;

    private final ProductCmptLinkCollection productCmptLinkCollection;

    public ProductPartCollection(PropertyValueCollection propertyValueCollection,
            ProductCmptLinkCollection productCmptLinkCollection) {
        this.propertyValueCollection = propertyValueCollection;
        this.productCmptLinkCollection = productCmptLinkCollection;
    }

    // checked internally
    @SuppressWarnings("unchecked")
    public <T extends IIpsObjectPart> List<T> getProductParts(Class<T> type) {
        if (IPropertyValue.class.isAssignableFrom(type)) {
            Class<? extends IPropertyValue> propertyValueType = (Class<? extends IPropertyValue>)type;
            return (List<T>)propertyValueCollection.getPropertyValues(propertyValueType);
        } else if (IProductCmptLink.class.isAssignableFrom(type)) {
            return (List<T>)productCmptLinkCollection.getLinks();
        }
        return new ArrayList<>();
    }

}
