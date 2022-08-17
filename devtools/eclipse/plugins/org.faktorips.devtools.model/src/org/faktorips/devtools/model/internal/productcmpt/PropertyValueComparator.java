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

import java.io.Serializable;
import java.util.Comparator;

import org.faktorips.devtools.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.model.productcmpt.PropertyValueType;

public class PropertyValueComparator implements Comparator<IPropertyValue>, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public int compare(IPropertyValue element1, IPropertyValue element2) {
        int comparedPropertyTypes = element1.getProductCmptPropertyType().compareTo(
                element2.getProductCmptPropertyType());
        if (comparedPropertyTypes == 0) {
            return compareSamePropertyType(element1, element2);
        } else {
            return comparedPropertyTypes;
        }
    }

    /**
     * Within the same property type the items should be compared by name
     */
    private int compareSamePropertyType(IPropertyValue element1, IPropertyValue element2) {
        int comparedName = element1.getPropertyName().compareTo(element2.getPropertyName());
        if (comparedName == 0) {
            return compareSamePropertyTypeAndName(element1, element2);
        } else {
            return comparedName;
        }
    }

    /**
     * Two elements with the same property type and the same name may be a {@link ConfiguredDefault}
     * and {@link ConfiguredValueSet}. So we just need to compare the {@link PropertyValueType}.
     */
    private int compareSamePropertyTypeAndName(IPropertyValue element1, IPropertyValue element2) {
        return element1.getPropertyValueType().compareTo(element2.getPropertyValueType());
    }

}
