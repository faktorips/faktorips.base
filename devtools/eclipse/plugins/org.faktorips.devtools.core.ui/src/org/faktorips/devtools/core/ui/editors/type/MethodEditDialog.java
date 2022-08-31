/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.type;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.DatatypeRefControl;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog2;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsobject.Modifier;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.model.type.IMethod;
import org.faktorips.devtools.model.type.ITypePart;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.runtime.util.StringBuilderJoiner;

public class MethodEditDialog extends IpsPartEditDialog2 {

    private IMethod method;
    private Combo modifierCombo;
    private Button abstractCheckbox;
    private Text nameText;
    private DatatypeRefControl datatypeControl;

    private Button changeOverTimeCheckbox;
    private ParametersEditControl parametersControl;

    public MethodEditDialog(IMethod method, Shell parentShell) {
        super(method, parentShell, Messages.MethodEditDialog_title, true);
        this.method = method;
    }

    public IMethod getMethod() {
        return method;
    }

    public Combo getModifierCombo() {
        return modifierCombo;
    }

    public Button getAbstractCheckbox() {
        return abstractCheckbox;
    }

    public Text getNameText() {
        return nameText;
    }

    public DatatypeRefControl getDatatypeControl() {
        return datatypeControl;
    }

    public Button getChangeOverTimeCheckbox() {
        return changeOverTimeCheckbox;
    }

    public ParametersEditControl getParametersControl() {
        return parametersControl;
    }

    @Override
    protected Composite createWorkAreaThis(Composite parent) {
        TabFolder folder = (TabFolder)parent;

        TabItem page = new TabItem(folder, SWT.NONE);
        page.setText(Messages.MethodEditDialog_signatureTitle);
        page.setControl(createGeneralPage(folder));

        return folder;
    }

    protected Control createGeneralPage(TabFolder folder) {

        Composite c = createTabItemComposite(folder, 1, false);

        Composite workArea = getToolkit().createGridComposite(c, 1, false, false);

        ((GridLayout)workArea.getLayout()).verticalSpacing = 20;

        createAdditionalControlsOnGeneralPage(workArea, getToolkit());

        Group methodSignatureGroup = getToolkit().createGroup(workArea, Messages.MethodEditDialog_signatureGroup);
        Composite propertyPane = getToolkit().createLabelEditColumnComposite(methodSignatureGroup);

        createModifierLabel(propertyPane);
        if (isProdCmptTypeEditDialog()) {
            createChangingOverTimeCheckbox(propertyPane);
        }
        createAbstractCheckbox(propertyPane);
        createDatatypeSection(propertyPane);
        createNameLabel(propertyPane);

        // parameters
        parametersControl = new ParametersEditControl(methodSignatureGroup, getToolkit(), SWT.NONE,
                Messages.MethodEditDialog_labelParameters, method.getIpsProject());
        parametersControl.setDataChangeable(isDataChangeable());
        parametersControl.initControl();
        parametersControl.setLayoutData(new GridData(GridData.FILL_BOTH));
        parametersControl.setInput(method);

        return c;
    }

    private void createModifierLabel(Composite propertyPane) {
        getToolkit().createFormLabel(propertyPane, Messages.MethodEditDialog_labelAccesModifier);
        modifierCombo = getToolkit().createCombo(propertyPane);
        modifierCombo.setFocus();
        getBindingContext().bindContent(modifierCombo, method, ITypePart.PROPERTY_MODIFIER, Modifier.class);
    }

    private void createAbstractCheckbox(Composite propertyPane) {
        getToolkit().createFormLabel(propertyPane, IpsStringUtils.EMPTY);
        abstractCheckbox = getToolkit().createButton(propertyPane, Messages.MethodEditDialog_labelAbstract, SWT.CHECK);
        getBindingContext().bindContent(abstractCheckbox, method, IMethod.PROPERTY_ABSTRACT);
    }

    private void createNameLabel(Composite propertyPane) {
        getToolkit().createFormLabel(propertyPane, Messages.MethodEditDialog_labelName);
        nameText = getToolkit().createText(propertyPane);
        getBindingContext().bindContent(nameText, method, IIpsElement.PROPERTY_NAME);
    }

    protected boolean isProdCmptTypeEditDialog() {
        return false;
    }

    private void createChangingOverTimeCheckbox(Composite propertyPane) {
        getToolkit().createFormLabel(propertyPane, IpsStringUtils.EMPTY);
        changeOverTimeCheckbox = getToolkit().createButton(
                propertyPane,
                NLS.bind(Messages.MethodEditDialog_labelChangeOverTimeCheckbox, IpsPlugin.getDefault()
                        .getIpsPreferences().getChangesOverTimeNamingConvention().getGenerationConceptNamePlural()),
                SWT.CHECK);
        getBindingContext().bindContent(changeOverTimeCheckbox, method,
                IProductCmptTypeMethod.PROPERTY_CHANGING_OVER_TIME);
    }

    private void createDatatypeSection(Composite propertyPane) {
        getToolkit().createFormLabel(propertyPane, Messages.MethodEditDialog_labelType);
        datatypeControl = getToolkit().createDatatypeRefEdit(method.getIpsProject(), propertyPane);
        datatypeControl.setVoidAllowed(true);
        datatypeControl.setOnlyValueDatatypesAllowed(false);
        datatypeControl.setAbstractAllowed(true);
        getBindingContext().bindContent(datatypeControl, method, IMethod.PROPERTY_DATATYPE);
    }

    protected void createAdditionalControlsOnGeneralPage(@SuppressWarnings("unused") Composite parent,
            @SuppressWarnings("unused") UIToolkit toolkit) {

        // nothing to do
    }

    @Override
    protected String buildTitle() {
        StringBuilder sb = new StringBuilder();
        sb.append(method.getParent().getName());
        sb.append('.');
        sb.append(method.getDatatype());
        sb.append(' ');
        sb.append(method.getName());
        sb.append('(');
        StringBuilderJoiner.join(sb, method.getParameters(), p -> {
            sb.append(p.getDatatype());
            sb.append(' ');
            sb.append(p.getName());
        });
        sb.append(')');
        return sb.toString();

    }

    @Override
    public void setDataChangeable(boolean changeable) {
        super.setDataChangeable(changeable);
        if (parametersControl != null) {
            parametersControl.setDataChangeable(changeable);
        }
    }
}
