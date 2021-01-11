/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.refactor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.enums.IEnumLiteralNameAttributeValue;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.refactor.IIpsProcessorBasedRefactoring;
import org.faktorips.devtools.core.refactor.IIpsRefactoring;
import org.faktorips.devtools.core.refactor.IpsRefactoringProcessor;
import org.faktorips.devtools.core.ui.UIToolkit;

/**
 * Abstract base class providing common functionality for {@link UserInputWizardPage}s used by
 * Faktor-IPS refactorings.
 * 
 * @author Alexander Weickmann
 */
abstract class IpsRefactoringUserInputPage extends UserInputWizardPage {

    /** {@link UIToolkit} to create UI elements with. */
    private final UIToolkit uiToolkit;

    /**
     * @param pageName A name for this user input page
     * 
     * @throws NullPointerException If any parameter is null
     */
    IpsRefactoringUserInputPage(String pageName) {
        super(pageName);

        uiToolkit = new UIToolkit(null);
    }

    @Override
    public final void createControl(Composite parent) {
        setPromptMessage();
        createControlThis(parent);
    }

    /**
     * Subclass implementation responsible for creating the page control.
     * 
     * @param parent The parent {@link Composite}
     * 
     * @see IDialogPage#createControl(Composite)
     */
    protected abstract void createControlThis(Composite parent);

    /**
     * Subclass implementation responsible for setting the prompt message.
     * <p>
     * This operation is called by {@link #createControl(Composite)} right before
     * {@link #createControlThis(Composite)} is called. The operation is also called by
     * {@link #resetPageMessages()}.
     */
    protected abstract void setPromptMessage();

    /**
     * Returns the name describing the given {@link IIpsElement}.
     */
    // TODO AW: This should be moved to the core model
    protected final String getIpsElementName(IIpsElement ipsElement) {
        String ipsElementName = ""; //$NON-NLS-1$
        if (ipsElement instanceof IAttribute) {
            ipsElementName = Messages.ElementNames_Attribute;
        } else if (ipsElement instanceof IMethod) {
            ipsElementName = Messages.ElementNames_Method;
        } else if (ipsElement instanceof IAssociation) {
            ipsElementName = Messages.ElementNames_Association;
        } else if (ipsElement instanceof IType) {
            ipsElementName = Messages.ElementNames_Type;
        } else if (ipsElement instanceof IEnumLiteralNameAttributeValue) {
            ipsElementName = Messages.ElementNames_EnumLiteralNameAttributeValue;
        } else if (ipsElement instanceof IValidationRule) {
            ipsElementName = Messages.ElementNames_ValidationRule;
        }
        return ipsElementName;
    }

    /**
     * Operation that should be called when any user input has changed, triggers validation.
     */
    protected final void userInputChanged() {
        try {
            boolean userInputValid = validateUserInput();
            setPageComplete(userInputValid);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Validates the user input, manages page messages and calls a subclass implementation.
     * <p>
     * Returns true if valid, false otherwise.
     */
    private final boolean validateUserInput() throws CoreException {
        resetPageMessages();

        RefactoringStatus status = new RefactoringStatus();
        validateUserInputThis(status);
        evaluateValidation(status);

        return !(status.hasError());
    }

    /**
     * Subclass implementation responsible for validating the user input.
     * 
     * @param status {@link RefactoringStatus} to add messages to
     */
    protected abstract void validateUserInputThis(RefactoringStatus status) throws CoreException;

    /**
     * Evaluates the given {@link RefactoringStatus} by setting appropriate page messages.
     */
    protected final void evaluateValidation(RefactoringStatus status) {
        for (RefactoringStatusEntry entry : status.getEntries()) {
            switch (entry.getSeverity()) {
                case RefactoringStatus.ERROR:
                case RefactoringStatus.FATAL:
                    setErrorMessage(entry.getMessage());
                    break;
                case RefactoringStatus.WARNING:
                    setMessage(entry.getMessage(), WARNING);
                    break;
                case RefactoringStatus.INFO:
                    setMessage(entry.getMessage(), INFORMATION);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Resets any messages to the default, which means the prompt message is shown.
     */
    protected final void resetPageMessages() {
        setErrorMessage(null);
        setMessage(null, WARNING);
        setMessage(null, INFORMATION);
        setPromptMessage();
    }

    /**
     * Returns the {@link UIToolkit} to create new UI elements with.
     */
    protected final UIToolkit getUiToolkit() {
        return uiToolkit;
    }

    /**
     * Returns the {@link IIpsRefactoring} associated with this wizard page.
     */
    protected final IIpsRefactoring getIpsRefactoring() {
        return (IIpsRefactoring)getRefactoring();
    }

    /**
     * Returns the {@link IpsRefactoringProcessor} associated if the refactoring is a
     * processor-based refactoring or null otherwise.
     */
    protected final IpsRefactoringProcessor getIpsRefactoringProcessor() {
        if (getIpsRefactoring() instanceof IIpsProcessorBasedRefactoring) {
            return ((IIpsProcessorBasedRefactoring)getIpsRefactoring()).getIpsRefactoringProcessor();
        }
        return null;
    }

}
