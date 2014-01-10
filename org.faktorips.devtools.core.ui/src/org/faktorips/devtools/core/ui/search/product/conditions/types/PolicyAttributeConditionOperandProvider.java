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

package org.faktorips.devtools.core.ui.search.product.conditions.types;

import java.util.List;

import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpt.IProductPartsContainer;
import org.faktorips.devtools.core.model.type.IAttribute;

final class PolicyAttributeConditionOperandProvider implements IOperandProvider {

    private final IAttribute attribute;

    public PolicyAttributeConditionOperandProvider(IAttribute attribute) {
        this.attribute = attribute;
    }

    @Override
    public Object getSearchOperand(IProductPartsContainer productPartsContainer) {
        List<IConfigElement> configElements = productPartsContainer.getProductParts(IConfigElement.class);
        for (IConfigElement configElement : configElements) {
            if (configElement.getPolicyCmptTypeAttribute().equals(attribute.getName())) {
                return configElement.getValueSet();
            }
        }
        return null;
    }

}