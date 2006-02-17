package org.faktorips.devtools.core.ui;

import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.faktorips.devtools.core.ui.wizards.productcmpt.OpenNewProductCmptWizardAction;

/**
 * Action-Delegate to open the new product component wizard in a context-menu
 * 
 * @author Thorsten Guenther
 */
public class NewProductCmptActionDelegate extends OpenNewProductCmptWizardAction implements IViewActionDelegate {

	public void init(IViewPart view) {
		super.init(view.getViewSite().getWorkbenchWindow());
	}
}
