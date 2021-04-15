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

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.ComboViewerField;
import org.faktorips.devtools.core.ui.controls.AbstractCheckbox;
import org.faktorips.devtools.core.ui.editors.CategoryPmo;
import org.faktorips.devtools.core.ui.editors.type.MethodEditDialog;
import org.faktorips.devtools.model.ContentChangeEvent;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeMethod;

public class ProductCmptTypeMethodEditDialog extends MethodEditDialog {

    public ProductCmptTypeMethodEditDialog(IProductCmptTypeMethod method, Shell parentShell) {
        super(method, parentShell);
    }

    @Override
    protected Composite createWorkAreaThis(Composite parent) {
        Composite c = super.createWorkAreaThis(parent);

        getNameText().addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (StringUtils.isEmpty(getMethod().getName())) {
                    getMethod().setName(getProductCmptTypeMethod().getDefaultMethodName());
                }
            }
        });

        return c;
    }

    private IProductCmptTypeMethod getProductCmptTypeMethod() {
        return (IProductCmptTypeMethod)getMethod();
    }

    @Override
    protected void createAdditionalControlsOnGeneralPage(Composite parent, UIToolkit toolkit) {
        Composite group = toolkit.createGroup(parent, Messages.ProductCmptTypeMethodEditDialog_formulaGroup);
        AbstractCheckbox checkbox = toolkit.createCheckbox(group,
                Messages.ProductCmptTypeMethodEditDialog_formulaCheckbox);
        getBindingContext().bindContent(checkbox, getMethod(),
                IProductCmptTypeMethod.PROPERTY_FORMULA_SIGNATURE_DEFINITION);

        AbstractCheckbox checkboxOptional = toolkit.createCheckbox(group,
                Messages.ProductCmptTypeMethodEditDialog_formulaMandatory);
        checkboxOptional.setToolTipText(Messages.ProductCmptTypeMethodEditDialog_formulaMandatoryHint);
        getBindingContext().bindContent(checkboxOptional, getMethod(),
                IProductCmptTypeMethod.PROPERTY_FORMULA_MANDATORY);
        getBindingContext().bindEnabled(checkboxOptional, getMethod(),
                IProductCmptTypeMethod.PROPERTY_FORMULA_OPTIONAL_SUPPORTED);

        AbstractCheckbox overloadsFormula = toolkit.createCheckbox(group,
                Messages.ProductCmptTypeMethodEditDialog_labelOverloadsFormula);
        getBindingContext().bindContent(overloadsFormula, getMethod(),
                IProductCmptTypeMethod.PROPERTY_OVERLOADS_FORMULA);
        getBindingContext().bindEnabled(overloadsFormula, getMethod(),
                IProductCmptTypeMethod.PROPERTY_FORMULA_SIGNATURE_DEFINITION);

        Composite area = getToolkit().createLabelEditColumnComposite(group);
        toolkit.createLabel(area, Messages.ProductCmptTypeMethodEditDialog_formulaNameLabel);
        Text formulaNameText = toolkit.createText(area);
        getBindingContext().bindContent(formulaNameText, getMethod(), IProductCmptTypeMethod.PROPERTY_FORMULA_NAME);
        getBindingContext().bindEnabled(formulaNameText, getMethod(),
                IProductCmptTypeMethod.PROPERTY_FORMULA_SIGNATURE_DEFINITION);

        toolkit.createLabel(area, Messages.ProductCmptTypeMethodEditDialog_categoryLabel);
        createCategoryCombo(area);
    }

    @Override
    protected boolean isProdCmptTypeEditDialog() {
        return true;
    }

    private void createCategoryCombo(Composite workArea) {
        Combo categoryCombo = getToolkit().createCombo(workArea);
        ComboViewerField<IProductCmptCategory> comboViewerField = new ComboViewerField<>(
                categoryCombo, IProductCmptCategory.class);

        CategoryPmo pmo = new CategoryPmo(getProductCmptTypeMethod());
        comboViewerField.setInput(pmo.getCategories());
        comboViewerField.setAllowEmptySelection(true);
        comboViewerField.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                IProductCmptCategory category = (IProductCmptCategory)element;
                return IIpsModel.get().getMultiLanguageSupport().getLocalizedLabel(category);
            }
        });

        getBindingContext().bindContent(comboViewerField, pmo, CategoryPmo.PROPERTY_CATEGORY);
        getBindingContext().bindEnabled(comboViewerField.getCombo(), getMethod(),
                IProductCmptTypeMethod.PROPERTY_FORMULA_SIGNATURE_DEFINITION);
    }

    @Override
    public void contentsChanged(ContentChangeEvent event) {
        super.contentsChanged(event);
        if (event.getIpsSrcFile().equals(getIpsPart().getIpsSrcFile())) {
            IProductCmptTypeMethod tMethod = (IProductCmptTypeMethod)getMethod();
            getDatatypeControl().setVoidAllowed(!tMethod.isFormulaSignatureDefinition());
            getDatatypeControl().setOnlyValueDatatypesAllowed(tMethod.isFormulaSignatureDefinition());
            setLabelCompositeEnabled(tMethod.isFormulaSignatureDefinition());
        }
    }

}
