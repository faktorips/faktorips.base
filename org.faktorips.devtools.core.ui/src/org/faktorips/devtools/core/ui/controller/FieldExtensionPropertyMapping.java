/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller;

import org.apache.commons.lang.ObjectUtils;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyAccess;

public class FieldExtensionPropertyMapping<T> implements FieldPropertyMapping<T> {

    protected EditField<T> field;
    protected IExtensionPropertyAccess object;
    protected String propertyId;

    public FieldExtensionPropertyMapping(EditField<T> edit, IExtensionPropertyAccess object, String extensionPropertyId) {
        this.field = edit;
        this.object = object;
        this.propertyId = extensionPropertyId;
    }

    @Override
    public EditField<T> getField() {
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

    @Override
    public Object getPropertyValue() {
        return object.getExtPropertyValue(propertyId);
    }

    @Override
    public String toString() {
        return object.getClass().getName() + '.' + propertyId + '-' + field;
    }
}
