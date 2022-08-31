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

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controls.RadioButtonGroup;
import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * {@link EditField} that can be used to bind a {@link RadioButtonGroup} to a model property, using
 * an instance of {@link BindingContext}.
 * 
 * @since 3.6
 * 
 * @author Alexander Weickmann
 * 
 * @see BindingContext
 * @see RadioButtonGroup
 */
public class RadioButtonGroupField<T> extends DefaultEditField<T> {

    private final RadioButtonGroup<T> radioButtonGroup;

    /**
     * <strong>Important:</strong> Do not add further radio buttons to the provided
     * {@link RadioButtonGroup} as this will cause this {@link EditField} to be out of sync with the
     * group.
     * 
     * @param radioButtonGroup the {@link RadioButtonGroup} to create this {@link EditField} for
     */
    public RadioButtonGroupField(RadioButtonGroup<T> radioButtonGroup) {
        this.radioButtonGroup = radioButtonGroup;
    }

    public RadioButtonGroup<T> getRadioButtonGroup() {
        return radioButtonGroup;
    }

    @Override
    public Control getControl() {
        return radioButtonGroup.getComposite();
    }

    public Button getButton(T value) {
        return radioButtonGroup.getRadioButton(value);
    }

    @Override
    public void setValue(T newValue) {
        radioButtonGroup.setSelection(newValue);
    }

    @Override
    public String getText() {
        Button selectedButton = radioButtonGroup.getSelectedButton();
        return selectedButton == null ? IpsStringUtils.EMPTY : selectedButton.getText();
    }

    @Override
    public void setText(String newText) {
        // There is no text that could be set
    }

    @Override
    public void insertText(String text) {
        // There is no text that could be set
    }

    @Override
    public void selectAll() {
        // It is not possible to select all radio buttons at once
    }

    @Override
    protected T parseContent() throws Exception {
        return radioButtonGroup.getSelectedOption();
    }

    @Override
    protected void addListenerToControl() {
        // Adds a selection listener to each radio button of the group
        for (Button button : radioButtonGroup.getRadioButtons()) {
            button.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    notifyChangeListeners(new FieldValueChangedEvent(RadioButtonGroupField.this));
                }
            });
        }
    }

}
