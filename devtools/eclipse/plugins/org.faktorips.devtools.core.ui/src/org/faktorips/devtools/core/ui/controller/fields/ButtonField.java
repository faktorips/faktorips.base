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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;

/**
 * Field for checkbox-, toggle- and radio- {@link Button buttons}. Sets the selection of a button
 * depending on a given value or text.
 * <p>
 * Can be configured to select the button on value <code>true</code> or de-select it in that case.
 * In other words the selection may be "inverted" by specifying the flag "select if true" (as
 * <code>false</code>) in {@link ButtonField#ButtonField(Button, boolean)}.
 * 
 * @author Stefan Widmaier
 */
public class ButtonField extends DefaultEditField<Boolean> {

    private final Button button;
    private final boolean selectIfTrue;

    /**
     * @param button the button this field sets the selection of
     * @param selectIfTrue whether or not to select the button on value <code>true</code>
     */
    public ButtonField(Button button, boolean selectIfTrue) {
        this.button = button;
        this.selectIfTrue = selectIfTrue;
    }

    /**
     * Creates a button field that selects the given button in case of value <code>true</code>.
     * 
     * @param button the button this field sets the selection of
     */
    public ButtonField(Button button) {
        this(button, true);
    }

    @Override
    public Control getControl() {
        return button;
    }

    @Override
    public Boolean parseContent() {
        Boolean content = button.getSelection();
        return invertBooleanIfNeccessary(content);
    }

    protected Boolean invertBooleanIfNeccessary(Boolean booleanObject) {
        if (isSelectIfTrue()) {
            return booleanObject;
        } else {
            return !booleanObject;
        }
    }

    @Override
    public void setValue(Boolean newValue) {
        button.setSelection(invertBooleanIfNeccessary(newValue));
    }

    @Override
    public String getText() {
        Boolean selection = invertBooleanIfNeccessary(button.getSelection());
        return Boolean.toString(selection);
    }

    @Override
    public void setText(String newText) {
        Boolean newSelection = invertBooleanIfNeccessary(Boolean.valueOf(newText));
        button.setSelection(newSelection.booleanValue());
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

    /**
     * Returns whether or not the contained button is selected in case of the calls
     * <code>setValue(Boolean.TRUE)</code> and <code>setText("true")</code>.
     */
    public boolean isSelectIfTrue() {
        return selectIfTrue;
    }

}
