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
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.refactor.RenameRefactoringProcessor;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.util.ArgumentCheck;

/**
 * The one-and-only input page a Faktor-IPS rename refactoring needs.
 * 
 * @author Alexander Weickmann
 */
class RenamePage extends UserInputWizardPage {

    /**
     * Text field to enable the user provide a new name for the <tt>IIpsElement</tt> to be
     * refactored.
     */
    private Text newNameTextField;

    /** The <tt>IIpsElement</tt> to be renamed. */
    private IIpsElement ipsElement;

    /**
     * Creates the <tt>RenamePage</tt>.
     * 
     * @param ipsElement The <tt>IIpsElement</tt> to be renamed.
     * 
     * @throws NullPointerException If <tt>ipsElement</tt> is <tt>null</tt>.
     */
    RenamePage(IIpsElement ipsElement) {
        super("RenamePage");
        ArgumentCheck.notNull(ipsElement);
        this.ipsElement = ipsElement;

        String ipsElementName = "";
        if (ipsElement instanceof IAttribute) {
            ipsElementName = Messages.RenameRefactoringWizard_Attribute;
        } else if (ipsElement instanceof IMethod) {
            ipsElementName = Messages.RenameRefactoringWizard_Method;
        } else if (ipsElement instanceof IType) {
            ipsElementName = Messages.RenameRefactoringWizard_Type;
        }
        setMessage(NLS.bind(Messages.RenamePage_message, ipsElementName, ipsElement.getName()));
    }

    public void createControl(Composite parent) {
        UIToolkit uiToolkit = new UIToolkit(null);
        Composite control = uiToolkit.createLabelEditColumnComposite(parent);
        uiToolkit.createLabel(control, Messages.RenamePage_labelNewName);
        newNameTextField = uiToolkit.createText(control);
        newNameTextField.setText(ipsElement.getName());
        newNameTextField.selectAll();
        newNameTextField.setFocus();
        newNameTextField.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent event) {
                String text = newNameTextField.getText();
                try {
                    boolean valid = validateNewName(text);
                    setPageComplete(valid);
                } catch (CoreException e) {
                    throw new RuntimeException(e);
                }
            }

        });
        setPageComplete(false);
        setControl(control);
    }

    /** Validates the new name provided by the user. */
    private boolean validateNewName(String newName) throws CoreException {
        // Reset any messages.
        setErrorMessage(null);
        setMessage(null, WARNING);
        setMessage(null, INFORMATION);

        RenameRefactoringProcessor renameRefactoring = (RenameRefactoringProcessor)((ProcessorBasedRefactoring)getRefactoring())
                .getProcessor();
        RefactoringStatus status = renameRefactoring.validateNewElementName(new NullProgressMonitor());
        evaluateValidation(status);
        return !(status.hasError());
    }

    /** Evaluates the given <tt>RefactoringStatus</tt> by setting appropriate page messages. */
    private void evaluateValidation(RefactoringStatus status) {
        for (RefactoringStatusEntry entry : status.getEntries()) {
            switch (entry.getSeverity()) {
                case RefactoringStatus.ERROR:
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

    @Override
    protected boolean performFinish() {
        ProcessorBasedRefactoring refactoring = (ProcessorBasedRefactoring)getRefactoring();
        RenameRefactoringProcessor processor = (RenameRefactoringProcessor)refactoring.getProcessor();
        processor.setNewElementName(newNameTextField.getText());
        return super.performFinish();
    }

}