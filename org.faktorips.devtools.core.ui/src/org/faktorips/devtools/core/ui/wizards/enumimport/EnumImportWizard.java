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
import org.faktorips.devtools.core.ui.wizards.enumcontent.EnumContentPage;
import org.faktorips.devtools.core.ui.wizards.ipsimport.ImportPreviewPage;
import org.faktorips.devtools.core.ui.wizards.ipsimport.IpsObjectImportWizard;
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
    private EnumContentPage newEnumContentPage;
    private SelectEnumPage selectContentsPage;
    private ImportPreviewPage tablePreviewPage;

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
            newEnumContentPage = new EnumContentPage(selection);
            addPage(newEnumContentPage);
            selectContentsPage = new SelectEnumPage(selection);
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
    public IWizardPage getNextPage(IWizardPage page) {
        if (page == filePage) {
            /*
             * Set the completed state on the opposite page to true so that the wizard can finish
             * normally.
             */
            selectContentsPage.setPageComplete(!filePage.isImportIntoExisting());
            newEnumContentPage.setPageComplete(filePage.isImportIntoExisting());

            /*
             * Validate the returned Page so that finished state is already set to true if all
             * default settings are correct.
             */
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

        // TODO AW: out commented for release 2.3.0.rfinal
        // if (page == selectContentsPage || page == newEnumContentPage) {
        // IEnumType enumType = getEnumType();
        // tablePreviewPage.reinit(filePage.getFilename(), filePage.getFormat(), enumType);
        // tablePreviewPage.validatePage();
        // return tablePreviewPage;
        // }

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
                            .getNullRepresentation(), filePage.isImportIgnoreColumnHeaderRow(), new MessageList(),
                            filePage.isImportIntoExisting());
                }
            };
            IIpsModel model = IpsPlugin.getDefault().getIpsModel();
            model.runAndQueueChangeEvents(runnable, null);
            IpsUIPlugin.getDefault().openEditor(enumTypeOrContent.getIpsSrcFile());
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }

        // Don't keep wizard open.
        return true;
    }

    /**
     * @return The enum type defining the structure for import.
     */
    private IEnumType getEnumType() {
        try {
            if (filePage.isImportIntoExisting()) {
                IEnumValueContainer enumValueContainer = selectContentsPage.getEnum();
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

    /**
     * @return the enum type or content as a target for import.
     * @throws CoreException
     */
    private IEnumValueContainer getEnumValueContainer() throws CoreException {
        if (filePage.isImportIntoExisting()) {
            return selectContentsPage.getEnum();
        } else {
            IIpsSrcFile ipsSrcFile = newEnumContentPage.createIpsSrcFile(new NullProgressMonitor());
            newEnumContentPage.finishIpsObjects(ipsSrcFile.getIpsObject(), new ArrayList<IIpsObject>());
            return newEnumContentPage.getCreatedEnumContent();
        }
    }

}
