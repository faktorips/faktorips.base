/***************************************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) dürfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1
 * (vor Gründung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorips.org/legal/cl-v01.html eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn GmbH - initial API and implementation
 * 
 **************************************************************************************************/

package org.faktorips.devtools.core.ui.wizards.tableimport;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.ui.wizards.ResultDisplayer;
import org.faktorips.devtools.extsystems.AbstractExternalTableFormat;
import org.faktorips.util.message.MessageList;

/**
 * Wizard to import external tables into ipstablecontents.
 * 
 * @author Thorsten Waertel, Thorsten Guenther
 */
public class TableImportWizard extends Wizard implements IImportWizard {
    public static String ID = "org.faktorips.devtools.core.ui.wizards.tableimport.TableImportWizard"; //$NON-NLS-1$
    private static String DIALOG_SETTINGS_KEY = "TableImportWizard"; //$NON-NLS-1$

    private IStructuredSelection selection;
    private SelectFileAndImportMethodPage filePage;
    private NewContentsPage newContentsPage;
    private SelectContentsPage selectContentsPage;

    private boolean hasNewDialogSettings;
    
    /**
     * Create a new import-wizard.
     */
    public TableImportWizard() {
        super();
        setWindowTitle(Messages.TableImport_title);
        this.setDefaultPageImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("wizards/TableImportWizard.png")); //$NON-NLS-1$

        IDialogSettings workbenchSettings= IpsPlugin.getDefault().getDialogSettings();
        IDialogSettings section= workbenchSettings.getSection(DIALOG_SETTINGS_KEY); //$NON-NLS-1$
        if (section == null)
            hasNewDialogSettings = true;
        else {
            hasNewDialogSettings = false;
            setDialogSettings(section);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void addPages() {
        try {
            filePage = new SelectFileAndImportMethodPage(null);
            addPage(filePage);
            newContentsPage = new NewContentsPage(selection);
            addPage(newContentsPage);
            selectContentsPage = new SelectContentsPage(selection);
            addPage(selectContentsPage);
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean performFinish() {
        try {
            final String filename = filePage.getFilename();
            final AbstractExternalTableFormat format = filePage.getFormat();
            final ITableStructure structure = getTableStructure();
            ITableContents contents = getTableContents();
            final ITableContentsGeneration generation = (ITableContentsGeneration)contents.getGenerations()[0];
            final String nullRepresentation = filePage.getNullRepresentation();

            // no append, so remove any existing content
            if (!filePage.isImportExistingAppend()) {
                generation.clear();
            }

            MessageList messageList = new MessageList();
            IWorkspaceRunnable runnable = format.getImportTableOperation(structure, new Path(filename),
                    generation, nullRepresentation, filePage.isImportIgnoreColumnHeaderRow(), messageList);

            /*
             * use a ProgressMonitorDialog to display the progress and allow the user to cancel the
             * process - which both is not possible if only getContainer().run() is called.
             */
            ProgressMonitorDialog pmd = new ProgressMonitorDialog(getShell());
            pmd.setCancelable(true);
            pmd.open();
            IIpsModel model = IpsPlugin.getDefault().getIpsModel(); 
            model.runAndQueueChangeEvents(runnable, pmd.getProgressMonitor());
            pmd.close();
            if (!messageList.isEmpty()) {
                getShell().getDisplay().syncExec(new ResultDisplayer(getShell(), messageList));
            }

            // save the dialog settings
            if (hasNewDialogSettings) {
                IDialogSettings workbenchSettings = IpsPlugin.getDefault().getDialogSettings();
                IDialogSettings section = workbenchSettings.getSection(DIALOG_SETTINGS_KEY);
                section = workbenchSettings.addNewSection(DIALOG_SETTINGS_KEY);
                setDialogSettings(section);
            }
            selectContentsPage.saveWidgetValues();
            newContentsPage.saveWidgetValues();
            filePage.saveWidgetValues();
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(new IpsStatus("An error occured during the import process.", e)); //$NON-NLS-1$
        }

        // this implementation of this method should always return true since this causes the wizard
        // dialog to close. in either case if an exception arises or not it doesn't make sense to
        // keep the dialog up
        return true;
    }

    /**
     * @return the table-structure the imported table content has to follow.
     */
    private ITableStructure getTableStructure() {
        try {
            if (filePage.isImportIntoExisting()) {
                return selectContentsPage.getTableContents().findTableStructure();
            } else {
                return newContentsPage.getTableStructure();
            }
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
        return null;
    }

    /**
     * @return The table contents to import into.
     */
    private ITableContents getTableContents() {
        try {
            if (filePage.isImportIntoExisting()) {
                return selectContentsPage.getTableContents();
            } else {
                return newContentsPage.getTableContents();
            }
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.selection = selection;
    }

    /**
     * {@inheritDoc}
     */
    public IWizardPage getNextPage(IWizardPage page) {
        if (page == filePage) {
            // set the comleted state on the opposite page to true so that the wizard can finish
            // normally
            selectContentsPage.setPageComplete(!filePage.isImportIntoExisting());
            newContentsPage.setPageComplete(filePage.isImportIntoExisting());
            // Validate the returned Page so that finished state is already set to true if all
            // default settings are correct
            if (filePage.isImportIntoExisting()) {
                selectContentsPage.validatePage();
                return selectContentsPage;
            }
            newContentsPage.validatePage();
            return newContentsPage;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public IWizardPage getPreviousPage(IWizardPage page) {
        if (page == selectContentsPage || page == newContentsPage) {
            return filePage;
        }
        return null;
    }
}
