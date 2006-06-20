/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.tableimport;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.tablecontents.AbstractXlsTableImportOperation;
import org.faktorips.devtools.core.model.tablecontents.XlsTableImportIntoExistingOperation;
import org.faktorips.devtools.core.model.tablecontents.XlsTableImportIntoNewOperation;

/**
 * 
 * @author Thorsten Waertel
 */
public class TableImportWizard extends Wizard implements IImportWizard {
	
	private IStructuredSelection selection;
	private SelectFileAndImportMethodPage filePage;
	private NewContentsPage newContentsPage;
    private SelectContentsPage selectContentsPage;

	/**
	 * 
	 */
	public TableImportWizard() {
		super();
		setWindowTitle(Messages.TableImport_title);
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
            selectContentsPage = new SelectContentsPage(selection);
            addPage(selectContentsPage);
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean performFinish() {
		try {
			ISchedulingRule schedulingRule = newContentsPage.getIpsProject().getCorrespondingResource();
            final String filename = filePage.getFilename();
            final boolean importExisting = filePage.isImportIntoExisting();
            final boolean append = filePage.isImportExistingAppend();
            final IIpsPackageFragment pack = newContentsPage.getIpsPackageFragment();
            final String structure = newContentsPage.getTableStructureName();
            final String contents = importExisting ? selectContentsPage.getTableContents().getQualifiedName() : newContentsPage.getTableContentsName();
            final IIpsProject project = selectContentsPage.getIpsProject();
			WorkspaceModifyOperation operation = new WorkspaceModifyOperation(schedulingRule){

				protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
                    AbstractXlsTableImportOperation tio;
                    if (importExisting) {
                        tio = new XlsTableImportIntoExistingOperation(filename, append, project, contents);
                    } else {
                        tio = new XlsTableImportIntoNewOperation(filename, pack, structure, contents);
                    }
                    tio.run(monitor);
                }
				
			};
			getContainer().run(true, true, operation);
			
		} catch (Exception e) {
			IpsPlugin.logAndShowErrorDialog(new IpsStatus("An error occured during the export process.",e)); //$NON-NLS-1$
		}
		//this implementation of this method should always return true since this causes the wizard dialog to close.
		//in either case if an exception arises or not it doesn't make sense to keep the dialog up
		return true;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}

    /**
     * {@inheritDoc}
     */
    public IWizardPage getNextPage(IWizardPage page) {
        if (page == filePage) {
            // set the comleted state on the opposite page to true so that the wizard can finish normally
            selectContentsPage.setPageComplete(!filePage.isImportIntoExisting());
            newContentsPage.setPageComplete(filePage.isImportIntoExisting());
            // Validate the returned Page so that finished state is already set to true if all default settings are correct
            if (filePage.isImportIntoExisting()) {
            	selectContentsPage.validatePage();
            	return selectContentsPage;
            }
            newContentsPage.validatePage();
            return newContentsPage;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public IWizardPage getPreviousPage(IWizardPage page) {
        if (page == selectContentsPage || page == newContentsPage) {
            return filePage;
        }
        return null;
    }
}
