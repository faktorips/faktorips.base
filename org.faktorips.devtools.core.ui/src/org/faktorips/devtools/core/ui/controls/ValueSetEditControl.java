/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
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
import org.faktorips.devtools.core.internal.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.core.internal.model.valueset.RangeValueSet;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSetOwner;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.core.ui.IDataChangeableReadWriteAccess;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.DefaultUIController;
import org.faktorips.devtools.core.ui.controller.fields.ComboField;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;

/**
 * A control to define the type of value set and edit a value set. 
 */
// NOTE: there is a constraint to faktor ips enum types with separate enum content that are used in a
// modeling context. For those the possibility to restrict the values of the datatype must not be
// possible. Hence in this case only all values has to be displayed in the validTypesComboField
public class ValueSetEditControl extends ControlComposite implements IDataChangeableReadWriteAccess{

    private Combo validTypesCombo;
    private ComboField validTypesComboField;
    private RangeEditControl rangeControl;
    private Composite enumControl;
    private Composite allValuesControl;
    private ValueDatatype datatype; // The datatype the values in the set are values of.

    private Composite valueSetArea; // is used to change the layout
    private IValueSetOwner valueSetOwner;
    private UIToolkit toolkit;
    private DefaultUIController uiController;
    private TableElementValidator tableElementValidator;

    private Label typLabel;
    
    private Composite groupComposite;
    private Group group;
    
    private boolean dataChangeable;
    
    //there is a contraint to faktor ips enum types with separate enum content that is used in a modeling context in
    //faktor ips. For those the possibility to restrict the values of the datatype must not be possible. And only all
    //values has to be displayed in the validTypesComboField
    private boolean usedForModelling;
    
