/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.model.type;

import org.faktorips.devtools.core.internal.model.productcmpt.AttributeValue;
import org.faktorips.devtools.core.internal.model.productcmpt.ConfigElement;
import org.faktorips.devtools.core.internal.model.productcmpt.Formula;
import org.faktorips.devtools.core.internal.model.productcmpt.TableContentUsage;
import org.faktorips.devtools.core.internal.model.productcmpt.ValidationRuleConfig;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValueContainer;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpt.IValidationRuleConfig;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;

/**
 * Enumeration that specifies the different (sub)types of product component properties.
 * 
 * @see IProductCmptProperty#getProductCmptPropertyType()
 * 
 * @author Jan Ortmann
 * @author Stefan Widmaier
 */
public enum ProductCmptPropertyType {

    /**
     * The product component property is an attribute of a product component type.
     * <p>
     * An {@link IProductCmptProperty} with this type can be safely casted to
     * {@link IProductCmptTypeAttribute}.
     * <p>
     * An {@link IPropertyValue} with this type can be safely casted to {@link IAttributeValue}.
     * 
     */
    VALUE(Messages.ProductCmptPropertyType_productAttribute) {
        @Override
        public IPropertyValue createPropertyValue(IPropertyValueContainer parent, String id, String propertyName) {
            return new AttributeValue(parent, id, propertyName);
        }

        @Override
        public Class<? extends IPropertyValue> getValueClass() {
            return IAttributeValue.class;
        }

    },

    /**
     * The product component property is a table structure usage of a product component type.
     * <p>
     * An {@link IProductCmptProperty} with this type can be safely casted to
     * {@link ITableStructureUsage}.
     * <p>
     * An {@link IPropertyValue} with this type can be safely casted to {@link ITableContentUsage}.
     */
    TABLE_CONTENT_USAGE(Messages.ProductCmptPropertyType_tableUsage) {
        @Override
        public IPropertyValue createPropertyValue(IPropertyValueContainer parent, String id, String propertyName) {
            return new TableContentUsage(parent, id, propertyName);
        }

        @Override
        public Class<? extends IPropertyValue> getValueClass() {
            return ITableContentUsage.class;
        }

    },

    /**
     * The product component property is a method of a product component type.
     * <p>
     * An {@link IProductCmptProperty} with this type can be safely casted to
     * {@link IProductCmptTypeMethod}.
     * <p>
     * An {@link IPropertyValue} with this type can be safely casted to {@link IFormula}.
     */
    FORMULA(Messages.ProductCmptPropertyType_fomula) {
        @Override
        public IPropertyValue createPropertyValue(IPropertyValueContainer parent, String id, String propertyName) {
            return new Formula(parent, id, propertyName);
        }

        @Override
        public Class<? extends IPropertyValue> getValueClass() {
            return IFormula.class;
        }

    },

    /**
     * The product component property is an attribute of an policy component type.
     * <p>
     * An {@link IProductCmptProperty} with this type can be safely casted to
     * {@link IPolicyCmptTypeAttribute}.
     * <p>
     * An {@link IPropertyValue} with this type can be safely casted to {@link IConfigElement}.
     */
    DEFAULT_VALUE_AND_VALUESET(Messages.ProductCmptPropertyType_defaultValueAndValueSet) {
        @Override
        public IPropertyValue createPropertyValue(IPropertyValueContainer parent, String id, String propertyName) {
            return new ConfigElement(parent, id, propertyName);
        }

        @Override
        public Class<? extends IPropertyValue> getValueClass() {
            return IConfigElement.class;
        }

    },

    /**
     * The product component property is a validation rule an policy component type.
     * <p>
     * An {@link IProductCmptProperty} with this type can be safely casted to
     * {@link IValidationRule}.
     * <p>
     * An {@link IPropertyValue} with this type can be safely casted to
     * {@link IValidationRuleConfig}.
     */
    VALIDATION_RULE_CONFIG(Messages.ProductCmptPropertyType_ValidationRule) {
        @Override
        public IPropertyValue createPropertyValue(IPropertyValueContainer parent, String id, String propertyName) {
            return new ValidationRuleConfig(parent, id, propertyName);
        }

        @Override
        public Class<? extends IPropertyValue> getValueClass() {
            return IValidationRuleConfig.class;
        }

    };

    private final String name;

    private ProductCmptPropertyType(String name) {
        this.name = name;
    }

    /**
     * Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * Creates a property value for this type of product component property.
     * 
     * @return the newly create {@link IPropertyValue}
     */
    public abstract IPropertyValue createPropertyValue(IPropertyValueContainer parent, String id, String propertyName);

    public abstract Class<? extends IPropertyValue> getValueClass();

}
