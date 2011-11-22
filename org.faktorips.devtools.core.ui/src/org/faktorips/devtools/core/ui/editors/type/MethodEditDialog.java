/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.type;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IParameter;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.controls.DatatypeRefControl;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog2;

public class MethodEditDialog extends IpsPartEditDialog2 {

    protected IMethod method;

    protected Combo modifierCombo;
    protected Checkbox abstractCheckbox;
    protected Text nameText;
    protected DatatypeRefControl datatypeControl;

    private ParametersEditControl parametersControl;

    public MethodEditDialog(IMethod method, Shell parentShell) {
        super(method, parentShell, Messages.MethodEditDialog_title, true);
        this.method = method;
    }

    @Override
    protected Composite createWorkAreaThis(Composite parent) {
        TabFolder folder = (TabFolder)parent;

        TabItem page = new TabItem(folder, SWT.NONE);
        page.setText(Messages.MethodEditDialog_signatureTitle);
        page.setControl(createGeneralPage(folder));

        return folder;
    }

    private Control createGeneralPage(TabFolder folder) {

        Composite c = createTabItemComposite(folder, 1, false);
        Composite workArea = getToolkit().createGridComposite(c, 1, false, false);
        ((GridLayout)workArea.getLayout()).verticalSpacing = 20;

        createAdditionalControlsOnGeneralPage(workArea, getToolkit());

        Group methodSignatureGroup = getToolkit().createGroup(workArea, Messages.MethodEditDialog_signatureGroup);
        Composite propertyPane = getToolkit().createLabelEditColumnComposite(methodSignatureGroup);

        getToolkit().createFormLabel(propertyPane, Messages.MethodEditDialog_labelAccesModifier);
        modifierCombo = getToolkit().createCombo(propertyPane);
        modifierCombo.setFocus();
        getBindingContext().bindContent(modifierCombo, method, IMethod.PROPERTY_MODIFIER, Modifier.class);

        getToolkit().createFormLabel(propertyPane, Messages.MethodEditDialog_labelAbstract);
        abstractCheckbox = getToolkit().createCheckbox(propertyPane);
        getBindingContext().bindContent(abstractCheckbox, method, IMethod.PROPERTY_ABSTRACT);

        getToolkit().createFormLabel(propertyPane, Messages.MethodEditDialog_labelType);
        datatypeControl = getToolkit().createDatatypeRefEdit(method.getIpsProject(), propertyPane);
        datatypeControl.setVoidAllowed(true);
        datatypeControl.setOnlyValueDatatypesAllowed(false);
        datatypeControl.setAbstractAllowed(true);
        getBindingContext().bindContent(datatypeControl, method, IMethod.PROPERTY_DATATYPE);

        getToolkit().createFormLabel(propertyPane, Messages.MethodEditDialog_labelName);
        nameText = getToolkit().createText(propertyPane);
        getBindingContext().bindContent(nameText, method, IIpsElement.PROPERTY_NAME);

        // parameters
        parametersControl = new ParametersEditControl(methodSignatureGroup, getToolkit(), SWT.NONE,
                Messages.MethodEditDialog_labelParameters, method.getIpsProject());
        parametersControl.setDataChangeable(isDataChangeable());
        parametersControl.initControl();
        parametersControl.setLayoutData(new GridData(GridData.FILL_BOTH));
        parametersControl.setInput(method);

        return c;
    }

    protected void createAdditionalControlsOnGeneralPage(@SuppressWarnings("unused") Composite parent,
            @SuppressWarnings("unused") UIToolkit toolkit) {

        // nothing to do
    }

    @Override
    protected String buildTitle() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(method.getParent().getName());
        buffer.append('.');
        buffer.append(method.getDatatype());
        buffer.append(' ');
        buffer.append(method.getName());
        buffer.append('(');
        IParameter[] params = method.getParameters();
        for (int i = 0; i < params.length; i++) {
            if (i > 0) {
                buffer.append(", "); //$NON-NLS-1$
            }
            buffer.append(params[i].getDatatype());
            buffer.append(' ');
            buffer.append(params[i].getName());
        }
        buffer.append(')');
        return buffer.toString();

    }

    @Override
    public void setDataChangeable(boolean changeable) {
        super.setDataChangeable(changeable);
        if (parametersControl != null) {
            parametersControl.setDataChangeable(changeable);
        }
    }
}
