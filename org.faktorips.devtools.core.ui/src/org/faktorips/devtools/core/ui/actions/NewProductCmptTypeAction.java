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
import org.faktorips.devtools.core.ui.wizards.productcmpttype.OpenNewProductCmptTypeWizardAction;

/**
 * Opens the wizard for creating a new ProductCmptType.
 * 
 * @author Joerg Ortmann
 */
public class NewProductCmptTypeAction extends Action {

    private IWorkbenchWindow window;

    public NewProductCmptTypeAction(IWorkbenchWindow window) {
        super();
        this.window = window;
        setText(Messages.NewProductCmptTypeAction_name);
        setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("NewProductCmptTypeWizard.gif")); //$NON-NLS-1$
    }

    @Override
    public void run() {
        OpenNewProductCmptTypeWizardAction openAction = new OpenNewProductCmptTypeWizardAction();
        openAction.init(window);
        openAction.run(this);
    }

}
