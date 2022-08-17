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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.dialogs.IpsPackageSortDefDialog;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
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
        super.setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("elcl16/alphab_sort_co.gif")); //$NON-NLS-1$

    }

    @Override
    public void run(IStructuredSelection selection) {
        Object element = selection.getFirstElement();

        IIpsPackageFragment packageFragment = null;

        if (element instanceof IIpsElement) {
            IIpsElement ipsElement = (IIpsElement)element;
            IIpsProject project = ipsElement.getIpsProject();
            if (project.isProductDefinitionProject()) {
                packageFragment = findPackageFragment(ipsElement);
            }
        }
        if (packageFragment != null) {
            IpsPackageSortDefDialog dialog = new IpsPackageSortDefDialog(Display.getCurrent().getActiveShell(),
                    Messages.IpsEditSortOrderAction_dialogTitle, packageFragment);
            dialog.open();
        } else {
            MessageDialog dialog = new MessageDialog(Display.getCurrent().getActiveShell(),
                    Messages.IpsEditSortOrderAction_dialogTitle, (Image)null,
                    Messages.IpsEditSortOrderAction_dialogInfoText, MessageDialog.INFORMATION,
                    new String[] { IDialogConstants.OK_LABEL }, 0);
            dialog.open();
        }

    }

    private static IIpsPackageFragment findPackageFragment(IIpsElement ipsElement) {
        IIpsPackageFragment packageFragment = null;

        if (ipsElement instanceof IIpsPackageFragment) {
            packageFragment = (IIpsPackageFragment)ipsElement;
        } else if (ipsElement instanceof IIpsPackageFragmentRoot) {
            packageFragment = ((IIpsPackageFragmentRoot)ipsElement).getDefaultIpsPackageFragment();
        } else {
            IIpsElement parent = ipsElement.getParent();
            while (parent != null && !(parent instanceof IIpsPackageFragment)) {
                parent = parent.getParent();
            }
            if (parent instanceof IIpsPackageFragment) {
                packageFragment = (IIpsPackageFragment)parent;
            }
        }
        return packageFragment;
    }
}
