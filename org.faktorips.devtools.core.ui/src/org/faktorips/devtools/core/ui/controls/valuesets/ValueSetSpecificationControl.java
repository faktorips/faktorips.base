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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSetOwner;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.core.ui.IDataChangeableReadWriteAccess;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.DefaultUIController;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;
import org.faktorips.devtools.core.ui.controller.fields.ComboField;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.controls.ControlComposite;
import org.faktorips.devtools.core.ui.controls.Messages;

/**
 * A control to specify the value set belonging to a {@link IValueSetOwner} . The control also
 * allows to change the type of the value set. Which value set types are allowed are defined by
 * {@link IValueSetOwner#getAllowedValueSetTypes(org.faktorips.devtools.core.model.ipsproject.IIpsProject)}
 */
public class ValueSetSpecificationControl extends ControlComposite implements IDataChangeableReadWriteAccess {

    private IValueSetOwner valueSetOwner;

    private ValueSetControlEditMode editMode = ValueSetControlEditMode.ALL_KIND_OF_SETS;

    // Label, Combo & Field for the allowed value set types
    private Label valueSetTypeLabel;
    private Combo valueSetTypesCombo;
    private ComboField valueSetTypeField;
    private List<ValueSetType> allowedValueSetTypes = new ArrayList<ValueSetType>();

    private Checkbox concreteValueSetCheckbox = null;
    private CheckboxField concreteValueSetField = null;

    private Control valueSetEditControl; // control showing the value set
    // can be safely casted to IValueSetEditControl

    private Composite valueSetArea; // area around the value set, used to change the layout

    // The last selected value set that is not an unrestricted value set.
    private IValueSet lastRestrictedValueSet;

    private UIToolkit toolkit;
    private DefaultUIController uiController;

    private boolean dataChangeable;
    private Label concreteValueSetLabel;

    /**
     * Creates a new control which contains a combo box and depending on the value of the box a
     * EnumValueSetEditControl or a RangeEditControl. the following general layout is used: the main
     * layout is a gridlayout with one column. In the first row a composite with a 2 column
     * gridlayout is created. In the second row a stacklayout is used to swap the
     * EnumValueSetEditControl and RangeEditControl dynamically.
     */
    public ValueSetSpecificationControl(Composite parent, UIToolkit toolkit, DefaultUIController uiController,
            IValueSetOwner valueSetOwner, List<ValueSetType> allowedValueSetTypes, ValueSetControlEditMode editMode) {
        super(parent, SWT.NONE);
        this.valueSetOwner = valueSetOwner;
        this.toolkit = toolkit;
        this.uiController = uiController;
        this.editMode = editMode;

        initControls(toolkit);
        setAllowedValueSetTypes(allowedValueSetTypes);
    }

    private void initControls(UIToolkit toolkit) {
        initLayout();

        Composite twoColumnArea = toolkit.createLabelEditColumnComposite(this);
        createValueSetTypesCombo(toolkit, twoColumnArea);
        createConcreteValueSetCheckbox(toolkit, twoColumnArea);

        toolkit.createVerticalSpacer(this, 5);
        createValueSetArea(toolkit, this);
        showControlForValueSet();
    }

