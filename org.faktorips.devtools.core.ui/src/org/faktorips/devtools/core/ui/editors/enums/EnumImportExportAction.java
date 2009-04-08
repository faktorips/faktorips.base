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

package org.faktorips.devtools.core.ui.editors.enums;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWizard;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.enums.IEnumValueContainer;
import org.faktorips.devtools.core.ui.actions.IpsAction;
import org.faktorips.devtools.core.ui.actions.Messages;
import org.faktorips.devtools.core.ui.wizards.tableexport.TableExportWizard;
import org.faktorips.devtools.core.ui.wizards.tableimport.EnumImportWizard;

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
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void run(IStructuredSelection selection) {
        runInternal(selection);
    }

    /**
     * Special run method returns the result of the dialog.
     * Returns <code>true</code> if the dialog was closed with the ok return code otherwise <code>false</code>.
     */
    protected boolean runInternal(IStructuredSelection selection) {
        IWorkbenchWizard wizard;
        if (isImport){
            wizard = new EnumImportWizard();
            ((EnumImportWizard)wizard).setImportIntoExisting(true);
        } else {
            wizard = new TableExportWizard(); 
        }
        
        if (! (selection.getFirstElement() instanceof IEnumValueContainer)){
            IpsPlugin.logAndShowErrorDialog(new IpsStatus("The selected element is no enum value container!")); //$NON-NLS-1$
        }
        
        // TODO rg: implement checkAndSaveDirtyStateBeforeImport()
//        if (isImport){
//            if (! checkAndSaveDirtyStateBeforeImport((ITableContents)selection.getFirstElement())){
//                // abort import
//                return false;
//            }
//        }
        
        wizard.init(IpsPlugin.getDefault().getWorkbench(), selection);
        WizardDialog dialog = new WizardDialog(shell, wizard);
        return dialog.open() == WizardDialog.OK;
    }
    
    protected void initImportAction(){
        setText(Messages.TableImportExportAction_importActionTitle);
        setToolTipText(Messages.TableImportExportAction_importActionTooltip);
        // TODO rg: create ENUM images
        setImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("ImportTableContents.gif")); //$NON-NLS-1$
        this.isImport = true;
    }

    protected void initExportAction() {
        setText(Messages.TableImportExportAction_exportActionTitle);
        setToolTipText(Messages.TableImportExportAction_exportActionTooltip);
     // TODO rg: create ENUM images
        setImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("ExportTableContents.gif")); //$NON-NLS-1$
    }
    
    // TODO rg: code duplication in TableImportExportAction
    //      consider changing the selection type to IIpsObject and refactor this+TableImportExportAction
    private static class SimpleSelectionProvider implements ISelectionProvider {
        
        private IEnumValueContainer valueContainer;

        public SimpleSelectionProvider(IEnumValueContainer valueContainer) {
            this.valueContainer = valueContainer;
        }
        
        public void addSelectionChangedListener(ISelectionChangedListener listener) {
        }
        
        public ISelection getSelection() {
            return new StructuredSelection(valueContainer);
        }
        
        public void removeSelectionChangedListener(ISelectionChangedListener listener) {
        }
        
        public void setSelection(ISelection selection) {
        }
    }
}
