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
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.internal.refactor.RenamePolicyCmptTypeAttributeRefactoringProcessor;
import org.faktorips.devtools.core.internal.refactor.RenameRefactoringProcessor;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.ui.UIToolkit;

/**
 * 
 * 
 * @author Alexander Weickmann
 */
public class RenameRefactoringWizard extends RefactoringWizard {

    public RenameRefactoringWizard(IPolicyCmptTypeAttribute policyCmptTypeAttribute) {
        super(new RenameRefactoring(new RenamePolicyCmptTypeAttributeRefactoringProcessor(policyCmptTypeAttribute)),
                DIALOG_BASED_USER_INTERFACE | NO_PREVIEW_PAGE);
        setDefaultPageTitle("Rename Policy Component Type Attribute");
    }

    @Override
    protected void addUserInputPages() {
        addPage(new RenameAttributePage());
    }

    private static class RenameAttributePage extends UserInputWizardPage {

        private Text newNameTextField;

        protected RenameAttributePage() {
            super("RenameAttributePage");
        }

        public void createControl(Composite parent) {
            UIToolkit uiToolkit = new UIToolkit(null);
            Composite control = uiToolkit.createLabelEditColumnComposite(parent);
            uiToolkit.createLabel(control, "New name:");
            newNameTextField = uiToolkit.createText(control);
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