    private void initLayout() {
        GridLayout mainAreaLayout = new GridLayout(1, false);
        mainAreaLayout.marginHeight = 0;
        mainAreaLayout.marginWidth = 0;
        setLayout(mainAreaLayout);
        setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_BOTH));
    }

    private void createValueSetArea(UIToolkit toolkit, Composite parent) {
        valueSetArea = toolkit.createComposite(parent);
        GridData stackData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_BOTH);
        valueSetArea.setLayoutData(stackData);
        valueSetArea.setLayout(new StackLayout());
    }

    /**
     * Returns the value set being edited.
     */
    public IValueSet getValueSet() {
        return valueSetOwner.getValueSet();
    }

    /**
     * Returns the type of the value set being edited.
     */
    public ValueSetType getValueSetType() {
        return valueSetOwner.getValueSet().getValueSetType();
    }

    public ValueDatatype getValueDatatype() {
        try {
            ValueDatatype datatype = valueSetOwner.findValueDatatype(valueSetOwner.getIpsProject());
            if (datatype == null) {
                return Datatype.STRING;
            }
            return datatype;
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Selects the given value set type. Or the first item if the value set type is not in the list
     * of available item.
     */
    public void setValueSetType(ValueSetType valueSetType) {
        valueSetTypeField.setText(valueSetType.getName());
        // value set in the valueSetOwner is updated through event handling
    }

    /**
     * Returns the edit mode being used.
     */
    public ValueSetControlEditMode getEditMode() {
        return editMode;
    }

    /**
     * Sets the new edit mode. If the new edit mode does not allow to edit abstract value sets, but
     * the current value set being edited is abstract, it is set to not abstract.
     */
    public void setEditMode(ValueSetControlEditMode newEditMode) {
        if (editMode == newEditMode) {
            return;
        }
        editMode = newEditMode;
        IValueSet valueSet = getValueSet();
        if (!editMode.canDefineAbstractSets() && valueSet.isAbstract() && !valueSet.isUnrestricted()) {
            valueSet.setAbstract(false);
        }
        updateUI();
    }

    private Control showControlForValueSet() {
        StackLayout layout = (StackLayout)valueSetArea.getLayout();
        layout.topControl = updateControlWithCurrentValueSetOrCreateNewIfNeccessary(valueSetArea);
        setDataChangeable(isDataChangeable()); // set data changeable state of controls
        return layout.topControl;
    }

    private Control updateControlWithCurrentValueSetOrCreateNewIfNeccessary(Composite parent) {
        IValueSet valueSet = getValueSet();
        if (valueSet.isAbstract() || valueSet.isUnrestricted()) {
            // no further editing possible, return empty composite
            return toolkit.createComposite(parent);
        }
        ValueDatatype valueDatatype = getValueDatatype();
        if (getValueSetEditControl() != null && getValueSetEditControl().canEdit(valueSet, valueDatatype)) {
            // the current composite can be reused to edit the current value set
            getValueSetEditControl().setValueSet(valueSet, valueDatatype);
            return valueSetEditControl.getParent(); // have to return the parent here, as there is a
            // group control (see below) around the edit control. There has to be a better way to do
            // this!
        }
        // Creates a new composite to edit the current value set
        Group group = createGroupAroundValueSet(parent, valueSet.getValueSetType().getName());
        ValueSetEditControlFactory factory = new ValueSetEditControlFactory();
        Control c = factory.newControl(valueSet, valueDatatype, group, toolkit, uiController);
        c.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_BOTH));
        setValueSetEditControl(c);
        return group;
    }

    /**
     * Returns the value set control casted to {@link IValueSetEditControl}. Neccessary as the SWT
     * type Control is a class and not an interface.
     */
    private IValueSetEditControl getValueSetEditControl() {
        return (IValueSetEditControl)valueSetEditControl;
    }

    /**
     * Sets the value set control. Neccessary as the SWT type Control is a class and not an
     * interface.
     */
    private void setValueSetEditControl(Control newValueSetEditControl) {
        valueSetEditControl = newValueSetEditControl;
    }

    private Group createGroupAroundValueSet(Composite parent, String title) {
        Group group = toolkit.createGroup(parent, title);
        GridLayout layout = new GridLayout(1, false);
        layout.marginHeight = 3;
        group.setLayout(layout);
        GridData labelGridData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_BOTH);
        group.setLayoutData(labelGridData);
        return group;
    }

    public int getPreferredLabelWidth() {
        int width1 = valueSetTypeLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
        if (concreteValueSetLabel == null) {
            return width1;
        }
        int width2 = concreteValueSetLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
        return Math.max(width1, width2);
    }

    /**
     * Sets the width of the type label. The method could be used to align the control in the second
     * column with a control in one position (row) above.
     */
    public void setLabelWidthHint(int widthHint) {
        Object layoutData = valueSetTypeLabel.getLayoutData();
        if (layoutData instanceof GridData) {
            ((GridData)layoutData).widthHint = widthHint;
        }
        if (concreteValueSetLabel != null) {
            layoutData = concreteValueSetLabel.getLayoutData();
            if (layoutData instanceof GridData) {
                ((GridData)layoutData).widthHint = widthHint;
            }
        }
    }

    public Label getValueSetTypeLabel() {
        return valueSetTypeLabel;
    }

    private void createValueSetTypesCombo(UIToolkit toolkit, Composite parentArea) {
        valueSetTypeLabel = toolkit.createLabel(parentArea, Messages.ValueSetEditControl_labelType);

        valueSetTypesCombo = toolkit.createCombo(parentArea);
        valueSetTypesCombo.setText(getValueSetType().getName());
        valueSetTypeField = new ComboField(valueSetTypesCombo);
        valueSetTypeField.addChangeListener(new ValueSetTypeModifyListener());

        toolkit.setDataChangeable(valueSetTypesCombo, isDataChangeable());
    }

    private void createConcreteValueSetCheckbox(UIToolkit toolkit, Composite parent) {
        if (!editMode.canDefineAbstractSets()) {
            return; // the user has no choice, so need to create a checkbox
        }
        concreteValueSetLabel = toolkit.createLabel(parent, Messages.ValueSetSpecificationControl_specifyBoundsValues);
        concreteValueSetCheckbox = toolkit.createCheckbox(parent);
        concreteValueSetField = new CheckboxField(concreteValueSetCheckbox);
        updateConcreteValueSetCheckbox();
        concreteValueSetField.addChangeListener(new ValueChangeListener() {

            @Override
            public void valueChanged(FieldValueChangedEvent e) {
                boolean checked = ((Boolean)e.field.getValue());
                getValueSet().setAbstract(!checked);
                updateUI();
            }

        });
    }

    @Override
    public boolean setFocus() {
        return valueSetTypesCombo.setFocus();
    }

    /**
     * Sets the list of value set types the user can select in the Combo box.
     */
    public void setAllowedValueSetTypes(List<ValueSetType> valueSetTypes) {
        allowedValueSetTypes.clear();
        allowedValueSetTypes.addAll(valueSetTypes);
        ValueSetType oldType = getValueSetType();
        ValueSetType newType = valueSetTypes.get(0);

        valueSetTypesCombo.removeAll();
        for (ValueSetType type : valueSetTypes) {
            valueSetTypesCombo.add(type.getName());
            if (oldType == type) {
                newType = oldType;
            }
        }
        valueSetTypeField.setText(newType.getName());
        valueSetTypesCombo.setEnabled(valueSetTypes.size() > 1);
    }

    /**
     * Returns the list of value set types the user can select in the Combo box.
     */
    public List<ValueSetType> getAllowedValueSetTypes() {
        List<ValueSetType> types = new ArrayList<ValueSetType>();
        types.addAll(allowedValueSetTypes);
        return types;
    }

    private class ValueSetTypeModifyListener implements ValueChangeListener {
        /**
         * {@inheritDoc}
         */
        @Override
        public void valueChanged(FieldValueChangedEvent e) {
            String selectedText = e.field.getText();
            ValueSetType newValueSetType = ValueSetType.getValueSetTypeByName(selectedText);
            changeValueSetType(newValueSetType);
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
            newValueSet.setAbstract(getDefaultForAbstractProperty());
        } else {
            newValueSet.setValuesOf(lastRestrictedValueSet);
        }
        updateUI();
    }

    private void updateUI() {
        showControlForValueSet();
        valueSetArea.layout(); // show the new top control
        valueSetArea.getParent().layout(); // parent has to resize
        valueSetArea.getParent().getParent().layout(); // parent has to resize

        uiController.updateUI();
        updateConcreteValueSetCheckbox();
    }

    private void updateConcreteValueSetCheckbox() {
        if (concreteValueSetCheckbox != null) {
            IValueSet valueSet = valueSetOwner.getValueSet();
            boolean checked = !valueSet.isAbstract() && !valueSet.isUnrestricted();
            concreteValueSetCheckbox.setChecked(checked);
            updateConcreteValueSetCheckboxDataChangeableState();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDataChangeable(boolean changeable) {
        dataChangeable = changeable;
        toolkit.setDataChangeable(valueSetTypesCombo, changeable);
        toolkit.setDataChangeable(valueSetEditControl, changeable);
        updateConcreteValueSetCheckboxDataChangeableState();
    }

    private void updateConcreteValueSetCheckboxDataChangeableState() {
        if (!isDataChangeable()) {
            toolkit.setDataChangeable(concreteValueSetCheckbox, false);
            return;
        }
        boolean enabled = !getValueSet().isUnrestricted() && editMode.canDefineAbstractSets();
        toolkit.setDataChangeable(concreteValueSetCheckbox, enabled);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDataChangeable() {
        return dataChangeable;
    }

    @Override
    public void setEnabled(boolean enable) {
        setEnabledIfExistent(valueSetTypeLabel, enable);
        setEnabledIfExistent(valueSetTypesCombo, enable);
        setEnabledIfExistent(concreteValueSetCheckbox, enable);
        setEnabledIfExistent(valueSetEditControl, enable);
        // TODO RangeEditControl#setEnabled() sauber implementieren
    }

    private void setEnabledIfExistent(Control control, boolean enable) {
        if (control != null && !control.isDisposed()) {
            control.setEnabled(enable);
        }
    }

    private boolean getDefaultForAbstractProperty() {
        return editMode.canDefineAbstractSets();
    }

}
