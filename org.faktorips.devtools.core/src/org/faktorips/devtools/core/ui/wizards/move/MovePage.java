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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.IpsPckFragmentRefControl;

/**
 * Page to let the user select the target package for the move. 
 * 
 * @author Thorsten Guenther
 */
public class MovePage extends WizardPage implements ModifyListener {
	
	/**
	 * The input field for the target package. 
	 */
	private IpsPckFragmentRefControl targetInput;
	
	/**
	 * the package root used by the target input.
	 */
	private IIpsPackageFragmentRoot pckgRoot;
	
	/**
	 * The page-id to identify this page.
	 */
	private static final String PAGE_ID = "MoveWizard.move"; //$NON-NLS-1$

	/**
	 * Creates a new page to select the objects to copy.
	 */
	protected MovePage(IIpsElement[] selectedObjects) {
		super(PAGE_ID, Messages.MovePage_title, null);

		// find the package root
		if (selectedObjects[0] instanceof IIpsPackageFragment) {
			pckgRoot = ((IIpsPackageFragment)selectedObjects[0]).getRoot();
		}
		else if (selectedObjects[0] instanceof IIpsObject) {
			pckgRoot = ((IIpsObject)selectedObjects[0]).getIpsPackageFragment().getRoot();
		}

		super.setDescription(Messages.MovePage_description);
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

		toolkit.createFormLabel(inputRoot, Messages.MovePage_targetLabel);
		targetInput = toolkit.createPdPackageFragmentRefControl(pckgRoot, inputRoot);
		targetInput.getTextControl().addModifyListener(this);
	}

	/**
	 * Set the current completion state (and, if neccessary, messages for the user
	 * to help him to get the page complete).
	 */
	private void setPageComplete() {
		boolean complete = true;
		
		setMessage(null);
		
		if (targetInput == null) {
			// page not yet created, so do nothing.
			return;
		}
		
		String name = targetInput.getText();
		IIpsPackageFragment pack = targetInput.getPdPackageFragment();

		IStatus val= JavaConventions.validatePackageName(name);
		if (val.getSeverity() == IStatus.ERROR) {
			String msg = Messages.bind(Messages.errorNameNotValid, name);
			setMessage(msg, ERROR);
			complete = false;
		} else if (pack!=null && !pack.exists()) {
			setMessage(Messages.MovePage_infoPackageWillBeCreated, INFORMATION);
		}

		super.setPageComplete(complete);		
	}
	
	/**
	 * Returns the package selected as target. The returned package is neither guaranteed to exist nor
	 * to that it can be created.
	 */
	public IIpsPackageFragment getTarget() {
		return targetInput.getPdPackageFragment();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void modifyText(ModifyEvent e) {
		setPageComplete();
	}		
}


