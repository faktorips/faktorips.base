/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.tableimport;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;

import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.tableconversion.ITableFormat;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.wizards.ResultDisplayer;
import org.faktorips.devtools.core.ui.wizards.ipsimport.ImportPreviewPage;
import org.faktorips.devtools.core.ui.wizards.ipsimport.IpsObjectImportWizard;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.devtools.model.tablecontents.ITableRows;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.runtime.MessageList;

/**
 * Wizard to import external tables into ipstablecontents.
 *
 * @author Thorsten Waertel, Thorsten Guenther
 */
public class TableImportWizard extends IpsObjectImportWizard {

    public static final String ID = "org.faktorips.devtools.core.ui.wizards.tableimport.TableImportWizard"; //$NON-NLS-1$
    private static final String DIALOG_SETTINGS_KEY = "TableImportWizard"; //$NON-NLS-1$

    private TableContentsPage newTableContentsPage;
    private SelectTableContentsPage selectContentsPage;
    private ImportPreviewPage tablePreviewPage;

    public TableImportWizard() {
        super();
        setWindowTitle(Messages.TableImport_title);
        setDefaultPageImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(
                "wizards/TableImportWizard.png")); //$NON-NLS-1$
    }

    @Override
    public void addPages() {
        try {
            setIpsOIWStartingPage(new SelectFileAndImportMethodPage(null));
            getIpsOIWStartingPage().setImportIntoExisting(isImportIntoExisting());
            newTableContentsPage = new TableContentsPage(getSelection());
            selectContentsPage = new SelectTableContentsPage(getSelection());

            addPage(getIpsOIWStartingPage());
            addPage(newTableContentsPage);
            addPage(selectContentsPage);
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    @Override
    public IWizardPage getNextPage(IWizardPage page) {
        saveDataToWizard();
        SelectFileAndImportMethodPage startingPage = (SelectFileAndImportMethodPage)getIpsOIWStartingPage();
        if (page == startingPage) {
            /*
             * Set the completed state on the opposite page to true so that the wizard can finish
             * normally.
             */
            selectContentsPage.setPageComplete(!startingPage.isImportIntoExisting());
            newTableContentsPage.setPageComplete(startingPage.isImportIntoExisting());
            /*
             * Validate the returned Page so that finished state is already set to true if all
             * default settings are correct.
             */
            if (startingPage.isImportIntoExisting()) {
                selectContentsPage.validatePage();
                return selectContentsPage;
            }
            newTableContentsPage.validatePage();
            return newTableContentsPage;
        }

        if (page == selectContentsPage || page == newTableContentsPage) {
            ITableStructure tableStructure = getTableStructure();
            if (tablePreviewPage == null) {
                tablePreviewPage = new ImportPreviewPage(startingPage.getFilename(), startingPage.getFormat(),
                        tableStructure, startingPage.isImportIgnoreColumnHeaderRow());

                addPage(tablePreviewPage);
            } else {
                tablePreviewPage.reinit(startingPage.getFilename(), startingPage.getFormat(), tableStructure,
                        startingPage.isImportIgnoreColumnHeaderRow());
                tablePreviewPage.validatePage();
            }
            tablePreviewPage.validatePage();

            return tablePreviewPage;
        }

        return null;
    }

    @Override
    public IWizardPage getStartingPage() {
        return getIpsOIWStartingPage();
    }

    @Override
    public boolean canFinish() {
        if (isExcelTableFormatSelected()) {
            if (getContainer().getCurrentPage() == selectContentsPage) {
                if (selectContentsPage.isPageComplete()) {
                    return true;
                }
            }
            if (getContainer().getCurrentPage() == newTableContentsPage) {
                if (newTableContentsPage.isPageComplete()) {
                    return true;
                }
            }
        }
        return super.canFinish();
    }

    @Override
    public boolean performFinish() {
        int rowCount = 0;
        final SelectFileAndImportMethodPage startingPage = (SelectFileAndImportMethodPage)getIpsOIWStartingPage();
        try {
            final String filename = startingPage.getFilename();
            final ITableFormat format = startingPage.getFormat();
            final ITableStructure structure = getTableStructure();
            ITableContents contents = getTableContents();
            final ITableRows tableRows = contents.getTableRows();
            rowCount = tableRows.getNumOfRows();

            // no append, so remove any existing content
            if (!startingPage.isImportExistingAppend()) {
                tableRows.clear();
            }

            final MessageList messageList = new MessageList();
            final boolean ignoreColumnHeader = startingPage.isImportIgnoreColumnHeaderRow();

            ICoreRunnable runnable = $ -> format.executeTableImport(structure, new Path(filename), tableRows,
                    getNullRepresentation(),
                    ignoreColumnHeader, messageList, startingPage.isImportIntoExisting());
            IIpsModel.get().runAndQueueChangeEvents(runnable, null);

            if (!messageList.isEmpty()) {
                getShell().getDisplay().syncExec(
                        new ResultDisplayer(getShell(), Messages.TableImportWizard_operationName, messageList));
            }

            contents.getIpsObject().getIpsSrcFile().save(new NullProgressMonitor());
            IpsUIPlugin.getDefault().openEditor(contents.getIpsSrcFile());
        } catch (Exception e) {
            Throwable throwable = e;
            if (e instanceof InvocationTargetException) {
                throwable = ((InvocationTargetException)e).getCause();
            }
            IpsPlugin.logAndShowErrorDialog(new IpsStatus("An error occurred during the import process.", throwable)); //$NON-NLS-1$
        } finally {
            // save the dialog settings
            if (isHasNewDialogSettings()) {
                IDialogSettings workbenchSettings = IpsPlugin.getDefault().getDialogSettings();
                IDialogSettings settings = workbenchSettings.addNewSection(getDialogSettingsKey());
                setDialogSettings(settings);
            }
            selectContentsPage.saveWidgetValues();
            startingPage.saveWidgetValues();
        }
        rowCount = calculateRowCount(rowCount);
        MessageDialog.openInformation(getShell(), Messages.TableImportWizard_tableImportControlTitle,
                NLS.bind(Messages.TableImportWizard_tableImportControlBody, rowCount));

        // this implementation of this method should always return true since this causes the wizard
        // dialog to close. in either case if an exception arises or not it doesn't make sense to
        // keep the dialog up
        return true;
    }

    private int calculateRowCount(int oldRowCount) {
        if (newTableContentsPage.getCreatedTableContents() != null) {
            return newTableContentsPage.getCreatedTableContents().getTableRows().getNumOfRows();
        } else {
            if (getIpsOIWStartingPage().isImportExistingAppend()) {
                return getRowCountNewTable() - oldRowCount;
            } else {
                return getRowCountNewTable();
            }
        }
    }

    private int getRowCountNewTable() {
        return getTableContents().getTableRows().getNumOfRows();
    }

    /**
     * @return the table-structure the imported table content has to follow.
     */
    private ITableStructure getTableStructure() {
        try {
            if (getIpsOIWStartingPage().isImportIntoExisting()) {
                ITableContents tableContents = (ITableContents)selectContentsPage.getTargetForImport();
                return tableContents.findTableStructure(tableContents.getIpsProject());
            } else {
                return newTableContentsPage.getTableStructure();
            }
        } catch (IpsException e) {
            IpsPlugin.log(e);
        }
        return null;
    }

    /**
     * @return The table contents to import into.
     */
    private ITableContents getTableContents() {
        if (getIpsOIWStartingPage().isImportIntoExisting()) {
            return (ITableContents)selectContentsPage.getTargetForImport();
        }
        IIpsSrcFile ipsSrcFile = newTableContentsPage.createIpsSrcFile(new NullProgressMonitor());
        newTableContentsPage.finishIpsObjects(ipsSrcFile.getIpsObject(), new HashSet<>());
        return newTableContentsPage.getCreatedTableContents();
    }

    @Override
    protected String getDialogSettingsKey() {
        return DIALOG_SETTINGS_KEY;
    }

}
