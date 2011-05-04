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

package org.faktorips.devtools.core.ui.editors.pctype;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.pctype.MessageSeverity;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.IpsObjectUIController;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;
import org.faktorips.devtools.core.ui.controller.fields.EnumValueField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog2;
import org.faktorips.util.message.MessageList;

public class RuleEditDialog extends IpsPartEditDialog2 {

    private IValidationRule rule;

    // edit fields
    private TextField nameField;
    private TextField msgCodeField;
    private EnumValueField msgSeverityField;
    private TextField msgTextField;
    private CheckboxField appliedToAllField;
    private RuleFunctionsControl rfControl;
    private CheckboxField specifiedInSrcField;
    private Text msgText;
    private Label charCount;

    private Checkbox configurableByProductBox;
    private Checkbox defaultActivationBox;

    private ProductConfigurablePresentationObject state;

    public RuleEditDialog(IValidationRule rule, Shell parentShell) {
        super(rule, parentShell, Messages.RuleEditDialog_title, true);
        this.rule = rule;
        state = new ProductConfigurablePresentationObject(rule);
    }

    @Override
    protected Composite createWorkAreaThis(Composite parent) {
        TabFolder folder = (TabFolder)parent;

        TabItem msgPage = new TabItem(folder, SWT.NONE);
        msgPage.setText(Messages.RuleEditDialog_messageTitle);
        msgPage.setControl(createMessagePage(folder));

        TabItem functionsPage = new TabItem(folder, SWT.NONE);
        functionsPage.setText(Messages.RuleEditDialog_functionTitle);
        functionsPage.setControl(createFunctionsPage(folder));

        TabItem attributesPage = new TabItem(folder, SWT.NONE);
        attributesPage.setText(Messages.RuleEditDialog_attrTitle);
        attributesPage.setControl(createAttributesPage(folder));
        /*
         * the update cycle for changes to model objects is extended so that the gui will be updated
         * due to model changes. The update cycle gui -> model -> gui is currently not implemented
         * in a super class but should be considered in the future. It is necessary here because
         * changes made to the model within the RuleFunctionsControl need to be communicated to the
         * gui so that other controls can adjust their current state.
         */
        final ContentsChangeListenerForWidget listener = new ContentsChangeListenerForWidget() {
            @Override
            public void contentsChangedAndWidgetIsNotDisposed(ContentChangeEvent event) {
                if (!event.getIpsSrcFile().exists()) {
                    return;
                }
                if (event.getIpsSrcFile().equals(rule.getIpsObject().getIpsSrcFile())) {
                    bindingContext.updateUI();
                }
            }
        };
        listener.setWidget(parent);
        rule.getIpsModel().addChangeListener(listener);

        bindFields();
        return folder;
    }

    private Control createMessagePage(TabFolder folder) {
        Composite workArea = createTabItemComposite(folder, 1, false);
        ((GridLayout)workArea.getLayout()).verticalSpacing = 20;

        // general group
        createGeneralGroup(workArea);

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

        return workArea;
    }

    protected void createGeneralGroup(Composite workArea) {
        Group generalGroup = uiToolkit.createGroup(workArea, Messages.RuleEditDialog_messageTitle);
        generalGroup.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
        Composite nameComposite = uiToolkit.createLabelEditColumnComposite(generalGroup);
        uiToolkit.createFormLabel(nameComposite, Messages.RuleEditDialog_labelName);
        Text nameText = uiToolkit.createText(nameComposite);

        uiToolkit.createFormLabel(nameComposite, Messages.RuleEditDialog_ProductConfig_label);
        Composite configurableComposite = uiToolkit.createComposite(nameComposite);
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        configurableComposite.setLayout(layout);
        configurableByProductBox = uiToolkit.createCheckbox(configurableComposite,
                Messages.RuleEditDialog_Configurable_CheckboxLabel);
        defaultActivationBox = uiToolkit.createCheckbox(configurableComposite,
                Messages.RuleEditDialog_ActivatedByDefault_CheckboxLabel);

        nameText.setFocus();
        nameField = new TextField(nameText);
    }

    private void updateCharCount() {
        String msg = NLS.bind(Messages.RuleEditDialog_contains, new Integer(msgText.getText().length()));
        charCount.setText(msg);
        charCount.getParent().layout();
    }

    private Control createFunctionsPage(TabFolder folder) {
        Composite workArea = createTabItemComposite(folder, 1, false);
        ((GridLayout)workArea.getLayout()).verticalSpacing = 20;
        Checkbox appliedToAllCheckbox = uiToolkit.createCheckbox(workArea,
                Messages.RuleEditDialog_labelApplyInAllBusinessFunctions);
        rfControl = new RuleFunctionsControl(workArea);
        rfControl.initialize(super.getIpsPart(), null);
        appliedToAllField = new CheckboxField(appliedToAllCheckbox);

        return workArea;
    }

