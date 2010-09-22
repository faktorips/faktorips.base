/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.wizards.productrelease;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.util.TypedSelection;

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
            IResource resource = (IResource)selection.getFirstElement().getAdapter(IResource.class);
            if (resource != null) {
                IProject project = resource.getProject();
                ipsProject = IpsPlugin.getDefault().getIpsModel().getIpsProject(project);
            }
        }

        ProductReleaserBuilderWizard wizard = new ProductReleaserBuilderWizard();
        wizard.setIpsProject(ipsProject);

        WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
        dialog.open();
    }

    private TypedSelection<IAdaptable> getCurrentSelection() {
        ISelection selection = null;
        if (window != null) {
            selection = window.getSelectionService().getSelection();
        }
        return new TypedSelection<IAdaptable>(IAdaptable.class, selection);
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        // TODO Auto-generated method stub
    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub

    }

}
