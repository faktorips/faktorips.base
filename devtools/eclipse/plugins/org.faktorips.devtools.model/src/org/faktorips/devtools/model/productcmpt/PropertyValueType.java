/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.productcmpt;

import java.io.Serializable;
import java.util.Comparator;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.internal.ipsobject.IpsObjectPart;
import org.faktorips.devtools.model.internal.productcmpt.AttributeValue;
import org.faktorips.devtools.model.internal.productcmpt.ConfiguredDefault;
import org.faktorips.devtools.model.internal.productcmpt.ConfiguredValueSet;
import org.faktorips.devtools.model.internal.productcmpt.Formula;
import org.faktorips.devtools.model.internal.productcmpt.TableContentUsage;
import org.faktorips.devtools.model.internal.productcmpt.ValidationRuleConfig;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.type.IProductCmptProperty;
import org.faktorips.devtools.model.type.ProductCmptPropertyType;
import org.faktorips.devtools.model.util.NullSafeComparableComparator;
import org.faktorips.devtools.model.value.IValue;
import org.faktorips.devtools.model.value.ValueFactory;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.runtime.internal.IpsStringUtils;

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
    ATTRIBUTE_VALUE(IAttributeValue.class, AttributeValue.class, IAttributeValue.TAG_NAME) {

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
        public Function<IPropertyValue, Object> getValueGetter() {
            return propertyValue -> {
                if (propertyValue instanceof IAttributeValue attributeValue) {
                    return attributeValue.getValueHolder();
                }
                throw new IllegalArgumentException("Illegal parameter " + propertyValue); //$NON-NLS-1$
            };
        }

        @Override
        public Function<IPropertyValue, Object> getInternalValueGetter() {
            // TODO FIPS-11022
            return getValueGetter();
        }

        @Override
        public ProductCmptPropertyType getCorrespondingPropertyType() {
            return ProductCmptPropertyType.PRODUCT_CMPT_TYPE_ATTRIBUTE;
        }

        @Override
        public BiConsumer<IPropertyValue, Object> getValueSetter() {
            return (propertyValue, value) -> {
                if (value instanceof IValueHolder<?> && propertyValue instanceof IAttributeValue attributeValue) {
                    attributeValue.setValueHolder(((IValueHolder<?>)value).copy(attributeValue));
                } else {
                    throw new IllegalArgumentException();
                }
            };
        }

        @Override
        public boolean isPartOfComposite() {
            return false;
        }

    },

    /**
     * Type for {@link ITableContentUsage}
     * <p>
     * An {@link IPropertyValue} with this type can be safely casted to {@link ITableContentUsage}.
     */
    TABLE_CONTENT_USAGE(ITableContentUsage.class, TableContentUsage.class, ITableContentUsage.TAG_NAME) {

        @Override
        public ITableContentUsage createPropertyValue(IPropertyValueContainer container,
                IProductCmptProperty property,
                String partId) {
            return new TableContentUsage(container, partId, property == null ? "" : property.getPropertyName()); //$NON-NLS-1$
        }

        @Override
        public Function<IPropertyValue, Object> getValueGetter() {
            return propertyValue -> {
                if (propertyValue instanceof ITableContentUsage contentUsage) {
                    return contentUsage.getTableContentName();
                }
                throw new IllegalArgumentException("Illegal parameter " + propertyValue); //$NON-NLS-1$
            };
        }

        @Override
        public Function<IPropertyValue, Object> getInternalValueGetter() {
            // TODO FIPS-11023
            return getValueGetter();
        }

        @Override
        public ProductCmptPropertyType getCorrespondingPropertyType() {
            return ProductCmptPropertyType.TABLE_STRUCTURE_USAGE;
        }

        @Override
        public BiConsumer<IPropertyValue, Object> getValueSetter() {
            return (propertyValue, value) -> {
                if ((value == null || value instanceof String) && propertyValue instanceof ITableContentUsage element) {
                    element.setTableContentName((String)value);
                } else {
                    throw new IllegalArgumentException();
                }
            };
        }

        @Override
        public boolean isPartOfComposite() {
            return false;
        }

    },

    /**
     * Type for {@link IFormula}
     * <p>
     * An {@link IPropertyValue} with this type can be safely casted to {@link IFormula}.
     */
    FORMULA(IFormula.class, Formula.class, IFormula.TAG_NAME) {

        @Override
        public IFormula createPropertyValue(IPropertyValueContainer container,
                IProductCmptProperty property,
                String partId) {
            return new Formula(container, partId, property == null ? "" : property.getPropertyName()); //$NON-NLS-1$
        }

        @Override
        public Function<IPropertyValue, Object> getValueGetter() {
            return propertyValue -> {
                if (propertyValue instanceof IFormula formula) {
                    return formula.getExpression();
                }
                throw new IllegalArgumentException("Illegal parameter " + propertyValue); //$NON-NLS-1$
            };
        }

        @Override
        public Function<IPropertyValue, Object> getInternalValueGetter() {
            // TODO FIPS-11025
            return getValueGetter();
        }

        @Override
        public ProductCmptPropertyType getCorrespondingPropertyType() {
            return ProductCmptPropertyType.FORMULA_SIGNATURE_DEFINITION;
        }

        @Override
        public BiConsumer<IPropertyValue, Object> getValueSetter() {
            return (propertyValue, value) -> {
                if ((value == null || value instanceof String) && propertyValue instanceof IFormula element) {
                    element.setExpression((String)value);
                } else {
                    throw new IllegalArgumentException();
                }
            };
        }

        @Override
        public boolean isPartOfComposite() {
            return false;
        }

    },

    /**
     * Type for {@link IConfiguredValueSet}.
     * <p>
     * An {@link IPropertyValue} with this type can be safely casted to {@link IConfiguredValueSet}.
     */
    CONFIGURED_VALUESET(IConfiguredValueSet.class, ConfiguredValueSet.class, IConfiguredValueSet.TAG_NAME) {

        @Override
        public IConfiguredValueSet createPropertyValue(IPropertyValueContainer container,
                IProductCmptProperty property,
                String partId) {
            String policyAttributeName = property != null ? property.getName() : IpsStringUtils.EMPTY;
            IConfiguredValueSet configuredValueSet = new ConfiguredValueSet(container, policyAttributeName, partId);
            IPolicyCmptTypeAttribute attribute = (IPolicyCmptTypeAttribute)property;
            if (attribute != null) {
                configuredValueSet.setValueSetCopy(attribute.getValueSet());
                if (Datatype.BOOLEAN.getQualifiedName().equals(attribute.getDatatype())
                        || Datatype.PRIMITIVE_BOOLEAN.getQualifiedName().equals(attribute.getDatatype())) {
                    // Special case (FIPS-1344): For boolean values, we only support
                    // enum value sets, because unrestricted value sets do not yield
                    // any benefit.
                    configuredValueSet.convertValueSetToEnumType();
                }
            }
            return configuredValueSet;

        }

        @Override
        public Function<IPropertyValue, Object> getValueGetter() {
            return propertyValue -> {
                if (propertyValue instanceof IConfiguredValueSet configuredValueSet) {
                    return configuredValueSet.getValueSet();
                }
                throw new IllegalArgumentException("Illegal parameter " + propertyValue); //$NON-NLS-1$
            };
        }

        @Override
        public Function<IPropertyValue, Object> getInternalValueGetter() {
            return propertyValue -> {
                if (propertyValue instanceof IConfiguredValueSet configuredValueSet) {
                    return ((ConfiguredValueSet)configuredValueSet).getValueSetInternal();
                }
                throw new IllegalArgumentException("Illegal parameter " + propertyValue); //$NON-NLS-1$
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
            return new ValueSetComparator<>();
        }

        @Override
        public BiConsumer<IPropertyValue, Object> getValueSetter() {
            return (propertyValue, value) -> {
                if (value instanceof IValueSet && propertyValue instanceof IConfiguredValueSet element) {
                    element.setValueSet(((IValueSet)value).copy(element, element.getIpsModel().getNextPartId(element)));
                } else {
                    throw new IllegalArgumentException();
                }
            };
        }

        @Override
        public boolean isPartOfComposite() {
            return true;
        }

        @Override
        protected boolean isProperXmlTagName(String xmlTagName) {
            return super.isProperXmlTagName(xmlTagName) || IConfiguredValueSet.LEGACY_TAG_NAME.equals(xmlTagName);
        }

    },

    /**
     * Type for {@link IConfiguredDefault}.
     * <p>
     * An {@link IPropertyValue} with this type can be safely casted to {@link IConfiguredDefault}.
     */
    CONFIGURED_DEFAULT(IConfiguredDefault.class, ConfiguredDefault.class, IConfiguredDefault.TAG_NAME) {

        @Override
        public IConfiguredDefault createPropertyValue(IPropertyValueContainer container,
                IProductCmptProperty property,
                String partId) {
            String policyAttributeName = property != null ? property.getName() : IpsStringUtils.EMPTY;
            IConfiguredDefault configuredDefault = new ConfiguredDefault(container, policyAttributeName, partId);
            IPolicyCmptTypeAttribute attribute = (IPolicyCmptTypeAttribute)property;
            if (attribute != null) {
                configuredDefault.setValue(attribute.getDefaultValue());
            }
            return configuredDefault;
        }

        @Override
        public Function<IPropertyValue, Object> getValueGetter() {
            return propertyValue -> {
                if (propertyValue instanceof IConfiguredDefault configuredDefault) {
                    return configuredDefault.getValue();
                }
                throw new IllegalArgumentException("Illegal parameter " + propertyValue); //$NON-NLS-1$
            };
        }

        @Override
        public Function<IPropertyValue, Object> getInternalValueGetter() {
            // TODO FIPS-11021
            return getValueGetter();
        }

        @Override
        public ProductCmptPropertyType getCorrespondingPropertyType() {
            return ProductCmptPropertyType.POLICY_CMPT_TYPE_ATTRIBUTE;
        }

        @Override
        public BiConsumer<IPropertyValue, Object> getValueSetter() {
            return (propertyValue, value) -> {
                if ((value == null || value instanceof String) && propertyValue instanceof IConfiguredDefault element) {
                    element.setValue((String)value);
                } else {
                    throw new IllegalArgumentException();
                }
            };
        }

        @Override
        public boolean isPartOfComposite() {
            return true;
        }

        @Override
        protected boolean isProperXmlTagName(String xmlTagName) {
            return super.isProperXmlTagName(xmlTagName) || IConfiguredDefault.LEGACY_TAG_NAME.equals(xmlTagName);
        }

    },

    /**
     * Type for {@link IValidationRuleConfig}.
     * <p>
     * An {@link IPropertyValue} with this type can be safely casted to
     * {@link IValidationRuleConfig}.
     */
    VALIDATION_RULE_CONFIG(IValidationRuleConfig.class, ValidationRuleConfig.class, IValidationRuleConfig.TAG_NAME) {

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
        public Function<IPropertyValue, Object> getValueGetter() {
            return propertyValue -> {
                if (propertyValue instanceof IValidationRuleConfig ruleConfig) {
                    return ruleConfig.isActive();
                }
                throw new IllegalArgumentException("Illegal parameter " + propertyValue); //$NON-NLS-1$
            };
        }

        @Override
        public Function<IPropertyValue, Object> getInternalValueGetter() {
            // TODO FIPS-11024
            return getValueGetter();
        }

        @Override
        public ProductCmptPropertyType getCorrespondingPropertyType() {
            return ProductCmptPropertyType.VALIDATION_RULE;
        }

        @Override
        public BiConsumer<IPropertyValue, Object> getValueSetter() {
            return (propertyValue, value) -> {
                if (value instanceof Boolean && propertyValue instanceof IValidationRuleConfig ruleConfig) {
                    ruleConfig.setActive((Boolean)value);
                } else {
                    throw new IllegalArgumentException();
                }
            };
        }

        @Override
        public boolean isPartOfComposite() {
            return false;
        }

    };

    private final Class<? extends IPropertyValue> interfaceClass;
    private final Class<? extends IpsObjectPart> implClass;
    private final String xmlTag;

    PropertyValueType(Class<? extends IPropertyValue> interfaceClass, Class<? extends IpsObjectPart> implClass,
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

    public abstract Function<IPropertyValue, Object> getValueGetter();

    public abstract BiConsumer<IPropertyValue, Object> getValueSetter();

    /**
     * Returns the getter for the internal value, independent of the template settings.
     *
     * @since 24.1.1
     */
    public abstract Function<IPropertyValue, Object> getInternalValueGetter();

    /**
     * Copies the value from one {@link IPropertyValue} to the other.
     *
     * @since 24.1.1
     */
    public void copyValue(IPropertyValue from, IPropertyValue to) {
        getValueSetter().accept(to, getValueGetter().apply(from));
    }

    public <T> Comparator<T> getValueComparator() {
        return new NullSafeComparableComparator<>();
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
    public Class<? extends IIpsObjectPart> getImplementationClass() {
        return implClass;
    }

    /**
     * Returns the XML tag for {@link IPropertyValue} of this {@link PropertyValueType}.
     */
    public String getValueXmlTagName() {
        return xmlTag;
    }

    @SuppressWarnings("unchecked")
    protected <T extends IIpsObjectPartContainer> T checkContainerType(IIpsObjectPartContainer container,
            Class<T> concreteContainerClass) {
        if (!concreteContainerClass.isInstance(container)) {
            throw new IllegalArgumentException(
                    "The container for " + name() + " must be a " + concreteContainerClass.getSimpleName()); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return (T)container;
    }

    public abstract boolean isPartOfComposite();

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
            if (type.isProperXmlTagName(xmlTagName)) {
                return type;
            }
        }
        return null;
    }

    protected boolean isProperXmlTagName(String xmlTagName) {
        return getValueXmlTagName().equals(xmlTagName);
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
     * @param parent The container that is used as parent object, the created element is
     *            <strong>not</strong> added to it
     * @param productCmptProperty The {@link IProductCmptProperty} that may be set in the new value
     *            if it is not null
     * @param partId The part id of the generated {@link IPropertyValue}
     * @param clazz The class that specifies the type of the created element
     */

    @SuppressWarnings("unchecked")
    public static <T extends IPropertyValue> T createPropertyValue(IPropertyValueContainer parent,
            IProductCmptProperty productCmptProperty,
            String partId,
            Class<T> clazz) {
        return (T)PropertyValueType.getTypeForValueClass(clazz).createPropertyValue(parent,
                productCmptProperty, partId);
    }

    private static class ValueSetComparator<T> implements Comparator<T>, Serializable {
        /**
         * Comment for <code>serialVersionUID</code>
         */
        private static final long serialVersionUID = 1L;

        @Override
        public int compare(T o1, T o2) {
            if (o1 instanceof IValueSet && o2 instanceof IValueSet) {
                return ((IValueSet)o1).compareTo((IValueSet)o2);
            } else {
                throw new IllegalArgumentException("This comparator could only compare two value sets, but got: " //$NON-NLS-1$
                        + o1 + " and " + o2); //$NON-NLS-1$
            }
        }
    }

}
