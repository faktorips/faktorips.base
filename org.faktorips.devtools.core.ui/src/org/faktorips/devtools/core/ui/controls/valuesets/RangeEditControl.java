/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls.valuesets;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.valueset.RangeValueSet;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.valueset.IRangeValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.core.ui.IDataChangeableReadWriteAccess;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.IpsObjectUIController;
import org.faktorips.devtools.core.ui.controller.UIController;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.controls.ControlComposite;
import org.faktorips.devtools.core.ui.controls.Messages;

/**
 * A composite that consits of three textfields for lower bound, upper bound and step. if there is a
 * uicontroller supplied it is used to establish a mapping between the modell object and the control
 * which represents the object property.
 */
public class RangeEditControl extends ControlComposite implements IDataChangeableReadWriteAccess, IValueSetEditControl {

    private UIToolkit uiToolkit;

    private IRangeValueSet range;
    private EditField lowerfield;
    private EditField upperfield;
    private EditField stepfield;
    private IpsObjectUIController uiController;
    private Checkbox containsNullCB;
    private CheckboxField containsNullField;

    private boolean dataChangeable;

    public RangeEditControl(Composite parent, UIToolkit toolkit, ValueDatatype valueDatatype, IRangeValueSet range,
            UIController uiController) {
        super(parent, SWT.NONE);
        this.range = range;
        uiToolkit = toolkit;

        setLayout();
        Composite workArea = createWorkArea(uiToolkit, this);
        createTextControls(uiToolkit, workArea, valueDatatype, range, range.getIpsProject());

        if (uiController instanceof IpsObjectUIController) {
            this.uiController = (IpsObjectUIController)uiController;
        } else {
            this.uiController = new IpsObjectUIController((IIpsObjectPart)range.getParent());
        }
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
            layoutWorkArea.horizontalSpacing = 12; // this is important for the diplayed icons !!

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
            IValueSet valueSet,
            IIpsProject ipsProject) {
        ValueDatatypeControlFactory ctrlFactory = IpsUIPlugin.getDefault()
                .getValueDatatypeControlFactory(valueDatatype);

        toolkit.createFormLabel(workArea, Messages.RangeEditControl_labelMinimum);
        lowerfield = ctrlFactory.createEditField(uiToolkit, workArea, valueDatatype, valueSet, ipsProject);
        lowerfield.getControl().setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER | GridData.FILL_HORIZONTAL));

        toolkit.createLabel(workArea, Messages.RangeEditControl_labelMaximum);
        upperfield = ctrlFactory.createEditField(uiToolkit, workArea, valueDatatype, valueSet, ipsProject);
        upperfield.getControl().setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER | GridData.FILL_HORIZONTAL));

        toolkit.createFormLabel(workArea, Messages.RangeEditControl_labelStep);
        stepfield = ctrlFactory.createEditField(uiToolkit, workArea, valueDatatype, valueSet, ipsProject);
        stepfield.getControl().setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER | GridData.FILL_HORIZONTAL));

        toolkit.createLabel(
                workArea,
                NLS.bind(Messages.RangeEditControl_labelIncludeNull, IpsPlugin.getDefault().getIpsPreferences()
                        .getNullPresentation()));
        containsNullCB = toolkit.createCheckbox(workArea);

        if (toolkit.getFormToolkit() != null) {
            toolkit.getFormToolkit().paintBordersFor(workArea);
            toolkit.getFormToolkit().adapt(workArea);
        }
    }

    private void connectToModel() {
        containsNullField = new CheckboxField(containsNullCB);
        uiController.add(upperfield, range, IRangeValueSet.PROPERTY_UPPERBOUND);
        uiController.add(lowerfield, range, IRangeValueSet.PROPERTY_LOWERBOUND);
        uiController.add(stepfield, range, IRangeValueSet.PROPERTY_STEP);
        uiController.add(containsNullField, range, IValueSet.PROPERTY_CONTAINS_NULL);
        uiController.updateUI();
    }

    public IRangeValueSet getRange() {
        return range;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ValueSetType getValueSetType() {
        return ValueSetType.RANGE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canEdit(IValueSet valueSet, ValueDatatype valueDatatype) {
        if (valueSet == null) {
            return false;
        }
        return valueSet.isRange();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IValueSet getValueSet() {
        return range;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValueSet(IValueSet valueSet, ValueDatatype valueDatatype) {
        range = (RangeValueSet)valueSet;
        uiController.remove(upperfield);
        uiController.remove(lowerfield);
        uiController.remove(stepfield);
        uiController.remove(containsNullField);
        uiController.add(upperfield, range, IRangeValueSet.PROPERTY_UPPERBOUND);
        uiController.add(lowerfield, range, IRangeValueSet.PROPERTY_LOWERBOUND);
        uiController.add(stepfield, range, IRangeValueSet.PROPERTY_STEP);
        uiController.add(containsNullField, range, IValueSet.PROPERTY_CONTAINS_NULL);
        uiController.updateUI();
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
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDataChangeable() {
        return dataChangeable;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDataChangeable(boolean changeable) {
        dataChangeable = changeable;

        uiToolkit.setDataChangeable(lowerfield.getControl(), changeable);
        uiToolkit.setDataChangeable(upperfield.getControl(), changeable);
        uiToolkit.setDataChangeable(stepfield.getControl(), changeable);
        uiToolkit.setDataChangeable(containsNullCB, changeable);
    }
}
