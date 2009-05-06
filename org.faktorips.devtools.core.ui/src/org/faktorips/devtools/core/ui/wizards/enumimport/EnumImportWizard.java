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
import java.util.List;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumValueContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.WorkbenchRunnableAdapter;
import org.faktorips.devtools.core.ui.controls.EnumRefControl;
import org.faktorips.devtools.core.ui.wizards.enumcontent.EnumContentPage;
import org.faktorips.devtools.core.ui.wizards.ipsimport.IpsObjectImportWizard;
import org.faktorips.devtools.core.ui.wizards.tableimport.SelectFileAndImportMethodPage;
import org.faktorips.devtools.core.ui.wizards.tableimport.TablePreviewPage;
import org.faktorips.devtools.tableconversion.ITableFormat;
import org.faktorips.util.message.MessageList;

/**
 * Wizard to import tabular data into IEnumType or IEnumContent.
 * 
 * @author Roman Grutza
 */
public class EnumImportWizard extends IpsObjectImportWizard {

    public final static String ID = "org.faktorips.devtools.core.ui.wizards.enumimport.EnumImportWizard"; //$NON-NLS-1$
    protected final static String DIALOG_SETTINGS_KEY = "EnumImportWizard"; //$NON-NLS-1$

    private SelectFileAndImportMethodPage filePage;
    private ImportedEnumContentPage newEnumContentPage;
    private SelectEnumPage selectContentsPage;
    private TablePreviewPage tablePreviewPage;

    public EnumRefControl enumControl;

    public EnumImportWizard() {
        setWindowTitle(Messages.EnumImportWizard_title);
        this.setDefaultPageImageDescriptor(IpsUIPlugin.getDefault().getImageDescriptor("wizards/EnumImportWizard.png")); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addPages() {
        try {
            filePage = new SelectFileAndImportMethodPage(null);
            addPage(filePage);
            newEnumContentPage = new ImportedEnumContentPage(selection);
            addPage(newEnumContentPage);
            selectContentsPage = new SelectEnumPage(selection);
            addPage(selectContentsPage);
            tablePreviewPage = new TablePreviewPage(selection);
            addPage(tablePreviewPage);

            filePage.setImportIntoExisting(importIntoExisting);
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IWizardPage getNextPage(IWizardPage page) {
        if (page == filePage) {
            // set the completed state on the opposite page to true so that the wizard can finish
            // normally
            selectContentsPage.setPageComplete(!filePage.isImportIntoExisting());
            newEnumContentPage.setPageComplete(filePage.isImportIntoExisting());
            // Validate the returned Page so that finished state is already set to true if all
            // default settings are correct
            if (filePage.isImportIntoExisting()) {
                selectContentsPage.validatePage();
                return selectContentsPage;
            }
            try {
                newEnumContentPage.validatePage();
            } catch (CoreException e) {
                IpsPlugin.log(e);
                return null;
            }
            return newEnumContentPage;
        }

        if (page == selectContentsPage || page == newEnumContentPage) {

            tablePreviewPage.setFilename(filePage.getFilename());
            tablePreviewPage.setTableFormat(filePage.getFormat());
            try {
                tablePreviewPage.setTableStructure(getEnumValueContainer().findEnumType(
                        getEnumValueContainer().getIpsProject()));
            } catch (CoreException e) {
                // not fatal, only the preview is not available if could not determine IEnumType
                IpsPlugin.logAndShowErrorDialog(e);
            }
            tablePreviewPage.validatePage();

            return tablePreviewPage;
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean performFinish() {
        final ITableFormat format = filePage.getFormat();
        try {
            final IEnumValueContainer enumTypeOrContent = getEnumValueContainer();

            if (filePage.isImportExistingReplace()) {
                enumTypeOrContent.clear();
            }

            IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
                public void run(IProgressMonitor monitor) throws CoreException {
                    format.executeEnumImport(enumTypeOrContent, new Path(filePage.getFilename()), filePage
                            .getNullRepresentation(), filePage.isImportIgnoreColumnHeaderRow(), new MessageList());
                }
            };
            IIpsModel model = IpsPlugin.getDefault().getIpsModel();
            model.runAndQueueChangeEvents(runnable, null);
            WorkbenchRunnableAdapter runnableAdapter = new WorkbenchRunnableAdapter(runnable);

        } catch (CoreException e) {
            IpsPlugin.log(e);
        }

        // don't keep wizard open
        return true;
    }

    /**
     * @return the enum type or content as a target for import.
     * @throws CoreException
     */
    private IEnumValueContainer getEnumValueContainer() throws CoreException {
        if (filePage.isImportIntoExisting()) {
            return selectContentsPage.getEnum();
        } else {
            IIpsSrcFile ipsSrcFile = newEnumContentPage.createIpsSrcFile(new NullProgressMonitor());
            newEnumContentPage.finishIpsObjects(ipsSrcFile.getIpsObject(), new ArrayList());
            return newEnumContentPage.getEnumContent();
        }
    }

    private class ImportedEnumContentPage extends EnumContentPage {

        private IIpsSrcFile ipsSrcFile;

        public ImportedEnumContentPage(IStructuredSelection selection) {
            super(selection);
        }

        public IEnumContent getEnumContent() {
            if (ipsSrcFile != null) {
                try {
                    return (IEnumContent)ipsSrcFile.getIpsObject();
                } catch (CoreException e) {
                    IpsPlugin.log(e);
                }
            }
            return null;
        }

        @Override
        protected void finishIpsObjects(IIpsObject newIpsObject, List modifiedIpsObjects) throws CoreException {
            super.finishIpsObjects(newIpsObject, modifiedIpsObjects);
        }

        @Override
        protected IIpsSrcFile createIpsSrcFile(IProgressMonitor monitor) throws CoreException {
            ipsSrcFile = super.createIpsSrcFile(monitor);
            return ipsSrcFile;
        }

    }
}
