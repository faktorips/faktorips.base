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
import org.faktorips.devtools.core.model.enums.IEnumValueContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.wizards.enumexport.EnumExportWizard;
import org.faktorips.devtools.core.ui.wizards.enumimport.EnumImportWizard;
import org.faktorips.devtools.core.ui.wizards.ipsimport.IpsObjectImportWizard;

/**
 * Action that opens a wizard for importing or exporting enum types and contents.
 * 
 * @author Roman Grutza
 */
public class EnumImportExportAction extends IpsAction {

    private Shell shell;
    private boolean isImport;

    protected EnumImportExportAction(Shell shell, ISelectionProvider selectionProvider) {
        super(selectionProvider);
        this.shell = shell;
    }

    protected EnumImportExportAction(Shell shell, IEnumValueContainer valueContainer) {
        super(new SimpleSelectionProvider(valueContainer));
        this.shell = shell;
    }

    public static EnumImportExportAction createEnumImportAction(Shell shell, ISelectionProvider selectionProvider) {
        EnumImportExportAction enumImportExportAction = new EnumImportExportAction(shell, selectionProvider);
        enumImportExportAction.initImportAction();
        return enumImportExportAction;
    }

    public static EnumImportExportAction createEnumExportAction(Shell shell, ISelectionProvider selectionProvider) {
        EnumImportExportAction enumImportExportAction = new EnumImportExportAction(shell, selectionProvider);
        enumImportExportAction.initExportAction();
        return enumImportExportAction;
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
            wizard = new EnumImportWizard();
            ((IpsObjectImportWizard)wizard).setImportIntoExisting(true);
        } else {
            wizard = new EnumExportWizard();
        }

        if (!(selection.getFirstElement() instanceof IEnumValueContainer)) {
            IpsPlugin.logAndShowErrorDialog(new IpsStatus("The selected element is no enum value container!"));
        }

        if (isImport) {
            if (!checkAndSaveDirtyStateBeforeImport((IEnumValueContainer)selection.getFirstElement())) {
                // abort import
                return false;
            }
        }

        wizard.init(IpsPlugin.getDefault().getWorkbench(), selection);
        WizardDialog dialog = new WizardDialog(shell, wizard);
        return dialog.open() == Window.OK;
    }

    protected void initImportAction() {
        setText(Messages.EnumImportExportAction_importActionTitle);
        setToolTipText(Messages.EnumImportExportAction_importActionTooltip);
        setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("Import.gif")); //$NON-NLS-1$
        isImport = true;
    }

    protected void initExportAction() {
        setText(Messages.EnumImportExportAction_exportActionTitle);
        setToolTipText(Messages.EnumImportExportAction_exportActionTooltip);
        setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("Export.gif")); //$NON-NLS-1$
    }

    private boolean checkAndSaveDirtyStateBeforeImport(final IIpsObject enumIpsObject) {
        if (!enumIpsObject.getIpsSrcFile().isDirty()) {
            return true;
        }
        boolean confirmation = MessageDialog.openConfirm(shell,
                Messages.EnumImportExportAction_confirmDialogDirtyTableContentsTitle,
                Messages.EnumImportExportAction_confirmDialogDirtyTableContentsText);
        if (!confirmation) {
            return false;
        }
        Runnable run = new Runnable() {
            @Override
            public void run() {
                try {
                    enumIpsObject.getIpsSrcFile().save(true, null);
                } catch (CoreException e) {
                    IpsPlugin.logAndShowErrorDialog(e);
                }
            }
        };
        BusyIndicator.showWhile(shell.getDisplay(), run);

        return true;
    }

    // TODO rg: code duplication in TableImportExportAction
    // consider changing the selection type to IIpsObject and refactor this+TableImportExportAction
    private static class SimpleSelectionProvider implements ISelectionProvider {

        private IEnumValueContainer valueContainer;

        public SimpleSelectionProvider(IEnumValueContainer valueContainer) {
            this.valueContainer = valueContainer;
        }

        @Override
        public void addSelectionChangedListener(ISelectionChangedListener listener) {
        }

        @Override
        public ISelection getSelection() {
            return new StructuredSelection(valueContainer);
        }

        @Override
        public void removeSelectionChangedListener(ISelectionChangedListener listener) {
        }

        @Override
        public void setSelection(ISelection selection) {
        }
    }
}
