/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.pctype.rule;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog2;
import org.faktorips.devtools.core.ui.editors.pctype.ContentsChangeListenerForWidget;
import org.faktorips.devtools.core.ui.editors.pctype.Messages;
import org.faktorips.devtools.core.ui.editors.pctype.RuleFunctionsControl;
import org.faktorips.devtools.core.ui.editors.pctype.ValidatedAttributesControl;

public class RuleEditDialog extends IpsPartEditDialog2 {

    private IValidationRule rule;

    // edit fields
    private CheckboxField appliedToAllField;
    private RuleFunctionsControl rfControl;
    private CheckboxField specifiedInSrcField;

    private ValidationRuleEditingUI ruleUI = new ValidationRuleEditingUI(getToolkit());

    private ValidationRuleMarkerPMO ruleMarkerPMO;

    public RuleEditDialog(IValidationRule rule, Shell parentShell) {
        super(rule, parentShell, Messages.RuleEditDialog_title, true);
        this.rule = rule;

        ruleMarkerPMO = ValidationRuleMarkerPMO.createFor(rule.getIpsProject(), rule);
    }

    @Override
    protected Composite createWorkAreaThis(Composite parent) {
        TabFolder folder = (TabFolder)parent;

        TabItem msgPage = new TabItem(folder, SWT.NONE);
        msgPage.setText(Messages.RuleEditDialog_generalTitle);
        msgPage.setControl(createGeneralPage(folder));

        if (isCreateFunctionsPage()) {
            TabItem functionsPage = new TabItem(folder, SWT.NONE);
            functionsPage.setText(Messages.RuleEditDialog_functionTitle);
            functionsPage.setControl(createFunctionsPage(folder));
        }

        TabItem attributesPage = new TabItem(folder, SWT.NONE);
        attributesPage.setText(Messages.RuleEditDialog_attrTitle);
        attributesPage.setControl(createAttributesPage(folder));

        IIpsProject ipsProject = getIpsPart().getIpsProject();
        if (ipsProject.getReadOnlyProperties().isMarkerEnumsEnabled()) {
            TabItem markerPage = new TabItem(folder, SWT.NONE);
            markerPage.setText(Messages.ValidationRuleMarkerUI_TabName_Markers);
            markerPage.setControl(createMarkersPage(folder));
        }

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
                if (event.isAffected(rule)) {
                    getBindingContext().updateUI();
                }
            }
        };
        listener.setWidget(parent);
        // rule.getIpsModel().addChangeListener(listener);

        bindFields();
        return folder;
    }

    private Control createGeneralPage(TabFolder folder) {
        Composite workArea = createTabItemComposite(folder, 1, false);
        ((GridLayout)workArea.getLayout()).verticalSpacing = 20;

        ruleUI.initUI(workArea);

        return workArea;
    }

    private boolean isCreateFunctionsPage() {
        return getIpsPart().getIpsProject().getReadOnlyProperties().isBusinessFunctionsForValdiationRulesEnabled();
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

    private Control createMarkersPage(TabFolder folder) {
        Composite workArea = createTabItemComposite(folder, 1, false);

        ValidationRuleMarkerUI validationRuleMarkerUI = new ValidationRuleMarkerUI(getToolkit());
        validationRuleMarkerUI.createUI(workArea, ruleMarkerPMO);

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
        if (isCreateFunctionsPage()) {
            getBindingContext().bindContent(appliedToAllField, rule,
                    IValidationRule.PROPERTY_APPLIED_FOR_ALL_BUSINESS_FUNCTIONS);
        }
        getBindingContext().bindContent(specifiedInSrcField, rule,
                IValidationRule.PROPERTY_VALIDATIED_ATTR_SPECIFIED_IN_SRC);
    }

}
