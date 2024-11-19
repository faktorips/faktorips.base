/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.tableexport;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.tableconversion.ITableFormat;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.wizards.ResultDisplayer;
import org.faktorips.devtools.core.ui.wizards.ipsexport.IpsObjectExportWizard;
import org.faktorips.devtools.core.ui.wizards.ipsexport.TableFormatPropertiesPage;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.runtime.MessageList;

/**
 * Wizard for exporting ipstablecontents to external files.
 *
 * @author Thorsten Waertel, Thorsten Guenther
 */
public class TableExportWizard extends IpsObjectExportWizard {

    private static final String DIALOG_SETTINGS_KEY = "TableExportWizard"; //$NON-NLS-1$

    /* The details-page of this wizard */
    private TableExportPage exportPage;
    private TableSelectionPage selectionPage;
    private boolean isMassExport = false;

    /**
     * Create a new TableExportWizard
     */
    public TableExportWizard() {
        setWindowTitle(Messages.TableExport_title);
        setDefaultPageImageDescriptor(
                IpsUIPlugin.getImageHandling().createImageDescriptor("wizards/TableExportWizard.png")); //$NON-NLS-1$

        IDialogSettings workbenchSettings = IpsUIPlugin.getDefault().getDialogSettings();
        IDialogSettings section = workbenchSettings.getSection(DIALOG_SETTINGS_KEY);
        if (section == null) {
            hasNewDialogSettings = true;
        } else {
            hasNewDialogSettings = false;
            setDialogSettings(section);
        }
    }

