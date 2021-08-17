/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;

/**
 * Sets the next-button in {@link WizardDialog} as default selection, if available. Otherwise the
 * {@link WizardDialog} sets the finish-button as default.
 */
public class NextButtonDefaultWizardDialog extends WizardDialog {

    public NextButtonDefaultWizardDialog(Shell parentShell, IWizard newWizard) {
        super(parentShell, newWizard);
    }

    /**
     * Overrides {@link WizardDialog#updateButtons()} in order to declare the next button as default
     * selection, if available.
     */
    @Override
    public void updateButtons() {
        super.updateButtons();
        Button nextButton = getButton(IDialogConstants.NEXT_ID);
        if (nextButton.isEnabled()) {
            getShell().setDefaultButton(nextButton);
        }
    }
}
