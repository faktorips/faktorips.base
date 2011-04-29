/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.wizards.ipspackage.NewIpsPackageWizard;

/**
 * Opens a dialog to let the user specify a package name and creates the package folder in the
 * filesystem. It is possible to create subpackages by specifying a path separated with dots (".").
 * This action will then create the package defined by the path and all parent packages if they have
 * not been existing yet. The package name must be valid according to the java package name
 * conventions. Packages may only be created in <code>IpsSourceFolder</i>s.
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
        wizard.init(IpsPlugin.getDefault().getWorkbench(), selection);
        WizardDialog dialog = new WizardDialog(shell, wizard);
        dialog.open();
    }

}
