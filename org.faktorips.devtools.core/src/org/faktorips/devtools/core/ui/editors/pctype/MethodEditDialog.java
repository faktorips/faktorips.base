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

package org.faktorips.devtools.core.ui.editors.pctype;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.model.pctype.IMethod;
import org.faktorips.devtools.core.model.pctype.IParameter;
import org.faktorips.devtools.core.model.pctype.Modifier;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;
import org.faktorips.devtools.core.ui.controller.fields.ComboField;
import org.faktorips.devtools.core.ui.controller.fields.EnumValueField;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.controls.DatatypeRefControl;
import org.faktorips.devtools.core.ui.controls.ParametersEditControl;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog;


/**
 *
 */
public class MethodEditDialog extends IpsPartEditDialog  {

    private IMethod method;
    
    // edit fields
    private ComboField modifierField;
    private CheckboxField abstractField;
    private TextField nameField;
    private TextButtonField datatypeField;
    
    private ParametersEditControl parametersControl;
    
    /**
     * @param parentShell
     * @param windowTitle
     */
    public MethodEditDialog(IMethod method, Shell parentShell) {
        super(method, parentShell, Messages.MethodEditDialog_title, true);
        this.method = method;
    }

    /** 
     * {@inheritDoc}
     */
    protected Composite createWorkArea(Composite parent) throws CoreException {
        TabFolder folder = (TabFolder)parent;
        
        TabItem page = new TabItem(folder, SWT.NONE);
        page.setText(Messages.MethodEditDialog_signatureTitle);
        page.setControl(createGeneralPage(folder));
        
        createDescriptionTabItem(folder);
        return folder;
    }
    
    private Control createGeneralPage(TabFolder folder) {
        
        Composite c = createTabItemComposite(folder, 1, false);
        Composite workArea = uiToolkit.createGridComposite(c, 1, false, false);
        ((GridLayout)workArea.getLayout()).verticalSpacing = 20;
        
        // method properties
        Composite propertyPane = uiToolkit.createLabelEditColumnComposite(workArea);
        
        uiToolkit.createFormLabel(propertyPane, Messages.MethodEditDialog_labelAccesModifier);
        Combo modifierCombo = uiToolkit.createCombo(propertyPane, Modifier.getEnumType());
        modifierCombo.setFocus();

        uiToolkit.createFormLabel(propertyPane, Messages.MethodEditDialog_labelAbstract);
        Checkbox abstractCheckbox = uiToolkit.createCheckbox(propertyPane);
        
        uiToolkit.createFormLabel(propertyPane, Messages.MethodEditDialog_labelType);
        DatatypeRefControl datatypeControl = uiToolkit.createDatatypeRefEdit(method.getIpsProject(), propertyPane);
        datatypeControl.setVoidAllowed(true);
        datatypeControl.setOnlyValueDatatypesAllowed(false);
        

        uiToolkit.createFormLabel(propertyPane, Messages.MethodEditDialog_labelName);
        Text nameText = uiToolkit.createText(propertyPane);
        
        // parameters
        parametersControl = new ParametersEditControl(workArea, uiToolkit, SWT.NONE, Messages.MethodEditDialog_labelParameters, method.getIpsProject());
        parametersControl.setDataChangeable(isDataChangeable());
        parametersControl.initControl();
        parametersControl.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        // create fields
        abstractField = new CheckboxField(abstractCheckbox);
        modifierField = new EnumValueField(modifierCombo, Modifier.getEnumType());
        nameField = new TextField(nameText);
        datatypeField = new TextButtonField(datatypeControl);
        
        return c;
    }
    
	protected Point getInitialSize() {
	    return new Point(800, 600);
	}
	
    /** 
     * {@inheritDoc}
     */
    protected void connectToModel() {
        super.connectToModel();
        uiController.add(modifierField, IMethod.PROPERTY_MODIFIER);
        uiController.add(abstractField, IMethod.PROPERTY_ABSTRACT);
        uiController.add(nameField, IMethod.PROPERTY_NAME);
        uiController.add(datatypeField, IMethod.PROPERTY_DATATYPE);
        parametersControl.setInput(method);
    }
    
    /** 
     * {@inheritDoc}
     */
    protected String buildTitle() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(method.getParent().getName());
        buffer.append('.');
        buffer.append(method.getDatatype());
        buffer.append(' ');
        buffer.append(method.getName());
        buffer.append('(');
        IParameter[] params = method.getParameters(); 
        for (int i=0; i<params.length; i++) {
            if (i>0) {
                buffer.append(", "); //$NON-NLS-1$
            }
            buffer.append(params[i].getDatatype());
            buffer.append(' ');
            buffer.append(params[i].getName());
        }
        buffer.append(')');
        return buffer.toString();
        
    }

    /**
     * {@inheritDoc}
     */
    public void setDataChangeable(boolean changeable) {
        super.setDataChangeable(changeable);
        if (parametersControl != null){
            parametersControl.setDataChangeable(changeable);
        }
    }
}
