/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

import org.apache.commons.lang.ObjectUtils;
import org.faktorips.devtools.core.ui.controller.fields.AbstractEnumDatatypeBasedField;
import org.faktorips.devtools.core.ui.controller.fields.ComboField;

public class FieldPropertyMappingByPropertyDescriptor implements FieldPropertyMapping {

    protected EditField field;
    protected Object object;
    protected PropertyDescriptor property;

    public FieldPropertyMappingByPropertyDescriptor(EditField edit, Object object, PropertyDescriptor property) {
        this.field = edit;
        this.object = object;
        this.property = property;
    }

    @Override
    public EditField getField() {
        return field;
    }

    @Override
    public Object getObject() {
        return object;
    }

    @Override
    public String getPropertyName() {
        return property.getName();
    }

    @Override
    public void setPropertyValue() {
        if (field.getControl().isDisposed()) {
            return;
        }
        if (!field.isTextContentParsable()) {
            return;
        }
        if (ObjectUtils.equals(getPropertyValue(), field.getValue())) {
            return; // value hasn't changed
        }
        try {
            Method setter = property.getWriteMethod();
            setter.invoke(object, new Object[] { field.getValue() });
        } catch (Exception e) {
            // TODO catch Exception needs to be documented properly or specialized
            throw new RuntimeException("Error setting property value " + property.getName(), e); //$NON-NLS-1$
        }
    }

    @Override
    public void setControlValue() {
        try {
            if (field.getControl().isDisposed()) {
                return;
            }
            Object propertyValue = getPropertyValue();

            // if we have a field which maintans a list - update it.
            if (field instanceof AbstractEnumDatatypeBasedField) {
                ((AbstractEnumDatatypeBasedField)field).reInit();
            }

            if (field.isTextContentParsable() && ObjectUtils.equals(propertyValue, field.getValue())) {
                if (field instanceof ComboField) {
                    /*
                     * special case: if the field is a combo field the getValue method returns null
                     * if there is no selection and if the null value is selected, therefore we must
                     * check here if the getValue is a valid selection or nothing is selected. If
                     * there is no valid selection set the new value (e.g. the null value)
                     */
                    if (((ComboField)field).getCombo().getSelectionIndex() != -1) {
                        // the selection in the combo is valid and equal to the property value,
                        // don't set the new value
                        return;
                    }
                } else {
                    return;
                }
            }
            field.setValue(propertyValue, false);

        } catch (Exception e) {
            // TODO catch Exception needs to be documented properly or specialized
            throw new RuntimeException("Error setting value in control for property " + property.getName(), e); //$NON-NLS-1$
        }
    }

    protected Object getPropertyValue() {
        try {
            Method getter = property.getReadMethod();
            return getter.invoke(object, new Object[0]);
        } catch (Exception e) {
            // TODO catch Exception needs to be documented properly or specialized
            throw new RuntimeException("Error getting property value " + property.getName()); //$NON-NLS-1$
        }
    }

    @Override
    public String toString() {
        return object.getClass().getName() + '.' + property.getName() + '-' + field;
    }
}
