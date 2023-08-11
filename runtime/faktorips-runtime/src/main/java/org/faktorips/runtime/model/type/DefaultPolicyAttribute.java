/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.model.type;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentLinkSource;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.runtime.model.IpsModel;
import org.faktorips.runtime.model.annotation.IpsAllowedValues;
import org.faktorips.runtime.model.annotation.IpsAllowedValuesSetter;
import org.faktorips.runtime.model.annotation.IpsAttribute;
import org.faktorips.runtime.model.annotation.IpsConfiguredAttribute;
import org.faktorips.runtime.model.annotation.IpsDefaultValue;
import org.faktorips.runtime.model.annotation.IpsDefaultValueSetter;
import org.faktorips.runtime.model.annotation.IpsExtensionProperties;
import org.faktorips.values.Decimal;
import org.faktorips.values.Money;
import org.faktorips.values.ObjectUtil;
import org.faktorips.valueset.OrderedValueSet;
import org.faktorips.valueset.UnrestrictedValueSet;
import org.faktorips.valueset.ValueSet;

public class DefaultPolicyAttribute extends PolicyAttribute {

    public static final String MSGCODE_DEFAULT_VALUE_NOT_IN_VALUE_SET = "POLICY_ATTRIBUTE-DEFAULT_VALUE_NOT_IN_VALUE_SET";

    public static final String PROPERTY_DEFAULT_VALUE = "defaultValue";

    private static final Map<Class<?>, Object> NULL_OBJECTS = new HashMap<>();

    private static final String RESOURCE_BUNDLE_NAME = DefaultPolicyAttribute.class.getName();
    private static final String MSG_KEY_DEFAULT_VALUE_NOT_IN_VALUE_SET = "Validation.DefaultValueNotInValueSet";

    static {
        NULL_OBJECTS.put(Decimal.class, Decimal.NULL);
        NULL_OBJECTS.put(Money.class, Money.NULL);
        NULL_OBJECTS.put(String.class, IpsStringUtils.EMPTY);
    }

    private final Method getter;

    private final Method setter;

    private Method defaultValueGetter;
    private Method defaultValueSetter;
    private Field defaultField;

    private Map<Type, Method> valueSetMethods = new HashMap<>(2);
    private Map<Type, Field> valueSetFields = new HashMap<>(2);
    private Method allowedValuesSetter;

