/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search.product.conditions.types;

import java.util.List;

import org.faktorips.devtools.model.productcmpt.IConfiguredValueSet;
import org.faktorips.devtools.model.productcmpt.IProductPartsContainer;
import org.faktorips.devtools.model.type.IAttribute;

final class PolicyAttributeConditionOperandProvider implements IOperandProvider {

    private final IAttribute attribute;

    public PolicyAttributeConditionOperandProvider(IAttribute attribute) {
        this.attribute = attribute;
    }

    @Override
    public Object getSearchOperand(IProductPartsContainer productPartsContainer) {
        List<IConfiguredValueSet> configuredValueSets = productPartsContainer
                .getProductParts(IConfiguredValueSet.class);
        for (IConfiguredValueSet configValueSet : configuredValueSets) {
            if (configValueSet.getPolicyCmptTypeAttribute().equals(attribute.getName())) {
                return configValueSet.getValueSet();
            }
        }
        return null;
    }

}
