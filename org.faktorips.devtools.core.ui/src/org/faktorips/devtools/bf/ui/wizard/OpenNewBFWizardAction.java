package org.faktorips.devtools.bf.ui.wizard;

import org.eclipse.ui.INewWizard;
import org.faktorips.devtools.core.ui.wizards.OpenNewWizardAction;

//TODO has to be registered
public class OpenNewBFWizardAction extends OpenNewWizardAction {

    /**
     * {@inheritDoc}
     */
    public INewWizard createWizard() {
        return new NewBFWizard();
    }

    /**
     * {@inheritDoc}
     */
    public void dispose() {
        // nothing to do
    }

}
