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

package org.faktorips.devtools.core.ui.wizards.tableexport;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.TableExportOperation;
import org.faktorips.devtools.core.model.tablecontents.TableFileFormat;

/**
 * 
 * @author Thorsten Waertel
 */
public class TableExportWizard extends Wizard implements IExportWizard {
	
	private IStructuredSelection selection;
	private TableExportPage exportPage;

	/**
	 * 
	 */
	public TableExportWizard() {
		super();
		setWindowTitle(Messages.TableExport_title);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void addPages() {
        try {
        	exportPage = new TableExportPage(selection);
        	addPage(exportPage);
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean performFinish() {
		try {
			ISchedulingRule schedulingRule = exportPage.getIpsProject().getCorrespondingResource();
            final ITableContents exportContents = exportPage.getTableContents();
            final String exportFilename = exportPage.getFilename();
            final String format = exportPage.getFormat();
            
            File exportFile = new File(exportFilename);
            if (exportFile.exists()) {
                MessageDialog dialog = new MessageDialog(getContainer().getShell(),
                        Messages.TableExportWizard_msgFileExistsTitle,
                        (Image) null, Messages.TableExportWizard_msgFileExists
                        , MessageDialog.QUESTION,
                        new String[] { IDialogConstants.YES_LABEL,
                                IDialogConstants.NO_LABEL }, 0);
                // ensure yes is the default
                if (dialog.open() != 0) {
                    return false;
                }
            }
            
			WorkspaceModifyOperation operation = new WorkspaceModifyOperation(schedulingRule){

				protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
                    TableFileFormat fileFormat = TableFileFormat.getAttributeType(format);
                    if (fileFormat == TableFileFormat.XLS) {
                        TableExportOperation teo = new TableExportOperation(exportContents, exportFilename);
                        teo.run(monitor);
                    }
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

}
