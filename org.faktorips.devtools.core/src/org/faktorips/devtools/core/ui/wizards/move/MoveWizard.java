package org.faktorips.devtools.core.ui.wizards.move;


import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.refactor.MoveOperation;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.product.IProductCmpt;

/**
 * A wizard to move and/or rename package fragements or product components.
 * 
 * @author Thorsten Guenther
 */
public class MoveWizard extends Wizard {
	
	/**
	 * The page to query the data (new name, new package) from.
	 */
	private IWizardPage sourcePage;
	
	/**
	 * All selected objects to move.
	 */
	private IIpsElement[] selectedObjects;
	
	/**
	 * Operate as move-wizard (does not show the input for new name). Value == 1.
	 */
	public static final int OPERATION_MOVE = 1;
	
	/**
	 * Operate as rename-wizard (does only allow a single selected object). Value == 10.
	 */
	public static final int OPERATION_RENAME = 10;
	
	/**
	 * The mode we are operating in
	 *  @see MoveWizard#OPERATION_MOVE;
	 *  @see MoveWizard#OPERATION_RENAME;
	 */
	private int operation;
	
	/**
	 * If the wizard encounters an selection error (e.g. more then one object 
	 * selected in mode rename) this string is set to a human readable error message. 
	 */
	private String selectionError;
	
	/**
	 * Creates a new wizard which can make a deep copy of the given product
	 */
	public MoveWizard(IStructuredSelection selection, int operation) {
		super();
	
		this.operation = operation;
		if (operation == OPERATION_MOVE) {
			super.setWindowTitle(Messages.MoveWizard_titleMove);
		} 
		else if (operation == OPERATION_RENAME) {
			super.setWindowTitle(Messages.MoveWizard_titleRename);
		}
		else {
			String msg = Messages.bind(Messages.MoveWizard_warnInvalidOperation, ""+operation); //$NON-NLS-1$
			IpsStatus status = new IpsStatus(msg);
			IpsPlugin.log(status);
			this.operation = OPERATION_MOVE;
		}
		
		Object[] selected = selection.toArray();
		this.selectedObjects = new IIpsElement[selected.length];
		for (int i = 0; i < selection.size(); i++) {
			if (selected[i] instanceof IProductCmpt || selected[i] instanceof IIpsPackageFragment) {
				this.selectedObjects[i] = (IIpsElement)selected[i];
			}
			else {
				selectionError = Messages.MoveWizard_errorUnsupported;
				
				// does not make sense to work on...
				break;
			}
		}
		
		if (selectionError == null && operation == OPERATION_RENAME && selectedObjects.length > 1) {
			selectionError = Messages.MoveWizard_errorToManySelected;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void addPages() {
		if (selectionError != null) {
			sourcePage = new ErrorPage(selectionError);
		}
		else if (operation == MoveWizard.OPERATION_MOVE) {
			sourcePage = new MovePage(selectedObjects);
		}
		else if (operation == MoveWizard.OPERATION_RENAME) {
			sourcePage = new RenamePage(selectedObjects[0]);
		}
		super.addPage(sourcePage);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean performFinish() {
		boolean finished = false;
		
		try {
			MoveOperation move = null;;
			if (operation == OPERATION_MOVE) {
				move = new MoveOperation(this.selectedObjects, ((MovePage)sourcePage).getTarget());
				
			}
			else if (operation == OPERATION_RENAME) {
				move = new MoveOperation(this.selectedObjects, new String[] {((RenamePage)sourcePage).getNewName()});
			}
			ProgressMonitorDialog dialog = new ProgressMonitorDialog(super.getShell());
			dialog.run(false, false, move);
			finished = true;
		} catch (InvocationTargetException e) {
			IpsPlugin.logAndShowErrorDialog(e);
		} catch (InterruptedException e) {
			IpsPlugin.logAndShowErrorDialog(e);
		} catch (CoreException e) {
			if (e.getStatus() != null) {
				IStatus status = e.getStatus();
				if (status instanceof IpsStatus) {
					MessageDialog.openError(getShell(), Messages.MoveWizard_error, e.getMessage());
				}
			}
			IpsPlugin.log(e);
		}		
		return finished;
	}
	
}
