/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.migration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.versionmanager.AbstractIpsFeatureMigrationOperation;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

/**
 * @author Thorsten Guenther
 */
public class OpenMigrationWizardAction implements IWorkbenchWindowActionDelegate, IObjectActionDelegate {
    private IWorkbenchWindow window;
    private List<IIpsProject> preSelected = new ArrayList<IIpsProject>();

    @Override
    public void dispose() {
        // nothing to do
    }

    @Override
    public void init(IWorkbenchWindow window) {
        this.window = window;
    }

    @Override
    public void run(IAction action) {
        // save dirty editors
        if (!IpsUIPlugin.getDefault().saveAllEditors()) {
            return;
        }

        MigrationWizard wizard = new MigrationWizard(preSelected);
        wizard.init(window.getWorkbench(), getCurrentSelection());
        WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
        dialog.open();
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        if (selection.isEmpty()) {
            action.setEnabled(false);
            return;
        }

        if (selection instanceof IStructuredSelection) {
            preSelected = new ArrayList<IIpsProject>();
            IStructuredSelection sel = (IStructuredSelection)selection;
            for (Iterator<?> iter = sel.iterator(); iter.hasNext();) {
                Object selected = iter.next();
                if (selected instanceof IJavaProject) {
                    IIpsProject project = IpsPlugin.getDefault().getIpsModel()
                            .getIpsProject(((IJavaProject)selected).getProject());
                    addPreselection(project);
                } else if (selected instanceof IIpsProject) {
                    addPreselection((IIpsProject)selected);
                }
            }
        }
    }

    private void addPreselection(IIpsProject project) {
        if (project.exists()) {
            try {
                AbstractIpsFeatureMigrationOperation operation = IpsPlugin.getDefault().getMigrationOperation(project);
                if (!operation.isEmpty()) {
                    preSelected.add(project);
                }
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
        }
    }

    private IStructuredSelection getCurrentSelection() {
        if (window != null) {
            ISelection selection = window.getSelectionService().getSelection();
            if (selection instanceof IStructuredSelection) {
                return (IStructuredSelection)selection;
            }
            IWorkbenchPart part = window.getPartService().getActivePart();
            if (part instanceof IEditorPart) {
                IEditorInput input = ((IEditorPart)part).getEditorInput();
                if (input instanceof IFileEditorInput) {
                    return new StructuredSelection(((IFileEditorInput)input).getFile());
                }
            }
        }
        return null;
    }

    @Override
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        window = targetPart.getSite().getWorkbenchWindow();
    }

}
