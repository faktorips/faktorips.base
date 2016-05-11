/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.type;

import org.apache.commons.lang.ObjectUtils;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpt.IValidationRuleConfig;
import org.faktorips.devtools.core.model.productcmpt.PropertyValueType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;

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
    PRODUCT_CMPT_TYPE_ATTRIBUTE(Messages.ProductCmptPropertyType_productAttribute, PropertyValueType.ATTRIBUTE_VALUE) {

    },

    /**
     * The {@link IProductCmptProperty} is an {@link ITableStructureUsage} of an
     * {@link IProductCmptType}.
     * <p>
     * An {@link IProductCmptProperty} with this type can be safely casted to
     * {@link ITableStructureUsage}.
     * <p>
     * An {@link IPropertyValue} with this type can be safely casted to {@link ITableContentUsage}.
     */
    TABLE_STRUCTURE_USAGE(Messages.ProductCmptPropertyType_tableUsage, PropertyValueType.TABLE_CONTENT_USAGE) {

    },

    /**
     * The {@link IProductCmptProperty} is an {@link IProductCmptTypeMethod} of an
     * {@link IProductCmptType} that is marked as <em>formula signature</em>.
     * <p>
     * An {@link IProductCmptProperty} with this type can be safely casted to
     * {@link IProductCmptTypeMethod}.
     * <p>
     * An {@link IPropertyValue} with this type can be safely casted to {@link IFormula}.
     */
    FORMULA_SIGNATURE_DEFINITION(Messages.ProductCmptPropertyType_fomula, PropertyValueType.FORMULA) {

    },

    /**
     * The {@link IProductCmptProperty} is an {@link IPolicyCmptTypeAttribute} of an
     * {@link IPolicyCmptType}.
     * <p>
     * An {@link IProductCmptProperty} with this type can be safely casted to
     * {@link IPolicyCmptTypeAttribute}.
     * <p>
     * An {@link IPropertyValue} with this type can be safely casted to {@link IConfigElement}.
     */
    POLICY_CMPT_TYPE_ATTRIBUTE(Messages.ProductCmptPropertyType_defaultValueAndValueSet,
            PropertyValueType.CONFIG_ELEMENT) {

    },

    /**
     * The {@link IProductCmptProperty} is an {@link IValidationRule} of an {@link IPolicyCmptType}.
     * <p>
     * An {@link IProductCmptProperty} with this type can be safely casted to
     * {@link IValidationRule}.
     * <p>
     * An {@link IPropertyValue} with this type can be safely casted to
     * {@link IValidationRuleConfig}.
     */
    VALIDATION_RULE(Messages.ProductCmptPropertyType_ValidationRule, PropertyValueType.VALIDATION_RULE_CONFIG) {

    };

    private final String name;

    private final PropertyValueType valueType;

    private ProductCmptPropertyType(String name, PropertyValueType valueType) {
        this.name = name;
        this.valueType = valueType;
    }

    /**
     * Returns the name of this property type.
     */
    public String getName() {
        return name;
    }

    public PropertyValueType getValueType() {
        return valueType;
    }

    public boolean isMatchingPropertyValue(String propertyName, IPropertyValue propertyValue) {
        return this == propertyValue.getProductCmptPropertyType()
                && ObjectUtils.equals(propertyName, propertyValue.getPropertyName());
    }

}
