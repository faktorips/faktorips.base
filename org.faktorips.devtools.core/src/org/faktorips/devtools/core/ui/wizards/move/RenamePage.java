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

package org.faktorips.devtools.core.ui.wizards.move;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.ui.UIToolkit;

/**
 * Page to let the user select a new name for the object to rename. 
 * 
 * @author Thorsten Guenther
 */
public class RenamePage extends WizardPage implements  ModifyListener {
	
	/**
	 * The input field holding the new name.
	 */
	private Text newName;
	
	/**
	 * The object to rename
	 */
	private IIpsElement renameObject;
	
	/**
	 * The page-id to identify this page.
	 */
	private static final String PAGE_ID = "MoveWizard.configure"; //$NON-NLS-1$

	/**
	 * Creates a new page to select the objects to copy.
	 */
	protected RenamePage(IIpsElement renameObject) {
		super(PAGE_ID, Messages.RenamePage_rename, null);
		
		this.renameObject = renameObject;
	
		super.setDescription(Messages.RenamePage_msgChooseNewName);
		setPageComplete();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void createControl(Composite parent) {
        UIToolkit toolkit = new UIToolkit(null);

		Composite root = toolkit.createComposite(parent);
		root.setLayout(new GridLayout(1, false));
		root.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		setControl(root);

		Composite inputRoot = toolkit.createLabelEditColumnComposite(root);

		toolkit.createLabel(inputRoot, Messages.RenamePage_newName);
		newName = toolkit.createText(inputRoot);
		newName.addModifyListener(this);
	}

	/**
	 * Set the current completion state (and, if neccessary, messages for the user
	 * to help him to get the page complete).
	 */
	private void setPageComplete() {

		super.setMessage(null);
		super.setPageComplete(false);
		
		if (newName == null) {
			// page not yet created, do nothing.
			return;
		}
		
		String name = newName.getText(); 
		// must not be empty
		if (name.length() == 0) {
			setMessage(Messages.RenamePage_errorNameIsEmpty, ERROR);
			return;
		}
		if (name.indexOf('.') != -1) {
			setMessage(Messages.RenamePage_errorNameQualified, ERROR);
			return;
		}
		
		if (renameObject instanceof IProductCmpt) {
			IStatus val= JavaConventions.validateJavaTypeName(name);
			if (val.getSeverity() == IStatus.ERROR) {
				String msg = Messages.bind(Messages.errorNameNotValid, name);
				setMessage(msg, ERROR);
				return;
			} else if (val.getSeverity() == IStatus.WARNING) {
				setMessage(Messages.RenamePage_warningDiscouraged, WARNING);
				// continue checking
			}		
		}
		else {
			IStatus val= JavaConventions.validatePackageName(name);
			if (val.getSeverity() == IStatus.ERROR) {
				String msg = Messages.bind(Messages.errorNameNotValid, name);
				setMessage(msg, ERROR);
				return;
			}
		}
		
		IIpsPackageFragment pack = null;
		
		if (renameObject instanceof IProductCmpt) {
			pack = ((IProductCmpt)renameObject).getIpsPackageFragment();
			IIpsSrcFile newFile = pack.getIpsSrcFile(((IProductCmpt)renameObject).getIpsObjectType().getFileName(newName.getText()));
			if (newFile.exists()) {
				setMessage(Messages.RenamePage_errorFileExists, ERROR);
				return;
			}
		}
		else if (renameObject instanceof IIpsPackageFragment){
			pack = (IIpsPackageFragment)renameObject;
			IFolder folder = (IFolder)pack.getCorrespondingResource();
			IFolder newFolder = folder.getFolder(newName.getText());
			if (newFolder.exists()) {
				setMessage(Messages.RenamePage_errorFolderExists, ERROR);
				return;
			}
		}
		
		super.setPageComplete(true);
	}
	
	/**
	 * Returns the name the user has entered. The name is allways qualified with the package name
	 * of the package containing the object to rename.
	 */
	public String getNewName() {
		String name = ""; //$NON-NLS-1$
		if (this.renameObject instanceof IProductCmpt) {
			name = ((IProductCmpt)this.renameObject).getIpsPackageFragment().getName();
		}
		else if (this.renameObject instanceof IIpsPackageFragment) {
			IIpsPackageFragment parent = ((IIpsPackageFragment)this.renameObject).getIpsParentPackageFragment();
			if (parent != null) {
				name = parent.getName();
			}
			else {
				name = ""; //$NON-NLS-1$
			}
		}
		
		if (!name.equals("")) { //$NON-NLS-1$
			name += "."; //$NON-NLS-1$
		}
		return name + newName.getText();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void modifyText(ModifyEvent e) {
		setPageComplete();
	}		
}


