/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.ui.Messages;

/**
 * Create a new Folder
 * 
 * @author Thorsten Guenther
 */
public class NewFolderAction extends IpsAction {

	private Shell shell;
	
	public NewFolderAction(Shell shell, ISelectionProvider selectionProvider) {
		super(selectionProvider);
		this.shell = shell;
		setText(Messages.NewFolderAction_name);
	}

	/** 
	 * {@inheritDoc}
	 */
	public void run(IStructuredSelection selection) {
		try {
			Object selected  = selection.getFirstElement();
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
