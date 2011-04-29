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

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object doGetValue() {
        String returnValue = editField.getValue();
        if (IpsPlugin.getDefault().getIpsPreferences().getNullPresentation().equals(returnValue)) {
            return null;
        }
        return returnValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doSetFocus() {
        editField.selectAll();
        editField.getControl().setFocus();
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMappedValue() {
        return false;
    }
}
