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

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionDelegate;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.ui.dialogs.IpsPackageSortDefDialog;

/**
 *
 * @author Markus Blum
 */
public class EditSortOrderAction extends ActionDelegate implements IObjectActionDelegate {

    private Shell shell;
    private ISelection selection;

    /**
     * {@inheritDoc}
     */
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        shell = targetPart.getSite().getShell();
    }

    /**
     * {@inheritDoc}
     */
    public void run(IAction action) {

        if (! (selection instanceof IStructuredSelection))
            return;

        Object element =  ((IStructuredSelection) selection).getFirstElement();

        if (element instanceof IIpsProject) {
            IIpsProject project = (IIpsProject)element;

            if (project.isProductDefinitionProject()) {
                IpsPackageSortDefDialog dialog = new IpsPackageSortDefDialog(shell, "Edit Sort Order", project);
                dialog.open();
            } else {
                MessageDialog dialog = new MessageDialog( shell
                                                 , "Edit Sort Order"
                                                 , (Image) null
                                                 , "Sortierung wird nur für Produktdefinitions-Projekte unterstützt."
                                                 , MessageDialog.INFORMATION
                                                 , new String[] {IDialogConstants.OK_LABEL}
                                                 , 0);
                dialog.open();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void selectionChanged(IAction action, ISelection selection) {
        this.selection= selection;
    }

}
