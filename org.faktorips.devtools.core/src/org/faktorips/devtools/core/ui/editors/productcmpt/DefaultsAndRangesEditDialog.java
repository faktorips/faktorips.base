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

package org.faktorips.devtools.core.ui.editors.productcmpt;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.RangeValueSet;
import org.faktorips.devtools.core.model.IEnumValueSet;
import org.faktorips.devtools.core.model.IValueSet;
import org.faktorips.devtools.core.model.ValueSetType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controls.EnumValueSetChooser;
import org.faktorips.devtools.core.ui.controls.RangeEditControl;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog;

/**
 *
 */
public class DefaultsAndRangesEditDialog extends IpsPartEditDialog {

    private IConfigElement configElement;
    
    // edit fields
    private TextField defaultValueField;

    private boolean viewOnly;
    
    /**
     * @param parentShell
     * @param title
     */
    public DefaultsAndRangesEditDialog(IConfigElement configElement, Shell parentShell) {
        this(configElement, parentShell, false);
    }
    
    public DefaultsAndRangesEditDialog(IConfigElement configElement, Shell parentShell, boolean viewOnly) {
        super(configElement, parentShell, Messages.PolicyAttributeEditDialog_editLabel, true);
        this.configElement = configElement;
        this.viewOnly = viewOnly;
    }

    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.editors.EditDialog#createWorkArea(org.eclipse.swt.widgets.Composite)
     */
    protected Composite createWorkArea(Composite parent) throws CoreException {
        TabFolder folder = (TabFolder)parent;

        TabItem firstPage = new TabItem(folder, SWT.NONE);
        firstPage.setText(Messages.PolicyAttributeEditDialog_properties);
        firstPage.setControl(createFirstPage(folder));

        createDescriptionTabItem(folder);
        super.setEnabledDescription(!viewOnly);
        
        return folder;
    }

    private Control createFirstPage(TabFolder folder) {
        Composite c = createTabItemComposite(folder, 1, false);

        Composite workArea = uiToolkit.createLabelEditColumnComposite(c);
        workArea.setLayoutData(new GridData(GridData.FILL_BOTH));
        uiToolkit.createFormLabel(workArea, Messages.PolicyAttributeEditDialog_defaultValue);
        Text defaultValueText = uiToolkit.createText(workArea);
        defaultValueText.setEnabled(!viewOnly);

        defaultValueField = new TextField(defaultValueText);
        Control valueSetControl = createValueSetControl(workArea);
        if (valueSetControl != null) {
            GridData valueSetGridData = new GridData(GridData.FILL_BOTH);
            valueSetGridData.horizontalSpan = 2;
            valueSetControl.setLayoutData(valueSetGridData);
        }
        valueSetControl.setEnabled(!viewOnly);
        return c;
    }

    private Composite createValueSetControl(Composite workArea) {
        try {
            IValueSet valueSet = configElement.getValueSet();
            IValueSet attrValueSet = null;
            IAttribute attribute = configElement.findPcTypeAttribute();
            if (attribute!=null) {
                attrValueSet = attribute.getValueSet();
                if (valueSet.getValueSetType() == ValueSetType.ALL_VALUES) {
                    valueSet = attrValueSet;
                }
            }
            
            if (valueSet.getValueSetType() == ValueSetType.RANGE) {
                RangeEditControl rangeEditControl = new RangeEditControl(workArea, uiToolkit, (RangeValueSet)valueSet, uiController);
                return rangeEditControl;
            } 
            if (valueSet.getValueSetType() == ValueSetType.ENUM) {
            	EnumValueSetChooser chooser = new EnumValueSetChooser(workArea, uiToolkit, (IEnumValueSet)attrValueSet, (IEnumValueSet)valueSet, uiController);
                return chooser;
            } 
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
        return null;
    }

    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.editors.IpsPartEditDialog#connectToModel()
     */
    protected void connectToModel() {
        super.connectToModel();
        uiController.add(defaultValueField, IConfigElement.PROPERTY_VALUE);
    }
}
