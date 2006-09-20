/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.testcasetype;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.contentassist.ContentAssistHandler;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.ui.CompletionUtil;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.TextButtonControl;
import org.faktorips.util.StringUtil;

/**
 * Control to select a validation rule.
 * 
 * @author Joerg Ortmann
 */
public class ValidationRuleRefControl extends TextButtonControl {
    private String dialogTitle;
    private String dialogMessage;
    private ITestCaseType testCaseType;
    
    public ValidationRuleRefControl(
            Composite parent, 
            UIToolkit toolkit,
            ITestCaseType testCaseType) {
        super(parent, toolkit, Messages.ValidationRuleRefControl_Button_Browse);
        this.dialogTitle = Messages.RelationRefControl_Title;
        this.dialogMessage = Messages.RelationRefControl_Description;
        this.testCaseType = testCaseType;
        
        ValidationRuleCompletionProcessor completionProcessor = new ValidationRuleCompletionProcessor(testCaseType);
        completionProcessor.setComputeProposalForEmptyPrefix(true);
        ContentAssistHandler.createHandlerForText(text, CompletionUtil.createContentAssistant(completionProcessor));
    }

    /**
     * {@inheritDoc}
     */
    protected void buttonClicked() {
        try {
            ElementListSelectionDialog selectDialog = new ElementListSelectionDialog(getShell(), new DefaultLabelProvider());
            selectDialog.setTitle(dialogTitle);
            selectDialog.setMessage(dialogMessage);
            selectDialog.setElements(getValidationRules());
            selectDialog.setFilter(StringUtil.unqualifiedName(super.getText()));
            if (selectDialog.open()==Window.OK) {
                if (selectDialog.getResult().length>0) {
                    IValidationRule validationRule = (IValidationRule)selectDialog.getResult()[0];
                    setText(validationRule.getName());
                } else {
                    setText(""); //$NON-NLS-1$
                }
            }
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }
    
    /**
     * Returns all validation rule which are candidates for a test rule parameter
     */
    protected IValidationRule[] getValidationRules() throws CoreException {
        return testCaseType.getTestRuleCandidates();
    }
}
