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

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.modeltype.IModelType;
import org.faktorips.runtime.modeltype.IModelTypeAttribute;

/**
 * A {@link ModelTypeAttribute} represent an attribute from the PolicyCmptType or the
 * ProductCmptType.
 */
public class ModelTypeAttribute extends AbstractModelElement implements IModelTypeAttribute {

    private ModelType modelType;

    private Class<?> datatype;

    private ValueSetType valueSetType = ValueSetType.AllValues;

    private AttributeType attributeType = AttributeType.CHANGEABLE;

    private boolean isProductRelevant = false;

    private Method getter;

    public ModelTypeAttribute(String name, ModelType modelType) {
        super(name);
        this.modelType = modelType;
    }

    @Override
    public IModelType getModelType() {
        return modelType;
    }

    @Override
    public Class<?> getDatatype() throws ClassNotFoundException {
        return datatype;
    }

    @Override
    public AttributeType getAttributeType() {
        return attributeType;
    }

    @Override
    public ValueSetType getValueSetType() {
        return valueSetType;
    }

    @Override
    public boolean isProductRelevant() {
        return isProductRelevant;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getName());
        sb.append(": ");
        sb.append(datatype.getSimpleName());
        sb.append('(');
        sb.append(attributeType);
        sb.append(", ");
        sb.append(valueSetType);
        if (isProductRelevant) {
            sb.append(", ");
            sb.append("isProductRelevant");
        }
        sb.append(')');
        return sb.toString();
    }

    @Override
    public Object getValue(IModelObject source) {
        try {
            if (AttributeType.CONSTANT == attributeType) {
                Field field = source.getClass().getField(getName().toUpperCase());
                return field.get(source);
            }
            return getter.invoke(source);
        } catch (IllegalArgumentException e) {
            handleGetterError(source, e);
        } catch (IllegalAccessException e) {
            handleGetterError(source, e);
        } catch (InvocationTargetException e) {
            handleGetterError(source, e);
        } catch (SecurityException e) {
            handleGetterError(source, e);
        } catch (NoSuchFieldException e) {
            handleGetterError(source, e);
        }
        return null;
    }

    @Override
    public void setValue(IModelObject source, Object value) {
        try {
            PropertyDescriptor propertyDescriptor = new PropertyDescriptor(getName(), source.getClass());
            propertyDescriptor.getWriteMethod().invoke(source, value);
        } catch (IntrospectionException e) {
            handleSetterError(source, value, e);
        } catch (IllegalArgumentException e) {
            handleSetterError(source, value, e);
        } catch (IllegalAccessException e) {
            handleSetterError(source, value, e);
        } catch (InvocationTargetException e) {
            handleSetterError(source, value, e);
        }
    }

    private void handleGetterError(IModelObject source, Exception e) {
        throw new IllegalArgumentException(String.format("Could not get attribute %s on source object %s.", getName(),
                source), e);
    }

    private void handleSetterError(IModelObject source, Object value, Exception e) {
        throw new IllegalArgumentException(String.format(
                "Could not write attribute %s on source object %s to value %s.", getName(), source, value), e);
    }
}
