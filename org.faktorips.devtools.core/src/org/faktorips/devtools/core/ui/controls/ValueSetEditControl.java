/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.RangeValueSet;
import org.faktorips.devtools.core.model.IEnumValueSet;
import org.faktorips.devtools.core.model.IValueSet;
import org.faktorips.devtools.core.model.ValueSetType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.DefaultUIController;

/**
 * A control to define the type of value set and edit a value set. 
 */
public class ValueSetEditControl extends ControlComposite {

    private Combo validTypesCombo;
    private RangeEditControl rangeControl;
    private Composite enumControl;
    private Composite allValuesControl;
    private ValueDatatype datatype; // The datatype the values in the set are values of.

    private Composite valueSetArea; // is used to change the layout
    private IAttribute attribute;
    private UIToolkit toolkit;
    private DefaultUIController uiController;
    private TableElementValidator tableElementValidator;

    /**
     * Generates a new control which contains a combo box and depending on the value of the box a EnumValueSetEditControl
     * or a or a RangeEditControl.
     * the following general layout is used. the main layout is a gridlayout with one collom. in the first row there is
     * a composite with a gridlayout with 2 columns generated. In the second row there is a stacklayout used . 
     */
    public ValueSetEditControl(Composite parent, UIToolkit toolkit, DefaultUIController uiController, IAttribute attribute,
            TableElementValidator tableElementValidator) {
        super(parent, SWT.NONE);
        this.attribute = attribute;
        this.toolkit = toolkit;
        this.uiController = uiController;
        this.tableElementValidator = tableElementValidator;
        
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
        IValueSet valueSet = attribute.getValueSet();
        getControlForValueSet(valueSet);
        validTypesCombo.setText(valueSet.getValueSetType().getName());
        if (toolkit.getFormToolkit() != null) {
            toolkit.getFormToolkit().adapt(this); // has to be done after the text control is created!
        }
    }

    private Composite getControlForValueSet(IValueSet valueSet) {
    	Composite retValue;
    	if (valueSet.getValueSetType() == ValueSetType.ENUM) {
    		EnumDatatype enumType = null;
    		try {
				Datatype type = this.attribute.findDatatype();
				if (type instanceof EnumDatatype) {
					enumType = (EnumDatatype)type;
				}
			} catch (CoreException e) {
				IpsPlugin.log(e);
			}
			
			if (enumType != null) {
				enumControl = new EnumValueSetChooser(valueSetArea, toolkit, null, (IEnumValueSet)valueSet, enumType, uiController);
			} else {
				enumControl = new EnumValueSetEditControl((IEnumValueSet)valueSet, valueSetArea, tableElementValidator);
			}
			
    		retValue = enumControl;
    	} else if (valueSet.getValueSetType() == ValueSetType.RANGE) {
    		if (rangeControl == null) {
    			rangeControl = new RangeEditControl(valueSetArea, toolkit, (RangeValueSet)valueSet, uiController);
    		}
    		rangeControl.setValueSet(valueSet);
    		retValue = rangeControl;
    	} else {
    		if (allValuesControl == null) {
    			allValuesControl = toolkit.createComposite(valueSetArea);
    		}
    		retValue = allValuesControl;
    	}
        return retValue;
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
        Label label = toolkit.createFormLabel(this, Messages.ValueSetEditControl_labelType);
        GridData labelGridData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
        label.setLayoutData(labelGridData);
        validTypesCombo = toolkit.createCombo(this);

        ValueSetType[] types = ValueSetType.getValueSetTypes();

        for (int i = 0; i < types.length; i++) {
            validTypesCombo.add(types[i].getName());
        }

        validTypesCombo.addModifyListener(new TypeModifyListener());
    }

    public boolean setFocus() {
        return validTypesCombo.setFocus();
    }

    /**
     * Sets the avaibale value set types and the datatype.
     */
    public void setTypes(ValueSetType[] valueSetTypes, ValueDatatype datatype) {
        this.datatype = datatype;
        ValueSetType oldType = getValueSetType();
        ValueSetType newType = valueSetTypes[0];
        
        // needed to reset the value set (and value set type) to the 
        // value it was before the attribute was marked overwriting.
        if (attribute != null && attribute.getValueSet() != null) {
        	oldType = attribute.getValueSet().getValueSetType();
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
    
    private class TypeModifyListener implements ModifyListener {

		/**
		 * {@inheritDoc}
		 */
		public void modifyText(ModifyEvent e) {
            String selectedText = validTypesCombo.getText();
            IValueSet oldValueSet = attribute.getValueSet();
            if (selectedText.equals(ValueSetType.RANGE.getName())) {
            	attribute.setValueSetType(ValueSetType.RANGE);
            	if (oldValueSet.getValueSetType() == ValueSetType.RANGE) {
            		attribute.getValueSet().setValuesOf(oldValueSet);
            	}
            } else if (selectedText.equals(ValueSetType.ENUM.getName())) {
        		attribute.setValueSetType(ValueSetType.ENUM);
        		IEnumValueSet valueSet = (IEnumValueSet)attribute.getValueSet();
            	if (oldValueSet.getValueSetType() == ValueSetType.ENUM) {
            		valueSet.setValuesOf(oldValueSet);
            	}
            	if (datatype instanceof EnumDatatype && valueSet.size() == 0) {
            		valueSet.addValuesFromDatatype((EnumDatatype)datatype);
            		enumControl = (EnumValueSetChooser)getControlForValueSet(valueSet);
            	}
            } else if (selectedText.equals(ValueSetType.ALL_VALUES.getName())) {
            	attribute.setValueSetType(ValueSetType.ALL_VALUES);
            }
            ((StackLayout)valueSetArea.getLayout()).topControl = getControlForValueSet(attribute.getValueSet());

            valueSetArea.layout(); // show the new top control
            valueSetArea.getParent().getParent().layout(); // parent has to resize
		}
    }
}
