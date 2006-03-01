package org.faktorips.devtools.core.ui.actions;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.ui.wizards.move.MoveWizard;

/**
 * Opens the move wizeard in rename-mode to allow the user to enter the 
 * new name for the object to rename.
 * 
 * @author Thorsten Guenther
 */
public class RenameAction extends IpsAction {

	private Shell shell;
	
	public RenameAction(Shell shell, ISelectionProvider selectionProvider) {
		super(selectionProvider);
		this.shell = shell;
		setText(Messages.RenameAction_name);
	}

	/** 
	 * {@inheritDoc}
	 */
	public void run(IStructuredSelection selection) {
		MoveWizard move = new MoveWizard(selection, MoveWizard.OPERATION_RENAME);
		WizardDialog wd = new WizardDialog(shell, move);
		wd.open();
	}
}
