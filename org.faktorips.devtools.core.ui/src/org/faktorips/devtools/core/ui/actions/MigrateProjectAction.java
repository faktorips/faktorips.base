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
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.wizards.migration.OpenMigrationWizardAction;

/**
 * Opens the wizard for project-migration
 * 
 * @author Thorsten Guenther
 */
public class MigrateProjectAction extends Action {

    private IWorkbenchWindow window;
    private IStructuredSelection selection;

    public MigrateProjectAction(IWorkbenchWindow window, IStructuredSelection selection) {
        super();
        this.window = window;
        this.selection = selection;
        setText(Messages.MigrateProjectAction_text);
        setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("MigrationWizard.gif")); //$NON-NLS-1$
    }

    @Override
    public void run() {
        OpenMigrationWizardAction action = new OpenMigrationWizardAction();
        action.init(window);
        action.selectionChanged(this, selection);
        action.run(this);
    }

}
