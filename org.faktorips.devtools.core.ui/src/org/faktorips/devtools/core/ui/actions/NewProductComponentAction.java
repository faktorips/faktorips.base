/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.wizards.productcmpt.OpenNewProductCmptWizardAction;

/**
 * Open the new product component wizard.
 * 
 * @author Thorsten Guenther
 */
public class NewProductComponentAction extends Action {

    private IWorkbenchWindow window;

    public NewProductComponentAction(IWorkbenchWindow window) {
        super();
        this.window = window;
        setText(Messages.NewProductComponentAction_name);
        setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("NewProductCmptWizard.gif")); //$NON-NLS-1$
    }

    @Override
    public void run() {
        OpenNewProductCmptWizardAction o = new OpenNewProductCmptWizardAction();
        o.init(window);
        o.run(this);
    }

}
