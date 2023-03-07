/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.pctype.rule;

import java.util.Locale;
import java.util.Set;

import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.ExtensionPropertyControlFactory;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.binding.InternationalStringPresentationObject;
import org.faktorips.devtools.core.ui.binding.IpsObjectPartPmo;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;
import org.faktorips.devtools.core.ui.controller.fields.ComboViewerField;
import org.faktorips.devtools.core.ui.controller.fields.EnumField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.editors.CategoryPmo;
import org.faktorips.devtools.core.ui.editors.pctype.AttributeEditDialog;
import org.faktorips.devtools.core.ui.editors.pctype.Messages;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.extproperties.IExtensionPropertyDefinition;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.ISupportedLanguage;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.devtools.model.pctype.MessageSeverity;
import org.faktorips.devtools.model.productcmpttype.IProductCmptCategory;

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
 */
public class ValidationRuleEditingUI {

    private final UIToolkit uiToolkit;

    private boolean uiInitialized = false;

    private CharCountPainter charCountPainter;
    private TextField nameField;
    private TextField msgCodeField;
    private EnumField<MessageSeverity> msgSeverityField;
    private TextField msgTextField;
    private Checkbox changingOverTimeBox;
    private Checkbox configurableByProductBox;
    private Checkbox defaultActivationBox;

    private ComboViewerField<Locale> localeComboField;

    private ComboViewerField<IProductCmptCategory> categoryField;

    private ExtensionPropertyControlFactory extFactory;

