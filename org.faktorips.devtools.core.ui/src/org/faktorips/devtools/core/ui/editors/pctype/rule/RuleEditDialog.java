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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog2;
import org.faktorips.devtools.core.ui.editors.pctype.Messages;
import org.faktorips.devtools.core.ui.editors.pctype.ValidatedAttributesControl;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IValidationRule;

public class RuleEditDialog extends IpsPartEditDialog2 {

    private IValidationRule rule;

    // edit fields
    private CheckboxField appliedToAllField;
    private CheckboxField specifiedInSrcField;

    private ValidationRuleEditingUI ruleUI = new ValidationRuleEditingUI(getToolkit());
    private ValidationRuleMarkerUI validationRuleMarkerUI = new ValidationRuleMarkerUI(getToolkit());

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

        TabItem attributesPage = new TabItem(folder, SWT.NONE);
        attributesPage.setText(Messages.RuleEditDialog_attrTitle);
        attributesPage.setControl(createAttributesPage(folder));

        IIpsProject ipsProject = getIpsPart().getIpsProject();
        if (validationRuleMarkerUI.isMarkerEnumsEnabled(ipsProject)) {
            TabItem markerPage = new TabItem(folder, SWT.NONE);
            markerPage.setText(Messages.ValidationRuleMarkerUI_TabName_Markers);
            markerPage.setControl(createMarkersPage(folder));
        }

        bindFields();
        return folder;
    }

    private Control createGeneralPage(TabFolder folder) {
        Composite workArea = createTabItemComposite(folder, 1, false);
        ((GridLayout)workArea.getLayout()).verticalSpacing = 20;

        ruleUI.initUI(workArea);

        return workArea;
    }

    private Control createMarkersPage(TabFolder folder) {
        Composite workArea = createTabItemComposite(folder, 1, false);

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
        getBindingContext().bindContent(specifiedInSrcField, rule,
                IValidationRule.PROPERTY_VALIDATIED_ATTR_SPECIFIED_IN_SRC);
    }

}
