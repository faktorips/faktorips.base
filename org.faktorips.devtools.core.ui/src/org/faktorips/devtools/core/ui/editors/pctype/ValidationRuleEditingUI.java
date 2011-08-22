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

import java.util.Locale;

import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.ISupportedLanguage;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.pctype.MessageSeverity;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.binding.InternationalStringPresentationObject;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;
import org.faktorips.devtools.core.ui.controller.fields.EnumValueField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
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
    // private Label charCountPainter;
    private CharCountPainter charCountPainter;
    private TextField nameField;
    private TextField msgCodeField;
    private EnumValueField msgSeverityField;
    private TextField msgTextField;
    private final UIToolkit uiToolkit;
    private Checkbox configurableByProductBox;
    private Checkbox defaultActivationBox;

    private CTabFolder msgTextFolder;

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
        GridData layoutData = new GridData(GridData.FILL_BOTH);
        msgComposite.setLayoutData(layoutData);

        uiToolkit.createFormLabel(msgComposite, Messages.RuleEditDialog_labelCode);
        Text codeText = uiToolkit.createText(msgComposite);
        uiToolkit.createFormLabel(msgComposite, Messages.RuleEditDialog_labelSeverity);
        Combo severityCombo = uiToolkit.createCombo(msgComposite, MessageSeverity.getEnumType());
        Label label = uiToolkit.createFormLabel(msgComposite, Messages.RuleEditDialog_labelText);
        label.getParent().setLayoutData(
                new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING));

        msgTextFolder = new CTabFolder(msgComposite, SWT.BOTTOM | SWT.BORDER);
        GridData folderLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        folderLayoutData.heightHint = 50;
        folderLayoutData.widthHint = UIToolkit.DEFAULT_WIDTH;
        msgTextFolder.setLayoutData(folderLayoutData);

        msgText = uiToolkit.createMultilineText(msgTextFolder);
        msgText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                updateCharCount();
            }
        });

        // uiToolkit.createVerticalSpacer(msgComposite, 1);

        //        charCountPainter = uiToolkit.createFormLabel(msgComposite, ""); //$NON-NLS-1$
        // charCountPainter.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, true, true));
        charCountPainter = new CharCountPainter();
        msgText.addPaintListener(charCountPainter);
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
        charCountPainter.setText(msg);
        msgText.redraw();
        // charCountPainter.getParent().layout();
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
        final InternationalStringPresentationObject msgTextPMO = new InternationalStringPresentationObject(
                rule.getMessageText());
        createTabsForLocales(rule.getIpsProject());
        msgTextFolder.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                CTabItem selectedTabItem = msgTextFolder.getSelection();
                Locale locale = (Locale)selectedTabItem.getData();
                msgTextPMO.setLocale(locale);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // nothing to do
            }
        });

        msgTextPMO.setLocale(rule.getIpsProject().getProperties().getDefaultLanguage().getLocale());
        bindingContext.bindContent(msgTextField, msgTextPMO, InternationalStringPresentationObject.PROPERTY_TEXT);
        bindingContext.bindContent(new CheckboxField(configurableByProductBox), rule,
                IValidationRule.PROPERTY_CONFIGURABLE_BY_PRODUCT_COMPONENT);
        bindingContext.bindContent(new CheckboxField(defaultActivationBox), rule,
                IValidationRule.PROPERTY_ACTIVATED_BY_DEFAULT);

        bindingContext.bindEnabled(configurableByProductBox, rule.getIpsObject(),
                IPolicyCmptType.PROPERTY_CONFIGURABLE_BY_PRODUCTCMPTTYPE);
        bindingContext.bindEnabled(defaultActivationBox, rule,
                IValidationRule.PROPERTY_CONFIGURABLE_BY_PRODUCT_COMPONENT);
    }

    private void createTabsForLocales(IIpsProject ipsProject) {
        for (CTabItem item : msgTextFolder.getItems()) {
            item.dispose();
        }
        for (ISupportedLanguage supportedLanguage : ipsProject.getProperties().getSupportedLanguages()) {
            CTabItem tabItem = new CTabItem(msgTextFolder, SWT.NONE);
            tabItem.setText(supportedLanguage.getLanguageName());
            tabItem.setData(supportedLanguage.getLocale());
            tabItem.setControl(msgText);
            if (supportedLanguage.isDefaultLanguage()) {
                msgTextFolder.setSelection(tabItem);
            }
        }
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

}
