/*******************************************************************************
 * Copyright (c) 2007 Faktor Zehn GmbH und andere.
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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.dialogs.IpsPackageSortDefDialog;


/**
 *
 * Contribute the context menu for editing the package sort order.
 *
 * @author Markus Blum
 */
public class IpsEditSortOrderAction extends IpsAction {


    public IpsEditSortOrderAction(ISelectionProvider selectionProvider) {
        super(selectionProvider);
        super.setText(Messages.IpsEditSortOrderAction_text);
        super.setDescription(Messages.IpsEditSortOrderAction_description);
        super.setToolTipText(Messages.IpsEditSortOrderAction_tooltip);
        super.setImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("elcl16/alphab_sort_co.gif")); //$NON-NLS-1$

    }

    /**
     * {@inheritDoc}
     */
    public void run(IStructuredSelection selection) {
        Object element = ((IStructuredSelection) selection).getFirstElement();

        if (element instanceof IIpsElement) {
            IIpsElement ipsElement = (IIpsElement)element;
            IIpsProject project = ipsElement.getIpsProject();

            if (project.isProductDefinitionProject()) {
                IpsPackageSortDefDialog dialog = new IpsPackageSortDefDialog(Display.getCurrent().getActiveShell(), Messages.IpsEditSortOrderAction_dialogTitle, project);
                dialog.open();
            } else {
                MessageDialog dialog = new MessageDialog( Display.getCurrent().getActiveShell()
                                                 , Messages.IpsEditSortOrderAction_dialogTitle
                                                 , (Image) null
                                                 , Messages.IpsEditSortOrderAction_dialogInfoText
                                                 , MessageDialog.INFORMATION
                                                 , new String[] {IDialogConstants.OK_LABEL}
                                                 , 0);
                dialog.open();
            }
        }

    }
}
