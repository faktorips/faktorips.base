/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.wizards.refactor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.util.ArgumentCheck;

/**
 * This is an abstract base class providing common functionality for <tt>UserInputWizardPage</tt>s
 * used by Faktor-IPS refactorings.
 * 
 * @author Alexander Weickmann
 */
abstract class IpsRefactoringUserInputPage extends UserInputWizardPage {

    /** The <tt>IIpsElement</tt> to be renamed. */
    private IIpsElement ipsElement;

    /** The <tt>UIToolkit</tt> to create UI elements with. */
    private final UIToolkit uiToolkit;

    /**
     * Creates a <tt>IpsRefactoringUserInputPage</tt>.
     * 
     * @param pageName A name for this page.
     * @param ipsElement The <tt>IIpsElement</tt> to be renamed.
     * 
     * @throws NullPointerException If any parameter is <tt>null</tt>.
     */
    IpsRefactoringUserInputPage(IIpsElement ipsElement, String pageName) {
        super(pageName);
        ArgumentCheck.notNull(ipsElement);
        this.ipsElement = ipsElement;
        uiToolkit = new UIToolkit(null);
        setPromptMessage();
    }

    /**
     * Subclass implementation responsible for setting the prompt message. This operation is called
     * by the constructor of <tt>IpsRefactoringUserInputPage</tt>.
     */
    protected abstract void setPromptMessage();

    /** Returns the name describing the <tt>IIpsElement</tt> to be refactored. */
    // TODO AW: This should be moved to the core model -> IIpsElement#getElementName().
    protected final String getIpsElementName() {
        String ipsElementName = "";
        if (ipsElement instanceof IAttribute) {
            ipsElementName = Messages.ElementNames_Attribute;
        } else if (ipsElement instanceof IMethod) {
            ipsElementName = Messages.ElementNames_Method;
        } else if (ipsElement instanceof IType) {
            ipsElementName = Messages.ElementNames_Type;
        }
        return ipsElementName;
    }

    /** Operation that should be called when any user input has changed, triggers validation. */
    protected final void userInputChanged() {
        try {
            boolean userInputValid = validateUserInput();
            setPageComplete(userInputValid);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Validates the user input, manages page messages and calls a subclass implementation. Returns
     * <tt>true</tt> if valid, <tt>false</tt> otherwise.
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
     * @param status A <tt>RefactoringStatus</tt> to add messages to.
     */
    protected abstract void validateUserInputThis(RefactoringStatus status) throws CoreException;

    /** Evaluates the given <tt>RefactoringStatus</tt> by setting appropriate page messages. */
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

    /** Resets any messages to the default, which means the prompt message is shown. */
    protected final void resetPageMessages() {
        setErrorMessage(null);
        setMessage(null, WARNING);
        setMessage(null, INFORMATION);
        setPromptMessage();
    }

    /** Returns the <tt>IIpsElement</tt> to be refactored. */
    protected final IIpsElement getIpsElement() {
        return ipsElement;
    }

    /** Returns the <tt>UIToolkit</tt> to create new UI elements with. */
    protected final UIToolkit getUiToolkit() {
        return uiToolkit;
    }

}
