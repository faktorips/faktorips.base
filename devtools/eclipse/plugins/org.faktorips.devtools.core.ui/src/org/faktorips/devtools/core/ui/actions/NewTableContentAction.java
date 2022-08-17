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
import org.faktorips.devtools.core.ui.wizards.tablecontents.OpenNewTableContentsWizardAction;

/**
 * Open the new product component wizard.
 * 
 * @author Thorsten Guenther
 */
public class NewTableContentAction extends Action {

    private IWorkbenchWindow window;

    public NewTableContentAction(IWorkbenchWindow window) {
        super();
        this.window = window;
        setText(Messages.NewTableContentAction_name);
        setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("NewTableContentsWizard.gif")); //$NON-NLS-1$
    }

    @Override
    public void run() {
        OpenNewTableContentsWizardAction o = new OpenNewTableContentsWizardAction();
        o.init(window);
        o.run(this);
    }

}
