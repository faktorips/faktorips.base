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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.model.annotation.IpsAttribute;
import org.faktorips.runtime.model.annotation.IpsConfiguredAttribute;
import org.faktorips.runtime.model.annotation.IpsExtensionProperties;

/**
 * A {@link ModelTypeAttribute} represent an attribute from the PolicyCmptType or the
 * ProductCmptType.
 */
public class ModelTypeAttribute extends AbstractModelTypeAttribute {

    private final Method getter;

    private final Method setter;

    public ModelTypeAttribute(ModelType modelType, Method getter, Method setter) {
        super(modelType, getter.getAnnotation(IpsAttribute.class), getter.getAnnotation(IpsExtensionProperties.class),
                getter.getReturnType());
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public boolean isProductRelevant() {
        if (getModelType() instanceof ProductModel) {
            return true;
        } else {
            return getter.isAnnotationPresent(IpsConfiguredAttribute.class);
        }
    }

    @Override
    public Object getValue(IModelObject source) {
        try {
            return getter.invoke(source);
        } catch (IllegalAccessException e) {
            handleGetterError(source, e);
        } catch (InvocationTargetException e) {
            handleGetterError(source, e);
        } catch (SecurityException e) {
            handleGetterError(source, e);
        }
        return null;
    }

    @Override
    public void setValue(IModelObject source, Object value) {
        try {
            if (setter != null) {
                setter.invoke(source, value);
            } else {
                handleSetterError(source, value, null);
            }
        } catch (IllegalArgumentException e) {
            handleSetterError(source, value, e);
        } catch (IllegalAccessException e) {
            handleSetterError(source, value, e);
        } catch (InvocationTargetException e) {
            handleSetterError(source, value, e);
        }
    }

    private void handleSetterError(IModelObject source, Object value, Exception e) {
        throw new IllegalArgumentException(String.format(
                "Could not write attribute %s on source object %s to value %s.", getName(), source, value), e);
    }
}
