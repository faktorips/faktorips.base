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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
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
		Object selected  = selection.getFirstElement();
		IResource res = null;
		
		if (selected instanceof IIpsProject) {
//				res = ((IIpsProject)selected).getIpsPackageFragmentRoots()[0].getEnclosingResource();
			res = ((IIpsProject)selected).getProject();
		} 
		else if (selected instanceof IIpsElement) {
			res = ((IIpsElement)selected).getEnclosingResource();
		}else if(selected instanceof IResource){
			res= (IResource) selected;
		}
		
		while (res != null && !(res instanceof IContainer)) {
			res = res.getParent();
		}
		
		if (res == null) {
			MessageDialog.openError(shell, Messages.NewFolderAction_titleNewFolder, Messages.NewFolderAction_msgNoParentFound);
			return;
		}
		
		String parent = res.getName(); 
		String message = Messages.bind(Messages.NewFolderAction_descriptionNewFolder, parent);
		Validator validator = new Validator((IContainer) res);
		InputDialog d = new InputDialog(shell, Messages.NewFolderAction_titleNewFolder, message, Messages.NewFolderAction_valueNewFolder, validator);
		d.open();
		if (d.getReturnCode() == InputDialog.OK) {
			IFolder newFolder = getFolder((IContainer)res, d.getValue());
			try {
				newFolder.create(true, true, null);
			} catch (CoreException e) {
				IpsPlugin.log(e);
				return;
			}
		}
	}

	/**
	 * Creates an <code>IFolder</code> which might or might not exist.
	 */
	private IFolder getFolder(IContainer parent, String name) {
		return ((IContainer)parent).getFolder(new Path(name));
	}

	/**
	 * Checks that the entered name does not result in an existing folder.
	 * 
	 * @author Thorsten Guenther
	 */
	private class Validator implements IInputValidator {

		private IContainer parent;
		
		public Validator(IContainer parent) {
			this.parent = parent;
		}
		/**
		 * {@inheritDoc}
		 */
		public String isValid(String newText) {
			int pointIndex= newText.indexOf("."); //$NON-NLS-1$
			if(pointIndex!=-1){
				return Messages.NewFolderAction_msgFolderNameMustNotContainDots;
			}
			IFolder folder = getFolder(parent, newText);
			if(folder!=null){
				if (folder.exists()) {
					return NLS.bind(Messages.NewFolderAction_msgFolderAllreadyExists, folder.getFullPath().toOSString());
				}
			}
			return null;
		}
	}
}
