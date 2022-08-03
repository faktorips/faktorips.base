/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller.fields;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Control;
import org.faktorips.devtools.core.ui.controls.AbstractCheckbox;

public class CheckboxField extends DefaultEditField<Boolean> {

    private AbstractCheckbox checkbox;

    public CheckboxField(AbstractCheckbox checkbox) {
        this.checkbox = checkbox;
    }

    @Override
    public Control getControl() {
        return checkbox;
    }

    public AbstractCheckbox getCheckbox() {
        return checkbox;
    }

    @Override
    public Boolean parseContent() {
        return checkbox.isChecked();
    }

    @Override
    public void setValue(Boolean newValue) {
        checkbox.setChecked(newValue);
    }

    @Override
    public String getText() {
        return checkbox.isChecked() ? "true" : "false"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public void setText(String newText) {
        checkbox.setChecked(Boolean.parseBoolean(newText));
    }

    @Override
    public void insertText(String text) {
        // nothing to do
    }

    @Override
    public void selectAll() {
        // nothing to do
    }

    @Override
    protected void addListenerToControl() {
        checkbox.getButton().addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                notifyChangeListeners(new FieldValueChangedEvent(CheckboxField.this));
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // nothing to do
            }
        });
    }
}
