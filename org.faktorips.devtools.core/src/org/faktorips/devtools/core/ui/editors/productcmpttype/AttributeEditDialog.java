/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) d�rfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung � Version 0.1 (vor Gr�ndung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpttype;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.model.pctype.Modifier;
import org.faktorips.devtools.core.model.productcmpttype2.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.ui.controller.IpsObjectUIController;
import org.faktorips.devtools.core.ui.controls.DatatypeRefControl;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog2;
import org.faktorips.devtools.core.ui.editors.pctype.Messages;
import org.faktorips.devtools.core.ui.editors.type.DefaultValueAndValueSetTabPage;

/**
 * Dialog to edit an attribute.
 * 
 * @author Jan Ortmann
 */
public class AttributeEditDialog extends IpsPartEditDialog2 {

    /*
     * Folder which contains the pages shown by this dialog. Used to modify which page
     * is shown.
     */
    private TabFolder folder;
    
    private IProductCmptTypeAttribute attribute;

    // the page to edit default value and value set
    private DefaultValueAndValueSetTabPage defaultValueAndValueSetPage;

    
    /**
     * @param part
     * @param parentShell
     * @param windowTitle
     */
    public AttributeEditDialog(IProductCmptTypeAttribute a, Shell parentShell) {
        super(a, parentShell, "Edit Attribute", true);
        this.attribute = a;
    }

    /**
     * {@inheritDoc}
     */
    protected Composite createWorkArea(Composite parent) throws CoreException {
        folder = (TabFolder)parent;
        
        TabItem generalItem = new TabItem(folder, SWT.NONE);
        generalItem.setText("Generell");
        generalItem.setControl(createGeneralPage(folder));

        TabItem valuesItem = new TabItem(folder, SWT.NONE);
        valuesItem.setText("Values");
        valuesItem.setControl(createValueSetPage(folder));
        
        return folder;
    }

    private Control createGeneralPage(TabFolder folder) {

        Composite c = createTabItemComposite(folder, 1, false);
        Composite workArea = uiToolkit.createLabelEditColumnComposite(c);

        uiToolkit.createFormLabel(workArea, "Name:");
        Text nameText = uiToolkit.createText(workArea);
        bindingContext.bindContent(nameText, attribute, IProductCmptTypeAttribute.PROPERTY_NAME);
        
        uiToolkit.createFormLabel(workArea, Messages.AttributeEditDialog_labelDatatype);
        DatatypeRefControl datatypeControl = uiToolkit.createDatatypeRefEdit(attribute.getIpsProject(), workArea);
        datatypeControl.setVoidAllowed(false);
        datatypeControl.setOnlyValueDatatypesAllowed(true);
        bindingContext.bindContent(datatypeControl, attribute, IProductCmptTypeAttribute.PROPERTY_DATATYPE);

        uiToolkit.createFormLabel(workArea, Messages.AttributeEditDialog_labelModifier);
        Combo modifierCombo = uiToolkit.createCombo(workArea, Modifier.getEnumType());
        bindingContext.bindContent(modifierCombo, attribute, IProductCmptTypeAttribute.PROPERTY_MODIFIER, Modifier.getEnumType());
        
//        policyCmptTypeAttributeField.addChangeListener(new ValueChangeListener() {
//
//            public void valueChanged(FieldValueChangedEvent e) {
//                updateEnabledState();
//                // defaultValueAndValueSetPage.updateAfterChangeInValueSetOwner();
//            }
//            
//        });
        
//        datatypeField.addChangeListener(new ValueChangeListener(){
//            public void valueChanged(FieldValueChangedEvent e) {
//                // set the datatype of the attribute without using the ui controller, 
//                // because we need this information when creating the value set controls,
//                // maybe the ui controler notification is to late
//                attribute.setDatatype(e.field.getText());
//                // defaultValueAndValueSetPage.updateAfterChangeInValueSetOwner();
//            }
//        });
        return c;
    }
    
    private Control createValueSetPage(TabFolder folder) {
        IpsObjectUIController uiController = new IpsObjectUIController(attribute);
        defaultValueAndValueSetPage = new DefaultValueAndValueSetTabPage(folder, attribute, uiController, uiToolkit);
        return defaultValueAndValueSetPage;
    }
    
}
