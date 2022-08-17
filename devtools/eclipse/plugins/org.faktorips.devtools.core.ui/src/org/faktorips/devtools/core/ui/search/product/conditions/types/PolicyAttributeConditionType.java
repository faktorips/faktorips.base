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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.devtools.model.type.ProductCmptPropertyType;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.runtime.IProductComponent;

/**
 * A condition for product relevant {@link IAttribute IAttributes} of a {@link IPolicyCmptType}.
 * <p>
 * The condition tests, whether the argument matches the {@link IValueSet}, which is defined in the
 * {@link IProductComponent} for the product relevant Attribute of the {@link IPolicyCmptType}.
 * <p>
 * The conditions only uses the {@link ValueSetSearchOperatorType AllowanceSearchOperatorTypes}
 * 
 * @author dicker
 */
public class PolicyAttributeConditionType extends AbstractAttributeConditionType {

    @Override
    public List<IIpsElement> getSearchableElements(IProductCmptType productCmptType) {
        List<IIpsElement> policyCmptTypeAttributes = new ArrayList<>();

        IPolicyCmptType policyCmptType;
        policyCmptType = productCmptType.findPolicyCmptType(productCmptType.getIpsProject());
        if (policyCmptType == null) {
            return Collections.emptyList();
        }

        List<IAttribute> attributes;
        attributes = policyCmptType.findAllAttributes(policyCmptType.getIpsProject());

        for (IAttribute attribute : attributes) {
            if (!(attribute instanceof IPolicyCmptTypeAttribute)) {
                continue;
            }
            IPolicyCmptTypeAttribute policyCmptTypeAttribute = (IPolicyCmptTypeAttribute)attribute;
            if (policyCmptTypeAttribute
                    .getProductCmptPropertyType() == ProductCmptPropertyType.POLICY_CMPT_TYPE_ATTRIBUTE
                    && policyCmptTypeAttribute.isProductRelevant()) {
                policyCmptTypeAttributes.add(policyCmptTypeAttribute);
            }
        }

        return policyCmptTypeAttributes;
    }

    @Override
    public List<? extends ISearchOperatorType> getSearchOperatorTypes(IIpsElement searchableElement) {
        ValueSetSearchOperatorType[] values = ValueSetSearchOperatorType.values();
        return Arrays.asList(values);
    }

    @Override
    public IOperandProvider createOperandProvider(IIpsElement elementPart) {
        IAttribute attribute = (IAttribute)elementPart;
        return new PolicyAttributeConditionOperandProvider(attribute);
    }

    @Override
    public String getName() {
        return Messages.PolicyAttributeCondition_conditionName;
    }
}
