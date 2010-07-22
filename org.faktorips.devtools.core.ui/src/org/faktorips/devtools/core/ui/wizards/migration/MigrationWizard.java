/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.wizards.migration;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

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
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.versionmanager.AbstractIpsFeatureMigrationOperation;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.wizards.ResultDisplayer;
import org.faktorips.util.message.MessageList;

/**
 * Supports migration for an IpsProject
 * 
 * @author Thorsten Guenther
 */
public class MigrationWizard extends Wizard implements IWorkbenchWizard {
    private ProjectSelectionPage projectSelectionPage;
    private List<IIpsProject> preSelected;

    public MigrationWizard(List<IIpsProject> preSelected) {
        setNeedsProgressMonitor(true);
        setWindowTitle(Messages.MigrationWizard_title);
        setDefaultPageImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(
                "wizards/MigrationWizard.png")); //$NON-NLS-1$
        this.preSelected = preSelected;
    }

    @Override
    public void addPages() {
        super.addPages();
        projectSelectionPage = new ProjectSelectionPage(preSelected);
        super.addPage(projectSelectionPage);
        super.addPage(new MigrationPage(projectSelectionPage));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean performFinish() {
        try {
            MigrateProjects migrationOperation = new MigrateProjects();
            getContainer().run(false, true, migrationOperation);
            MessageList messageList = migrationOperation.getMessageList();
            if (!messageList.isEmpty()) {
                getShell().getDisplay().syncExec(
                        new ResultDisplayer(getShell(), Messages.MigrationWizard_title, messageList));
            }
        } catch (InvocationTargetException e1) {
            MessageDialog.openError(getShell(), Messages.MigrationWizard_titleError, Messages.MigrationWizard_msgError);
            IpsPlugin.log(e1);
        } catch (InterruptedException e1) {
            // the user pressed "cancel", so ignore it.
            MessageDialog.openInformation(getShell(), Messages.MigrationWizard_titleAbortion,
                    Messages.MigrationWizard_msgAbortion);
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
    }

    class MigrateProjects implements IRunnableWithProgress {
        private MessageList messageList;

        @Override
        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
            IIpsProject[] projects = projectSelectionPage.getProjects();
            monitor.beginTask("Migration", projects.length * 10000); //$NON-NLS-1$
            try {
                for (IIpsProject project : projects) {
                    IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 10000);
                    try {
                        AbstractIpsFeatureMigrationOperation migrationOperation = IpsPlugin.getDefault()
                                .getMigrationOperation(project);
                        migrationOperation.run(subMonitor);
                        messageList = migrationOperation.getMessageList();
                    } catch (CoreException e) {
                        IpsPlugin.log(e);
                    }
                }
            } finally {
                monitor.done();
            }
        }

        public MessageList getMessageList() {
            return messageList;
        }
    }
}
