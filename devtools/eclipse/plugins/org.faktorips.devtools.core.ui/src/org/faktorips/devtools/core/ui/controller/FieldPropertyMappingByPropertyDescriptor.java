/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class FieldPropertyMappingByPropertyDescriptor<T> extends AbstractFieldPropertyMapping<T> {

    private PropertyDescriptor property;

    public FieldPropertyMappingByPropertyDescriptor(EditField<T> edit, Object object, PropertyDescriptor property) {
        super(edit, object, property.getName());
        this.property = property;
    }

    @Override
    public void setPropertyValueInternal() {
        try {
            Method setter = property.getWriteMethod();
            if (setter == null) {
                throw new RuntimeException(
                        "Error setting property value " + property.getName() + ": Found no setter method"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            setter.invoke(getObject(), getField().getValue());
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Error setting property value " + property.getName() + ": Illegal Access", e); //$NON-NLS-1$ //$NON-NLS-2$
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Error setting property value " + property.getName() + ": Illegal Argument", e); //$NON-NLS-1$ //$NON-NLS-2$
        } catch (InvocationTargetException e) {
            throw new RuntimeException(
                    "Error setting property value " + property.getName() + ": Setter throws an exception", e); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    @Override
    public T getPropertyValue() {
        try {
            Method getter = property.getReadMethod();
            return invoke(getter);

            // CSOFF: IllegalCatch
        } catch (Exception e) {
            throw new RuntimeException("Error getting property value " + property.getName(), e); //$NON-NLS-1$
        }
        // CSON: IllegalCatch
    }

    @SuppressWarnings("unchecked")
    private T invoke(Method getter) throws IllegalAccessException, InvocationTargetException {
        return (T)getter.invoke(getObject());
    }
}
