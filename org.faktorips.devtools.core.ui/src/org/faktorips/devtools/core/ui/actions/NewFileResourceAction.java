/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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
