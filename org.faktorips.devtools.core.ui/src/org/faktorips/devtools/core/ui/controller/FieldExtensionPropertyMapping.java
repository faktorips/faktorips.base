/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller;

import org.apache.commons.lang.ObjectUtils;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyAccess;

public class FieldExtensionPropertyMapping<T> implements FieldPropertyMapping {

    protected EditField<T> field;
    protected IExtensionPropertyAccess object;
    protected String propertyId;

    public FieldExtensionPropertyMapping(EditField<T> edit, IExtensionPropertyAccess object, String extensionPropertyId) {
        this.field = edit;
        this.object = object;
        this.propertyId = extensionPropertyId;
    }

    @Override
    public EditField<?> getField() {
        return field;
    }

    @Override
    public Object getObject() {
        return object;
    }

    @Override
    public String getPropertyName() {
        return propertyId;
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
        object.setExtPropertyValue(propertyId, field.getValue());
    }

    @Override
    public void setControlValue() {
        if (field.getControl().isDisposed()) {
            return;
        }
        try {
            @SuppressWarnings("unchecked")
            // the property is get by reflection - cannot cast safely
            T propertyValue = (T)getPropertyValue();
            if (field.isTextContentParsable() && ObjectUtils.equals(propertyValue, field.getValue())) {
                return;
            }
            field.setValue(propertyValue, false);
        } catch (Exception e) {
            throw new RuntimeException("Error setting value in control for property " + getPropertyName(), e); //$NON-NLS-1$            
        }
    }

    private Object getPropertyValue() {
        return object.getExtPropertyValue(propertyId);
    }

    @Override
    public String toString() {
        return object.getClass().getName() + '.' + propertyId + '-' + field;
    }
}