    /**
     * Adds the necessary wizard pages depending on the type of export operation. If multiple files
     * are selected, a mass export page is added. For single file exports, a single export page and
     * custom properties pages are added.
     */
    @Override
    public void addPages() {
        try {
            selectionPage = new TableSelectionPage(selection);
            if (selectionPage.getTableContents().size() > 1) {
                isMassExport = true;
                addPage(selectionPage);
            } else {
                exportPage = new TableExportPage(selection);
                addPage(exportPage);

                customPages = new HashMap<>();
                ITableFormat[] externalTableFormats = IpsPlugin.getDefault().getExternalTableFormats();
                for (ITableFormat format : externalTableFormats) {
                    if (IpsUIPlugin.getDefault().hasTableFormatCustomProperties(format)) {
                        TableFormatPropertiesPage customPage = new TableFormatPropertiesPage(format);
                        customPages.put(format, customPage);
                        addPage(customPage);
                    }
                }
            }

        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    /**
     * Executes the finish action of the wizard, which triggers the export operation. It determines
     * whether to execute a mass export or a single export operation based on the active page.
     *
     * @return true if the operation finishes successfully; false otherwise.
     */
    @Override
    public boolean performFinish() {
        return executeExportOperation(isMassExport ? createMassExportOperation() : createSingleExportOperation());
    }

    /**
     * Creates a WorkspaceModifyOperation for mass export, which exports all selected resources.
     *
     * @return the WorkspaceModifyOperation configured for mass export.
     */
    private WorkspaceModifyOperation createMassExportOperation() {
        List<IIpsSrcFile> srcFiles = selectionPage.getTableViewerChosenIpsSrcFiles();
        ISchedulingRule schedulingRule = selectionPage.findIpsElement(srcFiles.get(0)).getIpsProject()
                .getCorrespondingResource().unwrap();
        String pathFolder = selectionPage.getFolderPath();
        ITableFormat format = selectionPage.getFormat();
        String nullRepresentation = selectionPage.getNullRepresentation();
        boolean exportColumnHeaderRow = selectionPage.isExportColumnHeaderRow();

        return new WorkspaceModifyOperation(schedulingRule) {
            @Override
            protected void execute(IProgressMonitor monitor)
                    throws IpsException, InvocationTargetException, InterruptedException {
                if (!performMassExportLogic(monitor, pathFolder, srcFiles, format, nullRepresentation,
                        exportColumnHeaderRow)) {
                    throw new InterruptedException(Messages.TableExportWizard_operationCanceled);
                }
            }
        };
    }

    /**
     * Creates a WorkspaceModifyOperation for single export, which exports a single selected
     * resource.
     *
     * @return the WorkspaceModifyOperation configured for single export.
     */
    private WorkspaceModifyOperation createSingleExportOperation() {
        String exportFilename = exportPage.getFilename();
        ITableFormat format = exportPage.getFormat();
        String nullRepresentation = exportPage.getNullRepresentation();
        boolean exportColumnHeaderRow = exportPage.isExportColumnHeaderRow();
        ITableContents tableContents = exportPage.getTableContents();
        return new WorkspaceModifyOperation(exportPage.getIpsProject().getCorrespondingResource().unwrap()) {
            @Override
            protected void execute(IProgressMonitor monitor)
                    throws IpsException, InvocationTargetException, InterruptedException {
                // Return false if user cancels overwrite confirmation for single export
                if (!performSingleExportLogic(monitor, tableContents, exportFilename, format,
                        nullRepresentation,
                        exportColumnHeaderRow)) {
                    throw new InterruptedException(Messages.TableExportWizard_operationCanceled);
                }
            }
        };
    }

    /**
     * Executes the provided export operation within a progress dialog
     *
     * @param operation the WorkspaceModifyOperation to execute.
     * @return true if the operation completes successfully; false otherwise.
     */
    private boolean executeExportOperation(WorkspaceModifyOperation operation) {
        try {
            executeOperationWithProgress(operation);
            saveDialogSettings();
        } catch (InterruptedException ignoredException) {
            return true;
        } catch (Exception e) {
            Throwable throwable = (e instanceof InvocationTargetException) ? e.getCause() : e;
            IpsPlugin.logAndShowErrorDialog(new IpsStatus(Messages.TableExportWizard_operationError, throwable));
        } finally {
            saveWidgetSettings();
        }
        return true;
    }

    /**
     * Contains the logic for exporting multiple files in a batch operation..
     */
    private boolean performMassExportLogic(IProgressMonitor monitor,
            String pathFolder,
            List<IIpsSrcFile> srcFiles,
            ITableFormat format,
            String nullRepresentation,
            boolean exportColumnHeaderRow) {

        MessageList allMessages = new MessageList();
        monitor.beginTask(Messages.TableExportWizard_operationText, srcFiles.size());

        for (IIpsSrcFile srcFile : srcFiles) {
            SubMonitor subMonitor = SubMonitor.convert(monitor, Messages.TableExportWizard_operationName,
                    1);
            if (monitor.isCanceled()) {
                return false;
            }
            String exportFilename = pathFolder + "//"
                    + selectionPage.getFilename(srcFile, format.getDefaultExtension());
            performExport(selectionPage.getTableContents().get(srcFile), format, exportFilename,
                    nullRepresentation,
                    exportColumnHeaderRow, allMessages, subMonitor);
            monitor.worked(1);
        }
        monitor.done();
        displayMessages(allMessages);
        return true;
    }

    /**
     * Contains the logic for exporting a single file.
     */
    private boolean performSingleExportLogic(IProgressMonitor monitor,
            ITableContents tableContents,
            String exportFilename,
            ITableFormat format,
            String nullRepresentation,
            boolean exportColumnHeaderRow) {

        MessageList messageList = new MessageList();
        return performExport(tableContents, format, exportFilename, nullRepresentation,
                exportColumnHeaderRow, messageList, monitor);
    }

    /**
     * Executes the export of a table contents resource to a specified file location.
     *
     * @return true if export succeeds; false if overwrite is not confirmed.
     */
    private boolean performExport(ITableContents exportContents,
            ITableFormat format,
            String exportFilename,
            String nullRepresentation,
            boolean exportColumnHeaderRow,
            MessageList messageList,
            IProgressMonitor monitor) {
        File exportFile = new File(exportFilename);
        AtomicBoolean overwriteConfirmed = new AtomicBoolean(true);

        if (exportFile.exists()) {
            getShell().getDisplay().syncExec(() -> {
                MessageDialog dialog = new MessageDialog(getShell(), Messages.TableExportWizard_msgFileExistsTitle,
                        null,
                        exportFilename + " " + Messages.TableExportWizard_msgFileExists,
                        MessageDialog.QUESTION, new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL },
                        0);
                overwriteConfirmed.set(dialog.open() == Window.OK);
            });
            // for mass export the process continues if the user cancel the overwrite of a file
            if (!overwriteConfirmed.get()) {
                return isMassExport;
            }
        }

        format.executeTableExport(exportContents, new Path(exportFilename), nullRepresentation,
                exportColumnHeaderRow, messageList, monitor);
        return true;
    }

    private void executeOperationWithProgress(WorkspaceModifyOperation operation)
            throws InvocationTargetException, InterruptedException {
        ProgressMonitorDialog progressDialog = new ProgressMonitorDialog(getShell());
        progressDialog.setCancelable(true);
        progressDialog.open();
        ModalContext.run(operation, true, progressDialog.getProgressMonitor(), getShell().getDisplay());
        progressDialog.close();
    }

    private void displayMessages(MessageList messageList) {
        if (!messageList.isEmpty()) {
            getShell().getDisplay().syncExec(
                    new ResultDisplayer(getShell(), Messages.TableExportWizard_msgFileExistsTitle, messageList));
        }
    }

    @Override
    public void saveWidgetSettings() {

        if (isMassExport) {
            selectionPage.saveWidgetValues();
        } else {
            exportPage.saveWidgetValues();
        }

    }
}
