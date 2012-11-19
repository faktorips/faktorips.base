/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.productcmpt;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;

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
        return new ArrayList<T>();
    }

}
