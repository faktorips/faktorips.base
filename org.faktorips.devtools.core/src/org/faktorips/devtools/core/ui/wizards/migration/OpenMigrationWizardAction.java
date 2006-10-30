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

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;


/**
 * @author Joerg Ortmann
 */
public class OpenMigrationWizardAction implements IWorkbenchWindowActionDelegate {
    private IWorkbenchWindow window;
    
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
        MigrationWizard wizard = new MigrationWizard();
        wizard.init(window.getWorkbench(), getCurrentSelection());
        WizardDialog dialog = new WizardDialog(window.getShell(), new MigrationWizard());
        dialog.open();
    }

    /**
     * {@inheritDoc}
     */
    public void selectionChanged(IAction action, ISelection selection) {
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
}
