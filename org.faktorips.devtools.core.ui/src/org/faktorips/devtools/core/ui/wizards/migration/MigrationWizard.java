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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.versionmanager.AbstractIpsFeatureMigrationOperation;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.wizards.ResultDisplayer;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.versionmanager.options.IpsEnumMigrationOption;
import org.faktorips.devtools.model.versionmanager.options.IpsMigrationOption;
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

    /**
     * We blindly trust that the id of a migration is unique for a version.
     */
    @SuppressWarnings("unchecked")
    private static <T> void setSelectedValue(IpsMigrationOption<T> option, Object value) {
        option.setSelectedValue((T)value);
    }

    class MigrateProjects implements IRunnableWithProgress {

        private MessageList messageList;

        @Override
        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
            IIpsProject[] projects = projectSelectionPage.getProjects();
            SubMonitor subMonitor = SubMonitor.convert(monitor, "Migration", projects.length * 10000); //$NON-NLS-1$

            for (IIpsProject project : projects) {
                SubMonitor subProgressMonitor = subMonitor.split(10000);
                try {
                    AbstractIpsFeatureMigrationOperation migrationOperation = IpsPlugin.getDefault()
                            .getMigrationOperation(project);
                    setSelectedMigrationOptions(migrationOperation);
                    migrationOperation.run(subProgressMonitor);
                    messageList = migrationOperation.getMessageList();
                } catch (IpsException e) {
                    IpsPlugin.log(e);
                }
            }
        }

        private void setSelectedMigrationOptions(AbstractIpsFeatureMigrationOperation migrationOperation) {
            Map<String, IpsMigrationOption<?>> optionsFromUI = migrationPage.getOptions();
            migrationOperation.getOptions().forEach(o -> {
                if (optionsFromUI.containsKey(o.getId())) {
                    IpsMigrationOption<?> ipsMigrationOption = optionsFromUI.get(o.getId());
                    if (o instanceof IpsEnumMigrationOption<?>) {
                        IpsEnumMigrationOption<? extends Enum<?>> enumOption = (IpsEnumMigrationOption<?>)o;
                        enumOption.setSelectedEnumValue((Enum<?>)ipsMigrationOption.getSelectedValue());
                    } else {
                        setSelectedValue(o, ipsMigrationOption.getSelectedValue());
                    }

                }
            });
        }

        public MessageList getMessageList() {
            return messageList;
        }

    }

}
