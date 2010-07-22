/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.enumexport;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.enums.IEnumValueContainer;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.editors.enumcontent.EnumContentEditor;
import org.faktorips.devtools.core.ui.editors.enumtype.EnumTypeEditor;
import org.faktorips.devtools.core.ui.wizards.ResultDisplayer;
import org.faktorips.devtools.core.ui.wizards.ipsexport.IpsObjectExportWizard;
import org.faktorips.devtools.core.ui.wizards.ipsexport.TableFormatPropertiesPage;
import org.faktorips.devtools.tableconversion.ITableFormat;
import org.faktorips.util.message.MessageList;

/**
 * Wizard for exporting enumeration types and enumeration contents to external files.
 * 
 * @author Roman Grutza, Alexander Weickmann
 */
public class EnumExportWizard extends IpsObjectExportWizard {

    private static String DIALOG_SETTINGS_KEY = "EnumExportWizard"; //$NON-NLS-1$

    // mandatory page to select filename, table format etc.
    public EnumExportPage exportPage;

    public EnumExportWizard() {
        setWindowTitle(Messages.EnumExportWizard_title);
        setDefaultPageImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(
                "wizards/EnumExportWizard.png")); //$NON-NLS-1$

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
     * {@inheritDoc}
     */
    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        if (selection.isEmpty()) {
            IEditorPart activeEditor = workbench.getActiveWorkbenchWindow().getActivePage().getActiveEditor();
            if (activeEditor instanceof EnumTypeEditor) {
                EnumTypeEditor enumTypeEditor = (EnumTypeEditor)activeEditor;
                selection = new StructuredSelection(enumTypeEditor.getIpsObject());
            } else if (activeEditor instanceof EnumContentEditor) {
                EnumContentEditor enumContentEditor = (EnumContentEditor)activeEditor;
                selection = new StructuredSelection(enumContentEditor.getIpsObject());
            }
        }
        super.init(workbench, selection);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addPages() {
        try {
            exportPage = new EnumExportPage(selection);
            addPage(exportPage);

            // Add page for each table format having custom properties.
            customPages = new HashMap<ITableFormat, TableFormatPropertiesPage>();
            ITableFormat[] externalTableFormats = IpsPlugin.getDefault().getExternalTableFormats();
            for (ITableFormat format : externalTableFormats) {
                if (IpsUIPlugin.getDefault().hasTableFormatCustomProperties(format)) {
                    TableFormatPropertiesPage customPage = new TableFormatPropertiesPage(format);
                    customPages.put(format, customPage);
                    addPage(customPage);
                }
            }
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean performFinish() {
        try {
            ISchedulingRule schedulingRule = exportPage.getIpsProject().getCorrespondingResource();
            final IEnumValueContainer exportEnumContainer = exportPage.getEnum();
            final String exportFilename = exportPage.getFilename();
            final ITableFormat format = exportPage.getFormat();
            final String nullRepresentation = exportPage.getNullRepresentation();
            final boolean exportColumnHeaderRow = exportPage.isExportColumnHeaderRow();

            File exportFile = new File(exportFilename);
            if (exportFile.exists()) {
                MessageDialog dialog = new MessageDialog(getContainer().getShell(),
                        Messages.EnumExportWizard_msgFileExistsTitle, (Image)null,
                        Messages.EnumExportWizard_msgFileExists, MessageDialog.QUESTION, new String[] {
                                IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL }, 0);

                if (dialog.open() != Window.OK) {
                    // User did not say "yes" to overwrite the file, so return to the wizard.
                    return false;
                }
            }

            WorkspaceModifyOperation operation = new WorkspaceModifyOperation(schedulingRule) {
                @Override
                protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
                        InterruptedException {
                    MessageList messageList = new MessageList();
                    format.executeEnumExport(exportEnumContainer, new Path(exportFilename), nullRepresentation,
                            exportColumnHeaderRow, messageList);

                    if (!messageList.isEmpty()) {
                        getShell().getDisplay().syncExec(
                                new ResultDisplayer(getShell(), Messages.EnumExportWizard_operationName, messageList));
                    }
                }
            };

            /*
             * Use a ProgressMonitorDialog to display the progress and allow the user to cancel the
             * process - which both is not possible if only getContainer().run() is called.
             */
            ProgressMonitorDialog pmd = new ProgressMonitorDialog(getShell());
            pmd.setCancelable(true);
            pmd.open();
            ModalContext.run(operation, true, pmd.getProgressMonitor(), getShell().getDisplay());
            pmd.close();

            saveDialogSettings();

        } catch (InterruptedException ignoredException) {
        } catch (Exception e) {
            Throwable throwable = e;
            if (e instanceof InvocationTargetException) {
                throwable = ((InvocationTargetException)e).getCause();
            }
            IpsPlugin.logAndShowErrorDialog(new IpsStatus("An error occurred during the export process.", throwable)); //$NON-NLS-1$
        } finally {
            saveWidgetSettings();
        }

        /*
         * This implementation of this method should always return true since this causes the wizard
         * dialog to close. In either case if an exception arises or not it doesn't make sense to
         * keep the dialog up.
         */
        return true;
    }

    @Override
    public void saveWidgetSettings() {
        exportPage.saveWidgetValues();
    }
}
