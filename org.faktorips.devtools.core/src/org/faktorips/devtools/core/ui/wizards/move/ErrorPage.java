package org.faktorips.devtools.core.ui.wizards.move;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.UIToolkit;

/**
 * Page to show an error for the move/rename which makes operation impossible.    
 * 
 * @author Thorsten Guenther
 */
public class ErrorPage extends WizardPage {
	
	/**
	 * The page-id to identify this page.
	 */
	private static final String PAGE_ID = "MoveWizard.error"; //$NON-NLS-1$

	/**
	 * Creates a new page to select the objects to copy.
	 */
	protected ErrorPage(String text) {
		super(PAGE_ID, Messages.ErrorPage_error, null);
		
		super.setDescription(text);
		setPageComplete(false);
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

		toolkit.createLabel(root, this.getDescription());
	}
}


