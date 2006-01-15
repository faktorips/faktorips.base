package org.faktorips.devtools.core.ui.controls;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.EnumValueSet;
import org.faktorips.devtools.core.model.Range;
import org.faktorips.devtools.core.model.ValueSet;
import org.faktorips.devtools.core.model.ValueSetType;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.DefaultUIController;
import org.faktorips.values.DefaultEnumType;

/**
 * A control to define the type of value set and edit a value set. 
 */
public class ValueSetEditControl extends ControlComposite {

    private Combo validTypesCombo;
    private RangeEditControl rangeControl;
    private EnumValueSetEditControl enumControl;
    private ValueSetChangeListener valueSetChangeListener;
    private ValueDatatype datatype; // The datatype the values in the set are values of.

    private Composite valueSetArea; // is used to change the layout

    /**
     * Generates a new control which contains a combo box and depending on the value of the box a EnumValueSetEditControl
     * or a or a RangeEditControl.
     * the following general layout is used. the main layout is a gridlayout with one collom. in the first row there is
     * a composite with a gridlayout with 2 columns generated. In the second row there is a stacklayout used . 
     */
    public ValueSetEditControl(Composite parent, UIToolkit toolkit, DefaultUIController uiController, ValueSet valueSet,
            TableElementValidator tableElementValidator) {
        super(parent, SWT.NONE);
        initLayout();
        Composite parentArea;
        if (toolkit.getFormToolkit() == null) {
            parentArea = this;
        } else {
            parentArea = toolkit.getFormToolkit().createComposite(this);
            GridLayout formAreaLayout = new GridLayout(1, false);
            formAreaLayout.marginHeight = 3;
            formAreaLayout.marginWidth = 1;
            parentArea.setLayout(formAreaLayout);
        }
        parentArea.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_END | GridData.FILL_HORIZONTAL));
        createValidTypesCombo(toolkit, parentArea);
        valueSetArea = createValueCntrlArea(toolkit, parentArea);
        if (valueSet.isEnum()) {
            enumControl = new EnumValueSetEditControl((EnumValueSet)valueSet, valueSetArea, tableElementValidator);
            rangeControl = new RangeEditControl(valueSetArea, toolkit, new Range(), uiController);
        } else if (valueSet.isRange()) {
            enumControl = new EnumValueSetEditControl(new EnumValueSet(), valueSetArea, tableElementValidator);
            rangeControl = new RangeEditControl(valueSetArea, toolkit, (Range)valueSet, uiController);
        } else {
            enumControl = new EnumValueSetEditControl(new EnumValueSet(), valueSetArea, tableElementValidator);
            rangeControl = new RangeEditControl(valueSetArea, toolkit, new Range(), uiController);
        }
        validTypesCombo.setText(valueSet.getValueSetType().getName());
        if (toolkit.getFormToolkit() != null) {
            toolkit.getFormToolkit().adapt(this); // has to be done after the text control is created!
        }
    }

    private void initLayout() {
        GridLayout mainAreaLayout = new GridLayout(2, false);
        mainAreaLayout.marginHeight = 0;
        mainAreaLayout.marginWidth = 0;
        setLayout(mainAreaLayout);
    }

    private Composite createValueCntrlArea(UIToolkit toolkit, Composite parentArea) {
        Composite valueArea = toolkit.createComposite(parentArea);
        GridData stackData = new GridData(GridData.VERTICAL_ALIGN_END | GridData.FILL_HORIZONTAL);
        stackData.horizontalSpan = 2;
        valueArea.setLayoutData(stackData);
        valueArea.setLayout(new StackLayout());
        return valueArea;
    }

    private void createValidTypesCombo(UIToolkit toolkit, Composite parentArea) {
        Label label = toolkit.createFormLabel(this, "Type:");
        GridData labelGridData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
        label.setLayoutData(labelGridData);
        validTypesCombo = toolkit.createCombo(this);

        ValueSetType[] types = ValueSetType.getValueSetTypes();

        for (int i = 0; i < types.length; i++) {
            validTypesCombo.add(types[i].getName());
        }

        validTypesCombo.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                if (valueSetChangeListener == null) {
                    return;
                }
                Control topControl = ((StackLayout)valueSetArea.getLayout()).topControl; 
                String selectedText = validTypesCombo.getText();
                if (selectedText.equals(ValueSetType.RANGE.getName())) {
                    topControl = rangeControl;
                    valueSetChangeListener.valueSetChanged(rangeControl.getRange()); // fires a change event
                } else {
                    if (selectedText.equals(ValueSetType.ENUM.getName())) {
                        if (topControl==null && datatype!=null && datatype instanceof DefaultEnumType && enumControl.getEnumValueSet().getNumOfValues()==0) {
                            // until now the value set was AllValues, now the user has selected enumeration
                            // the datatype itself is an enum type
                            // => so default the enum value set with the values from the type.
                            enumControl.setValueSet(new EnumValueSet((DefaultEnumType )datatype));
                        }
                        topControl = enumControl;
                        valueSetChangeListener.valueSetChanged(enumControl.getEnumValueSet()); // fires a change event
                    } else {
                        if (selectedText.equals(ValueSetType.ALL_VALUES.getName())) {
                            valueSetChangeListener.valueSetChanged(ValueSet.ALL_VALUES); // fires a change event
                        }
                    }
                }
                ((StackLayout)valueSetArea.getLayout()).topControl = topControl;
                valueSetArea.layout(); // Displaying the changes
            }
        });
    }

    public boolean setFocus() {
        return validTypesCombo.setFocus();
    }

    /**
     * Sets the ValueSetChangeListener to the two controls which change the valueset
     */
    public void setValueSetChangelistener(ValueSetChangeListener valuesetchangelistener) {
        rangeControl.setValueSetChangeListener(valuesetchangelistener);
        enumControl.setValueSetChangeListener(valuesetchangelistener);
        this.valueSetChangeListener = valuesetchangelistener;
    }

    /**
     * Sets the avaibale value set types and the datatype.
     */
    public void setTypes(ValueSetType[] valueSetTypes, ValueDatatype datatype) {
        this.datatype = datatype;
        ValueSetType oldType = getValueSetType();
        ValueSetType newType = valueSetTypes[0];
        if (((StackLayout)valueSetArea.getLayout()).topControl == enumControl) {
            enumControl.refresh();
        }
        validTypesCombo.removeAll();
        for (int i = 0; i < valueSetTypes.length; i++) {
            validTypesCombo.add(valueSetTypes[i].getName());
            if (oldType == valueSetTypes[i]) {
                newType = oldType;
            }
        }
        validTypesCombo.setText(newType.getName());
    }

    /**
     * Returns the value set type selected.
     */
    public ValueSetType getValueSetType() {
        return ValueSetType.getValueSetTypeByName(validTypesCombo.getText());
    }
}
