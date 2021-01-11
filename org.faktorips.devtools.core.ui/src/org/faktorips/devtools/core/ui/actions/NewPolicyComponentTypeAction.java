/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.wizards.policycmpttype.OpenNewPcTypeWizardAction;

/**
 * Opens the wizard for creating a new PolicyCmptType.
 * 
 * @author Stefan Widmaier
 */
public class NewPolicyComponentTypeAction extends Action {

    private IWorkbenchWindow window;

    public NewPolicyComponentTypeAction(IWorkbenchWindow window) {
        super();
        this.window = window;
        setText(Messages.NewPolicyComponentTypeAction_name);
        setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("NewPolicyCmptTypeWizard.gif")); //$NON-NLS-1$
    }

    @Override
    public void run() {
        OpenNewPcTypeWizardAction openAction = new OpenNewPcTypeWizardAction();
        openAction.init(window);
        openAction.run(this);
    }

}
