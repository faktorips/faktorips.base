/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.productcmpt;

import java.io.Serializable;
import java.util.Comparator;

import com.google.common.base.Function;

import org.apache.commons.lang.ObjectUtils;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPart;
import org.faktorips.devtools.core.internal.model.productcmpt.AttributeValue;
import org.faktorips.devtools.core.internal.model.productcmpt.ConfigElement;
import org.faktorips.devtools.core.internal.model.productcmpt.Formula;
import org.faktorips.devtools.core.internal.model.productcmpt.TableContentUsage;
import org.faktorips.devtools.core.internal.model.productcmpt.ValidationRuleConfig;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;
import org.faktorips.devtools.core.model.value.IValue;
import org.faktorips.devtools.core.model.value.ValueFactory;
import org.faktorips.devtools.core.model.valueset.IValueSet;

/**
 * Specifies the different types of {@link IPropertyValue}.
 * <p>
 * This enum contains methods providing information about the relationship of product component
 * properties to their corresponding property values. It also provides a convenient way to create
 * property values for given product component properties.
 * 
 * @see IPropertyValue
 */
public enum PropertyValueType {

    /**
     * Type for {@link IAttributeValue}.
     * <p>
     * An {@link IPropertyValue} with this {@link PropertyValueType} can be safely casted to
     * {@link IAttributeValue}.
     */
    ATTRIBUTE_VALUE(IAttributeValue.class, AttributeValue.class, AttributeValue.TAG_NAME) {

        @Override
        public IAttributeValue createPropertyValue(IPropertyValueContainer container,
                IProductCmptProperty property,
                String partId) {

            AttributeValue attributeValue = new AttributeValue(container, partId,
                    property == null ? "" : property.getPropertyName()); //$NON-NLS-1$
            IProductCmptTypeAttribute attribute = (IProductCmptTypeAttribute)property;

            final IValue<?> defaultValue;
            if (attribute != null) {
                defaultValue = ValueFactory.createValue(attribute.isMultilingual(), attribute.getDefaultValue());
            } else {
                defaultValue = ValueFactory.createStringValue(null);
            }
            AttributeValueType attributeValueType = AttributeValueType.getTypeFor(attribute);
            IValueHolder<?> valueHolder = attributeValueType.newHolderInstance(attributeValue, defaultValue);
            attributeValue.setValueHolderInternal(valueHolder);
            return attributeValue;
        }

        @Override
        public Function<IPropertyValue, Object> getValueFunction() {
            return new Function<IPropertyValue, Object>() {

                @Override
                public Object apply(IPropertyValue propertyValue) {
                    if (propertyValue instanceof IAttributeValue) {
                        IAttributeValue attributeValue = (IAttributeValue)propertyValue;
                        return attributeValue.getValueHolder();
                    }
                    throw new IllegalArgumentException("Illegal parameter " + propertyValue); //$NON-NLS-1$
                }
            };
        }

        @Override
        public ProductCmptPropertyType getCorrespondingPropertyType() {
            return ProductCmptPropertyType.PRODUCT_CMPT_TYPE_ATTRIBUTE;
        }

    },

    /**
     * Type for {@link ITableContentUsage}
     * <p>
     * An {@link IPropertyValue} with this type can be safely casted to {@link ITableContentUsage}.
     */
    TABLE_CONTENT_USAGE(ITableContentUsage.class, TableContentUsage.class, TableContentUsage.TAG_NAME) {

        @Override
        public ITableContentUsage createPropertyValue(IPropertyValueContainer container,
                IProductCmptProperty property,
                String partId) {

            ITableContentUsage tableUsage = new TableContentUsage(container, partId,
                    property == null ? "" : property.getPropertyName()); //$NON-NLS-1$
            return tableUsage;
        }

        @Override
        public Function<IPropertyValue, Object> getValueFunction() {
            return new Function<IPropertyValue, Object>() {

                @Override
                public Object apply(IPropertyValue propertyValue) {
                    if (propertyValue instanceof ITableContentUsage) {
                        ITableContentUsage contentUsage = (ITableContentUsage)propertyValue;
                        return contentUsage.getTableContentName();
                    }
                    throw new IllegalArgumentException("Illegal parameter " + propertyValue); //$NON-NLS-1$
                }
            };
        }

        @Override
        public ProductCmptPropertyType getCorrespondingPropertyType() {
            return ProductCmptPropertyType.TABLE_STRUCTURE_USAGE;
        }

    },

    /**
     * Type for {@link IFormula}
     * <p>
     * An {@link IPropertyValue} with this type can be safely casted to {@link IFormula}.
     */
    FORMULA(IFormula.class, Formula.class, Formula.TAG_NAME) {

        @Override
        public IFormula createPropertyValue(IPropertyValueContainer container,
                IProductCmptProperty property,
                String partId) {

            IFormula formula = new Formula(container, partId, property == null ? "" : property.getPropertyName()); //$NON-NLS-1$
            return formula;
        }

        @Override
        public Function<IPropertyValue, Object> getValueFunction() {
            return new Function<IPropertyValue, Object>() {

                @Override
                public Object apply(IPropertyValue propertyValue) {
                    if (propertyValue instanceof IFormula) {
                        IFormula formula = (IFormula)propertyValue;
                        return formula.getExpression();
                    }
                    throw new IllegalArgumentException("Illegal parameter " + propertyValue); //$NON-NLS-1$
                }
            };
        }

        @Override
        public ProductCmptPropertyType getCorrespondingPropertyType() {
            return ProductCmptPropertyType.FORMULA_SIGNATURE_DEFINITION;
        }

    },

