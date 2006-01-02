package org.faktorips.devtools.core.ui.wizards.tablecontents;

import org.eclipse.ui.INewWizard;
import org.faktorips.devtools.core.ui.wizards.OpenNewWizardAction;


/**
 *
 */
public class OpenNewTableContentsWizardAction extends OpenNewWizardAction {

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.wizards.OpenNewWizardAction#createWizard()
     */
    public INewWizard createWizard() {
        return new NewTableContentsWizard();
    }

    /** 
     * Overridden method.
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
     */
    public void dispose() {
        // nothing to do
    }

}
