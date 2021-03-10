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
import org.eclipse.ui.PlatformUI;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.wizards.ipspackage.NewIpsPackageWizard;

/**
 * Opens a dialog to let the user specify a package name and creates the package folder in the
 * filesystem. It is possible to create subpackages by specifying a path separated with dots (".").
 * This action will then create the package defined by the path and all parent packages if they have
 * not been existing yet. The package name must be valid according to the java package name
 * conventions. Packages may only be created in <code>IpsSourceFolder</code>s.
 * 
 * @author Daniel Hohenberger
 */
public class NewIpsPacketAction extends IpsAction {

    private Shell shell;

    public NewIpsPacketAction(Shell shell, ISelectionProvider selectionProvider) {
        super(selectionProvider);
        this.shell = shell;
        setText(Messages.NewIpsPacketAction_name);
        setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("NewIpsPackageFragment.gif")); //$NON-NLS-1$
    }

    @Override
    public void run(IStructuredSelection selection) {
        NewIpsPackageWizard wizard = new NewIpsPackageWizard();
        wizard.init(PlatformUI.getWorkbench(), selection);
        WizardDialog dialog = new WizardDialog(shell, wizard);
        dialog.open();
    }

}
