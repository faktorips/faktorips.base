/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.runtime.modeltype.internal;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.List;

import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.model.annotation.IpsAttribute;
import org.faktorips.runtime.model.annotation.IpsExtensionProperties;
import org.faktorips.runtime.modeltype.IProductAttributeModel;

public class ProductAttributeModel extends AbstractAttributeModel implements IProductAttributeModel {

    private final Method getter;

    private final Method setter;

    public ProductAttributeModel(ModelType modelType, boolean changingOverTime, Method getter, Method setter) {
        super(modelType, getter.getAnnotation(IpsAttribute.class), getter.getAnnotation(IpsExtensionProperties.class),
                getInnermostGenericClass(getter.getGenericReturnType()), changingOverTime);
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public boolean isProductRelevant() {
        return true;
    }

    @Override
    public AbstractAttributeModel createOverwritingAttributeFor(ModelType subModelType) {
        return new ProductAttributeModel(subModelType, isChangingOverTime(), getter, setter);
    }

    @Override
    public Object getValue(IProductComponent productComponent, Calendar effectiveDate) {
        return invokeMethod(getter, getRelevantProductObject(productComponent, effectiveDate));
    }

    private static final Class<?> getInnermostGenericClass(Type type) {
        if (type instanceof Class) {
            return (Class<?>)type;
        }
        if (type instanceof ParameterizedType) {
            return getInnermostGenericClass(((ParameterizedType)type).getActualTypeArguments()[0]);
        } else {
            throw new IllegalArgumentException("can't find class for " + type.toString());
        }
    }

    @Override
    public Boolean isMultiValue() {
        return getter.getReturnType().equals(List.class);
    }

}
