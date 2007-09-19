/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.migration;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsProject;


/**
 * Supports migration for an IpsProject
 * 
 * @author Thorsten Guenther
 */
public class MigrationWizard extends Wizard implements IWorkbenchWizard {
    private ProjectSelectionPage projectSelectionPage;
    private ArrayList preSelected;
    
    public MigrationWizard(ArrayList preSelected) {
        setNeedsProgressMonitor(true);
        setWindowTitle(Messages.MigrationWizard_title);
        setDefaultPageImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("wizards/MigrationWizard.png")); //$NON-NLS-1$
        this.preSelected = preSelected;
    }
    
    public void addPages() {
        super.addPages();
        projectSelectionPage = new ProjectSelectionPage(preSelected);
        super.addPage(projectSelectionPage);
        super.addPage(new MigrationPage(projectSelectionPage));
    }

    /**
     * {@inheritDoc}
     */
    public boolean performFinish() {
        try {
            getContainer().run(false, true, new MigrateProjects());
        }
        catch (InvocationTargetException e1) {
            MessageDialog.openError(getShell(), Messages.MigrationWizard_titleError, Messages.MigrationWizard_msgError);
            IpsPlugin.log(e1);
        }
        catch (InterruptedException e1) {
            // the user pressed "cancel", so ignore it.
            MessageDialog.openInformation(getShell(), Messages.MigrationWizard_titleAbortion, Messages.MigrationWizard_msgAbortion);
        }
        return true;
    }


    /**
     * {@inheritDoc}
     */
    public void init(IWorkbench workbench, IStructuredSelection selection) {
    }
    
    class MigrateProjects implements IRunnableWithProgress {

        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
            IIpsProject[] projects = projectSelectionPage.getProjects();
            monitor.beginTask("Migrate Projects", projects.length * 10000);
            try {
                for (int i = 0; i < projects.length; i++) {
                    IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 10000);
                    try {
                        IpsPlugin.getDefault().getMigrationOperation(projects[i]).run(subMonitor);
                    }
                    catch (CoreException e) {
                        IpsPlugin.log(e);
                    }
                }
            } finally {
                monitor.done();
            }
        }
        
    }
}
