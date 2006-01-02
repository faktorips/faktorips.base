package org.faktorips.devtools.core.ui.wizards.pctype;

import org.eclipse.ui.INewWizard;
import org.faktorips.devtools.core.ui.wizards.OpenNewWizardAction;


/**
 *
 */
public class OpenNewPcTypeWizardAction extends OpenNewWizardAction {

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.wizards.OpenNewWizardAction#createWizard()
     */
    public INewWizard createWizard() {
        return new NewPcTypeWizard();
    }

    /** 
     * Overridden method.
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
     */
    public void dispose() {
        // nothing to do
    }

}
