/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.valueset.EnumValueSet;
import org.faktorips.devtools.core.internal.model.valueset.ValueSet;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IUnrestrictedValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIDatatypeFormatter;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controls.Checkbox;

/**
 * {@link EditField} that can be used to bind a logical group of {@link Checkbox check boxes} to a
 * model property, using an instance of {@link BindingContext}.
 * 
 * @since 3.9
 * 
 * @see BindingContext
 * @see Checkbox
 */
public class CheckboxGroupField extends DefaultEditField<IValueSet> {
    private final List<Checkbox> checkboxes;
    private IValueSet currentValue;
    private final IConfigElement valueSetOwner;
    private final IPolicyCmptTypeAttribute property;
    private IValueSet lastRestrictedValueSet;
    private RadioButtonGroupField<?> radioButtonGroupField = null;
    private Map<String, Button> buttonMap = new HashMap<String, Button>();

    public CheckboxGroupField(List<Checkbox> checkboxes, IPolicyCmptTypeAttribute property, IConfigElement valueSetOwner) {
        this.checkboxes = new ArrayList<Checkbox>(checkboxes);
        this.valueSetOwner = valueSetOwner;
        this.property = property;
    }

    @Override
    public Control getControl() {
        return checkboxes.get(0).getParent();
    }

    @Override
    public void setValue(IValueSet newValue) {
        currentValue = newValue;
        if (newValue instanceof IUnrestrictedValueSet) {
            for (Checkbox cb : checkboxes) {
                cb.setChecked(true);
            }
            return;
        }
        if (newValue instanceof IEnumValueSet) {
            EnumValueSet enumValueSet = (EnumValueSet)newValue;
            ValueDatatype valueDatatype = enumValueSet.getValueDatatype();
            for (Checkbox cb : checkboxes) {
                for (String value : enumValueSet.getValues()) {
                    if (cb.getText().equals(
                            IpsUIPlugin.getDefault().getDatatypeFormatter().formatValue(valueDatatype, value))) {
                        cb.setChecked(checkAllowed(value));
                    }
                }
            }
        }
    }

    private boolean checkAllowed(String value) {
        if (property.getValueSet() instanceof IEnumValueSet) {
            IEnumValueSet allowedValues = (IEnumValueSet)property.getValueSet();
            return allowedValues.getValuesAsList().contains(value);
        }
        return true;
    }

    @Override
    public String getText() {
        StringBuilder builder = new StringBuilder();
        for (Checkbox cb : checkboxes) {
            builder.append(cb.getText());
        }
        return builder.toString();
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
        // We don't support this operation
    }

    @Override
    protected IValueSet parseContent() throws Exception {
        if (currentValue == null) {
            return null;
        }
        List<String> values = new ArrayList<String>();
        UIDatatypeFormatter datatypeFormatter = IpsUIPlugin.getDefault().getDatatypeFormatter();
        boolean allCheckboxesChecked = true;
        for (Checkbox cb : checkboxes) {
            if (cb.isChecked()) {
                ValueDatatype valueDatatype = ((ValueSet)currentValue).getValueDatatype();
                if (cb.getText().equalsIgnoreCase(datatypeFormatter.formatValue(valueDatatype, Boolean.toString(true)))) {
                    values.add(Boolean.toString(true));
                } else if (cb.getText().equalsIgnoreCase(
                        datatypeFormatter.formatValue(valueDatatype, Boolean.toString(false)))) {
                    values.add(Boolean.toString(false));
                } else if (cb.getText().equalsIgnoreCase(
                        IpsPlugin.getDefault().getIpsPreferences().getNullPresentation())) {
                    values.add(null);
                }
            } else {
                allCheckboxesChecked = false;
            }
        }
        if (!allCheckboxesChecked && currentValue instanceof IUnrestrictedValueSet) {
            changeValueSetType(ValueSetType.ENUM);
        } else if (allCheckboxesChecked && currentValue instanceof IEnumValueSet
                && property.getValueSet() instanceof IUnrestrictedValueSet) {
            changeValueSetType(ValueSetType.UNRESTRICTED);
        }
        if (currentValue instanceof EnumValueSet) {
            EnumValueSet currentEnumValue = (EnumValueSet)currentValue;
            if (!Arrays.equals(currentEnumValue.getValues(), values.toArray(new String[0]))) {
                currentEnumValue.setValues(values.toArray(new String[0]));
            }
        }
        return currentValue;
    }

    @Override
    protected void addListenerToControl() {
        // Adds a selection listener to each check box of the group
        for (Checkbox cb : checkboxes) {
            cb.getButton().addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    notifyChangeListeners(new FieldValueChangedEvent(CheckboxGroupField.this));
                }
            });
        }
    }

    private void changeValueSetType(ValueSetType newValueSetType) {
        IValueSet oldValueSet = valueSetOwner.getValueSet();
        if (!oldValueSet.isUnrestricted()) {
            lastRestrictedValueSet = oldValueSet;
        }
        if (oldValueSet.getValueSetType().equals(newValueSetType)) {
            return; // unchanged
        }
        valueSetOwner.setValueSetType(newValueSetType);
        IValueSet newValueSet = valueSetOwner.getValueSet();
        if (lastRestrictedValueSet == null) {
            newValueSet.setAbstract(false);
        } else {
            newValueSet.setValuesOf(lastRestrictedValueSet);
        }
    }

    public void setRadioButtonGroupField(RadioButtonGroupField<?> radioButtonGroupField) {
        this.radioButtonGroupField = radioButtonGroupField;
        buttonMap.clear();
        if (radioButtonGroupField != null) {
            for (Button radioButton : radioButtonGroupField.getRadioButtonGroup().getRadioButtons()) {
                buttonMap.put(radioButton.getText(), radioButton);
            }
            refreshRadioButtonState();
        }
    }

    public RadioButtonGroupField<?> getRadioButtonGroupField() {
        return radioButtonGroupField;
    }

    public void refreshRadioButtonState() {
        if (radioButtonGroupField == null) {
            return;
        }
        for (Checkbox checkbox : checkboxes) {
            setEnabledState(checkbox);
        }
    }

    private void setEnabledState(Checkbox checkBox) {
        if (checkBox == null || buttonMap.get(checkBox.getText()) == null) {
            return;
        }
        Button target = buttonMap.get(checkBox.getText());
        target.setEnabled(checkBox.isChecked());
        if (!checkBox.isChecked() && target == radioButtonGroupField.getRadioButtonGroup().getSelectedButton()) {
            Button nullButton = buttonMap.get(IpsPlugin.getDefault().getIpsPreferences().getNullPresentation());
            if (nullButton != null) {
                valueSetOwner.setValue(null);
                checkBox.setChecked(false);
            }
        }
    }
}
