package org.faktorips.devtools.core.internal.refactor.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.faktorips.devtools.core.ui.views.productdefinitionexplorer.ProductExplorer;
import org.faktorips.devtools.core.ui.wizards.move.MoveWizard;

/**
 * Opens the move wizeard in rename-mode to allow the user to enter the 
 * new name for the object to rename.
 * This action does only work if invoked on the product explorer.
 * 
 * @author Thorsten Guenther
 */
public class RenameActionDelegate implements IViewActionDelegate {

	ProductExplorer pe;
	
	/**
	 * {@inheritDoc}
	 */
	public void init(IViewPart view) {
		if (view instanceof ProductExplorer) {
			pe = (ProductExplorer)view;
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public void run(IAction action) {
		if (pe == null) {
			return;
		}
		MoveWizard move = new MoveWizard((IStructuredSelection)pe.getSelection(), MoveWizard.OPERATION_RENAME);
		WizardDialog wd = new WizardDialog(pe.getViewSite().getShell(), move);
		wd.open();
		
	}

	/**
	 * {@inheritDoc}
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		// nothing to do
	}

}