    /**
     * Type for {@link IConfigElement}.
     * <p>
     * An {@link IPropertyValue} with this type can be safely casted to {@link IConfigElement}.
     */
    CONFIG_ELEMENT(IConfigElement.class, ConfigElement.class, ConfigElement.TAG_NAME) {

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
                if (Datatype.BOOLEAN.getQualifiedName().equals(attribute.getDatatype())
                        || Datatype.PRIMITIVE_BOOLEAN.getQualifiedName().equals(attribute.getDatatype())) {
                    // Special case (FIPS-1344): For boolean values, we only support
                    // enum value sets, because unrestricted value sets do not yield
                    // any benefit.
                    configElement.convertValueSetToEnumType();
                }
            }
            return configElement;
        }

        @Override
        public Function<IPropertyValue, Object> getValueFunction() {
            return new Function<IPropertyValue, Object>() {

                @Override
                public Object apply(IPropertyValue propertyValue) {
                    // TODO FIPS-4556 at the moment only the value set is supplied
                    if (propertyValue instanceof IConfigElement) {
                        IConfigElement configElement = (IConfigElement)propertyValue;
                        return configElement.getValueSet();
                    }
                    throw new IllegalArgumentException("Illegal parameter " + propertyValue); //$NON-NLS-1$
                }
            };
        }

        @Override
        public ProductCmptPropertyType getCorrespondingPropertyType() {
            return ProductCmptPropertyType.POLICY_CMPT_TYPE_ATTRIBUTE;
        }

        /**
         * Creates a {@link Comparator} that compares two value set by the following rules:
         * 
         * Comparing two value sets by checking whether one valueset is contained in the other. If
         * both value set are contained in each other they are equal (0). If the first value set
         * contains the second one the comparator assumes that the first one is greater and will
         * return 1 if the second contains the first one it will return -1.
         */
        @Override
        public <T> Comparator<T> getValueComparator() {
            return new ValueSetComparator<T>();
        }

    },

    /**
     * Type for {@link IValidationRuleConfig}.
     * <p>
     * An {@link IPropertyValue} with this type can be safely casted to
     * {@link IValidationRuleConfig}.
     */
    VALIDATION_RULE_CONFIG(IValidationRuleConfig.class, ValidationRuleConfig.class, ValidationRuleConfig.TAG_NAME) {

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
        public Function<IPropertyValue, Object> getValueFunction() {
            return new Function<IPropertyValue, Object>() {

                @Override
                public Object apply(IPropertyValue propertyValue) {
                    if (propertyValue instanceof IValidationRuleConfig) {
                        IValidationRuleConfig ruleConfig = (IValidationRuleConfig)propertyValue;
                        return ruleConfig.isActive();
                    }
                    throw new IllegalArgumentException("Illegal parameter " + propertyValue); //$NON-NLS-1$
                }
            };
        }

        @Override
        public ProductCmptPropertyType getCorrespondingPropertyType() {
            return ProductCmptPropertyType.VALIDATION_RULE;
        }

    };

    private final Class<? extends IPropertyValue> interfaceClass;
    private final Class<? extends IpsObjectPart> implClass;
    private final String xmlTag;

    private PropertyValueType(Class<? extends IPropertyValue> interfaceClass, Class<? extends IpsObjectPart> implClass,
            String xmlTag) {
        this.interfaceClass = interfaceClass;
        this.implClass = implClass;
        this.xmlTag = xmlTag;
    }

    /**
     * Creates an {@link IPropertyValue} for this property type.
     * <p>
     * If you have the concrete class of the {@link IPropertyValue} you want to create, use the
     * typesafe method
     * {@link #createPropertyValue(IPropertyValueContainer, IProductCmptProperty, String, Class)}
     * 
     * @param container The {@link IPropertyValueContainer} the new part is created for
     * @param property The {@link IProductCmptProperty} a new value is created for
     * @param partId The new parts's id
     */
    public abstract IPropertyValue createPropertyValue(IPropertyValueContainer container,
            IProductCmptProperty property,
            String partId);

    public abstract Function<IPropertyValue, Object> getValueFunction();

    public <T> Comparator<T> getValueComparator() {
        return new NullSafeComparableComparator<T>();
    }

    /**
     * Returns the corresponding {@link ProductCmptPropertyType} to this {@link PropertyValueType}.
     */
    /*
     * Implementation detail: do not provide this property as field because ProductCmptPropertyType
     * also references this enum and it my be not fully initialized when calling the constructor
     */
    public abstract ProductCmptPropertyType getCorrespondingPropertyType();

    /**
     * Returns the class of the {@link IPropertyValue} represented by this {@link PropertyValueType}
     * .
     */
    public Class<? extends IPropertyValue> getInterfaceClass() {
        return interfaceClass;
    }

    /**
     * Returns the class of the default implementation corresponding to the {@link IPropertyValue}
     * represented by this {@link PropertyValueType}.
     */
    public Class<? extends IpsObjectPart> getImplementationClass() {
        return implClass;
    }

    /**
     * Returns the XML tag for {@link IPropertyValue} of this {@link PropertyValueType}.
     */
    public String getValueXmlTagName() {
        return xmlTag;
    }

    /**
     * Searches and returns a {@link PropertyValueType} that can create IPS object parts for the
     * given class.
     * <p>
     * This method also takes subclasses into account. For example, calling this method with
     * {@code AttributeValue.class} will return {@link #ATTRIBUTE_VALUE} even though it's interface
     * is {@link IAttributeValue}.
     * <p>
     * However, if the given class is not part of the {@link IPropertyValue} hierarchy, this method
     * returns null.
     * 
     * @param partType The class a {@link PropertyValueType} is searched for
     */
    public static PropertyValueType getTypeForValueClass(Class<? extends IIpsObjectPart> partType) {
        for (PropertyValueType type : values()) {
            if (type.getInterfaceClass().isAssignableFrom(partType)) {
                return type;
            }
        }
        throw new IllegalArgumentException("The class " + partType + " does not match any valid PropertyValueType"); //$NON-NLS-1$//$NON-NLS-2$
    }

    /**
     * Searches and returns a {@link PropertyValueType} that can create IPS object parts for the
     * given XML tag name.
     * <p>
     * Returns null if no appropriate property type is found.
     * 
     * @param xmlTagName The XML tag name a {@link PropertyValueType} is searched for
     */
    public static PropertyValueType getTypeForXmlTag(String xmlTagName) {
        for (PropertyValueType type : values()) {
            if (type.getValueXmlTagName().equals(xmlTagName)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Creates and returns a concrete {@link IPropertyValue} of the given property type.
     * <p>
     * This method does <strong>not</strong> add the created property value to the given container
     * but only using this information for setting the parent!
     * <p>
     * If the given {@link IProductCmptProperty} parameter is null, you have to specify the concrete
     * type to be created by setting the correct type parameter. If the parameter is not null, the
     * concrete type is obtained from this parameter and the caller has to ensure that the given
     * type is the same as the type obtained from the {@link IProductCmptProperty}'s type.
     * 
     * @param container The container that is used as parent object, the created element is
     *            <strong>not</strong> added to it
     * @param productCmptProperty The {@link IProductCmptProperty} that may be set in the new value
     *            if it is not null
     * @param partId The part id of the generated {@link IPropertyValue}
     * @param type The class that specifies the type of the created element
     */
    public static <T extends IPropertyValue> T createPropertyValue(IPropertyValueContainer container,
            IProductCmptProperty productCmptProperty,
            String partId,
            Class<T> type) {

        @SuppressWarnings("unchecked")
        // The enum could not be specialized with generics but the implementation is type safe
        T propertyValue = (T)PropertyValueType.getTypeForValueClass(type).createPropertyValue(container,
                productCmptProperty, partId);
        return propertyValue;
    }

    private static class ValueSetComparator<T> implements Comparator<T>, Serializable {
        /**
         * Comment for <code>serialVersionUID</code>
         */
        private static final long serialVersionUID = 1L;

        @Override
        public int compare(T o1, T o2) {
            if (o1 instanceof IValueSet && o2 instanceof IValueSet) {
                IValueSet valueSet1 = (IValueSet)o1;
                IValueSet valueSet2 = (IValueSet)o2;
                if (valueSet1.equals(valueSet2)) {
                    return 0;
                }
                boolean v1ContainsV2 = valueSet1.containsValueSet(valueSet2);
                boolean v2ContainsV1 = valueSet2.containsValueSet(valueSet1);
                if (v1ContainsV2 && v2ContainsV1) {
                    return 0;
                } else if (v1ContainsV2) {
                    return 1;
                } else {
                    return -1;
                }
            }
            throw new IllegalArgumentException("This comparator could only compare two value sets, but got: " //$NON-NLS-1$
                    + o1 + " and " + o2); //$NON-NLS-1$
        }
    }

    /**
     * Comparator that for objects of type T that assumes T to implement {@code Comparable<T>} and
     * allows objects to be null.
     */
    private static class NullSafeComparableComparator<T> implements Comparator<T>, Serializable {

        private static final long serialVersionUID = 1L;

        @Override
        @SuppressWarnings("unchecked")
        public int compare(T o1, T o2) {
            Comparable<T> c1 = (Comparable<T>)o1;
            Comparable<T> c2 = (Comparable<T>)o2;
            return ObjectUtils.compare(c1, c2);
        }

    }

}
