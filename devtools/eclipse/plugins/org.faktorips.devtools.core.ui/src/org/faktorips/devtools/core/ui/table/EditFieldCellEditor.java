/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.table;

import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.controller.EditField;

/**
 * {@link IpsCellEditor} that delegates calls getValue() and setValue() to an {@link EditField}.
 * 
 * @author Stefan Widmaier
 */
public class EditFieldCellEditor extends IpsCellEditor {

    private EditField<String> editField;

    public EditFieldCellEditor(EditField<String> editField) {
        super(editField.getControl());
        this.editField = editField;
    }

    @Override
    protected Object doGetValue() {
        String returnValue = editField.getValue();
        if (IpsPlugin.getDefault().getIpsPreferences().getNullPresentation().equals(returnValue)) {
            return null;
        }
        return returnValue;
    }

    @Override
    protected void doSetFocus() {
        editField.selectAll();
        editField.getControl().setFocus();
    }

    @Override
    protected void doSetValue(Object value) {
        editField.setValue(value == null ? null : value.toString());
    }

    protected String getText() {
        // let edit field handle null presentation
        return editField.getText();
    }

    protected void setText(String newText) {
        // let edit field handle null presentation
        editField.setText(newText);
    }

    @Override
    public boolean isMappedValue() {
        return false;
    }

    public EditField<String> getEditField() {
        return editField;
    }
}
