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

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWizard;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.ui.wizards.tableexport.TableExportWizard;
import org.faktorips.devtools.core.ui.wizards.tableimport.TableImportWizard;

/**
 * Action that opens the wizard for importing or exporting TableContents.
 */
public class TableImportExportAction extends IpsAction {
    private Shell shell;
    private IWorkbenchWizard wizard;
    
    private static class SimpleSelectionProvider implements ISelectionProvider {
        private ITableContents tableContents;
        
        public SimpleSelectionProvider(ITableContents tableContents) {
            this.tableContents = tableContents;
        }

        public void addSelectionChangedListener(ISelectionChangedListener listener) {
        }

        public ISelection getSelection() {
            return new StructuredSelection(tableContents);
        }

        public void removeSelectionChangedListener(ISelectionChangedListener listener) {
        }

        public void setSelection(ISelection selection) {
        }
    }
    
    public static TableImportExportAction createTableImportAction(Shell shell, ISelectionProvider selectionProvider) {
        TableImportWizard tableImportWizard = new TableImportWizard();
        tableImportWizard.setImportIntoExisting(true);
        return new TableImportExportAction(shell, selectionProvider, tableImportWizard, Messages.TableImportExportAction_importActionTitle,
                Messages.TableImportExportAction_importActionTooltip, IpsPlugin.getDefault().getImageDescriptor("ImportTableContents.gif")); //$NON-NLS-2$
    }

    public static TableImportExportAction createTableExportAction(Shell shell, ISelectionProvider selectionProvider) {
        return new TableImportExportAction(shell, selectionProvider, new TableExportWizard(), Messages.TableImportExportAction_exportActionTitle,
                Messages.TableImportExportAction_exportActionTooltip, IpsPlugin.getDefault().getImageDescriptor("ExportTableContents.gif")); //$NON-NLS-2$
    }
    
    public static TableImportExportAction createTableImportAction(Shell shell, ITableContents tableContents) {
        return createTableImportAction(shell, new SimpleSelectionProvider(tableContents));
    }

    public static TableImportExportAction createTableExportAction(Shell shell, ITableContents tableContents) {
        return createTableExportAction(shell, new SimpleSelectionProvider(tableContents));
    }
    
    private TableImportExportAction(Shell shell, ISelectionProvider selectionProvider, IWorkbenchWizard wizard, String text, String tooltip, ImageDescriptor imageDesrc) {
        super(selectionProvider);
        this.shell = shell;
        setText(text);
        setToolTipText(tooltip);
        setImageDescriptor(imageDesrc); //$NON-NLS-1$
        this.wizard = wizard;
    }
    
    private boolean isImport() {
        return wizard instanceof TableImportWizard;
    }

    /**
     * {@inheritDoc}
     */
    public void run(IStructuredSelection selection) {
        if (! (selection.getFirstElement() instanceof ITableContents)){
            IpsPlugin.logAndShowErrorDialog(new IpsStatus("The selected element is no table contents!")); //$NON-NLS-1$
        }
        
        if (isImport()){
            if (! checkAndSaveDirtyStateBeforeImport((ITableContents)selection.getFirstElement())){
                // abort import
                return;
            }
        }
        
        wizard.init(IpsPlugin.getDefault().getWorkbench(), selection);
        WizardDialog dialog = new WizardDialog(shell, wizard);
        dialog.open();
    }

    private boolean checkAndSaveDirtyStateBeforeImport(final ITableContents tableContents) {
        if (! tableContents.getIpsSrcFile().isDirty()) {
            return true;
        }
            boolean confirmation = MessageDialog.openConfirm(shell,
                Messages.TableImportExportAction_confirmDialogDirtyTableContentsTitle,
                Messages.TableImportExportAction_confirmDialogDirtyTableContentsText);
        if (!confirmation) {
            return false;
        }
        Runnable run = new Runnable() {
            public void run() {
                try {
                    tableContents.getIpsSrcFile().save(true, null);
                } catch (CoreException e) {
                    IpsPlugin.logAndShowErrorDialog(e);
                }
            }
        };
        BusyIndicator.showWhile(shell.getDisplay(), run);

        return true;
    }
}
