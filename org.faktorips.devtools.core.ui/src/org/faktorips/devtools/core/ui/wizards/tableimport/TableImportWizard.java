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
import org.eclipse.jface.wizard.IWizardPage;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.wizards.ResultDisplayer;
import org.faktorips.devtools.core.ui.wizards.ipsimport.ImportPreviewPage;
import org.faktorips.devtools.core.ui.wizards.ipsimport.IpsObjectImportWizard;
import org.faktorips.devtools.tableconversion.ITableFormat;
import org.faktorips.util.message.MessageList;

/**
 * Wizard to import external tables into ipstablecontents.
 * 
 * @author Thorsten Waertel, Thorsten Guenther
 */
public class TableImportWizard extends IpsObjectImportWizard {

    protected static String ID = "org.faktorips.devtools.core.ui.wizards.tableimport.TableImportWizard"; //$NON-NLS-1$
    protected final static String DIALOG_SETTINGS_KEY = "TableImportWizard"; //$NON-NLS-1$

    private SelectFileAndImportMethodPage filePage;
    private NewTableContentsPage newContentsPage;
    private SelectTableContentsPage selectContentsPage;
    private ImportPreviewPage tablePreviewPage;

    public TableImportWizard() {
        setWindowTitle(Messages.TableImport_title);
        this
                .setDefaultPageImageDescriptor(IpsUIPlugin.getDefault().getImageDescriptor(
                        "wizards/TableImportWizard.png")); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public void addPages() {
        try {
            // create pages
            filePage = new SelectFileAndImportMethodPage(null);
            addPage(filePage);
            newContentsPage = new NewTableContentsPage(selection);
            addPage(newContentsPage);
            selectContentsPage = new SelectTableContentsPage(selection);
            addPage(selectContentsPage);
            // TODO AW: preview feature out commented for release 2.3.0.rfinal
            // tablePreviewPage = new ImportPreviewPage(selection);
            // addPage(tablePreviewPage);

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
            final ITableFormat format = filePage.getFormat();
            final ITableStructure structure = getTableStructure();
            ITableContents contents = getTableContents();
            final ITableContentsGeneration generation = (ITableContentsGeneration)contents
                    .getGenerationsOrderedByValidDate()[0];
            final String nullRepresentation = filePage.getNullRepresentation();

            // no append, so remove any existing content
            if (!filePage.isImportExistingAppend()) {
                generation.clear();
            }

            final MessageList messageList = new MessageList();
            final boolean ignoreColumnHeader = filePage.isImportIgnoreColumnHeaderRow();

            IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
                public void run(IProgressMonitor monitor) throws CoreException {
                    format.executeTableImport(structure, new Path(filename), generation, nullRepresentation,
                            ignoreColumnHeader, messageList, filePage.isImportIntoExisting());
                }
            };
            IIpsModel model = IpsPlugin.getDefault().getIpsModel();
            model.runAndQueueChangeEvents(runnable, null);

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

            IpsUIPlugin.getDefault().openEditor(contents.getIpsSrcFile());
        } catch (Exception e) {
            Throwable throwable = e;
            if (e instanceof InvocationTargetException) {
                throwable = ((InvocationTargetException)e).getCause();
            }
            IpsPlugin.logAndShowErrorDialog(new IpsStatus("An error occurred during the import process.", throwable)); //$NON-NLS-1$
        } finally {
            selectContentsPage.saveWidgetValues();
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
    private ITableContents getTableContents() throws CoreException {
        if (filePage.isImportIntoExisting()) {
            return selectContentsPage.getTableContents();
        } else {
            return newContentsPage.getTableContents();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IWizardPage getStartingPage() {
        return filePage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IWizardPage getNextPage(IWizardPage page) {
        if (page == filePage) {
            /*
             * Set the completed state on the opposite page to true so that the wizard can finish
             * normally.
             */
            selectContentsPage.setPageComplete(!filePage.isImportIntoExisting());
            newContentsPage.setPageComplete(filePage.isImportIntoExisting());
            /*
             * Validate the returned Page so that finished state is already set to true if all
             * default settings are correct.
             */
            if (filePage.isImportIntoExisting()) {
                selectContentsPage.validatePage();
                return selectContentsPage;
            }
            newContentsPage.validatePage();
            return newContentsPage;
        }

        // TODO AW: out commented for release 2.3.0.rfinal
        // if (page == selectContentsPage || page == newContentsPage) {
        // tablePreviewPage.reinit(filePage.getFilename(), filePage.getFormat(),
        // getTableStructure());
        // tablePreviewPage.validatePage();
        // return tablePreviewPage;
        // }

        return null;
    }

}
