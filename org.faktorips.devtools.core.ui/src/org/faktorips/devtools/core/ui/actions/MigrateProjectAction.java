/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.wizards.migration.OpenMigrationWizardAction;

/**
 * Opens the wizard for project-migration
 * 
 * @author Thorsten Guenther
 */
public class MigrateProjectAction extends Action {

    private IWorkbenchWindow window;
    private IStructuredSelection selection;

    public MigrateProjectAction(IWorkbenchWindow window, IStructuredSelection selection) {
        super();
        this.window = window;
        this.selection = selection;
        setText(Messages.MigrateProjectAction_text);
        setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("MigrationWizard.gif")); //$NON-NLS-1$
    }

    @Override
    public void run() {
        OpenMigrationWizardAction action = new OpenMigrationWizardAction();
        action.init(window);
        action.selectionChanged(this, selection);
        action.run(this);
    }

}
