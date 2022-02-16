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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Control;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.editors.productcmpt.BooleanValueSetControl;
import org.faktorips.devtools.core.ui.util.SelectionListeners;
import org.faktorips.devtools.model.internal.valueset.EnumValueSet;
import org.faktorips.devtools.model.productcmpt.IConfiguredValueSet;
import org.faktorips.devtools.model.valueset.IEnumValueSet;
import org.faktorips.devtools.model.valueset.IValueSet;

public class BooleanValueSetField extends DefaultEditField<IValueSet> {

    private IConfiguredValueSet configuredValueSet;

    private BooleanValueSetControl booleanValueSetControl;

    public BooleanValueSetField(IConfiguredValueSet configuredValueSet, BooleanValueSetControl booleanValueSetControl) {
        this.configuredValueSet = configuredValueSet;
        this.booleanValueSetControl = booleanValueSetControl;
    }

    @Override
    public Control getControl() {
        return booleanValueSetControl;
    }

    @Override
    protected Control getControlForDecoration() {
        if (booleanValueSetControl.getNullCheckBox() != null) {
            return booleanValueSetControl.getNullCheckBox();
        } else {
            return super.getControlForDecoration();
        }
    }

    @Override
    public void setValue(IValueSet newValue) {
        try {
            booleanValueSetControl.getTrueCheckBox().setChecked(
                    newValue.containsValue(Boolean.TRUE.toString(), newValue.getIpsProject()));
            booleanValueSetControl.getFalseCheckBox().setChecked(
                    newValue.containsValue(Boolean.FALSE.toString(), newValue.getIpsProject()));
            if (booleanValueSetControl.getNullCheckBox() != null) {
                booleanValueSetControl.getNullCheckBox().setChecked(newValue.isContainsNull());
            }
        } catch (IpsException e) {
            e.printStackTrace();
        }
        booleanValueSetControl.updateEnabledState();
    }

    @Override
    public String getText() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setText(String newText) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void insertText(String text) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void selectAll() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected IEnumValueSet parseContent() {
        List<String> valuesAsList = createValuesAsList();
        return new EnumValueSet(configuredValueSet, valuesAsList, configuredValueSet.getIpsModel().getNextPartId(
                configuredValueSet));
    }

    private List<String> createValuesAsList() {
        List<String> valuesAsList = new ArrayList<>();
        addValue(valuesAsList, booleanValueSetControl.getTrueCheckBox(), Boolean.TRUE.toString());
        addValue(valuesAsList, booleanValueSetControl.getFalseCheckBox(), Boolean.FALSE.toString());
        addValue(valuesAsList, booleanValueSetControl.getNullCheckBox(), null);
        return valuesAsList;
    }

    private void addValue(List<String> valuesAsList, Checkbox checkbox, String nameOfCheckbox) {
        if (checkbox != null && checkbox.isChecked()) {
            valuesAsList.add(nameOfCheckbox);
        }
    }

    @Override
    protected void addListenerToControl() {
        addListenerToCheckbox(booleanValueSetControl.getTrueCheckBox());
        addListenerToCheckbox(booleanValueSetControl.getFalseCheckBox());
        addListenerToCheckbox(booleanValueSetControl.getNullCheckBox());
    }

    private void addListenerToCheckbox(Checkbox checkBox) {
        if (checkBox != null) {
            checkBox.getButton().addSelectionListener(SelectionListeners
                    .widgetSelected($ -> notifyChangeListeners(new FieldValueChangedEvent(BooleanValueSetField.this))));
        }
    }

}
