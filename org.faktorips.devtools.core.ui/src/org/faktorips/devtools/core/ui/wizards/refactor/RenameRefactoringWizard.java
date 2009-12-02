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

import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;
import org.eclipse.ltk.core.refactoring.participants.RenameProcessor;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.internal.refactor.RenamePolicyCmptTypeAttributeRefactoringProcessor;
import org.faktorips.devtools.core.internal.refactor.RenameRefactoringProcessor;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.util.ArgumentCheck;

/**
 * A wizard that is used to guide the user trough a Faktor-IPS rename refactoring.
 * 
 * @author Alexander Weickmann
 */
public class RenameRefactoringWizard extends RefactoringWizard {

    private final IIpsElement ipsElement;

    /**
     * Returns the <tt>RenameProcessor</tt> that is compatible to the provided <tt>IIpsElement</tt>.
     * <p>
     * This method must be static so that it is possible to call it in the constructor.
     * 
     * @param ipsElement The <tt>IIpsElement</tt> to be renamed.
     */
    private static RenameProcessor getRenameProcessor(IIpsElement ipsElement) {
        ArgumentCheck.notNull(ipsElement);
        if (ipsElement instanceof IPolicyCmptTypeAttribute) {
            return new RenamePolicyCmptTypeAttributeRefactoringProcessor((IPolicyCmptTypeAttribute)ipsElement);
        }
        throw new RuntimeException("The IPS element " + ipsElement + " is not supported by this RefactoringWizard.");
    }

    /**
     * Creates a <tt>RenameRefactoringWizard</tt>.
     * 
     * @param ipsElement The <tt>IIpsElement</tt> to be renamed.
     * 
     * @throws NullPointerException If <tt>ipsElement</tt> is <tt>null</tt>.
     */
    public RenameRefactoringWizard(IIpsElement ipsElement) {
        super(new RenameRefactoring(getRenameProcessor(ipsElement)), DIALOG_BASED_USER_INTERFACE | NO_PREVIEW_PAGE);
        setDefaultPageTitle("Rename Policy Component Type Attribute");
        setChangeCreationCancelable(false);
        this.ipsElement = ipsElement;
    }

    @Override
    protected void addUserInputPages() {
        addPage(new RenamePage());
    }

    /** The one-and-only input page the rename refactoring needs. */
    private class RenamePage extends UserInputWizardPage {

        /**
         * Text field to enable the user provide a new name for the <tt>IIpsElement</tt> to be
         * refactored.
         */
        private Text newNameTextField;

        /** Creates the <tt>RenamePage</tt>. */
        private RenamePage() {
            super("RenamePage");
        }

        public void createControl(Composite parent) {
            UIToolkit uiToolkit = new UIToolkit(null);
            Composite control = uiToolkit.createLabelEditColumnComposite(parent);
            uiToolkit.createLabel(control, "New name:");
            newNameTextField = uiToolkit.createText(control);
            newNameTextField.setText(ipsElement.getName());
            newNameTextField.selectAll();
            newNameTextField.setFocus();
            setControl(control);
        }

        @Override
        protected boolean performFinish() {
            ProcessorBasedRefactoring refactoring = (ProcessorBasedRefactoring)getRefactoring();
            RenameRefactoringProcessor processor = (RenameRefactoringProcessor)refactoring.getProcessor();
            processor.setNewName(newNameTextField.getText());
            return super.performFinish();
        }

    }

}
