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

package org.faktorips.devtools.core.ui.editors.productcmpttype;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.ComboViewerField;
import org.faktorips.devtools.core.ui.controls.AbstractCheckbox;
import org.faktorips.devtools.core.ui.editors.CategoryPmo;
import org.faktorips.devtools.core.ui.editors.type.MethodEditDialog;

public class ProductCmptTypeMethodEditDialog extends MethodEditDialog {

    public ProductCmptTypeMethodEditDialog(IProductCmptTypeMethod method, Shell parentShell) {
        super(method, parentShell);
    }

    @Override
    protected Composite createWorkAreaThis(Composite parent) {
        Composite c = super.createWorkAreaThis(parent);

        nameText.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (StringUtils.isEmpty(method.getName())) {
                    method.setName(getProductCmptTypeMethod().getDefaultMethodName());
                }
            }
        });

        return c;
    }

    private IProductCmptTypeMethod getProductCmptTypeMethod() {
        return (IProductCmptTypeMethod)method;
    }

    @Override
    protected void createAdditionalControlsOnGeneralPage(Composite parent, UIToolkit toolkit) {
        Composite group = toolkit.createGroup(parent, Messages.ProductCmptTypeMethodEditDialog_formulaGroup);
        AbstractCheckbox checkbox = toolkit.createCheckbox(group,
                Messages.ProductCmptTypeMethodEditDialog_formulaCheckbox);
        getBindingContext().bindContent(checkbox, method, IProductCmptTypeMethod.PROPERTY_FORMULA_SIGNATURE_DEFINITION);

        AbstractCheckbox overloadsFormula = toolkit.createCheckbox(group,
                Messages.ProductCmptTypeMethodEditDialog_labelOverloadsFormula);
        getBindingContext().bindContent(overloadsFormula, method, IProductCmptTypeMethod.PROPERTY_OVERLOADS_FORMULA);
        getBindingContext().bindEnabled(overloadsFormula, method,
                IProductCmptTypeMethod.PROPERTY_FORMULA_SIGNATURE_DEFINITION);

        Composite area = getToolkit().createLabelEditColumnComposite(group);
        toolkit.createLabel(area, Messages.ProductCmptTypeMethodEditDialog_formulaNameLabel);
        Text formulaNameText = toolkit.createText(area);
        getBindingContext().bindContent(formulaNameText, method, IProductCmptTypeMethod.PROPERTY_FORMULA_NAME);
        getBindingContext().bindEnabled(formulaNameText, method,
                IProductCmptTypeMethod.PROPERTY_FORMULA_SIGNATURE_DEFINITION);

        toolkit.createLabel(area, Messages.ProductCmptTypeMethodEditDialog_categoryLabel);
        createCategoryCombo(area);
    }

    private void createCategoryCombo(Composite workArea) {
        Combo categoryCombo = getToolkit().createCombo(workArea);
        ComboViewerField<IProductCmptCategory> comboViewerField = new ComboViewerField<IProductCmptCategory>(
                categoryCombo, IProductCmptCategory.class);

        CategoryPmo pmo = new CategoryPmo(getProductCmptTypeMethod());
        comboViewerField.setInput(pmo.getCategories());
        comboViewerField.setAllowEmptySelection(true);
        comboViewerField.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                IProductCmptCategory category = (IProductCmptCategory)element;
                return IpsPlugin.getMultiLanguageSupport().getLocalizedLabel(category);
            }
        });

        getBindingContext().bindContent(comboViewerField, pmo, CategoryPmo.PROPERTY_CATEGORY);
        getBindingContext().bindEnabled(comboViewerField.getCombo(), method,
                IProductCmptTypeMethod.PROPERTY_FORMULA_SIGNATURE_DEFINITION);
    }

    @Override
    public void contentsChanged(ContentChangeEvent event) {
        super.contentsChanged(event);
        if (event.getIpsSrcFile().equals(getIpsPart().getIpsSrcFile())) {
            IProductCmptTypeMethod tMethod = (IProductCmptTypeMethod)method;
            datatypeControl.setVoidAllowed(!tMethod.isFormulaSignatureDefinition());
            datatypeControl.setOnlyValueDatatypesAllowed(tMethod.isFormulaSignatureDefinition());
            setLabelCompositeEnabled(tMethod.isFormulaSignatureDefinition());
        }
    }

}
