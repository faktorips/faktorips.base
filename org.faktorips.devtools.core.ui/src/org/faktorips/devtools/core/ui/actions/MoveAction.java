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
import org.eclipse.ui.actions.MoveResourceAction;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.ui.wizards.move.MoveWizard;
import org.faktorips.devtools.core.ui.wizards.refactor.MoveRefactoringWizard;

/**
 * Opens the move wizard to allow the user to move a product component or package fragment.
 * 
 * @author Thorsten Guenther
 */
public class MoveAction extends IpsRefactoringAction {

    public MoveAction(Shell shell, ISelectionProvider selectionProvider) {
        super(shell, selectionProvider);
        setText(Messages.MoveAction_name);
    }

    @Override
    public void run(IStructuredSelection selection) {
        Object selected = selection.getFirstElement();

        // Open refactoring wizard if supported for selection.
        if (selected instanceof IIpsObject) {
            Refactoring refactoring = ((IIpsObject)selected).getMoveRefactoring();

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
            RefactoringWizard moveWizard = new MoveRefactoringWizard(refactoring, (IIpsElement)selected);
            openWizard(moveWizard);

            return;
        }

        if (selected instanceof IIpsElement) {
            MoveWizard move = new MoveWizard(selection, MoveWizard.OPERATION_MOVE);
            WizardDialog wd = new WizardDialog(getShell(), move);
            wd.open();
        } else if (selected instanceof IResource) {
            MoveResourceAction action = new MoveResourceAction(this);
            action.selectionChanged(selection);
            action.run();
        }
    }

}
