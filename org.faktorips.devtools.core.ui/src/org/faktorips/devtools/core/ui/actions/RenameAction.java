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
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.RenameResourceAction;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.ui.wizards.move.MoveWizard;
import org.faktorips.devtools.core.ui.wizards.refactor.RenameRefactoringWizard;

/**
 * Opens the move wizard in rename-mode to allow the user to enter the new name for the object to
 * rename.
 * 
 * @author Thorsten Guenther
 */
public class RenameAction extends IpsAction implements IShellProvider {

    private Shell shell;

    public RenameAction(Shell shell, ISelectionProvider selectionProvider) {
        super(selectionProvider);
        this.shell = shell;
        setText(Messages.RenameAction_name);
    }

    @Override
    public void run(IStructuredSelection selection) {
        Object selected = selection.getFirstElement();
        if (selected instanceof IPolicyCmptTypeAttribute) {
            RefactoringWizard renameWizard = new RenameRefactoringWizard((IPolicyCmptTypeAttribute)selected);
            RefactoringWizardOpenOperation op = new RefactoringWizardOpenOperation(renameWizard);
            try {
                op.run(getShell(), "");
            } catch (InterruptedException e) {
                // operation was canceled
            }
            return;
        }
        if (selected instanceof IIpsElement) {
            MoveWizard move = new MoveWizard(selection, MoveWizard.OPERATION_RENAME);
            WizardDialog wd = new WizardDialog(shell, move);
            wd.open();
        } else if (selected instanceof IResource) {
            RenameResourceAction action = new RenameResourceAction(this);
            action.selectionChanged(selection);
            action.run();
        }
    }

    public Shell getShell() {
        return shell;
    }
}
