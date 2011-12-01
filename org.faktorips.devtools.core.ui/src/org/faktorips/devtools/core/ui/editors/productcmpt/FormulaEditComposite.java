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

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.List;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controls.FormulaEditControl;
import org.faktorips.devtools.core.ui.forms.IpsSection;

/**
 * Provides controls that allow the user to edit an {@link IFormula}.
 * <p>
 * Provides content assist support.
 * 
 * @since 3.6
 * 
 * @author Alexander Weickmann, Faktor Zehn AG
 * 
 * @see IFormula
 */
public class FormulaEditComposite extends EditPropertyValueComposite<IProductCmptTypeMethod, IFormula> {

    public FormulaEditComposite(IProductCmptTypeMethod property, IFormula propertyValue, IpsSection parentSection,
            Composite parent, BindingContext bindingContext, UIToolkit toolkit) {

        super(property, propertyValue, parentSection, parent, bindingContext, toolkit);
        initControls();
    }

    @Override
    protected void createEditFields(List<EditField<?>> editFields) {
        createExpressionEditField(editFields);
    }

    private void createExpressionEditField(List<EditField<?>> editFields) {
        FormulaEditControl formulaEditControl = new FormulaEditControl(this, getToolkit(), getPropertyValue(),
                getShell(), getProductCmptPropertySection());
        KeyStroke keyStroke = null;
        try {
            keyStroke = KeyStroke.getInstance("Ctrl+Space"); //$NON-NLS-1$
        } catch (final ParseException e) {
            throw new IllegalArgumentException("KeyStroke \"Ctrl+Space\" could not be parsed.", e); //$NON-NLS-1$
        }
        new ContentProposalAdapter(formulaEditControl.getTextControl(), new TextContentAdapter(),
                new ExpressionProposalProvider(getPropertyValue()), keyStroke, new char[] { '.' });

        TextButtonField editField = new TextButtonField(formulaEditControl);
        editFields.add(editField);
        getBindingContext().bindContent(editField, getPropertyValue(), IFormula.PROPERTY_EXPRESSION);
    }

}
