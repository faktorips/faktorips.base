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

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.wizards.ipsarchiveexport.IpsArchiveExportWizard;

/**
 * Action delegate to create an ips archive.
 * 
 * @see org.faktorips.devtools.model.ipsproject.IIpsArchive
 * 
 * @author Jan Ortmann
 */
public class CreateIpsArchiveAction extends IpsAction {

    public CreateIpsArchiveAction(ISelectionProvider selectionProvider) {
        super(selectionProvider);
        super.setText(Messages.CreateIpsArchiveAction_Name);
        super.setDescription(Messages.CreateIpsArchiveAction_Description);
        super.setToolTipText(Messages.CreateIpsArchiveAction_Tooltip);
        super.setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("ExportIpsArchive.gif")); //$NON-NLS-1$
    }

    @Override
    public void run(IStructuredSelection selection) {
        if (selection == null) {
            return;
        }
        IpsArchiveExportWizard wizard = new IpsArchiveExportWizard();
        wizard.init(IpsPlugin.getDefault().getWorkbench(), selection);
        WizardDialog dialog = new WizardDialog(IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow()
                .getShell(), wizard);
        dialog.open();
    }

}
