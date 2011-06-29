/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.pctype;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.pctype.MessageSeverity;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;
import org.faktorips.devtools.core.ui.controller.fields.EnumValueField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controls.AbstractCheckbox;
import org.faktorips.devtools.core.ui.controls.Checkbox;

/**
 * Helper class to create the UI controls needed to define/edit an {@link IValidationRule}. An
 * instance of this class can be created at any time, its UI isn't created until
 * {@link #initUI(Composite)} is called.
 * <p>
 * To be used wherever a validation rule (definition) is created via GUI. A {@link BindingContext}
 * is used to link model and UI.
 * 
 * @see RuleEditDialog
 * @see AttributeEditDialog
 * 
 * @author Stefan Widmaier, FaktorZehn AG
 */
public class ValidationRuleEditingUI {

    private boolean uiInitialized = false;

    private Text msgText;
    private Label charCount;
    private TextField nameField;
    private TextField msgCodeField;
    private EnumValueField msgSeverityField;
    private TextField msgTextField;
    private final UIToolkit uiToolkit;
    private Checkbox configurableByProductBox;
    private Checkbox defaultActivationBox;

    public ValidationRuleEditingUI(UIToolkit uiToolkit) {
        this.uiToolkit = uiToolkit;
    }

    /**
     * Creates the GUI that is used to edit a {@link IValidationRule}.
     * <p>
     * Call {@link #bindFields(IValidationRule, BindingContext)} to connect/bind the controls to a
     * specific {@link IValidationRule}.
     * 
     * @param workArea composite to create the controls in
     */
    public void initUI(Composite workArea) {
        // general group
        createGeneralGroup(workArea);

        // config group
        createConfigGroup(workArea);

        // message group
        Group msgGroup = uiToolkit.createGroup(workArea, Messages.RuleEditDialog_messageGroupTitle);
        msgGroup.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
        Composite msgComposite = uiToolkit.createLabelEditColumnComposite(msgGroup);
        msgComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        uiToolkit.createFormLabel(msgComposite, Messages.RuleEditDialog_labelCode);
        Text codeText = uiToolkit.createText(msgComposite);
        uiToolkit.createFormLabel(msgComposite, Messages.RuleEditDialog_labelSeverity);
        Combo severityCombo = uiToolkit.createCombo(msgComposite, MessageSeverity.getEnumType());
        Label label = uiToolkit.createFormLabel(msgComposite, Messages.RuleEditDialog_labelText);
        label.getParent().setLayoutData(
                new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING));
        msgText = uiToolkit.createMultilineText(msgComposite);
        msgText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                updateCharCount();
            }
        });
        uiToolkit.createVerticalSpacer(msgComposite, 1);
        charCount = uiToolkit.createFormLabel(msgComposite, ""); //$NON-NLS-1$
        charCount.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, true, true));
        updateCharCount();

        // create fields
        msgCodeField = new TextField(codeText);
        msgTextField = new TextField(msgText);
        msgSeverityField = new EnumValueField(severityCombo, MessageSeverity.getEnumType());

        uiInitialized = true;
    }

    private void createConfigGroup(Composite workArea) {
        Group configGroup = uiToolkit.createGroup(workArea, Messages.AttributeEditDialog_ConfigurationGroup);
        configGroup.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
        Composite nameComposite = uiToolkit.createGridComposite(configGroup, 1, false, false);

        configurableByProductBox = uiToolkit.createCheckbox(nameComposite,
                Messages.RuleEditDialog_Configurable_CheckboxLabel);
        defaultActivationBox = uiToolkit.createCheckbox(nameComposite,
                Messages.RuleEditDialog_ActivatedByDefault_CheckboxLabel);

    }

    private void createGeneralGroup(Composite workArea) {
        Group generalGroup = uiToolkit.createGroup(workArea, Messages.RuleEditDialog_generalTitle);
        generalGroup.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
        Composite nameComposite = uiToolkit.createLabelEditColumnComposite(generalGroup);
        uiToolkit.createFormLabel(nameComposite, Messages.RuleEditDialog_labelName);
        Text nameText = uiToolkit.createText(nameComposite);

        nameText.setFocus();
        nameField = new TextField(nameText);
    }

    private void updateCharCount() {
        String msg = NLS.bind(Messages.RuleEditDialog_contains, new Integer(msgText.getText().length()));
        charCount.setText(msg);
        charCount.getParent().layout();
    }

    public TextField getNameField() {
        return nameField;
    }

    public TextField getMsgCodeField() {
        return msgCodeField;
    }

    public EnumValueField getMsgSeverityField() {
        return msgSeverityField;
    }

    public TextField getMsgTextField() {
        return msgTextField;
    }

    public AbstractCheckbox getConfigurableByProductBox() {
        return configurableByProductBox;
    }

    public AbstractCheckbox getDefaultActivationBox() {
        return defaultActivationBox;
    }

    /**
     * Binds all of this UI's controls to the given {@link IValidationRule} and
     * {@link BindingContext}.
     * 
     * @param rule the {@link IValidationRule} to connect/bind the UI to. The bound rule can then be
     *            edited by the controls created by this class.
     * @param bindingContext the {@link BindingContext} to remove bindings from
     */
    protected void bindFields(IValidationRule rule, BindingContext bindingContext) {
        bindingContext.bindContent(nameField, rule, IValidationRule.PROPERTY_NAME);
        bindingContext.bindContent(msgCodeField, rule, IValidationRule.PROPERTY_MESSAGE_CODE);
        bindingContext.bindContent(msgSeverityField, rule, IValidationRule.PROPERTY_MESSAGE_SEVERITY);
        bindingContext.bindContent(msgTextField, rule, IValidationRule.PROPERTY_MESSAGE_TEXT);
        bindingContext.bindContent(new CheckboxField(configurableByProductBox), rule,
                IValidationRule.PROPERTY_CONFIGURABLE_BY_PRODUCT_COMPONENT);
        bindingContext.bindContent(new CheckboxField(defaultActivationBox), rule,
                IValidationRule.PROPERTY_ACTIVATED_BY_DEFAULT);

        bindingContext.bindEnabled(configurableByProductBox, rule.getIpsObject(),
                IPolicyCmptType.PROPERTY_CONFIGURABLE_BY_PRODUCTCMPTTYPE);
        bindingContext.bindEnabled(defaultActivationBox, rule,
                IValidationRule.PROPERTY_CONFIGURABLE_BY_PRODUCT_COMPONENT);
    }

    public void setFocusToNameField() {
        nameField.getControl().setFocus();
    }

    /**
     * Returns <code>true</code> after {@link #initUI(Composite)} has been called.
     * <p>
     * If this method returns <code>true</code> all fields and controls available through getters
     * have been initialized.
     * 
     * @return <code>true</code> if the ui for a validation rule has been initialized,
     *         <code>false</code> otherwise.
     */
    public boolean isUiInitialized() {
        return uiInitialized;
    }

    /**
     * Removes all bindings for this UI's controls from the given context.
     * 
     * @param bindingContext the {@link BindingContext} to remove bindings from
     */
    protected void removeBindingsFromContext(BindingContext bindingContext) {
        bindingContext.removeBindings(nameField.getControl());
        bindingContext.removeBindings(msgCodeField.getControl());
        bindingContext.removeBindings(msgSeverityField.getControl());
        bindingContext.removeBindings(msgTextField.getControl());
        bindingContext.removeBindings(configurableByProductBox);
        bindingContext.removeBindings(defaultActivationBox);
    }

}
