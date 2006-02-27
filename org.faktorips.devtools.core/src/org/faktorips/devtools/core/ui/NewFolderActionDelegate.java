package org.faktorips.devtools.core.ui;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.ui.views.productdefinitionexplorer.ProductExplorer;

/**
 * Open a simple Dialog to ask the user for the name of a new folder. The parent for the new folder
 * is examined by asking the product definition explorer for the current selection. If this selection
 * does not lead to a valid IFolder-Resource, an Error-Dialog is opened and no folder is created. 
 * 
 * This action does only work if invoked on the product definition explorer.
 * 
 * @author Thorsten Guenther
 */
public class NewFolderActionDelegate implements IViewActionDelegate {

	/**
	 * The product explorer this action will work on.
	 */
	private ProductExplorer pe;
	
	/**
	 * {@inheritDoc}
	 */
	public void init(IViewPart view) {
		if (view instanceof ProductExplorer) {
			pe = (ProductExplorer)view;
		}

	}

	/**
	 * Tries to find a <code>IResource</code> as parent of the currently selected item. If this is not possible,
	 * no folder could be created and an error dialog is shown to the user. 
	 * 
	 * {@inheritDoc}
	 */
	public void run(IAction action) {
		if (pe == null) {
			return;
		}
		
		try {
			Shell shell = IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();
			Object selected = ((IStructuredSelection)pe.getSelection()).getFirstElement();
			IResource res = null;
			
			if (selected instanceof IIpsProject) {
				res = ((IIpsProject)selected).getIpsPackageFragmentRoots()[0].getEnclosingResource();
			} 
			else if (selected instanceof IIpsElement) {
				res = ((IIpsElement)selected).getEnclosingResource();
			}
			
			while (res != null && !(res instanceof IFolder)) {
				res = res.getParent();
			}
			
			if (res == null) {
				MessageDialog.openError(shell, Messages.NewFolderActionDelegate_titleNewFolder, Messages.NewFolderActionDelegate_msgNoParentFound);
				return;
			}
			
			String parent = res.getName(); 
			String message = Messages.bind(Messages.NewFolderActionDelegate_descriptionNewFolder, parent);
			Validator validator = new Validator((IFolder)res);
			InputDialog d = new InputDialog(shell, Messages.NewFolderActionDelegate_titleNewFolder, message, Messages.NewFolderActionDelegate_valueNewFolder, validator);
			d.open();
			if (d.getReturnCode() == InputDialog.OK) {
				IFolder newFolder = getFolder((IFolder)res, d.getValue());
				try {
					newFolder.create(true, true, null);
				} catch (CoreException e) {
					IpsPlugin.log(e);
					return;
				}
			}
		} catch (CoreException e) {
			IpsPlugin.log(e);
			return;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		// nothing to do
	}

	/**
	 * Creates an <code>IFolder</code> which might or might not exist.
	 */
	private IFolder getFolder(IFolder parent, String name) {
		return ((IFolder)parent).getFolder(name);
	}

	/**
	 * Checks that the entered name does not result in an existing folder.
	 * 
	 * @author Thorsten Guenther
	 */
	private class Validator implements IInputValidator {

		private IFolder parent;
		
		public Validator(IFolder parent) {
			this.parent = parent;
		}
		/**
		 * {@inheritDoc}
		 */
		public String isValid(String newText) {
			IFolder folder = getFolder(parent, newText);
			if (folder.exists()) {
				return NLS.bind(Messages.NewFolderActionDelegate_msgFolderAllreadyExists, folder.getFullPath().toOSString());
			}
			return null;
		}
	}
}
