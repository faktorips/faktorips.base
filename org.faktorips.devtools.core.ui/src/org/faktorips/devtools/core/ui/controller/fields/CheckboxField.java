/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.controller.fields;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Control;
import org.faktorips.devtools.core.ui.controls.AbstractCheckbox;

public class CheckboxField extends DefaultEditField {

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
    public Object parseContent() {
        return new Boolean(checkbox.isChecked());
    }

    @Override
    public void setValue(Object newValue) {
        checkbox.setChecked(((Boolean)newValue).booleanValue());
    }

    @Override
    public String getText() {
        return checkbox.isChecked() ? "true" : "false"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public void setText(String newText) {
        checkbox.setChecked(Boolean.valueOf(newText).booleanValue());
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
