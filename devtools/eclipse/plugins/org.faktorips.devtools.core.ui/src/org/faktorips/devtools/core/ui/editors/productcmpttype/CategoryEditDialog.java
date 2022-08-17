/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpttype;

import java.util.LinkedHashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.ui.ExtensionPropertyControlFactory;
import org.faktorips.devtools.core.ui.controller.fields.RadioButtonGroupField;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.controls.RadioButtonGroup;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog2;
import org.faktorips.devtools.model.extproperties.IExtensionPropertyDefinition;
import org.faktorips.devtools.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.model.productcmpttype.IProductCmptCategory.Position;

/**
 * Dialog that allows the user to edit the properties of an {@link IProductCmptCategory}.
 * 
 * @since 3.6
 */
public class CategoryEditDialog extends IpsPartEditDialog2 {

    private final ExtensionPropertyControlFactory extensionFactory;

    private Text nameText;

    public CategoryEditDialog(IProductCmptCategory category, Shell parentShell) {
        super(category, parentShell, Messages.CategoryEditDialog_title, true);
        extensionFactory = new ExtensionPropertyControlFactory(category);
    }

    @Override
    protected Composite createWorkAreaThis(Composite parent) {
        TabFolder tabFolder = (TabFolder)parent;

        TabItem generalPage = new TabItem(tabFolder, SWT.NONE);
        generalPage.setText(Messages.CategoryEditDialog_generalTabTitle);
        generalPage.setControl(createGeneralPage(tabFolder));

        setInitialFocus();

        return tabFolder;
    }

    private Control createGeneralPage(TabFolder tabFolder) {
        Composite page = createTabItemComposite(tabFolder, 1, false);

        createTopExtensionPropertyControls(page);

        createNameComposite(page);
        createVerticalSpacer(page);
        createPositionGroup(page);
        createVerticalSpacer(page);
        createDefaultsGroup(page);

        createBottomExtensionPropertyControls(page);

        extensionFactory.bind(getBindingContext());

        return page;
    }

    private void createTopExtensionPropertyControls(Composite page) {
        extensionFactory.createControls(page, getToolkit(), getCategory(), IExtensionPropertyDefinition.POSITION_TOP);
    }

    private void createNameComposite(Composite page) {
        Composite nameComposite = getToolkit().createLabelEditColumnComposite(page);
        getToolkit().createLabel(nameComposite, Messages.CategoryEditDialog_nameLabel);
        nameText = getToolkit().createText(nameComposite);
        getBindingContext().bindContent(nameText, getCategory(), IProductCmptCategory.PROPERTY_NAME);
    }

    private void createVerticalSpacer(Composite page) {
        getToolkit().createVerticalSpacer(page, 10);
    }

    private void createPositionGroup(Composite page) {
        LinkedHashMap<Position, String> options = new LinkedHashMap<>();
        options.put(Position.LEFT, Messages.CategoryEditDialog_positionLeft);
        options.put(Position.RIGHT, Messages.CategoryEditDialog_positionRight);
        RadioButtonGroup<Position> positionRadioGroup = getToolkit().createRadioButtonGroup(page,
                Messages.CategoryEditDialog_positionGroup, 2, options);

        getBindingContext().bindContent(new RadioButtonGroupField<>(positionRadioGroup), getCategory(),
                IProductCmptCategory.PROPERTY_POSITION);
    }

    private void createDefaultsGroup(Composite page) {
        Group defaultsGroup = getToolkit().createGridGroup(page, Messages.CategoryEditDialog_defaultsGroup, 1, false);

        Label explanationLabel = getToolkit().createLabel(defaultsGroup,
                Messages.CategoryEditDialog_defaultsExplanationLabel, SWT.WRAP);

        Composite defaultsComposite = getToolkit().createGridComposite(defaultsGroup, 1, false, true);
        ((GridLayout)defaultsComposite.getLayout()).verticalSpacing = 8;

        Checkbox defaultProductCmptTypeAttributesCheckbox = getToolkit().createCheckbox(defaultsComposite,
                Messages.CategoryEditDialog_defaultProductComponentTypeAttributes);
        Checkbox defaultPolicyCmptTypeAttributesCheckbox = getToolkit().createCheckbox(defaultsComposite,
                Messages.CategoryEditDialog_defaultPolicyComponentTypeAttributes);
        Checkbox defaultTableStructureUsagesCheckbox = getToolkit().createCheckbox(defaultsComposite,
                Messages.CategoryEditDialog_defaultTableStructureUsages);
        Checkbox defaultFormulaSignatureDefinitionsCheckbox = getToolkit().createCheckbox(defaultsComposite,
                Messages.CategoryEditDialog_defaultFormulaSignatureDefinitions);
        Checkbox defaultValidationRulesCheckbox = getToolkit().createCheckbox(defaultsComposite,
                Messages.CategoryEditDialog_defaultValidationRules);

        getBindingContext().bindContent(defaultProductCmptTypeAttributesCheckbox, getCategory(),
                IProductCmptCategory.PROPERTY_DEFAULT_FOR_PRODUCT_CMPT_TYPE_ATTRIBUTES);
        getBindingContext().bindContent(defaultPolicyCmptTypeAttributesCheckbox, getCategory(),
                IProductCmptCategory.PROPERTY_DEFAULT_FOR_POLICY_CMPT_TYPE_ATTRIBUTES);
        getBindingContext().bindContent(defaultTableStructureUsagesCheckbox, getCategory(),
                IProductCmptCategory.PROPERTY_DEFAULT_FOR_TABLE_STRUCTURE_USAGES);
        getBindingContext().bindContent(defaultFormulaSignatureDefinitionsCheckbox, getCategory(),
                IProductCmptCategory.PROPERTY_DEFAULT_FOR_FORMULA_SIGNATURE_DEFINITIONS);
        getBindingContext().bindContent(defaultValidationRulesCheckbox, getCategory(),
                IProductCmptCategory.PROPERTY_DEFAULT_FOR_VALIDATION_RULES);

        // Enable automatic line break for the explanation label
        GridData explanationGridData = new GridData();
        explanationGridData.widthHint = defaultsComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT).x * 2;
        explanationLabel.setLayoutData(explanationGridData);
    }

    private void createBottomExtensionPropertyControls(Composite page) {
        extensionFactory.createControls(page, getToolkit(), getCategory(),
                IExtensionPropertyDefinition.POSITION_BOTTOM);
    }

    private void setInitialFocus() {
        nameText.setFocus();
    }

    private IProductCmptCategory getCategory() {
        return (IProductCmptCategory)getIpsPart();
    }

}
