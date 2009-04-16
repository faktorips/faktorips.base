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

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.wizard.IWizardPage;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.enums.IEnumValueContainer;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.WorkbenchRunnableAdapter;
import org.faktorips.devtools.core.ui.controls.EnumRefControl;
import org.faktorips.devtools.core.ui.wizards.enumimport.Messages;
import org.faktorips.devtools.core.ui.wizards.ipsimport.IpsObjectImportWizard;
import org.faktorips.devtools.core.ui.wizards.tableimport.NewContentsPage;
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
    private NewContentsPage newContentsPage;
    private SelectEnumPage selectContentsPage;
    private TablePreviewPage tablePreviewPage;

    public EnumRefControl enumControl;

    
    public EnumImportWizard() {
        setWindowTitle(Messages.getString("EnumImportWizard.title")); //$NON-NLS-1$
        this.setDefaultPageImageDescriptor(IpsUIPlugin.getDefault().getImageDescriptor(
                        "wizards/EnumImportWizard.png")); //$NON-NLS-1$
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
    public IWizardPage getNextPage(IWizardPage page) {
        if (page == filePage) {
            // set the completed state on the opposite page to true so that the wizard can finish
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
        if (page == selectContentsPage || page == newContentsPage) {

            tablePreviewPage.setFilename(filePage.getFilename());
            tablePreviewPage.setTableFormat(filePage.getFormat());
            // TODO rg: generalize: setImportStructure(IIpsObject)
            // tablePreviewPage.setTableStructure(getTableStructure());
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
            final IEnumValueContainer enumTypeOrContent = selectContentsPage.getEnum();
            if (filePage.isImportExistingReplace()) {
                // TODO rg:, introduce clear() as in ITableContentsGeneration  
//                enumTypeOrContent.clear();
            }
            

            IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
                public void run(IProgressMonitor monitor) throws CoreException {
                    format.executeEnumImport(enumTypeOrContent, new Path(filePage.getFilename()),
                            filePage.getNullRepresentation(), filePage.isImportIgnoreColumnHeaderRow(),
                            new MessageList());
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
    
}
