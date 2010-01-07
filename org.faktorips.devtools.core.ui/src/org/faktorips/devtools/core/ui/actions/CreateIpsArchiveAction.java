/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
 * @see org.faktorips.devtools.core.model.ipsproject.IIpsArchive
 * 
 * @author Jan Ortmann
 */
public class CreateIpsArchiveAction extends IpsAction {

    /**
     * @param selectionProvider
     */
    public CreateIpsArchiveAction(ISelectionProvider selectionProvider) {
        super(selectionProvider);
        super.setText(Messages.CreateIpsArchiveAction_Name);
        super.setDescription(Messages.CreateIpsArchiveAction_Description);
        super.setToolTipText(Messages.CreateIpsArchiveAction_Tooltip);
        super.setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("ExportIpsArchive.gif")); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
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
