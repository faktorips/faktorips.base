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

package org.faktorips.devtools.core.ui.editors.productcmpttype;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory.Position;
import org.faktorips.devtools.core.ui.ExtensionPropertyControlFactory;
import org.faktorips.devtools.core.ui.controller.fields.RadioButtonGroupField;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.controls.RadioButtonGroup;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog2;

/**
 * Dialog that allows the user to edit the properties of an {@link IProductCmptCategory}.
 * 
 * @author Alexander Weickmann
 */
public class CategoryEditDialog extends IpsPartEditDialog2 {

    private final ExtensionPropertyControlFactory extensionFactory;

    private Text nameText;

    public CategoryEditDialog(IProductCmptCategory category, Shell parentShell) {
        super(category, parentShell, Messages.CategoryEditDialog_title, true);
        extensionFactory = new ExtensionPropertyControlFactory(category.getClass());
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
        RadioButtonGroup positionRadioGroup = getToolkit().createRadioButtonGroup(page,
                Messages.CategoryEditDialog_positionGroup, 2);
        Button leftButton = positionRadioGroup.addRadioButton(Messages.CategoryEditDialog_positionLeft);
        Button rightButton = positionRadioGroup.addRadioButton(Messages.CategoryEditDialog_positionRight);

        Map<Button, Position> options = new HashMap<Button, Position>();
        options.put(leftButton, Position.LEFT);
        options.put(rightButton, Position.RIGHT);
        getBindingContext().bindContent(new RadioButtonGroupField<Position>(positionRadioGroup, options),
                getCategory(), IProductCmptCategory.PROPERTY_POSITION);
    }

    private void createDefaultsGroup(Composite page) {
        Group defaultsGroup = getToolkit().createGridGroup(page, Messages.CategoryEditDialog_defaultsGroup, 1, false);

        Label explanationLabel = getToolkit().createLabel(defaultsGroup,
                Messages.CategoryEditDialog_defaultsExplanationLabel, SWT.WRAP);

        Composite defaultsComposite = getToolkit().createGridComposite(defaultsGroup, 1, false, true);
        ((GridLayout)defaultsComposite.getLayout()).verticalSpacing = 8;

        Checkbox defaultProductCmptTypeAttributesCheckbox = getToolkit().createCheckbox(defaultsComposite,
                Messages.CategoryEditDialog_defaultProductComponentTypeAttributes);
        Checkbox defaultFormulaSignatureDefinitionsCheckbox = getToolkit().createCheckbox(defaultsComposite,
                Messages.CategoryEditDialog_defaultFormulaSignatureDefinitions);
        Checkbox defaultTableStructureUsagesCheckbox = getToolkit().createCheckbox(defaultsComposite,
                Messages.CategoryEditDialog_defaultTableStructureUsages);
        Checkbox defaultPolicyCmptTypeAttributesCheckbox = getToolkit().createCheckbox(defaultsComposite,
                Messages.CategoryEditDialog_defaultPolicyComponentTypeAttributes);
        Checkbox defaultValidationRulesCheckbox = getToolkit().createCheckbox(defaultsComposite,
                Messages.CategoryEditDialog_defaultValidationRules);

        getBindingContext().bindContent(defaultProductCmptTypeAttributesCheckbox, getCategory(),
                IProductCmptCategory.PROPERTY_DEFAULT_FOR_PRODUCT_CMPT_TYPE_ATTRIBUTES);
        getBindingContext().bindContent(defaultFormulaSignatureDefinitionsCheckbox, getCategory(),
                IProductCmptCategory.PROPERTY_DEFAULT_FOR_FORMULA_SIGNATURE_DEFINITIONS);
        getBindingContext().bindContent(defaultTableStructureUsagesCheckbox, getCategory(),
                IProductCmptCategory.PROPERTY_DEFAULT_FOR_TABLE_STRUCTURE_USAGES);
        getBindingContext().bindContent(defaultPolicyCmptTypeAttributesCheckbox, getCategory(),
                IProductCmptCategory.PROPERTY_DEFAULT_FOR_POLICY_CMPT_TYPE_ATTRIBUTES);
        getBindingContext().bindContent(defaultValidationRulesCheckbox, getCategory(),
                IProductCmptCategory.PROPERTY_DEFAULT_FOR_VALIDATION_RULES);

        // Enable automatic line break for the explanation label
        GridData explanationGridData = new GridData();
        explanationGridData.widthHint = defaultsComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
        explanationLabel.setLayoutData(explanationGridData);
    }

    private void createBottomExtensionPropertyControls(Composite page) {
        extensionFactory
                .createControls(page, getToolkit(), getCategory(), IExtensionPropertyDefinition.POSITION_BOTTOM);
    }

    private void setInitialFocus() {
        nameText.setFocus();
    }

    private IProductCmptCategory getCategory() {
        return (IProductCmptCategory)getIpsPart();
    }

}