    public ValidationRuleEditingUI(UIToolkit uiToolkit) {
        this.uiToolkit = uiToolkit;
        extFactory = new ExtensionPropertyControlFactory(IValidationRule.class);
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
        GridData layoutData = new GridData(GridData.FILL_BOTH);
        msgComposite.setLayoutData(layoutData);

        uiToolkit.createFormLabel(msgComposite, Messages.RuleEditDialog_labelCode);
        Text codeText = uiToolkit.createText(msgComposite);
        uiToolkit.createFormLabel(msgComposite, Messages.RuleEditDialog_labelSeverity);
        Combo severityCombo = uiToolkit.createCombo(msgComposite);

        // text group
        Group textGroup = uiToolkit.createGroup(msgComposite, Messages.RuleEditDialog_groupText);
        ((GridData)textGroup.getLayoutData()).horizontalSpan = 2;
        Combo localeCombo = uiToolkit.createCombo(textGroup);

        Text msgText = uiToolkit.createMultilineText(textGroup);
        GridData msgTextLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        msgTextLayoutData.heightHint = 50;
        msgTextLayoutData.widthHint = UIToolkit.DEFAULT_WIDTH;
        msgText.setLayoutData(msgTextLayoutData);
        msgText.addModifyListener($ -> updateCharCount());

        charCountPainter = new CharCountPainter();
        msgText.addPaintListener(charCountPainter);

        // create fields
        localeComboField = new ComboViewerField<>(localeCombo, Locale.class);
        localeComboField.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                if (element instanceof Locale locale) {
                    return locale.getDisplayLanguage();
                }
                return super.getText(element);
            }
        });

        msgCodeField = new TextField(codeText);
        msgTextField = new TextField(msgText);
        msgSeverityField = new EnumField<>(severityCombo, MessageSeverity.class);

        updateCharCount();
        uiInitialized = true;
    }

    private void createConfigGroup(Composite workArea) {
        Group configGroup = uiToolkit.createGroup(workArea, Messages.AttributeEditDialog_ConfigurationGroup);
        configGroup.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));

        Composite labelEditColumnComposite = uiToolkit.createLabelEditColumnComposite(configGroup);
        GridData checkboxLayoutData = new GridData(GridData.FILL_HORIZONTAL);
        checkboxLayoutData.horizontalSpan = 2;

        configurableByProductBox = uiToolkit.createCheckbox(labelEditColumnComposite,
                Messages.RuleEditDialog_Configurable_CheckboxLabel);
        configurableByProductBox.setLayoutData(checkboxLayoutData);

        changingOverTimeBox = uiToolkit.createCheckbox(labelEditColumnComposite,
                NLS.bind(Messages.RuleEditDialog_ChangingOverTime_CheckboxLabel, IpsPlugin.getDefault()
                        .getIpsPreferences().getChangesOverTimeNamingConvention().getGenerationConceptNamePlural()));
        changingOverTimeBox.setLayoutData(checkboxLayoutData);

        defaultActivationBox = uiToolkit.createCheckbox(labelEditColumnComposite,
                Messages.RuleEditDialog_ActivatedByDefault_CheckboxLabel);
        defaultActivationBox.setLayoutData(checkboxLayoutData);

        uiToolkit.createFormLabel(labelEditColumnComposite, Messages.RuleEditDialog_labelCategory);
        createCategoryCombo(labelEditColumnComposite);

        createExtFactoryControls(labelEditColumnComposite);
    }

    private void createExtFactoryControls(Composite workArea) {
        if (extFactory.needsToCreateControlsFor(IExtensionPropertyDefinition.POSITION_BOTTOM)) {
            extFactory.createControls(workArea, uiToolkit, null, IExtensionPropertyDefinition.POSITION_BOTTOM);
        }
    }

    private void createCategoryCombo(Composite workArea) {
        Combo categoryCombo = uiToolkit.createCombo(workArea);
        categoryField = new ComboViewerField<>(categoryCombo, IProductCmptCategory.class);
        categoryField.setAllowEmptySelection(true);
        categoryField.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                IProductCmptCategory category = (IProductCmptCategory)element;
                return IIpsModel.get().getMultiLanguageSupport().getLocalizedLabel(category);
            }
        });
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
        String msg = NLS.bind(Messages.RuleEditDialog_contains, Integer.valueOf(msgTextField.getText().length()));
        charCountPainter.setText(msg);
        msgTextField.getTextControl().redraw();
    }

    /**
     * Binds all of this UI's controls to the given {@link IValidationRule} and
     * {@link BindingContext}.
     * 
     * @param rule the {@link IValidationRule} to connect/bind the UI to. The bound rule can then be
     *            edited by the controls created by this class.
     * @param bindingContext the {@link BindingContext} to remove bindings from
     */
    public void bindFields(IValidationRule rule, BindingContext bindingContext) {
        bindingContext.bindContent(nameField, rule, IValidationRule.PROPERTY_NAME);
        bindingContext.bindContent(msgSeverityField, rule, IValidationRule.PROPERTY_MESSAGE_SEVERITY);
        final InternationalStringPresentationObject msgTextPMO = new InternationalStringPresentationObject(
                rule.getMessageText());

        localeComboField.setInput(getSupportedLocales(rule.getIpsProject()));
        bindingContext.bindContent(localeComboField, msgTextPMO, InternationalStringPresentationObject.PROPERTY_LOCALE);

        msgTextPMO.setLocale(rule.getIpsProject().getReadOnlyProperties().getDefaultLanguage().getLocale());
        bindingContext.bindContent(msgTextField, msgTextPMO, InternationalStringPresentationObject.PROPERTY_TEXT);

        bindingContext.bindContent(new CheckboxField(configurableByProductBox), rule,
                IValidationRule.PROPERTY_CONFIGURABLE_BY_PRODUCT_COMPONENT);
        bindingContext.bindContent(new CheckboxField(changingOverTimeBox), rule,
                IValidationRule.PROPERTY_CHANGING_OVER_TIME);
        bindingContext.bindContent(new CheckboxField(defaultActivationBox), rule,
                IValidationRule.PROPERTY_ACTIVATED_BY_DEFAULT);

        extFactory.bind(rule, bindingContext);

        bindingContext.bindEnabled(configurableByProductBox, rule.getIpsObject(),
                IPolicyCmptType.PROPERTY_CONFIGURABLE_BY_PRODUCTCMPTTYPE);
        bindingContext.bindEnabled(changingOverTimeBox, rule,
                IValidationRule.PROPERTY_CONFIGURABLE_BY_PRODUCT_COMPONENT);
        bindingContext.bindEnabled(defaultActivationBox, rule,
                IValidationRule.PROPERTY_CONFIGURABLE_BY_PRODUCT_COMPONENT);
        bindingContext.bindEnabled(categoryField.getControl(), rule,
                IValidationRule.PROPERTY_CONFIGURABLE_BY_PRODUCT_COMPONENT);

        CategoryPmo categoryPmo = new CategoryPmo(rule);
        categoryField.setInput(categoryPmo.getCategories());
        bindingContext.bindContent(categoryField, categoryPmo, CategoryPmo.PROPERTY_CATEGORY);
        MsgCodePMO msgCodePmo = new MsgCodePMO(rule);
        bindingContext.bindContent(msgCodeField, msgCodePmo, MsgCodePMO.MSG_CODE);
    }

    private Locale[] getSupportedLocales(IIpsProject ipsProject) {
        Set<ISupportedLanguage> supportedLanguages = ipsProject.getReadOnlyProperties().getSupportedLanguages();
        Locale[] result = new Locale[supportedLanguages.size()];
        int i = 0;
        for (ISupportedLanguage supportedLanguage : supportedLanguages) {
            result[i] = supportedLanguage.getLocale();
            i++;
        }
        return result;
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
     *             <code>false</code> otherwise.
     */
    public boolean isUiInitialized() {
        return uiInitialized;
    }

    /**
     * Removes all bindings for this UI's controls from the given context.
     * 
     * @param bindingContext the {@link BindingContext} to remove bindings from
     */
    public void removeBindingsFromContext(BindingContext bindingContext) {
        bindingContext.removeBindings(nameField.getControl());
        bindingContext.removeBindings(categoryField.getControl());
        bindingContext.removeBindings(msgCodeField.getControl());
        bindingContext.removeBindings(msgSeverityField.getControl());
        bindingContext.removeBindings(configurableByProductBox);
        bindingContext.removeBindings(changingOverTimeBox);
        bindingContext.removeBindings(defaultActivationBox);
        bindingContext.removeBindings(localeComboField.getControl());
        bindingContext.removeBindings(msgTextField.getControl());
    }

    private static class CharCountPainter implements PaintListener {

        private String msg;

        @Override
        public void paintControl(PaintEvent e) {
            GC gc = e.gc;
            ColorRegistry colorRegistry = JFaceResources.getColorRegistry();
            Color color = colorRegistry.get(JFacePreferences.QUALIFIER_COLOR);
            if (color != null && !color.isDisposed()) {
                gc.setForeground(color);
            }
            Control control = (Control)e.getSource();
            int x = (control.getSize().x - gc.textExtent(msg).x) - control.getBorderWidth() - 2;
            int y = (control.getSize().y - gc.textExtent(msg).y) - control.getBorderWidth() - 2;
            gc.drawText(msg, x, y, true);
        }

        public void setText(String msg) {
            this.msg = msg;

        }

    }

    public static class MsgCodePMO extends IpsObjectPartPmo {

        private static final String DELIMITER = "."; //$NON-NLS-1$

        private static final String DELIMITER_REGEXT = "\\" + DELIMITER; //$NON-NLS-1$

        private static final String MSG_CODE = "messageCode"; //$NON-NLS-1$

        private IValidationRule validationRule;

        public MsgCodePMO(IValidationRule validationRule) {
            super(validationRule);
            this.validationRule = validationRule;
        }

        public void setMessageCode(String messageCode) {
            validationRule.setMessageCode(messageCode);
        }

        public String getMessageCode() {
            return validationRule.getMessageCode();
        }

        @Override
        protected void partHasChanged() {
            checkMessageCode();
        }

        private void checkMessageCode() {
            String codeText = validationRule.getMessageCode();
            if (needToUpdateMsgCode(codeText)) {
                updateMessageCode();
            }
        }

        private boolean needToUpdateMsgCode(String codeText) {
            if (codeText.isEmpty()) {
                return true;
            } else {
                return isGeneratedMsgCode(codeText);
            }
        }

        private boolean isGeneratedMsgCode(String codeText) {
            String[] splittedMsgCode = splitCodeMsgByPointsInText(codeText);
            if (splittedMsgCode.length == 3) {
                if (isPolicyNameInMsgCode(splittedMsgCode)) {
                    if (isSeverityOrNameInMsgCode(splittedMsgCode)) {
                        return true;
                    }
                }
            }
            return false;
        }

        private String[] splitCodeMsgByPointsInText(String codeText) {
            return codeText.split(DELIMITER_REGEXT);
        }

        private boolean isSeverityOrNameInMsgCode(String[] splittedMsgCode) {
            return isSeverityInMsgCode(splittedMsgCode) || isRuleNameInMsgCode(splittedMsgCode);
        }

        private boolean isSeverityInMsgCode(String[] splittedMsgCode) {
            return splittedMsgCode[0].equals(validationRule.getMessageSeverity().getId());
        }

        private boolean isRuleNameInMsgCode(String[] splittedMsgCode) {
            return splittedMsgCode[2].equals(validationRule.getName());
        }

        private boolean isPolicyNameInMsgCode(String[] splittedMsgCode) {
            String unqualifiedPolicyName = validationRule.getIpsObject().getQualifiedNameType().getUnqualifiedName();
            return unqualifiedPolicyName.equals(splittedMsgCode[1]);
        }

        private void updateMessageCode() {
            String generatedMsgCode = validationRule.getMessageSeverity().getId() + DELIMITER
                    + validationRule.getIpsObject().getQualifiedNameType().getUnqualifiedName() + DELIMITER
                    + validationRule.getName();
            validationRule.setMessageCode(generatedMsgCode);
        }
    }
}
