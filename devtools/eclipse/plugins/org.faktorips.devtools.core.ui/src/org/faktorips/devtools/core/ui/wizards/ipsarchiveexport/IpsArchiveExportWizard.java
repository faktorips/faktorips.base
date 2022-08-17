/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.ipsarchiveexport;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.WorkbenchRunnableAdapter;
import org.faktorips.devtools.model.CreateIpsArchiveOperation;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;

public class IpsArchiveExportWizard extends Wizard implements IExportWizard {

    public static final String ID = "org.faktorips.devtools.core.ui.wizards.ipsarchiveexport.IpsArchiveExportWizard"; //$NON-NLS-1$
    private static final String DIALOG_SETTINGS_KEY = "IpsArchiveExportWizard"; //$NON-NLS-1$

    private IpsArchivePackageWizardPage ipsArPackageWizardPage;

    private boolean hasNewDialogSettings;

    /**
     * The selection this wizard is called on.
     */
    private IStructuredSelection selection;

    /**
     * Create a new IpsArExportWizard
     */
    public IpsArchiveExportWizard() {
        super();
        setWindowTitle(Messages.IpsArchiveExportWizard_Export);
        setDefaultPageImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(
                "wizards/ExportIpsArchiveWizard.png")); //$NON-NLS-1$

        IDialogSettings workbenchSettings = IpsPlugin.getDefault().getDialogSettings();
        IDialogSettings section = workbenchSettings.getSection(DIALOG_SETTINGS_KEY);
        if (section == null) {
            hasNewDialogSettings = true;
        } else {
            hasNewDialogSettings = false;
            setDialogSettings(section);
        }
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.selection = selection;
        setNeedsProgressMonitor(true);
    }

    @Override
    public void addPages() {
        super.addPages();
        ipsArPackageWizardPage = new IpsArchivePackageWizardPage(selection);
        addPage(ipsArPackageWizardPage);
    }

    @Override
    public boolean performFinish() {
        File destinationFile = ipsArPackageWizardPage.getDestinationFile();
        if (!destinationFile.getParentFile().exists()) {
            if (!askForCreateParent(destinationFile.getParentFile())) {
                return false;
            }
        }

        if (!executeArchiveOperation()) {
            return false;
        }

        // save the dialog settings
        if (hasNewDialogSettings) {
            IDialogSettings workbenchSettings = IpsPlugin.getDefault().getDialogSettings();
            IDialogSettings section = workbenchSettings.getSection(DIALOG_SETTINGS_KEY);
            section = workbenchSettings.addNewSection(DIALOG_SETTINGS_KEY);
            setDialogSettings(section);
        }
        ipsArPackageWizardPage.saveWidgetValues();

        return true;
    }

    // TODO AW: Internationalize
    private boolean askForCreateParent(File folder) {
        if (MessageDialog.openQuestion(getShell(), "Create Directory", //$NON-NLS-1$
                NLS.bind("Directory {0} does not exist. Do you want to create it?", folder.getAbsolutePath()))) { //$NON-NLS-1$
            try {
                folder.mkdirs();
                return true;
            } catch (Exception e) {
                IpsPlugin.logAndShowErrorDialog(new Status(IStatus.ERROR, IpsPlugin.PLUGIN_ID, "Could not create " //$NON-NLS-1$
                        + folder.getAbsoluteFile(), e));
            }
        }
        return false;
    }

    private boolean executeArchiveOperation() {
        Object[] checkedObjects = ipsArPackageWizardPage.getCheckedElements();
        List<IIpsPackageFragmentRoot> checkedRoots = new ArrayList<>(checkedObjects.length);
        for (Object checkedObject : checkedObjects) {
            if (checkedObject instanceof IIpsPackageFragmentRoot) {
                checkedRoots.add((IIpsPackageFragmentRoot)checkedObject);
            }
        }

        CreateIpsArchiveOperation op = new CreateIpsArchiveOperation(
                checkedRoots.toArray(new IIpsPackageFragmentRoot[checkedRoots.size()]),
                ipsArPackageWizardPage.getDestinationFile());
        op.setInclJavaBinaries(ipsArPackageWizardPage.isInclJavaBinaries());
        op.setInclJavaSources(ipsArPackageWizardPage.isInclJavaSources());

        WorkbenchRunnableAdapter workbenchRunnableAdapter = new WorkbenchRunnableAdapter(op);
        try {
            getContainer().run(true, true, workbenchRunnableAdapter);
        } catch (InterruptedException e) {
            return false;
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
            return false;
        }
        return true;
    }
}
