/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt.template;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.faktorips.devtools.model.internal.util.Histogram;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.model.productcmpt.IPropertyValueContainer;
import org.faktorips.devtools.model.productcmpt.PropertyValueType;
import org.faktorips.devtools.model.productcmpt.template.ITemplatedValueIdentifier;
import org.faktorips.util.MultiMap;

public class PropertyValueHistograms {

    private final Map<ITemplatedValueIdentifier, Histogram<Object, IPropertyValue>> propertyValueHistorgram = new HashMap<>();

    private PropertyValueHistograms(MultiMap<ITemplatedValueIdentifier, IPropertyValue> propertyToValues) {
        for (Entry<ITemplatedValueIdentifier, Collection<IPropertyValue>> propertyValuesEntry : propertyToValues
                .asMap().entrySet()) {
            ITemplatedValueIdentifier name = propertyValuesEntry.getKey();
            Collection<IPropertyValue> propertyValues = propertyValuesEntry.getValue();
            populateHistogram(name, propertyValues);
        }
    }

    private PropertyValueType getPropertyValueType(Collection<IPropertyValue> propertyValues) {
        IPropertyValue propertyValue = propertyValues.iterator().next();
        return propertyValue.getPropertyValueType();
    }

    private void populateHistogram(ITemplatedValueIdentifier name, Collection<IPropertyValue> propertyValues) {
        if (!propertyValues.isEmpty()) {
            PropertyValueType valueType = getPropertyValueType(propertyValues);
            propertyValueHistorgram.put(name, new Histogram<>(valueType.getValueGetter(),
                    valueType.getValueComparator(), propertyValues));
        }
    }

    public static PropertyValueHistograms createFor(List<IProductCmpt> cmpts) {
        MultiMap<ITemplatedValueIdentifier, IPropertyValue> propertyToValues = MultiMap.createWithLinkedSetAsValues();
        for (IProductCmpt productCmpt : cmpts) {
            addAllPropertyValues(propertyToValues, productCmpt);
            IProductCmptGeneration productCmptGeneration = productCmpt.getLatestProductCmptGeneration();
            addAllPropertyValues(propertyToValues, productCmptGeneration);

        }
        return new PropertyValueHistograms(propertyToValues);
    }

    private static void addAllPropertyValues(MultiMap<ITemplatedValueIdentifier, IPropertyValue> propertyToValues,
            IPropertyValueContainer propertyValueContainer) {
        for (IPropertyValue propertyValue : propertyValueContainer.getAllPropertyValues()) {
            propertyToValues.put(propertyValue.getIdentifier(), propertyValue);
        }
    }

    public Histogram<Object, IPropertyValue> get(ITemplatedValueIdentifier propertyName) {
        return propertyValueHistorgram.get(propertyName);
    }

    public int size() {
        return propertyValueHistorgram.size();
    }

}
