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

import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
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

    /** Returns the <tt>IIpsProject</tt> the <tt>IIpsElement</tt> to be refactored belongs to. */
    protected final IIpsProject getIpsProject() {
        return ipsElement.getIpsProject();
    }

    /**
     * Returns the <tt>IIpsPackageFragmentRoot</tt> the <tt>IIpsElement</tt> to be refactored
     * belongs to.
     */
    protected final IIpsPackageFragmentRoot getIpsPackageFragmentRoot() {
        if (ipsElement instanceof IIpsObjectPartContainer) {
            IIpsObjectPartContainer objectPartContainer = (IIpsObjectPartContainer)ipsElement;
            return objectPartContainer.getIpsObject().getIpsPackageFragment().getRoot();
        }
        throw new RuntimeException("This IPS element is not supported.");
    }

    /** Returns the qualified name of the <tt>IIpsElement</tt> to be refactored. */
    protected final String getQualifiedName() {
        if (ipsElement instanceof IType) {
            IType type = (IType)ipsElement;
            return type.getQualifiedName();

        } else if (ipsElement instanceof IAttribute) {
            IAttribute attribute = (IAttribute)ipsElement;
            return attribute.getType().getQualifiedName() + "." + attribute.getName();

        } else if (ipsElement instanceof IMethod) {
            IMethod method = (IMethod)ipsElement;
            return method.getType().getQualifiedName() + "." + method.getName();
        }

        throw new RuntimeException("This IPS element is not supported.");
    }

}
