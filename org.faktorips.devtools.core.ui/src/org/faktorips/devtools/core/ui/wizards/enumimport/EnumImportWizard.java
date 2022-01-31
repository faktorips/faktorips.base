/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.enumimport;

import java.util.HashSet;

import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.editors.enumcontent.EnumContentEditor;
import org.faktorips.devtools.core.ui.editors.enumtype.EnumTypeEditor;
import org.faktorips.devtools.core.ui.wizards.ResultDisplayer;
import org.faktorips.devtools.core.ui.wizards.enumcontent.EnumContentPage;
import org.faktorips.devtools.core.ui.wizards.ipsimport.ImportPreviewPage;
import org.faktorips.devtools.core.ui.wizards.ipsimport.IpsObjectImportWizard;
import org.faktorips.devtools.core.ui.wizards.ipsimport.SelectImportTargetPage;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.enums.IEnumValueContainer;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.tableconversion.ITableFormat;
import org.faktorips.runtime.MessageList;

/**
 * Wizard to import tabular data into an <code>IEnumType</code> or <code>IEnumContent</code>.
 * 
 * @author Roman Grutza, Alexander Weickmann
 */
public class EnumImportWizard extends IpsObjectImportWizard {

    public static final String ID = "org.faktorips.devtools.core.ui.wizards.enumimport.EnumImportWizard"; //$NON-NLS-1$
    public static final String DIALOG_SETTINGS_KEY = "EnumImportWizard"; //$NON-NLS-1$

    private EnumContentPage newEnumContentPage;
    private SelectImportTargetPage selectContentsPage;
    private ImportPreviewPage tablePreviewPage;

    public EnumImportWizard() {
        super();
        setWindowTitle(Messages.EnumImportWizard_title);
        setDefaultPageImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(
                "wizards/EnumImportWizard.png")); //$NON-NLS-1$
    }

    @Override
    public void addPages() {
        try {
            setIpsOIWStartingPage(new SelectFileAndImportMethodPage(null));
            getIpsOIWStartingPage().setImportIntoExisting(isImportIntoExisting());
            newEnumContentPage = new EnumContentPage(getSelection());
            selectContentsPage = new SelectEnumPage(getSelection());

            addPage(getIpsOIWStartingPage());
            addPage(newEnumContentPage);
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
            newEnumContentPage.setPageComplete(startingPage.isImportIntoExisting());
            /*
             * Validate the returned Page so that finished state is already set to true if all
             * default settings are correct.
             */
            if (startingPage.isImportIntoExisting()) {
                selectContentsPage.validatePage();
                return selectContentsPage;
            }
            newEnumContentPage.validatePage();
            return newEnumContentPage;
        }

        if (page == selectContentsPage || page == newEnumContentPage) {
            IEnumType enumType = getEnumType();
            if (tablePreviewPage == null) {
                tablePreviewPage = new ImportPreviewPage(startingPage.getFilename(), startingPage.getFormat(),
                        enumType, startingPage.isImportIgnoreColumnHeaderRow());

                addPage(tablePreviewPage);
            } else {
                tablePreviewPage.reinit(startingPage.getFilename(), startingPage.getFormat(), enumType,
                        startingPage.isImportIgnoreColumnHeaderRow());
                tablePreviewPage.validatePage();
            }
            tablePreviewPage.validatePage();
            return tablePreviewPage;
        }

        return null;
    }

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

    @Override
    public boolean canFinish() {
        if (isExcelTableFormatSelected()) {
            if (getContainer().getCurrentPage() == selectContentsPage) {
                if (selectContentsPage.isPageComplete()) {
                    return true;
                }
            }
            if (getContainer().getCurrentPage() == newEnumContentPage) {
                if (newEnumContentPage.isPageComplete()) {
                    return true;
                }
            }
        }
        return super.canFinish();
    }

    @Override
    public boolean performFinish() {
        final SelectFileAndImportMethodPage startingPage = (SelectFileAndImportMethodPage)getIpsOIWStartingPage();
        final ITableFormat format = startingPage.getFormat();
        int enumCount = 0;
        try {
            final IEnumValueContainer enumTypeOrContent = getEnumValueContainer();
            enumCount = enumTypeOrContent.getEnumValuesCount();
            if (startingPage.isImportExistingReplace()) {
                enumTypeOrContent.clear();
            }

            final MessageList messageList = new MessageList();

            ICoreRunnable runnable = $ -> format.executeEnumImport(enumTypeOrContent,
                    new Path(startingPage.getFilename()),
                    startingPage.getNullRepresentation(), startingPage.isImportIgnoreColumnHeaderRow(),
                    messageList, startingPage.isImportIntoExisting());
            IIpsModel.get().runAndQueueChangeEvents(runnable, null);

            if (!messageList.isEmpty()) {
                getShell().getDisplay().syncExec(
                        new ResultDisplayer(getShell(), Messages.EnumImportWizard_operationName, messageList));
            }

            enumTypeOrContent.getIpsObject().getIpsSrcFile().save(true, new NullProgressMonitor());
            IpsUIPlugin.getDefault().openEditor(enumTypeOrContent.getIpsSrcFile());
        } catch (CoreRuntimeException e) {
            IpsPlugin.logAndShowErrorDialog(e);
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
        enumCount = calculateEnumCount(enumCount);
        MessageDialog.openInformation(getShell(), Messages.EnumImportWizard_EnumImportControlTitle,
                NLS.bind(Messages.EnumImportWizard_EnumImportControlBody, enumCount));

        // Don't keep wizard open.
        return true;
    }

    private int calculateEnumCount(int oldEnumCount) {
        if (newEnumContentPage.getCreatedEnumContent() != null) {
            return newEnumContentPage.getCreatedEnumContent().getEnumValuesCount();
        } else {
            if (getIpsOIWStartingPage().isImportExistingAppend()) {
                return getEnumCountNewTable() - oldEnumCount;
            } else {
                return getEnumCountNewTable();
            }
        }
    }

    private int getEnumCountNewTable() {
        return getEnumValueContainer().getEnumValuesCount();
    }

    /**
     * Returns the enumeration type defining the structure for import.
     */
    private IEnumType getEnumType() {
        try {
            if (getIpsOIWStartingPage().isImportIntoExisting()) {
                IEnumValueContainer enumValueContainer = (IEnumValueContainer)selectContentsPage.getTargetForImport();
                IIpsProject ipsProject = enumValueContainer.getIpsProject();
                if (ipsProject != null) {
                    return enumValueContainer.findEnumType(ipsProject);
                }
            } else {
                return newEnumContentPage.getEnumType();
            }
        } catch (CoreRuntimeException e) {
            IpsPlugin.log(e);
        }
        return null;
    }

    /**
     * Returns the enumeration type or enumeration content as a target for import.
     */
    private IEnumValueContainer getEnumValueContainer() throws CoreRuntimeException {
        if (getIpsOIWStartingPage().isImportIntoExisting()) {
            return (IEnumValueContainer)selectContentsPage.getTargetForImport();
        }
        IIpsSrcFile ipsSrcFile = newEnumContentPage.createIpsSrcFile(new NullProgressMonitor());
        newEnumContentPage.finishIpsObjects(ipsSrcFile.getIpsObject(), new HashSet<IIpsObject>());
        return newEnumContentPage.getCreatedEnumContent();
    }

    @Override
    protected String getDialogSettingsKey() {
        return DIALOG_SETTINGS_KEY;
    }

}
