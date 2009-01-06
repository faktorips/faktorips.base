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

package org.faktorips.devtools.core.ui.wizards.tableimport;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.ModalContext;
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
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.WorkbenchRunnableAdapter;
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
    private boolean importIntoExisting;
    
    /**
     * Create a new import-wizard.
     */
    public TableImportWizard() {
        super();
        setWindowTitle(Messages.TableImport_title);
        this.setDefaultPageImageDescriptor(IpsUIPlugin.getDefault().getImageDescriptor("wizards/TableImportWizard.png")); //$NON-NLS-1$

        IDialogSettings workbenchSettings= IpsUIPlugin.getDefault().getDialogSettings();
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
            // create pages
            filePage = new SelectFileAndImportMethodPage(null);
            addPage(filePage);
            newContentsPage = new NewContentsPage(selection);
            addPage(newContentsPage);
            selectContentsPage = new SelectContentsPage(selection);
            addPage(selectContentsPage);
            
            filePage.setImportIntoExisting(importIntoExisting);
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
            final ITableContentsGeneration generation = (ITableContentsGeneration)contents.getGenerationsOrderedByValidDate()[0];
            final String nullRepresentation = filePage.getNullRepresentation();

            // no append, so remove any existing content
            if (!filePage.isImportExistingAppend()) {
                generation.clear();
            }

            final MessageList messageList = new MessageList();
            final boolean ignoreColumnHeader = filePage.isImportIgnoreColumnHeaderRow();
            
            IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
                public void run(IProgressMonitor monitor) throws CoreException {
                    IWorkspaceRunnable runnableOperation = format.getImportTableOperation(structure,
                            new Path(filename), generation, nullRepresentation, ignoreColumnHeader , messageList);
                    IIpsModel model = IpsPlugin.getDefault().getIpsModel();
                    model.runAndQueueChangeEvents(runnableOperation, monitor);
                }
            };
            WorkbenchRunnableAdapter runnableAdapter = new WorkbenchRunnableAdapter(runnable);

            /*
             * use a ProgressMonitorDialog to display the progress and allow the user to cancel the
             * process - which both is not possible if only getContainer().run() is called.
             */
            ProgressMonitorDialog pmd = new ProgressMonitorDialog(getShell());
            pmd.setCancelable(true);
            pmd.open();
            ModalContext.run(runnableAdapter, true, pmd.getProgressMonitor(), getShell().getDisplay());
            pmd.close();
            if (!messageList.isEmpty()) {
                getShell().getDisplay().syncExec(
                        new ResultDisplayer(getShell(), Messages.TableImportWizard_operationName, messageList));
            }

            // save the dialog settings
            if (hasNewDialogSettings) {
                IDialogSettings workbenchSettings = IpsPlugin.getDefault().getDialogSettings();
                IDialogSettings section = workbenchSettings.getSection(DIALOG_SETTINGS_KEY);
                section = workbenchSettings.addNewSection(DIALOG_SETTINGS_KEY);
                setDialogSettings(section);
            }
        } catch (InterruptedException ignoredException) {
        } catch (Exception e) {
            Throwable throwable = e;
            if (e instanceof InvocationTargetException) {
                throwable = ((InvocationTargetException)e).getCause();
            }
            IpsPlugin.logAndShowErrorDialog(new IpsStatus("An error occurred during the import process.", throwable)); //$NON-NLS-1$
        } finally {
            selectContentsPage.saveWidgetValues();
            newContentsPage.saveWidgetValues();
            filePage.saveWidgetValues();
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
                return selectContentsPage.getTableContents().findTableStructure(
                        selectContentsPage.getTableContents().getIpsProject());
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
     * Sets if the table content will be imported into the existing table content (<code>true</code>),
     * or not (<code>false</code>)
     */
    public void setImportIntoExisting(boolean importIntoExisting){
        this.importIntoExisting = importIntoExisting;
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
