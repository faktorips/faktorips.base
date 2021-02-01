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
import org.faktorips.devtools.core.ui.wizards.bf.OpenNewBFWizardAction;

/**
 * Opens the wizard for creating a new business function.
 * 
 * @author Peter Erzberger
 */
public class NewBusinessFunctionAction extends Action {

    private IWorkbenchWindow window;

    /**
     * Creates a new <code>NewBusinessFunctionAction</code>.
     */
    public NewBusinessFunctionAction(IWorkbenchWindow window) {
        super();

        this.window = window;
        setText(Messages.NewBusinessFunctionAction_title);
        setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("NewBusinessFunction.gif")); //$NON-NLS-1$
    }

    @Override
    public void run() {
        OpenNewBFWizardAction action = new OpenNewBFWizardAction();
        action.init(window);
        action.run(this);
    }

}
