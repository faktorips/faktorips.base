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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.NumericDatatype;
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

    private Label typLabel;
    
    private Composite groupComposite;
    private Group group;
    
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
        valueSetArea = createValueControlArea(toolkit, parentArea);
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
                if (!(enumControl instanceof EnumValueSetChooser)) {
                    groupComposite = createEnumValueSetGroup(valueSetArea, Messages.ValueSetEditControl_labelAllowedValueSet);
                    enumControl = new EnumValueSetChooser(group, toolkit, null, (IEnumValueSet)valueSet, enumType,
                            uiController);
                    enumControl.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_END | GridData.FILL_HORIZONTAL));
                }
            }
            else {
                if (!(enumControl instanceof EnumValueSetEditControl)) {
                    groupComposite = createEnumValueSetGroup(valueSetArea, Messages.ValueSetEditControl_labelAllowedValueSet);
                    enumControl = new EnumValueSetEditControl((IEnumValueSet)valueSet, group, tableElementValidator);
                    enumControl.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_END | GridData.FILL_HORIZONTAL));
                }
            }
            
            retValue = groupComposite;
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

    private Composite createEnumValueSetGroup(Composite parent, String title) {
        Composite composite = toolkit.createComposite(parent);
        GridData gd = new GridData(GridData.VERTICAL_ALIGN_END | GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        composite.setLayoutData(gd);
        GridLayout layout = new GridLayout(1, false);
        layout.marginHeight = 10;
        layout.marginWidth = 0;
        composite.setLayout(layout);
        
        group = toolkit.createGroup(composite, title);
        group.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_END | GridData.FILL_HORIZONTAL));
        group.setLayout(new GridLayout(1, true));
        return composite;
    }
    
    private void initLayout() {
        GridLayout mainAreaLayout = new GridLayout(2, false);
        mainAreaLayout.marginHeight = 0;
        mainAreaLayout.marginWidth = 0;
        setLayout(mainAreaLayout);
    }

    private Composite createValueControlArea(UIToolkit toolkit, Composite parentArea) {
        Composite valueArea = toolkit.createComposite(parentArea);
        GridData stackData = new GridData(GridData.VERTICAL_ALIGN_END | GridData.FILL_HORIZONTAL);
        stackData.horizontalSpan = 2;
        valueArea.setLayoutData(stackData);
        valueArea.setLayout(new StackLayout());
        return valueArea;
    }

    /**
     * Sets the width of the type label. The method could be used to align the control in the second
     * column with a control in one position (row) above.
     */
    public void setLabelWidthHint(int widthHint){
        Object layoutData = typLabel.getLayoutData();
        if (layoutData instanceof GridData){
            ((GridData)layoutData).widthHint = widthHint;
        }
    }
    
    public Label getLabel(){
        return typLabel;
    }
    
    private void createValidTypesCombo(UIToolkit toolkit, Composite parentArea) {
        typLabel = toolkit.createFormLabel(this, Messages.ValueSetEditControl_labelType);
        GridData labelGridData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
        typLabel.setLayoutData(labelGridData);
        validTypesCombo = toolkit.createCombo(this);

        ValueSetType[] types = ValueSetType.getValueSetTypes();

        for (int i = 0; i < types.length; i++) {
            if (types[i] != ValueSetType.RANGE || datatype instanceof NumericDatatype) {
                validTypesCombo.add(types[i].getName());
            }
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
            if (valueSetTypes[i] != ValueSetType.RANGE || datatype instanceof NumericDatatype) {
                validTypesCombo.add(valueSetTypes[i].getName());
            }
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
            } else if (selectedText.equals(ValueSetType.ALL_VALUES.getName())) {
            	attribute.setValueSetType(ValueSetType.ALL_VALUES);
            }
            ((StackLayout)valueSetArea.getLayout()).topControl = getControlForValueSet(attribute.getValueSet());

            valueSetArea.layout(); // show the new top control
            valueSetArea.getParent().layout(); // parent has to resize
            valueSetArea.getParent().getParent().layout(); // parent has to resize
		}
    }
}
