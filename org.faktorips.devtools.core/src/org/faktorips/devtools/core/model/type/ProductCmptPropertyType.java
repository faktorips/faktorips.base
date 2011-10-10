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
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
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
    // TODO AW 10-10-2011: Should be renamed to ATTRIBUTE
    VALUE(Messages.ProductCmptPropertyType_productAttribute) {

        @Override
        public IAttributeValue createPropertyValue(IPropertyValueContainer container,
                IProductCmptProperty property,
                String partId) {
            IAttributeValue attrValue = new AttributeValue(container, partId,
                    property == null ? "" : property.getPropertyName()); //$NON-NLS-1$
            IProductCmptTypeAttribute attr = (IProductCmptTypeAttribute)property;
            attrValue.setValue(attr != null ? attr.getDefaultValue() : ""); //$NON-NLS-1$
            return attrValue;
        }

        @Override
        public Class<? extends IPropertyValue> getValueClass() {
            return IAttributeValue.class;
        }

        @Override
        public String getXmlTagName() {
            return AttributeValue.TAG_NAME;
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
        public ITableContentUsage createPropertyValue(IPropertyValueContainer container,
                IProductCmptProperty property,
                String partId) {
            ITableContentUsage tableUsage = new TableContentUsage(container, partId,
                    property == null ? "" : property.getPropertyName()); //$NON-NLS-1$
            return tableUsage;
        }

        @Override
        public Class<? extends IPropertyValue> getValueClass() {
            return ITableContentUsage.class;
        }

        @Override
        public String getXmlTagName() {
            return TableContentUsage.TAG_NAME;
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
        public IFormula createPropertyValue(IPropertyValueContainer container,
                IProductCmptProperty property,
                String partId) {
            IFormula formula = new Formula(container, partId, property == null ? "" : property.getPropertyName()); //$NON-NLS-1$
            return formula;
        }

        @Override
        public Class<? extends IPropertyValue> getValueClass() {
            return IFormula.class;
        }

        @Override
        public String getXmlTagName() {
            return Formula.TAG_NAME;
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
    // TODO AW 10-10-2011: Should be renamed to CONFIG_ELEMENT
    DEFAULT_VALUE_AND_VALUESET(Messages.ProductCmptPropertyType_defaultValueAndValueSet) {

        @Override
        public IConfigElement createPropertyValue(IPropertyValueContainer container,
                IProductCmptProperty property,
                String partId) {
            IConfigElement configElement = new ConfigElement(container, partId,
                    property == null ? "" : property.getPropertyName()); //$NON-NLS-1$
            IPolicyCmptTypeAttribute attribute = (IPolicyCmptTypeAttribute)property;
            if (attribute != null) {
                configElement.setPolicyCmptTypeAttribute(attribute.getName());
                configElement.setValue(attribute.getDefaultValue());
                configElement.setValueSetCopy(attribute.getValueSet());
            }
            return configElement;
        }

        @Override
        public Class<? extends IPropertyValue> getValueClass() {
            return IConfigElement.class;
        }

        @Override
        public String getXmlTagName() {
            return ConfigElement.TAG_NAME;
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
        public IValidationRuleConfig createPropertyValue(IPropertyValueContainer container,
                IProductCmptProperty property,
                String partId) {
            IValidationRuleConfig ruleConfig = new ValidationRuleConfig(container, partId,
                    property == null ? "" : property.getPropertyName()); //$NON-NLS-1$
            IValidationRule rule = (IValidationRule)property;
            if (rule != null) {
                ruleConfig.setActive(rule.isActivatedByDefault());
            }
            return ruleConfig;
        }

        @Override
        public Class<? extends IPropertyValue> getValueClass() {
            return IValidationRuleConfig.class;
        }

        @Override
        public String getXmlTagName() {
            return ValidationRuleConfig.TAG_NAME;
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
     * Creates a property value for this type of product component property. If you have the
     * concrete class of the {@link IPropertyValue} you want to create use the typesafe method
     * {@link #createPropertyValue(IPropertyValueContainer, IProductCmptProperty, String, Class)}
     * 
     * @param container the {@link IPropertyValueContainer} the new part is created for
     * @param property the {@link IProductCmptProperty} a new value is created for
     * @param partId the new parts's id
     * @return the newly created {@link IPropertyValue}
     */
    public abstract IPropertyValue createPropertyValue(IPropertyValueContainer container,
            IProductCmptProperty property,
            String partId);

    /**
     * @return the class of the {@link IPropertyValue} represented by this
     *         {@link ProductCmptPropertyType}.
     */
    public abstract Class<? extends IPropertyValue> getValueClass();

    /**
     * 
     * @return the XML tag for {@link IPropertyValue} of this {@link ProductCmptPropertyType}.
     */
    public abstract String getXmlTagName();

    /**
     * Searches a {@link ProductCmptPropertyType} that can create ips object parts for the given
     * class. This method also takes subclasses into account. E.g. calling this method with
     * {@link AttributeValue}.class will return VALUE even though its value class is
     * {@link IAttributeValue}.
     * <p>
     * However, if the given class is not part of the {@link IPropertyValue} hierarchy this method
     * returns <code>null</code>.
     * 
     * @param partType the class a {@link ProductCmptPropertyType} is searched for
     * @return the {@link ProductCmptPropertyType} that can create ips object parts for the given
     *         class, or <code>null</code> if none can be found.
     */
    public static ProductCmptPropertyType getTypeForValueClass(Class<? extends IIpsObjectPart> partType) {
        for (ProductCmptPropertyType type : values()) {
            if (type.getValueClass().isAssignableFrom(partType)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Searches a {@link ProductCmptPropertyType} that can create ips object parts for the given XML
     * tag name.
     * 
     * @param xmlTagName the xml tag name a {@link ProductCmptPropertyType} is searched for
     * @return the {@link ProductCmptPropertyType} that can create ips object parts for the given
     *         XML tag, or <code>null</code> if none can be found.
     */
    public static ProductCmptPropertyType getTypeForXmlTag(String xmlTagName) {
        for (ProductCmptPropertyType type : values()) {
            if (type.getXmlTagName().equals(xmlTagName)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Crating a concrete {@link IPropertyValue} of the given type. This method does NOT add the
     * created property value to the given container but only using this information for setting the
     * parent! The parameter productCmptProperty may be null. If the parameter is null, you have to
     * specify the concrete type to be created by setting the correct type parameter. If
     * productCmptProperty is not null the concrete type is getting from this parameter and the
     * caller have to ensure that the given type is the same as the type getting from the
     * productCmptPropertys type.
     * 
     * @param container The container that is used as parent object, the created element is NOT
     *            added to it.
     * @param productCmptProperty the {@link IProductCmptProperty} that may be set in the new value
     *            if it is not null
     * @param partId the partId of the generated {@link IPropertyValue}
     * @param type The class that specifies the type of the created element
     * @return the created element of the given type setup with the given parameter
     */
    public static <T extends IPropertyValue> T createPropertyValue(IPropertyValueContainer container,
            IProductCmptProperty productCmptProperty,
            String partId,
            Class<T> type) {

        @SuppressWarnings("unchecked")
        // The enum could not be specialized with generics but the implementation is type safe
        T propertyValue = (T)ProductCmptPropertyType.getTypeForValueClass(type).createPropertyValue(container,
                productCmptProperty, partId);
        return propertyValue;
    }
}
