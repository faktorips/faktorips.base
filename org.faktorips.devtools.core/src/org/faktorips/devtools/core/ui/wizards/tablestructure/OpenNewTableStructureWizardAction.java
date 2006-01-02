package org.faktorips.devtools.core.ui.wizards.tablestructure;

import org.eclipse.ui.INewWizard;
import org.faktorips.devtools.core.ui.wizards.OpenNewWizardAction;


/**
 *
 */
public class OpenNewTableStructureWizardAction extends OpenNewWizardAction {

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.wizards.OpenNewWizardAction#createWizard()
     */
    public INewWizard createWizard() {
        return new NewTableStructureWizard();
    }

    /** 
     * Overridden method.
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
     */
    public void dispose() {
        // nothing to do
    }

}
