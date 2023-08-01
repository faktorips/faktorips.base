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
import java.util.Calendar;
import java.util.List;

import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.model.annotation.IpsAttribute;
import org.faktorips.runtime.model.annotation.IpsExtensionProperties;

/**
 * Represents an attribute in a IpsProductCmptType.
 */
public class ProductAttribute extends Attribute {

    private final Method getter;

    private final Method setter;

    public ProductAttribute(Type type, boolean changingOverTime, Method getter, Method setter) {
        super(type, getter.getAnnotation(IpsAttribute.class), getter.getAnnotation(IpsExtensionProperties.class),
                getInnermostGenericClass(getter.getGenericReturnType()), changingOverTime, Deprecation.of(getter));
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
    public Object getValue(IProductComponent productComponent, Calendar effectiveDate) {
        return invokeMethod(getter, getRelevantProductObject(productComponent, effectiveDate));
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
    public Boolean isMultiValue() {
        return getter.getReturnType().equals(List.class);
    }

    private static final Class<?> getInnermostGenericClass(java.lang.reflect.Type type) {
        if (type instanceof Class) {
            return (Class<?>)type;
        }
        if (type instanceof ParameterizedType) {
            return getInnermostGenericClass(((ParameterizedType)type).getActualTypeArguments()[0]);
        } else if (type instanceof WildcardType) {
            return getInnermostGenericClass(((WildcardType)type).getUpperBounds()[0]);
        } else {
            throw new IllegalArgumentException("can't find class for " + type.toString());
        }
    }

}