    private Control createAttributesPage(TabFolder folder) {
        Composite workArea = createTabItemComposite(folder, 1, false);
        ((GridLayout)workArea.getLayout()).verticalSpacing = 20;
        Checkbox specifiedInSrc = uiToolkit.createCheckbox(workArea, Messages.RuleEditDialog_labelSpecifiedInSrc);
        specifiedInSrcField = new CheckboxField(specifiedInSrc);

        ValidatedAttributesControl validatedAttributesControl = new ValidatedAttributesControl(workArea);
        validatedAttributesControl.initialize(super.getIpsPart(), null);
        return workArea;
    }

    private void bindFields() {
        bindingContext.bindContent(nameField, rule, IValidationRule.PROPERTY_NAME);
        bindingContext.bindContent(msgCodeField, rule, IValidationRule.PROPERTY_MESSAGE_CODE);
        bindingContext.bindContent(msgSeverityField, rule, IValidationRule.PROPERTY_MESSAGE_SEVERITY);
        bindingContext.bindContent(msgTextField, rule, IValidationRule.PROPERTY_MESSAGE_TEXT);
        bindingContext
                .bindContent(appliedToAllField, rule, IValidationRule.PROPERTY_APPLIED_FOR_ALL_BUSINESS_FUNCTIONS);
        bindingContext
                .bindContent(specifiedInSrcField, rule, IValidationRule.PROPERTY_VALIDATIED_ATTR_SPECIFIED_IN_SRC);
        bindingContext.bindContent(new CheckboxField(configurableByProductBox), rule,
                IValidationRule.PROPERTY_CONFIGUREDABLE_BY_PRODUCT_COMPONENT);
        bindingContext.bindContent(new CheckboxField(defaultActivationBox), rule,
                IValidationRule.PROPERTY_ACTIVATED_BY_DEFAULT);

        bindingContext.bindEnabled(configurableByProductBox, state,
                ProductConfigurablePresentationObject.PROPERTY_CONFIGURABLE_ENABLED);
        bindingContext.bindEnabled(defaultActivationBox, state,
                ProductConfigurablePresentationObject.PROPERTY_DEFAULT_ACTIVATION_ENABLED);
    }

    @Override
    protected Point getInitialSize() {
        return new Point(500, 500);
    }

    class UIController extends IpsObjectUIController {

        public UIController(IIpsObjectPartContainer ipsObjectPartContainer) {
            super(ipsObjectPartContainer);
            setEnableWholeIpsObjectValidation(true);
        }

        @Override
        protected MessageList validatePartContainerAndUpdateUI() {
            MessageList list = super.validatePartContainerAndUpdateUI();
            rfControl.updateValidationStatus();
            return list;
        }
    }

    /**
     * Object to bind the enablement of this dialog's checkboxes to. As the enablement of checkboxes
     * is ui specific knowledge, a separate object (an not the rule itself) is used.
     * <p>
     * Public for access by {@link BindingContext}.
     * 
     * @author Stefan Widmaier
     */
    public static class ProductConfigurablePresentationObject {
        public static final String PROPERTY_CONFIGURABLE_ENABLED = "configurableEnabled"; //$NON-NLS-1$
        public static final String PROPERTY_DEFAULT_ACTIVATION_ENABLED = "defaultActivationEnabled"; //$NON-NLS-1$

        private IPolicyCmptType pcType = null;
        private IValidationRule rule;

        public ProductConfigurablePresentationObject(IValidationRule rule) {
            this.rule = rule;
            pcType = (IPolicyCmptType)rule.getIpsObject();
        }

        /**
         * Returns whether the checkbox {@link RuleEditDialog#configurableByProductBox} should be
         * enabled/editable.
         * <p>
         * The checkbox is enabled if the rule's PolicyComponentType is configurable by product
         * components.
         */
        public boolean getConfigurableEnabled() {
            return pcType != null && pcType.isConfigurableByProductCmptType();
        }

        /**
         * Returns whether the checkbox {@link RuleEditDialog#defaultActivationBox} should be
         * enabled/editable.
         * <p>
         * The checkbox is enabled if both the containing PolicyComponentType and the rule itself
         * are configurable by product components.
         */
        public boolean getDefaultActivationEnabled() {
            return getConfigurableEnabled() && rule.isConfigurableByProductComponent();
        }
    }

}
