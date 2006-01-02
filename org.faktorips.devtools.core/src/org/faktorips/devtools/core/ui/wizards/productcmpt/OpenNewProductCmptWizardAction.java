package org.faktorips.devtools.core.ui.wizards.productcmpt;

import org.eclipse.ui.INewWizard;
import org.faktorips.devtools.core.ui.wizards.OpenNewWizardAction;


/**
 *
 */
public class OpenNewProductCmptWizardAction extends OpenNewWizardAction {

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.wizards.OpenNewWizardAction#createWizard()
     */
    public INewWizard createWizard() {
        return new NewProductCmptWizard();
    }

    /** 
     * Overridden method.
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
     */
    public void dispose() {
        // nothing to do
    }

}
