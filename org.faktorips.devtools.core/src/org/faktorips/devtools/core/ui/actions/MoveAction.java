package org.faktorips.devtools.core.ui.actions;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.ui.wizards.move.MoveWizard;

/**
 * Opens the move wizard to allow the user to move a product component or package fragment.
 * 
 * @author Thorsten Guenther
 */
public class MoveAction extends IpsAction {

	private Shell shell;
	
	public MoveAction(Shell shell, ISelectionProvider selectionProvider) {
		super(selectionProvider);
		this.shell = shell;
		setText(Messages.MoveAction_name);
	}

	/** 
	 * {@inheritDoc}
	 */
	public void run(IStructuredSelection selection) {
		MoveWizard move = new MoveWizard(selection, MoveWizard.OPERATION_MOVE);
		WizardDialog wd = new WizardDialog(shell, move);
		wd.open();
	}
}
