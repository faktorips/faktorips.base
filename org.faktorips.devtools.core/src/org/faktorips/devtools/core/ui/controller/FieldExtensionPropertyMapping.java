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

import org.apache.commons.lang.ObjectUtils;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyAccess;


public class FieldExtensionPropertyMapping implements FieldPropertyMapping {
    
    protected EditField field;
    protected IExtensionPropertyAccess object;
    protected String propertyId;
    
    public FieldExtensionPropertyMapping(EditField edit, IExtensionPropertyAccess object, String extensionPropertyId) {
        this.field = edit;
        this.object = object;
        this.propertyId = extensionPropertyId;
    }
    
    /**
     * {@inheritDoc}
     */
    public EditField getField() {
        return field;
    }
    
    /**
     * {@inheritDoc}
     */
    public Object getObject() {
        return object;
    }
    
    /**
     * {@inheritDoc}
     */
    public String getPropertyName() {
        return propertyId;
    }
    
    /**
     * {@inheritDoc}
     */
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
    
    /**
     * {@inheritDoc}
     */
    public void setControlValue() {
        if (field.getControl().isDisposed()) {
            return;
        }
        try {
            Object propertyValue = getPropertyValue();
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
    
    public String toString() {
        return object.getClass().getName() + '.' + propertyId + '-' + field; 
    }
}