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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.IpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptNamingStrategy;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.util.message.MessageList;

/**
 * Page to let the user enter a new name for the object to rename. 
 * 
 * @author Thorsten Guenther
 */
public class RenamePage extends WizardPage implements ModifyListener {
	
	/**
	 * The input field holding the complete new name.
	 */
	private Text newName;
	
	/**
	 * Input holding the version id part of the name. 
	 */
	private Text versionId;
	
	/**
	 * Input for the constant part of the name.
	 */
	private Text constNamePart;
	
	/**
	 * The object to rename
	 */
	private IIpsElement renameObject;
	
	/**
	 * The page-id to identify this page.
	 */
	private static final String PAGE_ID = "MoveWizard.configure"; //$NON-NLS-1$

	/**
	 * The naming strategy to use for move/rename.
	 */
	private IProductCmptNamingStrategy namingStrategy;
	
	/**
	 * Creates a new page to select the objects to copy.
	 */
	protected RenamePage(IIpsElement renameObject) {
		super(PAGE_ID, Messages.RenamePage_rename, null);
		
		this.renameObject = renameObject;
	
		try {
			namingStrategy = renameObject.getIpsProject().getProductCmptNamingStratgey();
		} catch (CoreException e) {
			IpsPlugin.log(e);
		}
		
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

		if (renameObject instanceof IpsPackageFragment || renameObject instanceof ITableContents) {
			createControlForObject(toolkit, inputRoot, (IIpsElement)renameObject);
		}
		else if (renameObject instanceof IProductCmpt){
			createControlForProduct(toolkit, inputRoot, (IProductCmpt)renameObject);
		}
		newName.addModifyListener(this);
		setPageComplete();
	}

	/**
	 * Creates the input controlls for an IpsObject.
	 */
	private void createControlForObject(UIToolkit toolkit, Composite parent, IIpsElement obj) {
		toolkit.createLabel(parent, Messages.RenamePage_newName);
		newName = toolkit.createText(parent);
		newName.setText(obj.getName());
	}
	
	/**
	 * Creates the input controlls for a product component to rename
	 */
	private void createControlForProduct(UIToolkit toolkit, Composite parent, IProductCmpt product) {
		if (namingStrategy != null && namingStrategy.supportsVersionId()) {
			String label = NLS.bind(Messages.RenamePage_labelVersionId, IpsPlugin.getDefault().getIpsPreferences().getChangesOverTimeNamingConvention().getVersionConceptNameSingular());
			toolkit.createLabel(parent, label);
			versionId = toolkit.createText(parent);

			toolkit.createLabel(parent, Messages.RenamePage_labelConstNamePart);
			constNamePart = toolkit.createText(parent);

			toolkit.createLabel(parent, Messages.RenamePage_newName);
			newName = toolkit.createText(parent);
			newName.setEnabled(false);

			versionId.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					updateFullName();
				}
			});
			versionId.setText(namingStrategy.getVersionId(product.getName()));
			
			constNamePart.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					updateFullName();
				}
			});
			constNamePart.setText(namingStrategy.getKindId(product.getName()));
		} else {
			toolkit.createLabel(parent, Messages.RenamePage_newName);
			newName = toolkit.createText(parent);
			newName.setText(renameObject.getName());
		}
	}
	
	/**
	 * Constructs the full name out of the version id and the constant name part 
	 * using the active product coponent naming strategy.
	 */
	private void updateFullName() {
		newName.setText(namingStrategy.getProductCmptName(constNamePart.getText(), versionId.getText()));
	}

	/**
	 * If at least one message is contained in the given list, the first message is set
	 * as error-message. 
	 *  
	 * @param list The list to look for messages in.
	 * @return <code>true</code> if a message was found and set, <code>false</code> otherwise.
	 */
	private boolean setMessageFromList(MessageList list) {
		if (!list.isEmpty()) {
			setMessage(list.getMessage(0).getText(), ERROR);
			return true;
		} else {
			setMessage(null);
			return false;
		}
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

		if (namingStrategy != null && namingStrategy.supportsVersionId() && versionId != null) {
			if (setMessageFromList(namingStrategy.validateVersionId(versionId.getText()))) {
				return;
			}
			if (setMessageFromList(namingStrategy.validateKindId(constNamePart.getText()))) {
				return;
			}
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
		
		if (renameObject instanceof IProductCmpt || renameObject instanceof ITableContents) {
			IStatus val= JavaConventions.validateJavaTypeName(name);
			if (val.getSeverity() == IStatus.ERROR) {
				String msg = Messages.bind(Messages.errorNameNotValid, name);
				setMessage(msg, ERROR);
				return;
			} else if (val.getSeverity() == IStatus.WARNING) {
				setMessage(Messages.RenamePage_warningDiscouraged, WARNING);
				// continue checking
			}		
		} else {
			IStatus val= JavaConventions.validatePackageName(name);
			if (val.getSeverity() == IStatus.ERROR) {
				String msg = Messages.bind(Messages.errorNameNotValid, name);
				setMessage(msg, ERROR);
				return;
			}
		}
		
		IIpsPackageFragment pack = null;
		
		if (renameObject instanceof IProductCmpt || renameObject instanceof ITableContents) {
			pack = ((IIpsObject)renameObject).getIpsPackageFragment();
			IIpsSrcFile newFile = pack.getIpsSrcFile(((IIpsObject)renameObject).getIpsObjectType().getFileName(newName.getText()));
			if (newFile.exists()) {
				setMessage(Messages.RenamePage_errorFileExists, ERROR);
				return;
			} else {
				// fix for windows: can not rename to an object with a name only
				// different in case.
				if (hasContentWithNameEqualsIgnoreCase((IFolder)pack.getCorrespondingResource(), ((IIpsObject)renameObject).getIpsObjectType().getFileName(newName.getText()))) {
					setMessage(Messages.RenamePage_errorFileExists, ERROR);
					return;
				}
			}
		}
		else if (renameObject instanceof IIpsPackageFragment){
			pack = (IIpsPackageFragment)renameObject;
			IFolder folder = (IFolder)pack.getCorrespondingResource();
			IFolder newFolder = folder.getParent().getFolder(new Path(newName.getText()));
			
			if (newFolder.exists()) {
				setMessage(Messages.RenamePage_errorFolderExists, ERROR);
				return;
			} else {
				// fix for windows: can not rename to an object with a name only
				// different in case.
				if (hasContentWithNameEqualsIgnoreCase(folder.getParent(), newName.getText())) {
					setMessage(Messages.RenamePage_errorFolderExists, ERROR);
					return;
				}
			}
		}
		
		super.setPageComplete(true);
	}
	
	private boolean hasContentWithNameEqualsIgnoreCase(IContainer parentFolder, String name) {
		try {
			IResource[] children = parentFolder.members();
			for (int i = 0; i < children.length; i++) {
				if (children[i].getName().equalsIgnoreCase(name)) {
					return true;
				}
			}
		} catch (CoreException e) {
			IpsPlugin.log(e);
		}
		return false;
	}
	
	/**
	 * Returns the name the user has entered. The name is allways qualified with the package name
	 * of the package containing the object to rename.
	 */
	public String getNewName() {
		String name = ""; //$NON-NLS-1$
		if (this.renameObject instanceof IProductCmpt || this.renameObject instanceof ITableContents) {
			name = ((IIpsObject)this.renameObject).getIpsPackageFragment().getName();
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


