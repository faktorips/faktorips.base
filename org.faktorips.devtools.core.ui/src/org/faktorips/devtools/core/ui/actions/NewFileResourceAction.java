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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.wizards.newresource.BasicNewFileResourceWizard;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

public class NewFileResourceAction extends IpsAction {

    private Shell shell;

    public NewFileResourceAction(Shell s, ISelectionProvider provider) {
        super(provider);
        shell = s;
        setDescription(Messages.NewFileResourceAction_description);
        setText(Messages.NewFileResourceAction_name);
        setToolTipText(getDescription());
        setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("NewFileWizard.gif")); //$NON-NLS-1$
    }

    /**
     * Creates a new BasiNewFileWizard for creating an arbitrary file. {@inheritDoc}
     */
    @Override
    public void run(IStructuredSelection selection) {
        BasicNewFileResourceWizard wizard = new BasicNewFileResourceWizard();
        wizard.init(IpsPlugin.getDefault().getWorkbench(), selection);
        WizardDialog dialog = new WizardDialog(shell, wizard);
        dialog.open();
    }

}
