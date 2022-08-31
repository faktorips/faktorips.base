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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.faktorips.devtools.core.ui.controller.EditField;

/**
 * Generic type for all combo edit fields.
 * 
 * @see EditField for details about generic type T
 * 
 * @author dirmeier
 */
public abstract class ComboField<T> extends DefaultEditField<T> {

    private boolean immediatelyNotifyListener = false;

    private Combo combo;

    public ComboField(Combo combo) {
        this.combo = combo;
    }

    @Override
    public Control getControl() {
        return combo;
    }

    public Combo getCombo() {
        return combo;
    }

    @Override
    public String getText() {
        int i = combo.getSelectionIndex();
        if (i == -1) {
            // if no item is selected then return the text given in the text control of the combo
            return combo.getText();
        }
        return combo.getItem(i);
    }

    @Override
    public void setText(String newText) {
        immediatelyNotifyListener = true;
        try {
            combo.select(combo.indexOf(newText));
        } finally {
            immediatelyNotifyListener = false;
        }
    }

    @Override
    public void insertText(String text) {
        combo.setText(text);
    }

    @Override
    public void selectAll() {
        // nothing to do
    }

    @Override
    protected void addListenerToControl() {
        // add selection listener to get notifications if the user changes the selection
        combo.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                /*
                 * changes in combo fields will always be notified immediately, it is not necessary
                 * to delay the notification, when the user selects a new item the time for the
                 * change is long enough
                 */
                notifyChangeListeners(new FieldValueChangedEvent(ComboField.this), immediatelyNotifyListener);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // nothing to do
            }
        });
        // add modify listener to get changes when using combo#setText method
        combo.addModifyListener(
                $ -> notifyChangeListeners(new FieldValueChangedEvent(ComboField.this), immediatelyNotifyListener));
    }

}
