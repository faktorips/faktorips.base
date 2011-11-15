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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.faktorips.devtools.core.ui.controls.RadioButtonGroup;

/**
 * @author Alexander Weickmann
 */
public class RadioButtonGroupField<T> extends DefaultEditField<T> {

    private final RadioButtonGroup radioButtonGroup;

    private final Map<Button, T> options;

    /**
     * <strong>Important:</strong> Do not add further radio buttons to the provided
     * {@link RadioButtonGroup} as this will cause the edit field to be out of sync with the group.
     */
    public RadioButtonGroupField(RadioButtonGroup radioButtonGroup, Map<Button, T> options) {
        this.radioButtonGroup = radioButtonGroup;
        this.options = new HashMap<Button, T>(options);
    }

    @Override
    public Control getControl() {
        return radioButtonGroup.getGroup();
    }

    @Override
    public void setValue(T newValue) {
        for (Button button : options.keySet()) {
            if (newValue.equals(options.get(button))) {
                button.setSelection(true);
                break;
            }
        }
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
        return options.get(radioButtonGroup.getSelectedButton());
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
