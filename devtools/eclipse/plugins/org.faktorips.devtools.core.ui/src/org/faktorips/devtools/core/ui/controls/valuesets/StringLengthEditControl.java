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
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controls.ControlComposite;
import org.faktorips.devtools.core.ui.controls.Messages;
import org.faktorips.devtools.model.internal.valueset.StringLengthValueSet;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.valueset.IStringLengthValueSet;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.devtools.model.valueset.ValueSetType;

/**
 * A composite that provides a text field to set the maximum length of a
 * {@link StringLengthValueSet}. A {@link BindingContext} can be provided to create a mapping
 * between this control and the model object.
 */
public class StringLengthEditControl extends ControlComposite implements IValueSetEditControl {

    private IStringLengthValueSet valueSet;
    private UIToolkit uiToolkit;
    private BindingContext uiController;
    private EditField<String> maxLengthField;

    public StringLengthEditControl(Composite parent, UIToolkit toolkit,
            IStringLengthValueSet valueSet, BindingContext uiController) {
        super(parent, SWT.NONE);
        this.valueSet = valueSet;
        uiToolkit = toolkit;

        setLayout();
        Composite workArea = createWorkArea(uiToolkit, this);
        createTextControls(uiToolkit, workArea, Datatype.INTEGER, valueSet.getIpsProject());
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

        toolkit.createFormLabel(workArea, Messages.StringLengthEditControl_labelMaximum);
        maxLengthField = ctrlFactory.createEditField(uiToolkit, workArea, valueDatatype, null, ipsProject);

        /**
         * Configure the layout of the parent instead of the text control itself.
         * {@link ValueDatatypeControlFactory control factories} now create composites around all
         * text controls to be able to add additional controls later on (e.g. enum drop down button)
         */
        maxLengthField.getControl().getParent()
                .setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER | GridData.FILL_HORIZONTAL));

        if (toolkit.getFormToolkit() != null) {
            toolkit.getFormToolkit().paintBordersFor(workArea);
            toolkit.getFormToolkit().adapt(workArea);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        maxLengthField.getControl().setEnabled(enabled);
    }

    private void updateUI() {
        boolean enabled = super.isEnabled();
        setEnabled(enabled);
        uiController.updateUI();
    }

    private void connectToModel() {
        uiController.bindContent(maxLengthField, valueSet, IStringLengthValueSet.PROPERTY_MAXIMUMLENGTH);
        updateUI();
    }

    @Override
    public ValueSetType getValueSetType() {
        return ValueSetType.STRINGLENGTH;
    }

    @Override
    public void setValueSet(IValueSet newSet, ValueDatatype valueDatatype) {
        valueSet = (IStringLengthValueSet)newSet;
        uiController.removeBindings(maxLengthField.getControl());
        connectToModel();
    }

    @Override
    public IValueSet getValueSet() {
        return valueSet;
    }

    @Override
    public boolean canEdit(IValueSet valueSet, ValueDatatype valueDatatype) {
        if (valueSet == null) {
            return false;
        }
        return valueSet.isStringLength();
    }

    @Override
    public Composite getComposite() {
        return this;
    }

}
