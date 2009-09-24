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

package org.faktorips.devtools.core.ui.wizards.enumimport;

import java.util.ArrayList;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValueContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.controls.EnumRefControl;
import org.faktorips.devtools.core.ui.editors.enumcontent.EnumContentEditor;
import org.faktorips.devtools.core.ui.editors.enumtype.EnumTypeEditor;
import org.faktorips.devtools.core.ui.wizards.ResultDisplayer;
import org.faktorips.devtools.core.ui.wizards.enumcontent.EnumContentPage;
import org.faktorips.devtools.core.ui.wizards.ipsimport.ImportPreviewPage;
import org.faktorips.devtools.core.ui.wizards.ipsimport.IpsObjectImportWizard;
import org.faktorips.devtools.core.ui.wizards.ipsimport.SelectImportTargetPage;
import org.faktorips.devtools.tableconversion.ITableFormat;
import org.faktorips.util.message.MessageList;

/**
 * Wizard to import tabular data into an <tt>IEnumType</tt> or <tt>IEnumContent</tt>.
 * 
 * @author Roman Grutza, Alexander Weickmann
 */
public class EnumImportWizard extends IpsObjectImportWizard {

    public final static String ID = "org.faktorips.devtools.core.ui.wizards.enumimport.EnumImportWizard"; //$NON-NLS-1$
    protected final static String DIALOG_SETTINGS_KEY = "EnumImportWizard"; //$NON-NLS-1$

    private EnumContentPage newEnumContentPage;
    private SelectImportTargetPage selectContentsPage;
    private ImportPreviewPage tablePreviewPage;

    public EnumRefControl enumControl;

    public EnumImportWizard() {
        setWindowTitle(Messages.EnumImportWizard_title);
        setDefaultPageImageDescriptor(IpsUIPlugin.getDefault().getImageDescriptor("wizards/EnumImportWizard.png")); //$NON-NLS-1$
    }

    @Override
    public void addPages() {
        try {
            startingPage = new SelectFileAndImportMethodPage(null);
            startingPage.setImportIntoExisting(importIntoExisting);
            newEnumContentPage = new EnumContentPage(selection);
            selectContentsPage = new SelectEnumPage(selection);

            addPage(startingPage);
            addPage(newEnumContentPage);
            addPage(selectContentsPage);

        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    @Override
    public IWizardPage getNextPage(IWizardPage page) {
        saveDataToWizard();
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
            try {
                newEnumContentPage.validatePage();
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
            return newEnumContentPage;
        }

        if (page == selectContentsPage || page == newEnumContentPage) {
            IEnumType enumType = getEnumType();
            if (tablePreviewPage == null) {
                tablePreviewPage = new ImportPreviewPage(startingPage.getFilename(), startingPage.getFormat(),
                        enumType, startingPage.isImportIgnoreColumnHeaderRow());

                addPage(tablePreviewPage);
            } else {
                tablePreviewPage.reinit(startingPage.getFilename(), startingPage.getFormat(), enumType, startingPage
                        .isImportIgnoreColumnHeaderRow());
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
        final ITableFormat format = startingPage.getFormat();
        try {
            final IEnumValueContainer enumTypeOrContent = getEnumValueContainer();
            if (startingPage.isImportExistingReplace()) {
                enumTypeOrContent.clear();
            }

            final MessageList messageList = new MessageList();

            try {
                IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
                    public void run(IProgressMonitor monitor) throws CoreException {
                        format.executeEnumImport(enumTypeOrContent, new Path(startingPage.getFilename()), startingPage
                                .getNullRepresentation(), startingPage.isImportIgnoreColumnHeaderRow(), messageList,
                                startingPage.isImportIntoExisting());
                    }
                };
                IIpsModel model = IpsPlugin.getDefault().getIpsModel();
                model.runAndQueueChangeEvents(runnable, null);
            } catch (CoreException e) {
                MessageDialog.openError(getShell(), Messages.EnumImportWizard_title, e.getLocalizedMessage());
                return false;
            }

            if (!messageList.isEmpty()) {
                getShell().getDisplay().syncExec(
                        new ResultDisplayer(getShell(), Messages.EnumImportWizard_operationName, messageList));
            }

            IpsUIPlugin.getDefault().openEditor(enumTypeOrContent.getIpsSrcFile());
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }

        // Don't keep wizard open.
        return true;
    }

    /*
     * Returns the enumeration type defining the structure for import.
     */
    private IEnumType getEnumType() {
        try {
            if (startingPage.isImportIntoExisting()) {
                IEnumValueContainer enumValueContainer = (IEnumValueContainer)selectContentsPage.getTargetForImport();
                IIpsProject ipsProject = enumValueContainer.getIpsProject();
                if (ipsProject != null) {
                    return enumValueContainer.findEnumType(ipsProject);
                }
            } else {
                return newEnumContentPage.getEnumType();
            }
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
        return null;
    }

    /*
     * Returns the enumeration type or enumeration content as a target for import.
     */
    private IEnumValueContainer getEnumValueContainer() throws CoreException {
        if (startingPage.isImportIntoExisting()) {
            return (IEnumValueContainer)selectContentsPage.getTargetForImport();
        } else {
            IIpsSrcFile ipsSrcFile = newEnumContentPage.createIpsSrcFile(new NullProgressMonitor());
            newEnumContentPage.finishIpsObjects(ipsSrcFile.getIpsObject(), new ArrayList<IIpsObject>());
            return newEnumContentPage.getCreatedEnumContent();
        }
    }

}
