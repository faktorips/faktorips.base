/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.List;
import java.util.function.Function;

import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.fields.StyledTextButtonField;
import org.faktorips.devtools.core.ui.controls.FormulaEditControl;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.devtools.model.productcmpt.IFormula;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeMethod;

/**
 * Provides controls that allow the user to edit an {@link IFormula}.
 * <p>
 * Provides content assist support.
 * 
 * @since 3.6
 * 
 * @see IFormula
 */
public class FormulaEditComposite extends EditPropertyValueComposite<IProductCmptTypeMethod, IFormula> {

    private ContentProposalAdapter contentProposalAdapter;
    private ContentProposalListener contentProposalListener;

    public FormulaEditComposite(IProductCmptTypeMethod property, IFormula propertyValue, IpsSection parentSection,
            Composite parent, BindingContext bindingContext, UIToolkit toolkit) {

        super(property, propertyValue, parentSection, parent, bindingContext, toolkit);
        initControls();
    }

    @Override
    protected void createEditFields(List<EditField<?>> editFields) {
        EditField<?> editField = createExpressionEditField();
        createTemplateStatusButton(editField);
        editFields.add(editField);
    }

    private StyledTextButtonField createExpressionEditField() {
        FormulaEditControl formulaEditControl = new FormulaEditControl(this, getToolkit(), getPropertyValue(),
                getShell(), getProductCmptPropertySection());

        contentProposalAdapter = FormulaEdit.createContentProposalAdapter(formulaEditControl.getTextControl(),
                getPropertyValue());
        contentProposalListener = new ContentProposalListener(contentProposalAdapter);
        contentProposalAdapter.addContentProposalListener(contentProposalListener);

        StyledTextButtonField editField = new StyledTextButtonField(formulaEditControl);
        getBindingContext().bindContent(editField, getPropertyValue(), IFormula.PROPERTY_EXPRESSION);
        addChangingOverTimeDecorationIfRequired(editField);
        return editField;
    }

    @Override
    protected Function<IFormula, String> getToolTipFormatter() {
        return PropertyValueFormatter.FORMULA;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (contentProposalAdapter != null) {
            contentProposalAdapter.removeContentProposalListener(contentProposalListener);
        }
    }
}
