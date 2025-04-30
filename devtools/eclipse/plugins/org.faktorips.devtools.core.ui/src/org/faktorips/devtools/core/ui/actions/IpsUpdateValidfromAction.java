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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.ui.util.TypedSelection;
import org.faktorips.devtools.core.ui.wizards.refactor.IpsUpdateValidfromWizard;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;

public class IpsUpdateValidfromAction extends IpsAction {

    private Shell shell;

    public IpsUpdateValidfromAction(Shell shell, ISelectionProvider selectionProvider) {
        super(selectionProvider);
        this.shell = shell;
        setText(Messages.IpsUpdateValidFromAction_name);
    }

    @Override
    public void run(IStructuredSelection selection) {

        if (selection.size() == 1) {
            final TypedSelection<IAdaptable> typedSelection = new TypedSelection<>(IAdaptable.class, selection,
                    1);
            if (typedSelection.getElement().getAdapter(IProductCmpt.class) != null) {
                IProductCmpt root = typedSelection.getElement().getAdapter(IProductCmpt.class);
                IpsUpdateValidfromWizard dcw = new IpsUpdateValidfromWizard(root);
                WizardDialog wd = new WizardDialog(shell, dcw);
                wd.setBlockOnOpen(true);
                wd.open();
            }
        }

    }

}
