/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.ipspackage;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;

/**
 * Wizard zum Anlegen eines neuen Packages.
 */
public class NewIpsPackageWizard extends Wizard implements INewWizard {

    private IStructuredSelection selection;

    private IpsPackagePage packagePage;

    public NewIpsPackageWizard() {
        setWindowTitle(Messages.IpsPackagePage_title);
        setDefaultPageImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(
                "wizards/NewIpsPackageWizard.png")); //$NON-NLS-1$
    }

    @Override
    public final void addPages() {
        try {
            packagePage = createFirstPage(selection);
            addPage(packagePage);
            createAdditionalPages();
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    protected IpsPackagePage createFirstPage(IStructuredSelection selection) {
        return new IpsPackagePage(selection);
    }

    protected void createAdditionalPages() {
        // Empty default implementation
    }

    @Override
    public final boolean performFinish() {
        try {
            // IIpsPackageFragment pack = packagePage.getParentPackageFragment();
            IIpsPackageFragment pack = packagePage.getIpsPackageFragmentRoot().getDefaultIpsPackageFragment();
            String path = packagePage.getIpsPackagePath();
            if (!path.isEmpty()) {
                // do not add the when adding a new package in the root directory (default package)
                path += "."; //$NON-NLS-1$
            }
            path += packagePage.getIpsPackageName();
            pack.createSubPackage(path, true, null);
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
        return true;
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.selection = selection;
    }

}
