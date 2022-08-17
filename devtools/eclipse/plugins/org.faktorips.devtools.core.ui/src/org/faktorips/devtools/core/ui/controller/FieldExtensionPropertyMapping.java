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

import org.faktorips.devtools.model.extproperties.IExtensionPropertyAccess;

public class FieldExtensionPropertyMapping<T> extends AbstractFieldPropertyMapping<T> {

    public FieldExtensionPropertyMapping(EditField<T> edit, IExtensionPropertyAccess object,
            String extensionPropertyId) {
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

    @SuppressWarnings("unchecked")
    @Override
    public T getPropertyValue() {
        return (T)getObject().getExtPropertyValue(getPropertyName());
    }

    @Override
    public String toString() {
        return getObject().getClass().getName() + '.' + getPropertyName() + '-' + getField();
    }
}
