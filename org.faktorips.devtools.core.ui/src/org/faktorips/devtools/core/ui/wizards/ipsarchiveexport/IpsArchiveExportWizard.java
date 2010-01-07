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
import org.faktorips.devtools.core.model.CreateIpsArchiveOperation;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.WorkbenchRunnableAdapter;

public class IpsArchiveExportWizard extends Wizard implements IExportWizard {
    public static String ID = "org.faktorips.devtools.core.ui.wizards.ipsarchiveexport.IpsArchiveExportWizard"; //$NON-NLS-1$
    private static String DIALOG_SETTINGS_KEY = "IpsArchiveExportWizard"; //$NON-NLS-1$

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

    /**
     * {@inheritDoc}
     */
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.selection = selection;
        setNeedsProgressMonitor(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addPages() {
        super.addPages();
        ipsArPackageWizardPage = new IpsArchivePackageWizardPage(selection);
        addPage(ipsArPackageWizardPage);
    }

    /**
     * {@inheritDoc}
     */
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

    private boolean askForCreateParent(File folder) {
        if (MessageDialog.openQuestion(getShell(), "Create Directory", NLS.bind(
                "Directory {0} does not exists. Do you want to create it?", folder.getAbsolutePath()))) {
            try {
                folder.mkdirs();
                return true;
            } catch (Exception e) {
                IpsPlugin.logAndShowErrorDialog(new Status(IStatus.ERROR, IpsPlugin.PLUGIN_ID, "Could not create "
                        + folder.getAbsoluteFile(), e));
            }
        }
        return false;
    }

    private boolean executeArchiveOperation() {
        Object[] checkedObjects = ipsArPackageWizardPage.getCheckedElements();
        List<IIpsPackageFragmentRoot> checkedRoots = new ArrayList<IIpsPackageFragmentRoot>(checkedObjects.length);
        for (int i = 0; i < checkedObjects.length; i++) {
            if (checkedObjects[i] instanceof IIpsPackageFragmentRoot) {
                checkedRoots.add((IIpsPackageFragmentRoot)checkedObjects[i]);
            }
        }

        CreateIpsArchiveOperation op = new CreateIpsArchiveOperation(checkedRoots
                .toArray(new IIpsPackageFragmentRoot[checkedRoots.size()]), ipsArPackageWizardPage.getDestinationFile());
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
