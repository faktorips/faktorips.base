/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls.valuesets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.ui.IDataChangeableReadWriteAccess;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.controls.ControlComposite;
import org.faktorips.devtools.core.ui.controls.Messages;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.valueset.IRangeValueSet;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.devtools.model.valueset.ValueSetType;

/**
 * A composite that consists of three textfields for lower bound, upper bound and step. If there is
 * a uicontroller supplied, it is used to establish a mapping between the model object and the
 * control which represents the object property.
 */
public class RangeEditControl extends ControlComposite implements IDataChangeableReadWriteAccess, IValueSetEditControl {

    private UIToolkit uiToolkit;

    private IRangeValueSet range;
    private EditField<String> lowerfield;
    private EditField<String> upperfield;
    private EditField<String> stepfield;
    private CheckboxField emptyRangeCheckboxField;
    private BindingContext uiController;
    private boolean dataChangeable;

    public RangeEditControl(Composite parent, UIToolkit toolkit, ValueDatatype valueDatatype, IRangeValueSet range,
            BindingContext uiController) {
        super(parent, SWT.NONE);
        this.range = range;
        uiToolkit = toolkit;

        setLayout();
        Composite workArea = createWorkArea(uiToolkit, this);
        createTextControls(uiToolkit, workArea, valueDatatype, range.getIpsProject());
        this.uiController = uiController;
        connectToModel();
    }

    private void setLayout() {
        setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout layout = new GridLayout(1, false);
        layout.marginHeight = 10;
        layout.marginWidth = 0;
        setLayout(layout);
    }

    private Composite createWorkArea(UIToolkit toolkit, Composite parent) {
        Composite workArea;
        GridLayout layoutWorkArea = new GridLayout(2, false);
        if (toolkit.getFormToolkit() == null) {
            workArea = toolkit.createComposite(parent);
            layoutWorkArea.marginHeight = 0;
            layoutWorkArea.marginWidth = 0;
            // this is important for the displayed icons !!
            layoutWorkArea.horizontalSpacing = 12;

        } else {
            workArea = toolkit.getFormToolkit().createComposite(parent);
            layoutWorkArea.marginHeight = 3;
            layoutWorkArea.marginWidth = 1;
        }
        // workArea.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_END |
        // GridData.FILL_HORIZONTAL));
        workArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        workArea.setLayout(layoutWorkArea);
        return workArea;
    }

    private void createTextControls(UIToolkit toolkit,
            Composite workArea,
            ValueDatatype valueDatatype,
            IIpsProject ipsProject) {
        ValueDatatypeControlFactory ctrlFactory = IpsUIPlugin.getDefault()
                .getValueDatatypeControlFactory(valueDatatype);

        toolkit.createFormLabel(workArea, Messages.RangeEditControl_labelMinimum);
        lowerfield = ctrlFactory.createEditField(uiToolkit, workArea, valueDatatype, null, ipsProject);
        /**
         * Configure the layout of the parent instead of the text control itself.
         * {@link ValueDatatypeControlFactory control factories} now create composites around all
         * text controls to be able to add additional controls later on (e.g. enum drop down button)
         */
        lowerfield.getControl().getParent()
                .setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER | GridData.FILL_HORIZONTAL));

        toolkit.createLabel(workArea, Messages.RangeEditControl_labelMaximum);
        upperfield = ctrlFactory.createEditField(uiToolkit, workArea, valueDatatype, null, ipsProject);
        upperfield.getControl().getParent()
                .setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER | GridData.FILL_HORIZONTAL));

        toolkit.createFormLabel(workArea, Messages.RangeEditControl_labelStep);
        stepfield = ctrlFactory.createEditField(uiToolkit, workArea, valueDatatype, null, ipsProject);
        stepfield.getControl().getParent()
                .setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER | GridData.FILL_HORIZONTAL));

        toolkit.createFormLabel(workArea, Messages.RangeEditControl_labelEmptyRange);
        Checkbox emptyRangeCheckbox = toolkit.createCheckbox(workArea);
        emptyRangeCheckbox.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER | GridData.FILL_HORIZONTAL));
        emptyRangeCheckboxField = new CheckboxField(emptyRangeCheckbox);
        emptyRangeCheckboxField.addChangeListener(e -> {
            range.setEmpty((Boolean)e.field.getValue());
            updateUI();
        });

        if (toolkit.getFormToolkit() != null) {
            toolkit.getFormToolkit().paintBordersFor(workArea);
            toolkit.getFormToolkit().adapt(workArea);
        }
    }

    private void connectToModel() {
        uiController.bindContent(upperfield, range, IRangeValueSet.PROPERTY_UPPERBOUND);
        uiController.bindContent(lowerfield, range, IRangeValueSet.PROPERTY_LOWERBOUND);
        uiController.bindContent(stepfield, range, IRangeValueSet.PROPERTY_STEP);
        uiController.bindContent(emptyRangeCheckboxField, range, IRangeValueSet.PROPERTY_EMPTY);
        updateUI();
    }

    private void updateUI() {
        boolean enabled = super.isEnabled() && !range.isEmpty();
        upperfield.getControl().setEnabled(enabled);
        lowerfield.getControl().setEnabled(enabled);
        stepfield.getControl().setEnabled(enabled);
        uiController.updateUI();
    }

    public IRangeValueSet getRange() {
        return range;
    }

    @Override
    public ValueSetType getValueSetType() {
        return ValueSetType.RANGE;
    }

    @Override
    public boolean canEdit(IValueSet valueSet, ValueDatatype valueDatatype) {
        if (valueSet == null) {
            return false;
        }
        return valueSet.isRange();
    }

    @Override
    public IValueSet getValueSet() {
        return range;
    }

    @Override
    public void setValueSet(IValueSet valueSet, ValueDatatype valueDatatype) {
        range = (IRangeValueSet)valueSet;
        uiController.removeBindings(upperfield.getControl());
        uiController.removeBindings(lowerfield.getControl());
        uiController.removeBindings(stepfield.getControl());
        uiController.removeBindings(emptyRangeCheckboxField.getControl());
        connectToModel();
    }

    public void setLower(String newText) {
        lowerfield.setText(newText);
    }

    public String getLower() {
        return lowerfield.getText();
    }

    public void setUpper(String newText) {
        upperfield.setText(newText);
    }

    public String getUpper() {
        return upperfield.getText();
    }

    public void setStep(String newText) {
        stepfield.setText(newText);
    }

    public String getStep() {
        return stepfield.getText();
    }

    @Override
    public boolean setFocus() {
        return lowerfield.getControl().setFocus();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        upperfield.getControl().setEnabled(enabled);
        lowerfield.getControl().setEnabled(enabled);
        stepfield.getControl().setEnabled(enabled);
        emptyRangeCheckboxField.getControl().setEnabled(enabled);
    }

    @Override
    public boolean isDataChangeable() {
        return dataChangeable;
    }

    @Override
    public void setDataChangeable(boolean changeable) {
        dataChangeable = changeable;

        uiToolkit.setDataChangeable(lowerfield.getControl(), changeable);
        uiToolkit.setDataChangeable(upperfield.getControl(), changeable);
        uiToolkit.setDataChangeable(stepfield.getControl(), changeable);
        uiToolkit.setDataChangeable(emptyRangeCheckboxField.getControl(), changeable);
    }

    @Override
    public Composite getComposite() {
        return this;
    }
}
