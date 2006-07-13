/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.pctype.relationwizard;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.faktorips.devtools.core.ui.UIToolkit;

public class ErrorPage extends AbstractPcTypeRelationWizardPage {
	private static final String PAGE_ID = "Error"; //$NON-NLS-1$
	
	private Label details;
	
	public ErrorPage(NewPcTypeRelationWizard newPcTypeRelationWizard) {
		super(PAGE_ID, Messages.NewPcTypeRelationWizard_error_title,
				Messages.NewPcTypeRelationWizard_error_desciption,
				newPcTypeRelationWizard);
		setPageComplete(true);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void connectToModel() {
	}

	/**
	 * {@inheritDoc}
	 */
	protected void createControls(Composite parent) {
		UIToolkit uiToolkit = wizard.getUiToolkit();
		details = uiToolkit.createLabel(parent,
				""); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	protected boolean updateControlStatus() {
		return true;
	}
	
	/**
	 * This method returns false because the navigation to this page will be
	 * done manually in case of an error.
	 * {@inheritDoc}
	 */
	protected boolean isPageVisible() {
		return false;
	}
	
	/**
	 * This method returns false to disable the finish button.
	 * {@inheritDoc}
	 */
	public boolean isPageComplete() {
		return !wizard.isError();
	}

	/**
	 * This method returns null to disable the next button.
	 * {@inheritDoc}
	 */
	public IWizardPage getPreviousPage() {
		return null;
	}

	/**
	 * Sets the details of the error.
	 */
	public void storeErrorDetails(String errorDetails) {
		details.setText(errorDetails);
		details.pack();
	}
}
