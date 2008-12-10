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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.wizards.migration.OpenMigrationWizardAction;

/**
 * Opens the wizard for project-migration
 * 
 * @author Thorsten Guenther
 */
public class MigrateProjectAction extends Action {
	private IWorkbenchWindow window;
    private IStructuredSelection selection;
	
	public MigrateProjectAction(IWorkbenchWindow window, IStructuredSelection selection){
		super();
		this.window = window;
        this.selection = selection;
		setText(Messages.MigrateProjectAction_text);
        setImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("MigrationWizard.gif")); //$NON-NLS-1$
	}
	
	public void run(){
        OpenMigrationWizardAction action = new OpenMigrationWizardAction();
        action.init(window);
        action.selectionChanged(this, selection);
        action.run(this);
	}

}
