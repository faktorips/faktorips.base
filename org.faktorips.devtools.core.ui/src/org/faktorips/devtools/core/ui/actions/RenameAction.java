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

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.RenameResourceAction;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.ui.wizards.move.MoveWizard;
import org.faktorips.devtools.core.ui.wizards.refactor.RenameRefactoringWizard;

/**
 * Opens the move wizard in rename-mode to allow the user to enter the new name for the object to
 * rename.
 * 
 * @author Thorsten Guenther
 */
public class RenameAction extends IpsRefactoringAction {

    public RenameAction(Shell shell, ISelectionProvider selectionProvider) {
        super(shell, selectionProvider);
        setText(Messages.RenameAction_name);
    }

    @Override
    public void run(IStructuredSelection selection) {
        Object selected = selection.getFirstElement();

        // Open refactoring wizard if supported for selection.
        if (selected instanceof IAttribute || selected instanceof IIpsObject) {
            Refactoring refactoring = ((IIpsElement)selected).getRenameRefactoring();

            // Check initial conditions.
            try {
                boolean checkSucceeded = checkInitialConditions(refactoring);
                if (!(checkSucceeded)) {
                    return;
                }
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }

            // Open the refactoring wizard.
            RefactoringWizard renameWizard = new RenameRefactoringWizard(refactoring, (IIpsElement)selected);
            openWizard(renameWizard);

            return;
        }

        if (selected instanceof IIpsElement) {
            MoveWizard move = new MoveWizard(selection, MoveWizard.OPERATION_RENAME);
            WizardDialog wd = new WizardDialog(getShell(), move);
            wd.open();
        } else if (selected instanceof IResource) {
            RenameResourceAction action = new RenameResourceAction(this);
            action.selectionChanged(selection);
            action.run();
        }
    }

}
