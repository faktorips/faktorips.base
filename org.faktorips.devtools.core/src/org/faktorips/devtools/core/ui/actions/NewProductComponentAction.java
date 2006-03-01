package org.faktorips.devtools.core.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.faktorips.devtools.core.ui.wizards.productcmpt.OpenNewProductCmptWizardAction;

/**
 * Open the new product component wizard.
 * 
 * @author Thorsten Guenther
 */
public class NewProductComponentAction extends Action {

	private IWorkbenchWindow window;
	
	public NewProductComponentAction(IWorkbenchWindow window) {
		super();
		this.window = window;
		setText(Messages.NewProductComponentAction_name);
	}

	/** 
	 * {@inheritDoc}
	 */
	public void run() {
		OpenNewProductCmptWizardAction o = new OpenNewProductCmptWizardAction();
		o.init(window);
		o.run(this);
	}
}
