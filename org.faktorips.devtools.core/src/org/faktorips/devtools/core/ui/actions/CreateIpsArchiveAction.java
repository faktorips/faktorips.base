/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.wizards.ipsarchiveexport.IpsArchiveExportWizard;

/**
 * Action delegate to create an ips archive.
 * 
 * @see org.faktorips.devtools.core.model.IIpsArchive
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
        super.setImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("ExportIpsArchive.gif")); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
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
