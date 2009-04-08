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

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ui.IWorkbench;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.enums.IEnumValueContainer;
import org.faktorips.devtools.core.ui.controls.EnumRefControl;
import org.faktorips.devtools.tableconversion.ITableFormat;
import org.faktorips.util.message.MessageList;

/**
 * Wizard to import tabular data into IEnumType or IEnumContent.
 * 
 * @author Roman Grutza
 */
public class EnumImportWizard extends AbstractTableImportWizard {

    public final static String ID = "org.faktorips.devtools.core.ui.wizards.tableimport.EnumImportWizard"; //$NON-NLS-1$
    protected final static String DIALOG_SETTINGS_KEY = "EnumImportWizard"; //$NON-NLS-1$
    
    private SelectFileAndImportMethodPage filePage;
    private NewContentsPage newContentsPage;
    private SelectContentsPage selectContentsPage;
    private TablePreviewPage tablePreviewPage;

    public EnumRefControl enumControl;

    /**
     * {@inheritDoc}
     */
    public void addPages() {
        try {
            filePage = new SelectFileAndImportMethodPage(null);
            addPage(filePage);
            newContentsPage = new NewContentsPage(selection);
            addPage(newContentsPage);
            selectContentsPage = new SelectContentsPage(selection);
            addPage(selectContentsPage);
            tablePreviewPage = new TablePreviewPage(selection);
            addPage(tablePreviewPage);
            
            filePage.setImportIntoExisting(importIntoExisting);
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    @Override
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

    @Override
    public boolean performFinish() {
        // TODO rg: implement logic
        
//        ITableFormat format = filePage.getFormat();
//        IEnumValueContainer enumTypeOrContent = getEnumLogic();
//        try {
//            IWorkspaceRunnable importEnumOperation = format.getImportEnumOperation(
//                    enumTypeOrContent, 
//                    new Path(""), 
//                    "null", false, false, new MessageList());
//            
//            importEnumOperation.run(new NullProgressMonitor());
//            
//        } catch (CoreException e) {
//            IpsPlugin.log(e);
//        } 
        
        // don't keep wizard open
        return true;
    }
    
}
