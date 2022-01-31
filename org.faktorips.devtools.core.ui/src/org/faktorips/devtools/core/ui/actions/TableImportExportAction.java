/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWizard;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.wizards.ipsimport.IpsObjectImportWizard;
import org.faktorips.devtools.core.ui.wizards.tableexport.TableExportWizard;
import org.faktorips.devtools.core.ui.wizards.tableimport.TableImportWizard;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.tablecontents.ITableContents;

/**
 * Action that opens the wizard for importing or exporting TableContents.
 */
public class TableImportExportAction extends IpsAction {

    private Shell shell;

    private boolean isImport;

    protected TableImportExportAction(Shell shell, ISelectionProvider selectionProvider) {
        super(selectionProvider);
        this.shell = shell;
    }

    protected TableImportExportAction(Shell shell, ITableContents tableContents) {
        super(new SimpleSelectionProvider(tableContents));
        this.shell = shell;
    }

    public static TableImportExportAction createTableImportAction(Shell shell, ISelectionProvider selectionProvider) {
        TableImportExportAction tableImportExportAction = new TableImportExportAction(shell, selectionProvider);
        tableImportExportAction.initImportAction();
        return tableImportExportAction;
    }

    public static TableImportExportAction createTableExportAction(Shell shell, ISelectionProvider selectionProvider) {
        TableImportExportAction tableImportExportAction = new TableImportExportAction(shell, selectionProvider);
        tableImportExportAction.initExportAction();
        return tableImportExportAction;
    }

    protected void initImportAction() {
        setText(Messages.TableImportExportAction_importActionTitle);
        setToolTipText(Messages.TableImportExportAction_importActionTooltip);
        setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("Import.gif")); //$NON-NLS-1$
        isImport = true;
    }

    protected void initExportAction() {
        setText(Messages.TableImportExportAction_exportActionTitle);
        setToolTipText(Messages.TableImportExportAction_exportActionTooltip);
        setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("Export.gif")); //$NON-NLS-1$
    }

    @Override
    public void run(IStructuredSelection selection) {
        runInternal(selection);
    }

    /**
     * Special run method returns the result of the dialog. Returns <code>true</code> if the dialog
     * was closed with the ok return code otherwise <code>false</code>.
     */
    protected boolean runInternal(IStructuredSelection selection) {
        IWorkbenchWizard wizard;
        if (isImport) {
            wizard = new TableImportWizard();
            ((IpsObjectImportWizard)wizard).setImportIntoExisting(true);
        } else {
            wizard = new TableExportWizard();
        }

        if (!(selection.getFirstElement() instanceof ITableContents)) {
            IpsPlugin.logAndShowErrorDialog(new IpsStatus("The selected element is no table contents!")); //$NON-NLS-1$
        }

        if (isImport) {
            if (!checkAndSaveDirtyStateBeforeImport((ITableContents)selection.getFirstElement())) {
                // abort import
                return false;
            }
        }

        wizard.init(IpsPlugin.getDefault().getWorkbench(), selection);
        WizardDialog dialog = new WizardDialog(shell, wizard);
        return dialog.open() == Window.OK;
    }

    private boolean checkAndSaveDirtyStateBeforeImport(final ITableContents tableContents) {
        if (!tableContents.getIpsSrcFile().isDirty()) {
            return true;
        }
        boolean confirmation = MessageDialog.openConfirm(shell,
                Messages.TableImportExportAction_confirmDialogDirtyTableContentsTitle,
                Messages.TableImportExportAction_confirmDialogDirtyTableContentsText);
        if (!confirmation) {
            return false;
        }
        Runnable run = () -> {
            try {
                tableContents.getIpsSrcFile().save(true, null);
            } catch (CoreRuntimeException e) {
                IpsPlugin.logAndShowErrorDialog(e);
            }
        };
        BusyIndicator.showWhile(shell.getDisplay(), run);

        return true;
    }

    private static class SimpleSelectionProvider implements ISelectionProvider {
        private ITableContents tableContents;

        public SimpleSelectionProvider(ITableContents tableContents) {
            this.tableContents = tableContents;
        }

        @Override
        public void addSelectionChangedListener(ISelectionChangedListener listener) {
            // Nothing to do
        }

        @Override
        public ISelection getSelection() {
            return new StructuredSelection(tableContents);
        }

        @Override
        public void removeSelectionChangedListener(ISelectionChangedListener listener) {
            // Nothing to do
        }

        @Override
        public void setSelection(ISelection selection) {
            // Nothing to do
        }

    }
}
