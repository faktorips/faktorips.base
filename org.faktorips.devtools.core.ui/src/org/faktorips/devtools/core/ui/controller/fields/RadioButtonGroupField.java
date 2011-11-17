/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controls.RadioButtonGroup;

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

    @Override
    public Control getControl() {
        return radioButtonGroup.getGroup();
    }

    @Override
    public void setValue(T newValue) {
        radioButtonGroup.setSelection(newValue);
    }

    @Override
    public String getText() {
        Button selectedButton = radioButtonGroup.getSelectedButton();
        return selectedButton == null ? StringUtils.EMPTY : selectedButton.getText();
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
