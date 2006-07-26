/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

import org.apache.commons.lang.ObjectUtils;
import org.faktorips.devtools.core.ui.controller.fields.AbstractEnumDatatypeBasedField;


class FieldPropertyMappingByPropertyDescriptor implements FieldPropertyMapping {
    
    protected EditField field;
    protected Object object;
    protected PropertyDescriptor property;
    
    FieldPropertyMappingByPropertyDescriptor(EditField edit, Object object, PropertyDescriptor property) {
        this.field = edit;
        this.object = object;
        this.property = property;
    }
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.ui.controller.FieldPropertyMapping#getField()
     */
    public EditField getField() {
        return field;
    }
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.ui.controller.FieldPropertyMapping#getObject()
     */
    public Object getObject() {
        return object;
    }
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.ui.controller.FieldPropertyMapping#getPropertyName()
     */
    public String getPropertyName() {
        return property.getName();
    }
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.ui.controller.FieldPropertyMapping#setPropertyValue()
     */
    public void setPropertyValue() {
        if (!field.isTextContentParsable()) {
            return;
        }
        if (ObjectUtils.equals(getPropertyValue(), field.getValue())) {
            return; // value hasn't changed
        }
        try {
            Method setter = property.getWriteMethod();
            setter.invoke(object, new Object[]{field.getValue()});
        } catch (Exception e) {
            throw new RuntimeException("Error setting property value " + property.getName(), e); //$NON-NLS-1$
        }
    }
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.ui.controller.FieldPropertyMapping#setControlValue()
     */
    public void setControlValue() {
        try {
            Object propertyValue = getPropertyValue();
            
            // if we have a field which maintans a list - update it.
            if (field instanceof AbstractEnumDatatypeBasedField) {
            	((AbstractEnumDatatypeBasedField)field).reInit();
            }

            if (field.isTextContentParsable() && ObjectUtils.equals(propertyValue, field.getValue())) {
                return;
            }
            field.setValue(propertyValue, false);

        } catch (Exception e) {
            throw new RuntimeException("Error setting value in control for property " + property.getName(), e); //$NON-NLS-1$
        }
    }
    
    protected Object getPropertyValue() {
        try {
            Method getter = property.getReadMethod();
            return getter.invoke(object, new Object[0]);
        } catch (Exception e) {
            throw new RuntimeException("Error getting property value " + property.getName()); //$NON-NLS-1$
        }
    }
    
    public String toString() {
        return object.getClass().getName() + '.' + property.getName() + '-' + field; 
    }
}