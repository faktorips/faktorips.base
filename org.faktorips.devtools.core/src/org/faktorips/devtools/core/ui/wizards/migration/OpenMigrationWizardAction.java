/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.migration;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.versionmanager.AbstractIpsContentMigrationOperation;


/**
 * @author Thorsten Guenther
 */
public class OpenMigrationWizardAction implements IWorkbenchWindowActionDelegate, IObjectActionDelegate {
    private IWorkbenchWindow window;
    private ArrayList preSelected = new ArrayList();
    /**
     * {@inheritDoc}
     */
    public void dispose() {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    public void init(IWorkbenchWindow window) {
        this.window = window;
    }

    /**
     * {@inheritDoc}
     */
    public void run(IAction action) {
        // check for open editors
        boolean openEditor = false;
        if (PlatformUI.isWorkbenchRunning()) {
            IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
            for (int i = 0; i < windows.length && !openEditor; i++) {
                IWorkbenchPage[] pages = windows[i].getPages();
                for (int j = 0; j < pages.length && !openEditor; j++) {
                    openEditor = pages[j].getEditorReferences().length > 0;
                }
            }
        }
        
        if (openEditor) {
            MessageDialog.openError(window.getShell(), Messages.OpenMigrationWizardAction_titleCantMigrate, Messages.OpenMigrationWizardAction_msgCantMigrate);
            return;
        }
        
        MigrationWizard wizard = new MigrationWizard(preSelected);
        wizard.init(window.getWorkbench(), getCurrentSelection());
        WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
        dialog.open();
    }

    /**
     * {@inheritDoc}
     */
    public void selectionChanged(IAction action, ISelection selection) {
        if (selection.isEmpty()) {
            action.setEnabled(false);
            return;
        }
        
        if (selection instanceof IStructuredSelection) {
            preSelected = new ArrayList();
            IStructuredSelection sel = (IStructuredSelection)selection;
            for(Iterator iter = sel.iterator(); iter.hasNext();) {
                Object selected = iter.next();
                if (selected instanceof IJavaProject) {
                    IIpsProject project = IpsPlugin.getDefault().getIpsModel().getIpsProject(((IJavaProject)selected).getProject());
                    addPreselection(project);
                }
                else if (selected instanceof IIpsProject) {
                    addPreselection((IIpsProject)selected);
                }
            }
            action.setEnabled(preSelected.size() > 0);
        }
        else {
            action.setEnabled(false);
        }
    }
    
    private void addPreselection(IIpsProject project) {
        if (project.exists()) {
            try {
                AbstractIpsContentMigrationOperation operation = IpsPlugin.getDefault().getMigrationOperation(project);
                if (!operation.isEmpty()) {
                    preSelected.add(project);
                }
            }
            catch (CoreException e) {
                IpsPlugin.log(e);
            }
        }
    }

    private IStructuredSelection getCurrentSelection() {
        if (window != null) {
            ISelection selection= window.getSelectionService().getSelection();
            if (selection instanceof IStructuredSelection) {
                return (IStructuredSelection) selection;
            }
        }
        IWorkbenchPart part = window.getPartService().getActivePart();
        if (part instanceof IEditorPart) {
            IEditorInput input = ((IEditorPart) part).getEditorInput();
            if (input instanceof IFileEditorInput) {
                return new StructuredSelection(((IFileEditorInput) input).getFile());
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        window = targetPart.getSite().getWorkbenchWindow();
    }
}
