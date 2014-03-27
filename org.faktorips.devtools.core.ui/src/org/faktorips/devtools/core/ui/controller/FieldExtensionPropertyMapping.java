/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller;

import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyAccess;

public class FieldExtensionPropertyMapping<T> extends AbstractFieldPropertyMapping<T> {

    public FieldExtensionPropertyMapping(EditField<T> edit, IExtensionPropertyAccess object, String extensionPropertyId) {
        super(edit, object, extensionPropertyId);
    }

    @Override
    public IExtensionPropertyAccess getObject() {
        return (IExtensionPropertyAccess)super.getObject();
    }

    @Override
    protected void setPropertyValueInternal() {
        getObject().setExtPropertyValue(getPropertyName(), getField().getValue());
    }

    @Override
    public T getPropertyValue() {
        @SuppressWarnings("unchecked")
        T extPropertyValue = (T)getObject().getExtPropertyValue(getPropertyName());
        return extPropertyValue;
    }

    @Override
    public String toString() {
        return getObject().getClass().getName() + '.' + getPropertyName() + '-' + getField();
    }
}
