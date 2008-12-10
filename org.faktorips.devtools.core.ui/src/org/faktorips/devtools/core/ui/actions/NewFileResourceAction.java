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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.wizards.newresource.BasicNewFileResourceWizard;
import org.faktorips.devtools.core.IpsPlugin;

public class NewFileResourceAction extends IpsAction {

	private Shell shell;

	public NewFileResourceAction(Shell s, ISelectionProvider provider){
		super(provider);
		shell = s;
        this.setDescription(Messages.NewFileResourceAction_description);
        this.setText(Messages.NewFileResourceAction_name);
        this.setToolTipText(this.getDescription());
        setImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("NewFileWizard.gif")); //$NON-NLS-1$
	}
	
	/**
	 * Creates a new BasiNewFileWizard for creating an arbitrary file.
	 * {@inheritDoc}
	 */
	public void run(IStructuredSelection selection) {
		BasicNewFileResourceWizard wizard= new BasicNewFileResourceWizard();
		wizard.init(IpsPlugin.getDefault().getWorkbench(), selection);
		WizardDialog dialog = new WizardDialog(shell, wizard);
		dialog.open();
	}

}
