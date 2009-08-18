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

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.core.runtime.CoreException;
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
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.wizards.ipsimport.IpsObjectImportWizard;
import org.faktorips.devtools.core.ui.wizards.tableexport.TableExportWizard;
import org.faktorips.devtools.core.ui.wizards.tableimport.TableImportWizard;

/**
 * Action that opens the wizard for importing or exporting TableContents.
 */
public class TableImportExportAction extends IpsAction {

    private Shell shell;

    private boolean isImport;

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
        TableImportExportAction tableImportExportAction = new TableImportExportAction(shell, selectionProvider);
        tableImportExportAction.initImportAction();
        return tableImportExportAction;
    }

    public static TableImportExportAction createTableExportAction(Shell shell, ISelectionProvider selectionProvider) {
        TableImportExportAction tableImportExportAction = new TableImportExportAction(shell, selectionProvider);
        tableImportExportAction.initExportAction();
        return tableImportExportAction;
    }

    public static TableImportExportAction createTableImportAction(Shell shell, ITableContents tableContents) {
        return createTableImportAction(shell, tableContents);
    }

    public static TableImportExportAction createTableExportAction(Shell shell, ITableContents tableContents) {
        return createTableExportAction(shell, tableContents);
    }

    protected TableImportExportAction(Shell shell, ISelectionProvider selectionProvider) {
        super(selectionProvider);
        this.shell = shell;
    }

    protected TableImportExportAction(Shell shell, ITableContents tableContents) {
        super(new SimpleSelectionProvider(tableContents));
        this.shell = shell;
    }

    protected void initImportAction() {
        setText(Messages.TableImportExportAction_importActionTitle);
        setToolTipText(Messages.TableImportExportAction_importActionTooltip);
        setImageDescriptor(IpsUIPlugin.getDefault().getImageDescriptor("Import.gif")); //$NON-NLS-1$
        isImport = true;
    }

    protected void initExportAction() {
        setText(Messages.TableImportExportAction_exportActionTitle);
        setToolTipText(Messages.TableImportExportAction_exportActionTooltip);
        setImageDescriptor(IpsUIPlugin.getDefault().getImageDescriptor("Export.gif")); //$NON-NLS-1$
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
