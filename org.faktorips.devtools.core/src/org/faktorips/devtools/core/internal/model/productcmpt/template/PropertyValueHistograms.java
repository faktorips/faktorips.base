/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.productcmpt.template;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpt.PropertyValueType;
import org.faktorips.devtools.core.util.Histogram;

public class PropertyValueHistograms {

    private final Map<String, Histogram<Object, IPropertyValue>> propertyValueHistorgram = new HashMap<String, Histogram<Object, IPropertyValue>>();

    private PropertyValueHistograms(Multimap<String, IPropertyValue> propertyToValues) {
        for (Entry<String, Collection<IPropertyValue>> propertyValuesEntry : propertyToValues.asMap().entrySet()) {
            String name = propertyValuesEntry.getKey();
            Collection<IPropertyValue> propertyValues = propertyValuesEntry.getValue();
            populateHistogram(name, propertyValues);
        }
    }

    private PropertyValueType getPropertyValueType(Collection<IPropertyValue> propertyValues) {
        IPropertyValue propertyValue = propertyValues.iterator().next();
        return propertyValue.getPropertyValueType();
    }

    private void populateHistogram(String name, Collection<IPropertyValue> propertyValues) {
        if (propertyValues.size() > 1) {
            PropertyValueType valueType = getPropertyValueType(propertyValues);
            propertyValueHistorgram.put(name, new Histogram<Object, IPropertyValue>(valueType.getValueFunction(),
                    valueType.getValueComparator(), propertyValues));
        }
    }

    public static PropertyValueHistograms createFor(List<IProductCmpt> cmpts) {
        Multimap<String, IPropertyValue> propertyToValues = LinkedListMultimap.create();
        for (IProductCmpt productCmpt : cmpts) {
            List<IPropertyValue> allPropertyValues = productCmpt.getAllPropertyValues();
            for (IPropertyValue propertyValue : allPropertyValues) {
                propertyToValues.put(propertyValue.getPropertyName(), propertyValue);
            }
        }
        return new PropertyValueHistograms(propertyToValues);
    }

    public Histogram<Object, IPropertyValue> get(String propertyName) {
        return propertyValueHistorgram.get(propertyName);
    }

}
