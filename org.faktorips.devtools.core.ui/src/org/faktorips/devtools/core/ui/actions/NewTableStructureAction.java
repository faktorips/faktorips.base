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
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.wizards.tablestructure.OpenNewTableStructureWizardAction;

/**
 * Opens the wizard for creating a new TableStructure.
 * 
 * @author Joerg Ortmann
 */
public class NewTableStructureAction extends Action {

    private IWorkbenchWindow window;

    public NewTableStructureAction(IWorkbenchWindow window) {
        super();
        this.window = window;
        setText(Messages.NewTableStructureAction_name);
        setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("NewTableStructureWizard.gif")); //$NON-NLS-1$
    }

    @Override
    public void run() {
        IWorkbenchWindowActionDelegate openAction = new OpenNewTableStructureWizardAction();
        openAction.init(window);
        openAction.run(this);
    }

}
