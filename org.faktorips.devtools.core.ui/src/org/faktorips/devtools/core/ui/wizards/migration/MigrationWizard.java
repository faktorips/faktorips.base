/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.migration;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.versionmanager.AbstractIpsFeatureMigrationOperation;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.wizards.ResultDisplayer;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.versionmanager.IpsMigrationOption;
import org.faktorips.runtime.MessageList;

/**
 * Supports migration for an IpsProject
 * 
 * @author Thorsten Guenther
 */
public class MigrationWizard extends Wizard implements IWorkbenchWizard {

    private ProjectSelectionPage projectSelectionPage;

    private List<IIpsProject> preSelected;

    private MigrationPage migrationPage;

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
        migrationPage = new MigrationPage(projectSelectionPage);
        super.addPage(migrationPage);
    }

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

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        // Nothing to do
    }

    class MigrateProjects implements IRunnableWithProgress {

        private MessageList messageList;

        @Override
        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
            IIpsProject[] projects = projectSelectionPage.getProjects();
            monitor.beginTask("Migration", projects.length * 10000); //$NON-NLS-1$
            try {
                for (IIpsProject project : projects) {
                    @SuppressWarnings("deprecation")
                    IProgressMonitor subMonitor = new org.eclipse.core.runtime.SubProgressMonitor(monitor, 10000);
                    try {
                        AbstractIpsFeatureMigrationOperation migrationOperation = IpsPlugin.getDefault()
                                .getMigrationOperation(project);
                        setSelectedMigrationOptions(migrationOperation);
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

        private void setSelectedMigrationOptions(AbstractIpsFeatureMigrationOperation migrationOperation) {
            Map<String, IpsMigrationOption> options = migrationPage.getOptions();
            migrationOperation.getOptions().forEach(o -> {
                if (options.containsKey(o.getId())) {
                    o.setActive(options.get(o.getId()).isActive());
                }
            });
        }

        public MessageList getMessageList() {
            return messageList;
        }

    }

}
