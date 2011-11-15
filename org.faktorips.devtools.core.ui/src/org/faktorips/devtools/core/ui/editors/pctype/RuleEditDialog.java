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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.ui.controller.IpsObjectUIController;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog2;
import org.faktorips.util.message.MessageList;

public class RuleEditDialog extends IpsPartEditDialog2 {

    private IValidationRule rule;

    // edit fields
    private CheckboxField appliedToAllField;
    private RuleFunctionsControl rfControl;
    private CheckboxField specifiedInSrcField;

    private ValidationRuleEditingUI ruleUI = new ValidationRuleEditingUI(getToolkit());

    public RuleEditDialog(IValidationRule rule, Shell parentShell) {
        super(rule, parentShell, Messages.RuleEditDialog_title, true);
        this.rule = rule;
    }

    @Override
    protected Composite createWorkAreaThis(Composite parent) {
        TabFolder folder = (TabFolder)parent;

        TabItem msgPage = new TabItem(folder, SWT.NONE);
        msgPage.setText(Messages.RuleEditDialog_generalTitle);
        msgPage.setControl(createGeneralPage(folder));

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
                    getBindingContext().updateUI();
                }
            }
        };
        listener.setWidget(parent);
        rule.getIpsModel().addChangeListener(listener);

        bindFields();
        return folder;
    }

    private Control createGeneralPage(TabFolder folder) {
        Composite workArea = createTabItemComposite(folder, 1, false);
        ((GridLayout)workArea.getLayout()).verticalSpacing = 20;

        ruleUI.initUI(workArea);

        return workArea;
    }

    private Control createFunctionsPage(TabFolder folder) {
        Composite workArea = createTabItemComposite(folder, 1, false);
        ((GridLayout)workArea.getLayout()).verticalSpacing = 20;
        Checkbox appliedToAllCheckbox = getToolkit().createCheckbox(workArea,
                Messages.RuleEditDialog_labelApplyInAllBusinessFunctions);
        rfControl = new RuleFunctionsControl(workArea);
        rfControl.initialize(super.getIpsPart(), null);
        appliedToAllField = new CheckboxField(appliedToAllCheckbox);

        return workArea;
    }

    private Control createAttributesPage(TabFolder folder) {
        Composite workArea = createTabItemComposite(folder, 1, false);
        ((GridLayout)workArea.getLayout()).verticalSpacing = 20;
        Checkbox specifiedInSrc = getToolkit().createCheckbox(workArea, Messages.RuleEditDialog_labelSpecifiedInSrc);
        specifiedInSrcField = new CheckboxField(specifiedInSrc);

        ValidatedAttributesControl validatedAttributesControl = new ValidatedAttributesControl(workArea);
        validatedAttributesControl.initialize(super.getIpsPart(), null);
        return workArea;
    }

    private void bindFields() {
        ruleUI.bindFields(rule, getBindingContext());
        getBindingContext().bindContent(appliedToAllField, rule,
                IValidationRule.PROPERTY_APPLIED_FOR_ALL_BUSINESS_FUNCTIONS);
        getBindingContext().bindContent(specifiedInSrcField, rule,
                IValidationRule.PROPERTY_VALIDATIED_ATTR_SPECIFIED_IN_SRC);
    }

    @Override
    protected Point getInitialSize() {
        return new Point(525, 575);
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

}
