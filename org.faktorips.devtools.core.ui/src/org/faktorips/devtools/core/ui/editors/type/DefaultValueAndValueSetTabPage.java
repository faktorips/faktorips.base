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

package org.faktorips.devtools.core.ui.editors.type;

import org.apache.commons.lang.ObjectUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.valueset.IValueSetOwner;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.IpsObjectUIController;
import org.faktorips.devtools.core.ui.controls.TableElementValidator;
import org.faktorips.devtools.core.ui.controls.ValueSetEditControl;
import org.faktorips.util.message.MessageList;

/**
 * 
 * @author Jan Ortmann
 */
public class DefaultValueAndValueSetTabPage extends Composite {

    private EditField defaultValueField;
    private Label labelDefaultValue;
    
    private ValueSetEditControl valueSetEditControl;
    
    // placeholder for the default edit field, the edit field for the default value depends on
    // the attributes datatype
    private Composite defaultEditFieldPlaceholder;
    
    private IValueSetOwner valueSetOwner;
    
    protected IpsObjectUIController uiController;
    
    private UIToolkit uiToolkit;
    
    private ValueDatatype previousDatatype;

    private ValueSetType prevSelectedType;
        
    public DefaultValueAndValueSetTabPage(TabFolder folder, IValueSetOwner owner, IpsObjectUIController uiController, UIToolkit uiToolkit) {
        super(folder, SWT.NONE);
        this.uiController = uiController;
        this.uiToolkit = uiToolkit;
        this.valueSetOwner = owner;
        GridLayout layout = new GridLayout(1, true);
        layout.marginHeight = 12;
        setLayout(layout);
        setLayoutData(new GridData(GridData.FILL_BOTH));
        
        Composite workArea = uiToolkit.createLabelEditColumnComposite(this);
        labelDefaultValue = uiToolkit.createFormLabel(workArea, Messages.DefaultValueAndValueSetTabPage_labelDefaultValue);
        
        defaultEditFieldPlaceholder = uiToolkit.createComposite(workArea);
        defaultEditFieldPlaceholder.setLayout(uiToolkit.createNoMarginGridLayout(1, true));
        defaultEditFieldPlaceholder.setLayoutData(new GridData(GridData.FILL_BOTH));
        createDefaultValueEditField();
        
        //TODO pk the last parameter of this constructor is set to true until the attribute contraints concept is introduced
        //to faktor ips. see the remark in the ValueSetEditControl class
        valueSetEditControl = new ValueSetEditControl(this, uiToolkit,  uiController, valueSetOwner, new Validator(), true);
        
        Object layoutData = valueSetEditControl.getLayoutData();
        if (layoutData instanceof GridData){
            // set the minimum height to show at least the maximum size of the selected ValueSetEditControl
            GridData gd = (GridData)layoutData;
            gd.heightHint = 300;
        }
        adjustLabelWidth();
    }

    private void adjustLabelWidth() {
        if (valueSetEditControl == null){
            return;
        }
        Label valueSetTypeLabel = valueSetEditControl.getLabel();
        // sets the label width of the value set control label, so the control will be horizontal aligned to the default value text
        //  the offset of 7 is calculated by the corresponding composites horizontal spacing and margins
        int widthDefaultLabel = labelDefaultValue.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
        int widthValueSetTypeLabel = valueSetTypeLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
        if (widthDefaultLabel > widthValueSetTypeLabel){
            valueSetEditControl.setLabelWidthHint(widthDefaultLabel + 7);
        } else {
            Object ld = defaultValueField.getControl().getLayoutData();
            if (ld instanceof GridData){
                ((GridData)ld).widthHint = widthValueSetTypeLabel - 7;
                labelDefaultValue.setLayoutData(ld);
            }
        }
    }
    
    public void updateAfterChangeInValueSetOwner() {
        createDefaultValueEditField();
        updateValueSetTypes();
        boolean updateable = valueSetOwner.isValueSetUpdateable();
        defaultValueField.getControl().setEnabled(updateable);
        valueSetEditControl.setEnabled(updateable);
    }

    
    /*
     * Create the default value edit field, if the field exists, recreate it
     */
    private void createDefaultValueEditField() {
        ValueDatatype datatype = null;
        try { 
            datatype = valueSetOwner.getValueDatatype();
        }
        catch (CoreException e) {
            IpsPlugin.log(e);
        }
        if (defaultValueField != null) {
            if (ObjectUtils.equals(datatype, previousDatatype)) {
                return; // same datatype => nothing to do
            } else {
                uiController.remove(defaultValueField);
                defaultValueField.getControl().dispose();
            }
        }
        previousDatatype = datatype;

        ValueDatatypeControlFactory datatypeCtrlFactory = IpsUIPlugin.getDefault().getValueDatatypeControlFactory(datatype);
        defaultValueField = datatypeCtrlFactory.createEditField(uiToolkit, defaultEditFieldPlaceholder, datatype, null, valueSetOwner.getValueSet().getIpsProject());
        defaultValueField.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
        adjustLabelWidth();

        defaultEditFieldPlaceholder.layout();
        defaultEditFieldPlaceholder.getParent().getParent().layout();

        uiController.add(defaultValueField, IPolicyCmptTypeAttribute.PROPERTY_DEFAULT_VALUE);
    }
    
    private void updateValueSetTypes() {
        ValueDatatype datatype = null;
        try { 
            datatype = valueSetOwner.getValueDatatype();
            if (previousDatatype != null){
                // the previous selection was a valid selection, thus store the selection, 
                // to restore it later if a new valid selection is done
                prevSelectedType = valueSetEditControl.getValueSetType();
            }
            previousDatatype = datatype;
            
            if (datatype != null) {
                ValueSetType[] types = ((IIpsObjectPartContainer)valueSetOwner).getIpsProject().getValueSetTypes(datatype);
                valueSetEditControl.setTypes(types, datatype);
                if (prevSelectedType != null){
                    // if the previous selction was a valid selection use this one as new selection in drop down,
                    // otherwise the default (first one) is selected
                    valueSetEditControl.selectValueSetType(prevSelectedType);
                }
            } else {
                valueSetEditControl.setTypes(new ValueSetType[]{ValueSetType.ALL_VALUES}, null);
            }
        }
        catch (CoreException e) {
            IpsPlugin.log(e);
            valueSetEditControl.setTypes(new ValueSetType[]{ValueSetType.ALL_VALUES}, null);
        }
    }

    private class Validator implements TableElementValidator {

        public MessageList validate(String element) {
            MessageList list;
            try {
                list = ((IpsObjectPartContainer)valueSetOwner).validate(((IpsObjectPartContainer)valueSetOwner).getIpsProject());
                // update the ui to set the current messages for all registered controls
                uiController.updateUI();
                return list.getMessagesFor(element);
            } catch (CoreException e) {
                IpsPlugin.log(e);
                return new MessageList();
            }
        }
    }
    
    
}
