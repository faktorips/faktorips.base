/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Control;
import org.faktorips.devtools.core.internal.model.valueset.EnumValueSet;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSetOwner;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.editors.productcmpt.BooleanValueSetControl;

public class BooleanValueSetField extends DefaultEditField<IEnumValueSet> {

    private IConfigElement propertyValue;

    private BooleanValueSetControl booleanValueSetControl;

    public BooleanValueSetField(IConfigElement propertyValue, BooleanValueSetControl booleanValueSetControl) {
        this.propertyValue = propertyValue;
        propertyValue.getIpsProject();
        this.booleanValueSetControl = booleanValueSetControl;
    }

    @Override
    public Control getControl() {
        return booleanValueSetControl;
    }

    @Override
    public void setValue(IEnumValueSet newValue) {
        try {
            booleanValueSetControl.getTrueCheckBox().setChecked(
                    newValue.containsValue(Boolean.TRUE.toString(), newValue.getIpsProject()));
            booleanValueSetControl.getFalseCheckBox().setChecked(
                    newValue.containsValue(Boolean.FALSE.toString(), newValue.getIpsProject()));
            booleanValueSetControl.getNullCheckBox().setChecked(newValue.isContainingNull());
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getText() {
        // There is no need to use the getText method.
        return null;
    }

    @Override
    public void setText(String newText) {
        // There is no need to use the setText method.
    }

    @Override
    public void insertText(String text) {
        // There is no need to use the insertText method.
    }

    @Override
    public void selectAll() {
        // There is no need to use the insertText method.

    }

    @Override
    protected IEnumValueSet parseContent() {
        // boolean containsNull =
        // booleanValueSetControl.getAttribute().getValueSet().isContainingNull();
        // booleanValueSetControl.getNullCheckBox().setEnabled(containsNull);

        List<String> valuesAsList = createValuesAsList();

        IValueSetOwner valueSetOwner = propertyValue.getValueSet().getValueSetOwner();
        return new EnumValueSet(valueSetOwner, valuesAsList, valueSetOwner.getIpsModel().getNextPartId(valueSetOwner));
    }

    private List<String> createValuesAsList() {
        List<String> valuesAsList = new ArrayList<String>();
        addValueToList(booleanValueSetControl.getTrueCheckBox(), valuesAsList, false);
        addValueToList(booleanValueSetControl.getFalseCheckBox(), valuesAsList, false);
        addValueToList(booleanValueSetControl.getNullCheckBox(), valuesAsList, true);
        return valuesAsList;
    }

    private void addValueToList(Checkbox checkbox, List<String> valuesAsList, boolean isNull) {
        if (checkbox.isChecked()) {
            if (isNull) {
                valuesAsList.add(null);
            } else {
                valuesAsList.add(checkbox.getText());
            }
        }
    }

    @Override
    protected void addListenerToControl() {
        addListenerToCheckbox(booleanValueSetControl.getTrueCheckBox());
        addListenerToCheckbox(booleanValueSetControl.getFalseCheckBox());
        addListenerToCheckbox(booleanValueSetControl.getNullCheckBox());
    }

    private void addListenerToCheckbox(Checkbox checkBox) {
        checkBox.getButton().addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                notifyChangeListeners(new FieldValueChangedEvent(BooleanValueSetField.this));
                parseContent();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // no default selection is needed
            }
        });
    }

}
