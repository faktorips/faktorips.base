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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;

public class ButtonField extends DefaultEditField {

    private final Button button;

    public ButtonField(Button button) {
        this.button = button;
    }

    @Override
    public Control getControl() {
        return button;
    }

    @Override
    public Object parseContent() {
        return new Boolean(button.getSelection());
    }

    @Override
    public void setValue(Object newValue) {
        button.setSelection((Boolean)newValue);
    }

    @Override
    public String getText() {
        return Boolean.toString(button.getSelection());
    }

    @Override
    public void setText(String newText) {
        button.setSelection(Boolean.valueOf(newText));
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
        button.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                notifyChangeListeners(new FieldValueChangedEvent(ButtonField.this));
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // nothing to do
            }

        });
    }

}