    public DefaultPolicyAttribute(PolicyCmptType policyCmptType, Method getter, Method setter,
            boolean changingOverTime) {
        super(policyCmptType, getter.getAnnotation(IpsAttribute.class),
                getter.getAnnotation(IpsExtensionProperties.class), getter.getReturnType(), changingOverTime,
                Deprecation.of(getter));
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public boolean isProductRelevant() {
        return getter.isAnnotationPresent(IpsConfiguredAttribute.class);
    }

    @Override
    public Object getValue(IModelObject modelObject) {
        return invokeMethod(getter, modelObject);
    }

    @Override
    public void setValue(IModelObject modelObject, Object value) {
        if (setter == null) {
            if (isOverriding()) {
                getSuperAttribute().setValue(modelObject, value);
            } else {
                throw new IllegalArgumentException(String.format("There is no setter for attribute %s in type %s.",
                        getName(), getType().getName()));
            }
        } else {
            invokeMethod(setter, modelObject, value);
        }
    }

    @Override
    public Object getDefaultValue(IModelObject modelObject) {
        if (!isProductRelevant()) {
            return invokeField(getDefaultValueField(), modelObject);
        } else {
            IConfigurableModelObject configurableModelObject = (IConfigurableModelObject)modelObject;
            return getDefaultValue(configurableModelObject.getProductComponent(),
                    configurableModelObject.getEffectiveFromAsCalendar());
        }
    }

    private Field getDefaultValueField() {
        if (defaultField == null) {
            defaultField = findDefaultValueField(getType());
        }
        return defaultField;
    }

    private Field findDefaultValueField(Type type) {
        return type.findDeclaredField(IpsDefaultValue.class, a -> a.value().equals(getName()))
                .orElseThrow(() -> new IllegalStateException(
                        "No field found for retrieving the default value of attribute: " + getType().getName()
                                + '.' + getName()));
    }

    @Override
    public Object getDefaultValue(IProductComponent source, Calendar effectiveDate) {
        if (!isProductRelevant()) {
            throw new IllegalStateException(
                    "Trying to find default value method in product class, but policy attribute " + getType().getName()
                            + '.' + getName() + " is not configurable.");
        }
        return invokeMethod(getDefaultValueGetter(getType().getProductCmptType()),
                getRelevantProductObject(source, effectiveDate));
    }

    private Method getDefaultValueGetter(Type type) {
        if (defaultValueGetter == null) {
            defaultValueGetter = findDefaultValueGetter(type);
        }
        return defaultValueGetter;
    }

    private Method findDefaultValueGetter(Type type) {
        return type.findDeclaredMethod(IpsDefaultValue.class, a -> a.value().equals(getName()))
                .orElseThrow(() -> new IllegalStateException(
                        "No method found for retrieving the default value of attribute: " + getType().getName()
                                + '.' + getName()));
    }

    @Override
    public void setDefaultValue(IConfigurableModelObject modelObject, Object defaultValue) {
        setDefaultValue(modelObject.getProductComponent(), modelObject.getEffectiveFromAsCalendar(),
                defaultValue);
    }

    @Override
    public void setDefaultValue(IProductComponent source, Calendar effectiveDate, Object defaultValue) {
        if (!isProductRelevant()) {
            throw new IllegalStateException(
                    "Trying to find default value method in product class, but policy attribute " + getType().getName()
                            + '.' + getName() + " is not configurable.");
        }
        invokeMethod(getDefaultValueSetter(getType().getProductCmptType()),
                getRelevantProductObject(source, effectiveDate), defaultValue);
    }

    private Method getDefaultValueSetter(Type type) {
        if (defaultValueSetter == null) {
            defaultValueSetter = findDefaultValueSetter(type);
        }
        return defaultValueSetter;
    }

    private Method findDefaultValueSetter(Type type) {
        Method method = type.searchDeclaredMethod(IpsDefaultValueSetter.class, a -> a.value().equals(getName()));
        if (method == null) {
            throw new IllegalStateException(
                    "No method found for setting the default value of attribute: " + getName());
        }
        return method;
    }

    @Override
    public ValueSet<?> getValueSet(IModelObject modelObject, IValidationContext context) {
        Method valueSetMethod = getValueSetMethod(getType());

        return getValueSet(valueSetMethod, modelObject, context);
    }

    @Override
    public ValueSet<?> getValueSet(IProductComponent source, Calendar effectiveDate, IValidationContext context) {
        Method valueSetMethod = getValueSetMethod(getType().getProductCmptType());
        Object productObject = getRelevantProductObject(source, effectiveDate);
        return getValueSet(valueSetMethod, productObject, context);
    }

    @Override
    public ValueSet<?> getValueSetFromModel() {
        Field valueSetField = getValueSetField(getType());
        try {
            return valueSetField == null ? new UnrestrictedValueSet<>() : (ValueSet<?>)valueSetField.get(null);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException("The value can not be retrieved.");
        }
    }

    // CSOFF: CyclomaticComplexity
    private ValueSet<?> getValueSet(Method valueSetMethod, Object object, IValidationContext context) {
        if (valueSetMethod == null) {
            if (Boolean.class.equals(getDatatype()) || boolean.class.equals(getDatatype())) {
                return new OrderedValueSet<>(!getDatatype().isPrimitive(), null, Boolean.TRUE, Boolean.FALSE);
            }
            if (getDatatype().isEnum()) {
                return new OrderedValueSet<>(true, null, getDatatype().getEnumConstants());
            }
            if (IpsModel.isEnumType(getDatatype()) && IpsModel.getEnumType(getDatatype()).isExtensible()) {
                if (object instanceof IProductComponentLinkSource) {
                    IRuntimeRepository repository = ((IProductComponentLinkSource)object).getRepository();
                    return new OrderedValueSet<>(repository.getEnumValues(getDatatype()), true, null);

                }
                if (object instanceof IConfigurableModelObject) {
                    IProductComponent productComponent = ((IConfigurableModelObject)object).getProductComponent();
                    if (productComponent != null) {
                        IRuntimeRepository repository = productComponent.getRepository();
                        return new OrderedValueSet<>(repository.getEnumValues(getDatatype()), true, null);
                    }
                }
            }
            return new UnrestrictedValueSet<>(!getDatatype().isPrimitive());
        } else if (valueSetMethod.getParameterTypes().length == 0) {
            return (ValueSet<?>)invokeMethod(valueSetMethod, object);
        } else if (valueSetMethod.getParameterTypes().length == 1) {
            return (ValueSet<?>)invokeMethod(valueSetMethod, object, context);
        } else {
            throw new IllegalStateException("The method for retrieving the allowed values of attribute: " + getName()
                    + " has too many arguments: " + valueSetMethod);
        }
    }
    // CSON: CyclomaticComplexity

    private Method getValueSetMethod(Type model) {
        if (valueSetMethods.containsKey(model)) {
            return valueSetMethods.get(model);
        } else {
            Method valueSetMethod = findValueSetMethod(model);
            valueSetMethods.put(model, valueSetMethod);
            return valueSetMethod;
        }
    }

    private Field getValueSetField(Type model) {
        if (valueSetFields.containsKey(model)) {
            return valueSetFields.get(model);
        } else {
            Field valueSetField = findValueSetField(model);
            valueSetFields.put(model, valueSetField);
            return valueSetField;
        }
    }

    @Override
    public DefaultPolicyAttribute createOverwritingAttributeFor(Type subType) {
        return new DefaultPolicyAttribute((PolicyCmptType)subType, getter, setter, isChangingOverTime());
    }

    private Method findValueSetMethod(Type type) {
        return type.searchDeclaredMethod(IpsAllowedValues.class, a -> a.value().equals(getName()));
    }

    private Field findValueSetField(Type type) {
        return type.findDeclaredField(IpsAllowedValues.class, a -> a.value().equals(getName())).orElse(null);
    }

    @Override
    public void setValueSet(IConfigurableModelObject modelObject, ValueSet<?> valueSet) {
        setValueSet(modelObject.getProductComponent(), modelObject.getEffectiveFromAsCalendar(), valueSet);
    }

    @Override
    public void setValueSet(IProductComponent source, Calendar effectiveDate, ValueSet<?> valueSet) {
        if (!isProductRelevant()) {
            throw new IllegalStateException(
                    "Trying to find setter method for allowed values in product class, but policy attribute "
                            + getType().getName() + '.' + getName() + " is not configurable.");
        }
        invokeMethod(getAllowedValuesSetter(getType().getProductCmptType()),
                getRelevantProductObject(source, effectiveDate), valueSet);
    }

    private Method getAllowedValuesSetter(Type type) {
        if (allowedValuesSetter == null) {
            allowedValuesSetter = findAllowedValuesSetter(type);
        }
        return allowedValuesSetter;
    }

    private Method findAllowedValuesSetter(Type type) {
        Method method = type.searchDeclaredMethod(IpsAllowedValuesSetter.class, a -> a.value().equals(getName()));
        if (method == null) {
            throw new IllegalStateException(
                    "No method found for setting the allowed values of attribute: " + getName());
        }
        return method;
    }

    @Override
    public void removeValue(IModelObject modelObject) {
        setValue(modelObject, NULL_OBJECTS.get(getDatatype()));
    }

    @Override
    public void validate(MessageList list,
            IValidationContext context,
            IProductComponent product,
            Calendar effectiveDate) {
        super.validate(list, context, product, effectiveDate);
        validateDefaultValue(list, context, product, effectiveDate);
    }

    <T> void validateDefaultValue(MessageList list,
            IValidationContext context,
            IProductComponent source,
            Calendar effectiveDate) {
        @SuppressWarnings("unchecked")
        T defaultValue = (T)getDefaultValue(source, effectiveDate);

        if (!ObjectUtil.isNull(defaultValue)) {
            @SuppressWarnings("unchecked")
            ValueSet<T> valueSet = (ValueSet<T>)getValueSet(source, effectiveDate, context);
            if (!valueSet.contains(defaultValue)) {
                Locale locale = context.getLocale();
                ResourceBundle messages = ResourceBundle.getBundle(RESOURCE_BUNDLE_NAME, locale);
                list.newError(MSGCODE_DEFAULT_VALUE_NOT_IN_VALUE_SET,
                        String.format(messages.getString(MSG_KEY_DEFAULT_VALUE_NOT_IN_VALUE_SET),
                                defaultValue, getLabel(locale), valueSet),
                        this,
                        PROPERTY_DEFAULT_VALUE);
            }
        }

    }

}
