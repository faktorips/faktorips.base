package org.faktorips.devtools.core.ui.controller;

import org.apache.commons.lang.ObjectUtils;
import org.faktorips.devtools.core.model.IExtensionPropertyAccess;


class FieldExtensionPropertyMapping implements FieldPropertyMapping {
    
    protected EditField field;
    protected IExtensionPropertyAccess object;
    protected String propertyId;
    
    FieldExtensionPropertyMapping(EditField edit, IExtensionPropertyAccess object, String extensionPropertyId) {
        this.field = edit;
        this.object = object;
        this.propertyId = extensionPropertyId;
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
        return propertyId;
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
        object.setExtPropertyValue(propertyId, field.getValue());
    }
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.ui.controller.FieldPropertyMapping#setControlValue()
     */
    public void setControlValue() {
        Object propertyValue = getPropertyValue();
        if (field.isTextContentParsable() && ObjectUtils.equals(propertyValue, field.getValue())) {
            return;
        }
        field.setValue(propertyValue, false);
    }
    
    private Object getPropertyValue() {
        return object.getExtPropertyValue(propertyId);
    }
    
    public String toString() {
        return object.getClass().getName() + '.' + propertyId + '-' + field; 
    }
}