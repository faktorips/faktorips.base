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

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Control;
import org.faktorips.devtools.core.internal.model.productcmpt.ConfigElement;
import org.faktorips.devtools.core.internal.model.valueset.EnumValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSetOwner;
import org.faktorips.devtools.core.ui.editors.productcmpt.BooleanValueSetControl;
import org.faktorips.devtools.core.ui.editors.productcmpt.BooleanValueSetPMO;

public class BooleanValueSetField extends DefaultEditField<EnumValueSet> {

    private ConfigElement propertyValue;

    private BooleanValueSetControl booleanValueSetControl;

    public BooleanValueSetField(ConfigElement propertyValue, BooleanValueSetControl booleanValueSetControl) {
        this.propertyValue = propertyValue;
        propertyValue.getIpsProject();
        this.booleanValueSetControl = booleanValueSetControl;
    }

    @Override
    public Control getControl() {
        return booleanValueSetControl;
    }

    @Override
    public void setValue(EnumValueSet newValue) {
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setText(String newText) {
        // TODO Auto-generated method stub
    }

    @Override
    public void insertText(String text) {
        // TODO Auto-generated method stub
    }

    @Override
    public void selectAll() {
        // TODO Auto-generated method stub

    }

    @Override
    protected EnumValueSet parseContent() {
        boolean isTrueSelected = booleanValueSetControl.getTrueCheckBox().isChecked();
        boolean isFalseSelected = booleanValueSetControl.getFalseCheckBox().isChecked();
        boolean isNullSelected = booleanValueSetControl.getNullCheckBox().isChecked();

        List<String> valuesAsList = booleanValueSetControl.getEnumValueSetProvider().getSourceEnumValueSet()
                .getValuesAsList();

        BooleanValueSetPMO.modifyValue(Boolean.TRUE.toString(), isTrueSelected, propertyValue);
        BooleanValueSetPMO.modifyValue(Boolean.FALSE.toString(), isFalseSelected, propertyValue);
        BooleanValueSetPMO.modifyValue(null, isNullSelected, propertyValue);

        IValueSetOwner valueSetOwner = propertyValue.getValueSet().getValueSetOwner();
        return new EnumValueSet(valueSetOwner, valuesAsList, valueSetOwner.getIpsModel().getNextPartId(valueSetOwner));
    }

    @Override
    protected void addListenerToControl() {
        booleanValueSetControl.addControlListener(new ControlListener() {

            @Override
            public void controlResized(ControlEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void controlMoved(ControlEvent e) {
                // TODO Auto-generated method stub

            }
        });
    }

}
