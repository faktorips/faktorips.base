/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.productcmpt;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;

/**
 * Comparator that compares two {@link IPropertyValue}s by there type and by the position the
 * corresponding {@link IProductCmptProperty} has in the model.
 * <p>
 * Example:
 * <p>
 * Given two formulas, the positions of the corresponding formula signatures in the product
 * component type's list of formula signatures defines the order.
 * 
 * @author Jan Ortmann
 */
public class PropertyValueComparator implements Comparator<IPropertyValue> {

    private IProductCmptType type;
    private Map<String, Integer> propIndexMap = null;

    public PropertyValueComparator(IProductCmptGeneration gen, IIpsProject ipsProject) {
        this(gen.getProductCmpt(), ipsProject);
    }

    public PropertyValueComparator(IProductCmpt productCmpt, IIpsProject ipsProject) {
        this(productCmpt.getProductCmptType(), ipsProject);
    }

    public PropertyValueComparator(String productCmptType, IIpsProject ipsProject) {
        try {
            type = ipsProject.findProductCmptType(productCmptType);
        } catch (Exception e) {
            IpsPlugin
                    .log(new IpsStatus("Error finding type for property comparator, type name = " + productCmptType, e)); //$NON-NLS-1$
        }
        initPropIndexMap(ipsProject);
    }

    public PropertyValueComparator(IProductCmptType type, IIpsProject ipsProject) {
        this.type = type;
        initPropIndexMap(ipsProject);
    }

    private void initPropIndexMap(IIpsProject ipsProject) {
        try {
            if (type != null) {
                propIndexMap = new HashMap<String, Integer>();
                List<IProductCmptProperty> props = type.findProductCmptProperties(true, ipsProject);
                int i = 0;
                for (IProductCmptProperty property : props) {
                    propIndexMap.put(property.getPropertyName(), i);
                    i++;
                }
            }
        } catch (Exception e) {
            IpsPlugin.log(new IpsStatus("Error initializing property comparator for type " + type, e)); //$NON-NLS-1$
        }
    }

    public IProductCmptType getProductCmptType() {
        return type;
    }

    @Override
    public int compare(IPropertyValue o1, IPropertyValue o2) {
        IPropertyValue prop1 = o1;
        IPropertyValue prop2 = o2;
        int typeCompare = prop1.getPropertyType().compareTo(prop2.getPropertyType());
        if (typeCompare != 0) {
            return typeCompare;
        }
        return getIndex(prop1) - getIndex(prop2);
    }

    private int getIndex(IPropertyValue prop) {
        if (type == null) {
            return 0;
        }
        Integer index = propIndexMap.get(prop.getPropertyName());
        if (index == null) {
            return 0;
        }
        return index.intValue();
    }

}
