/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.type;

import java.util.Objects;

import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.IConfiguredDefault;
import org.faktorips.devtools.model.productcmpt.IConfiguredValueSet;
import org.faktorips.devtools.model.productcmpt.IFormula;
import org.faktorips.devtools.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.model.productcmpt.IValidationRuleConfig;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.model.productcmpttype.ITableStructureUsage;

/**
 * Specifies the different types of product component properties.
 * <p>
 * This enum contains methods providing information about the relationship of product component
 * properties to their corresponding property values. It also provides a convenient way to create
 * property values for given product component properties.
 * 
 * @author Jan Ortmann
 * @author Stefan Widmaier
 * @author Alexander Weickmann
 * 
 * @see IProductCmptProperty
 */
public enum ProductCmptPropertyType {

    /**
     * Represents an {@link IProductCmptTypeAttribute} of an {@link IProductCmptType}.
     * <p>
     * An {@link IProductCmptProperty} with this {@link ProductCmptPropertyType} can be safely
     * casted to {@link IProductCmptTypeAttribute}.
     * <p>
     * An {@link IPropertyValue} with this {@link ProductCmptPropertyType} can be safely casted to
     * {@link IAttributeValue}.
     */
    PRODUCT_CMPT_TYPE_ATTRIBUTE(Messages.ProductCmptPropertyType_productAttribute),

    /**
     * The {@link IProductCmptProperty} is an {@link IPolicyCmptTypeAttribute} of an
     * {@link IPolicyCmptType}.
     * <p>
     * An {@link IProductCmptProperty} with this type can be safely casted to
     * {@link IPolicyCmptTypeAttribute}.
     * <p>
     * An {@link IPropertyValue} with this type can either be a {@link IConfiguredValueSet} or
     * {@link IConfiguredDefault}
     */
    POLICY_CMPT_TYPE_ATTRIBUTE(Messages.ProductCmptPropertyType_defaultValueAndValueSet),

    /**
     * The {@link IProductCmptProperty} is an {@link IProductCmptTypeMethod} of an
     * {@link IProductCmptType} that is marked as <em>formula signature</em>.
     * <p>
     * An {@link IProductCmptProperty} with this type can be safely casted to
     * {@link IProductCmptTypeMethod}.
     * <p>
     * An {@link IPropertyValue} with this type can be safely casted to {@link IFormula}.
     */
    FORMULA_SIGNATURE_DEFINITION(Messages.ProductCmptPropertyType_fomula),

    /**
     * The {@link IProductCmptProperty} is an {@link ITableStructureUsage} of an
     * {@link IProductCmptType}.
     * <p>
     * An {@link IProductCmptProperty} with this type can be safely casted to
     * {@link ITableStructureUsage}.
     * <p>
     * An {@link IPropertyValue} with this type can be safely casted to {@link ITableContentUsage}.
     */
    TABLE_STRUCTURE_USAGE(Messages.ProductCmptPropertyType_tableUsage),

    /**
     * The {@link IProductCmptProperty} is an {@link IValidationRule} of an {@link IPolicyCmptType}.
     * <p>
     * An {@link IProductCmptProperty} with this type can be safely casted to
     * {@link IValidationRule}.
     * <p>
     * An {@link IPropertyValue} with this type can be safely casted to
     * {@link IValidationRuleConfig}.
     */
    VALIDATION_RULE(Messages.ProductCmptPropertyType_ValidationRule);

    private final String name;

    ProductCmptPropertyType(String name) {
        this.name = name;
    }

    /**
     * Returns the name of this property type.
     */
    public String getName() {
        return name;
    }

    public boolean isMatchingPropertyValue(String propertyName, IPropertyValue propertyValue) {
        return this == propertyValue.getProductCmptPropertyType()
                && Objects.equals(propertyName, propertyValue.getPropertyName());
    }

}
