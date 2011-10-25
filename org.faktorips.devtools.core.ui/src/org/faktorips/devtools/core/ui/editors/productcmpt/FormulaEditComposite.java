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

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.contentassist.ContentAssistHandler;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.ui.CompletionUtil;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.CompositeUIController;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controls.FormulaEditControl;
import org.faktorips.util.message.ObjectProperty;

/**
 * TODO AW
 * 
 * @author Alexander Weickmann
 */
public final class FormulaEditComposite extends EditPropertyValueComposite<IProductCmptTypeMethod, IFormula> {

    public FormulaEditComposite(IProductCmptTypeMethod property, IFormula propertyValue,
            ProductCmptPropertySection propertySection, Composite parent, CompositeUIController uiMasterController,
            UIToolkit toolkit) {

        super(property, propertyValue, propertySection, parent, uiMasterController, toolkit);
        initControls();
    }

    @Override
    protected void createEditFields(Map<EditField<?>, ObjectProperty> editFieldsToEditedProperties)
            throws CoreException {

        createExpressionEditField(editFieldsToEditedProperties);
    }

    private void createExpressionEditField(Map<EditField<?>, ObjectProperty> editFieldsToEditedProperties)
            throws CoreException {

        FormulaEditControl formulaEditControl = new FormulaEditControl(this, getToolkit(), getPropertyValue(),
                getShell(), getProductCmptPropertySection());
        FormulaCompletionProcessor completionProcessor = new FormulaCompletionProcessor(getPropertyValue());
        ContentAssistHandler.createHandlerForText(formulaEditControl.getTextControl(),
                CompletionUtil.createContentAssistant(completionProcessor));
        TextButtonField editField = new TextButtonField(formulaEditControl);

        editFieldsToEditedProperties.put(editField,
                new ObjectProperty(getPropertyValue(), IFormula.PROPERTY_EXPRESSION));
    }

}
