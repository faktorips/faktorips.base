/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.productrelease;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.Wrappers;
import org.faktorips.devtools.core.ui.util.TypedSelection;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * This Action starts the product release builder wizard
 * 
 * @author dirmeier
 */
public class ShowReleaserBuilderWizardAction implements IWorkbenchWindowActionDelegate {

    private IWorkbenchWindow window;

    @Override
    public void init(IWorkbenchWindow window) {
        this.window = window;
    }

    @Override
    public void run(IAction action) {
        TypedSelection<IAdaptable> selection = getCurrentSelection();
        IIpsProject ipsProject = null;
        if (selection.isValid()) {
            IResource resource = selection.getFirstElement().getAdapter(IResource.class);
            if (resource != null) {
                IProject project = resource.getProject();
                ipsProject = IIpsModel.get().getIpsProject(Wrappers.wrap(project).as(AProject.class));
            }
        }

        ProductReleaserBuilderWizard wizard = new ProductReleaserBuilderWizard();
        wizard.setIpsProject(ipsProject);

        WizardDialog dialog = new ProductReleaseBuilderDialog(window.getShell(), wizard);
        dialog.open();
    }

    private TypedSelection<IAdaptable> getCurrentSelection() {
        ISelection selection = null;
        if (window != null) {
            selection = window.getSelectionService().getSelection();
        }
        return new TypedSelection<>(IAdaptable.class, selection);
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        // nothing to do
    }

    @Override
    public void dispose() {
        // nothing to dispose
    }

}