    /**
     * Generates a new control which contains a combo box and depending on the value of the box a EnumValueSetEditControl
     * or a or a RangeEditControl.
     * the following general layout is used. the main layout is a gridlayout with one collom. in the first row there is
     * a composite with a gridlayout with 2 columns generated. In the second row there is a stacklayout used . 
     */
    public ValueSetEditControl(Composite parent, UIToolkit toolkit, DefaultUIController uiController, IValueSetOwner owner                                                       ,
            TableElementValidator tableElementValidator, boolean usedForModelling) {
        super(parent, SWT.NONE);
        this.valueSetOwner = owner;
        this.toolkit = toolkit;
        this.uiController = uiController;
        this.tableElementValidator = tableElementValidator;
        this.usedForModelling = usedForModelling;
        
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
        IValueSet valueSet = valueSetOwner.getValueSet();
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
				Datatype type = valueSetOwner.getValueDatatype();
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
                } else {
                    // update ui with the current value set in the model object,
                    // because we reuse the old control and maybe the value set of the attribute has changed in the meanwhile
                    // (e.g. if we select all values then Attribute#setValueSetType is called and the internal enum values are deleted!)
                    ((EnumValueSetChooser)enumControl).setEnumTypeAndValueSet(enumType, (IEnumValueSet)this.valueSetOwner.getValueSet());
                }
            }
            else {
                if (!(enumControl instanceof EnumValueSetEditControl)) {
                    groupComposite = createEnumValueSetGroup(valueSetArea, Messages.ValueSetEditControl_labelAllowedValueSet);
                    enumControl = new EnumValueSetEditControl((IEnumValueSet)valueSet, group, tableElementValidator);
                    enumControl.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_END | GridData.FILL_HORIZONTAL));
                } else {
                    // update ui with the current value set in the model object,
                    // because we reuse the old control and maybe the value set of the attribute has changed in the meanwhile
                    // (e.g. if we select all values then Attribute#setValueSetType is called and the internal enum values are deleted!)
                    ((EnumValueSetEditControl)enumControl).setEnumValueSet((IEnumValueSet)this.valueSetOwner.getValueSet());
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
        
        // update data change state of controls
        setDataChangeable(isDataChangeable());
        
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

        validTypesComboField = new ComboField(validTypesCombo);
        validTypesComboField.addChangeListener(new TypeModifyListener());

        toolkit.setDataChangeable(validTypesCombo, isDataChangeable());
    }

    public boolean setFocus() {
        return validTypesCombo.setFocus();
    }

    /**
     * Sets the available value set types and the datatype.
     */
    public void setTypes(ValueSetType[] valueSetTypes, ValueDatatype datatype) {
        this.datatype = datatype;
        ValueSetType oldType = getValueSetType();
        ValueSetType newType = valueSetTypes[0];
        
        // needed to reset the value set (and value set type) to the 
        // value it was before the attribute was marked overwriting.
        if (valueSetOwner != null && valueSetOwner.getValueSet() != null) {
        	oldType = valueSetOwner.getValueSet().getValueSetType();
        }
        
        validTypesCombo.removeAll();
        //TODO pk 15-06-2009: as long as faktor ips doesn't support attribute value constraints the value set of
        //enum types with separate enum contents cannot be restricted  
        if (usedForModelling && datatype instanceof EnumTypeDatatypeAdapter
                && ((EnumTypeDatatypeAdapter)datatype).hasEnumContent()) {
            validTypesCombo.add(ValueSetType.ALL_VALUES.getName());
        } else {
            for (int i = 0; i < valueSetTypes.length; i++) {
                if (valueSetTypes[i] != ValueSetType.RANGE || datatype instanceof NumericDatatype) {
                    validTypesCombo.add(valueSetTypes[i].getName());
                }
                if (oldType == valueSetTypes[i]) {
                    newType = oldType;
                }
            }
        }
        validTypesComboField.setText(newType.getName());
    }

    /**
     * Returns the value set type selected.
     */
    public ValueSetType getValueSetType() {
        return ValueSetType.getValueSetTypeByName(validTypesCombo.getText());
    }
    
    /**
     * Selectes the given value set type. Or the first item if the value set type is not in the list
     * of available item.
     */
    public void selectValueSetType(ValueSetType valueSetType) {
        validTypesComboField.setText(valueSetType.getName());
    }
    
    private class TypeModifyListener implements ValueChangeListener {
        /**
		 * {@inheritDoc}
		 */
		public void valueChanged(FieldValueChangedEvent e) {
              String selectedText = e.field.getText();
                IValueSet oldValueSet = valueSetOwner.getValueSet();
                if (selectedText.equals(ValueSetType.RANGE.getName())) {
                    valueSetOwner.setValueSetType(ValueSetType.RANGE);
                    if (oldValueSet.getValueSetType() == ValueSetType.RANGE) {
                        valueSetOwner.getValueSet().setValuesOf(oldValueSet);
                    }
                } else if (selectedText.equals(ValueSetType.ENUM.getName())) {
                    valueSetOwner.setValueSetType(ValueSetType.ENUM);
                    IEnumValueSet valueSet = (IEnumValueSet)valueSetOwner.getValueSet();
                    if (oldValueSet.getValueSetType() == ValueSetType.ENUM) {
                        valueSet.setValuesOf(oldValueSet);
                    }
                } else if (selectedText.equals(ValueSetType.ALL_VALUES.getName())) {
                    valueSetOwner.setValueSetType(ValueSetType.ALL_VALUES);
                }
                ((StackLayout)valueSetArea.getLayout()).topControl = getControlForValueSet(valueSetOwner.getValueSet());

                valueSetArea.layout(); // show the new top control
                valueSetArea.getParent().layout(); // parent has to resize
                valueSetArea.getParent().getParent().layout(); // parent has to resize
                uiController.updateUI();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setDataChangeable(boolean changeable) {
        this.dataChangeable = changeable;
        
        toolkit.setDataChangeable(validTypesCombo, changeable);
        toolkit.setDataChangeable(rangeControl, changeable);
        toolkit.setDataChangeable(enumControl, changeable);
        toolkit.setDataChangeable(allValuesControl, changeable);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isDataChangeable() {
        return dataChangeable;
    }
}
