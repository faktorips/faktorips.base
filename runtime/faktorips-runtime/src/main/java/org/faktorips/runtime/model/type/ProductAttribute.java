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

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.runtime.model.annotation.IpsAttribute;
import org.faktorips.runtime.model.annotation.IpsExtensionProperties;
import org.faktorips.valueset.ValueSet;

/**
 * Represents an attribute in a {@link ProductCmptType}.
 */
public class ProductAttribute extends Attribute {

    public static final String MSGCODE_VALUE_NOT_IN_VALUE_SET = "PRODUCT_ATTRIBUTE-VALUE_NOT_IN_VALUE_SET";

    public static final String MSGKEY_VALUE_NOT_IN_VALUE_SET = "Validation.ValueNotInValueSet";

    public static final String MSGCODE_DUPLICATE_VALUE = "PRODUCT_ATTRIBUTE-DUPLICATE_VALUE";

    public static final String MSGKEY_DUPLICATE_VALUE = "Validation.DuplicateValue";

    public static final String PROPERTY_VALUE = "value";

    private final Method getter;

    private final Method setter;

    public ProductAttribute(Type type, boolean changingOverTime, Method getter, Method setter) {
        super(type, getter.getAnnotation(IpsAttribute.class), getter.getAnnotation(IpsExtensionProperties.class),
                findDatatype(getter), changingOverTime, Deprecation.of(getter));
        this.getter = getter;
        this.setter = setter;
    }

    /**
     * Returns the {@link ProductCmptType} this attribute belongs to.
     */
    @Override
    public ProductCmptType getType() {
        return (ProductCmptType)super.getType();
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated Use {@link #getType()}
     */
    @Deprecated
    @Override
    public ProductCmptType getModelType() {
        return getType();
    }

    @Override
    public boolean isProductRelevant() {
        return true;
    }

    @Override
    public Attribute createOverwritingAttributeFor(Type subType) {
        return new ProductAttribute(subType, isChangingOverTime(), getter, setter);
    }

    /**
     * Returns the value of this attribute in the given product component (or its generation
     * identified by the effectiveDate, if the attribute is changeable over time).
     * <p>
     * It is safe to cast the returned object to the class returned by {@link #getDatatype()},
     * except when the attribute is a {@linkplain #isMultiValue() multi-value} attribute - then a
     * {@link List} is returned (and it's contents can be cast to the class returned by
     * {@link #getDatatype()}).
     *
     * @param productComponent a product component based on the product component type this
     *            attribute belongs to.
     * @param effectiveDate (optional) the date to use for selecting the product component's
     *            generation, if this attribute {@link #isChangingOverTime()}
     */
    @SuppressWarnings("unchecked")
    public <T> T getValue(IProductComponent productComponent, Calendar effectiveDate) {
        return (T)invokeMethod(getter, getRelevantProductObject(productComponent, effectiveDate));
    }

    /**
     * Sets the value of this attribute in the given product component (or its generation identified
     * by the effectiveDate, if the attribute is changeable over time).
     *
     * @param productComponent a product component based on the product component type this
     *            attribute belongs to.
     * @param effectiveDate (optional) the date to use for selecting the product component's
     *            generation, if this attribute {@link #isChangingOverTime()}
     * @param value the new value
     * @since 24.1
     */
    public void setValue(IProductComponent productComponent, Calendar effectiveDate, Object value) {
        invokeMethod(setter, getRelevantProductObject(productComponent, effectiveDate), value);
    }

    /**
     * Whether this attribute has just one value or multiple values. If the attribute has multiple
     * values, {@link #getDatatype()} will still return the class of a single value, but
     * {@link #getValue(IProductComponent, Calendar)} will return a {@link List}.
     */
    public boolean isMultiValue() {
        return getter.getReturnType().equals(List.class);
    }

    @Override
    public void validate(MessageList list,
            IValidationContext context,
            IProductComponent product,
            Calendar effectiveDate) {
        super.validate(list, context, product, effectiveDate);
        validateValue(list, context, product, effectiveDate);
    }

    @SuppressWarnings("unchecked")
    private <T> void validateValue(MessageList list,
            IValidationContext context,
            IProductComponent product,
            Calendar effectiveDate) {
        validate(list, context,
                () -> (T)getValue(product, effectiveDate),
                () -> (ValueSet<T>)getValueSetFromModel(),
                (value, valueSet) -> valueSet.contains(value),
                MSGCODE_VALUE_NOT_IN_VALUE_SET,
                MSGKEY_VALUE_NOT_IN_VALUE_SET,
                PROPERTY_VALUE);
        validate(list, context,
                () -> (T)getValue(product, effectiveDate),
                this::isMultiValue,
                (value, multiValue) -> !multiValue || new HashSet<>((List<T>)value).size() == ((List<T>)value).size(),
                MSGCODE_DUPLICATE_VALUE,
                MSGKEY_DUPLICATE_VALUE,
                PROPERTY_VALUE);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getDefaultValueFromModel() {
        T defaultValue = super.getDefaultValueFromModel();
        if (isMultiValue() && IpsStringUtils.EMPTY.equals(defaultValue)) {
            return (T)Arrays.asList(defaultValue);
        }
        return defaultValue;
    }

    private static final Class<?> findDatatype(Method getter) {
        return getInnermostGenericClass(getter.getGenericReturnType(),
                getter.getAnnotation(IpsAttribute.class).primitive());
    }

    private static final Class<?> getInnermostGenericClass(java.lang.reflect.Type type, boolean primitive) {
        if (type instanceof Class) {
            Class<?> clazz = (Class<?>)type;
            if (primitive && !clazz.isPrimitive()) {
                // multi-value primitive attributes have type List<Wrapper-Type>, so get the
                // primitive type from the wrapper class
                try {
                    clazz = (Class<?>)clazz.getDeclaredField("TYPE").get(null);
                } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException
                        | SecurityException e) {
                    throw new IllegalArgumentException("can't find class for " + type.toString(), e);
                }
            }
            return clazz;
        }
        if (type instanceof ParameterizedType) {
            return getInnermostGenericClass(((ParameterizedType)type).getActualTypeArguments()[0], primitive);
        } else if (type instanceof WildcardType) {
            return getInnermostGenericClass(((WildcardType)type).getUpperBounds()[0], primitive);
        } else {
            throw new IllegalArgumentException("can't find class for " + type.toString());
        }
    }

    @Override
    protected String getResourceBundleName() {
        return ProductAttribute.class.getName();
    }

}
