/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.enumcontent;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.Wrappers;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IIpsModel;

/**
 * This action opens up a wizard that enables the user to create missing <code>IEnumContent</code>s.
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.4
 */
public class OpenCreateMissingEnumContentsWizardAction implements IObjectActionDelegate {

    private IWorkbenchWindow workbenchWindow;

    /** The preselected <code>IIpsElement</code>. */
    private IIpsElement preselectedIpsElement;

    @Override
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        workbenchWindow = targetPart.getSite().getWorkbenchWindow();
    }

    @Override
    public void run(IAction action) {
        CreateMissingEnumContentsWizard wizard = new CreateMissingEnumContentsWizard(preselectedIpsElement);
        wizard.open(workbenchWindow.getShell());
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        if (selection.isEmpty()) {
            return;
        }

        if (selection instanceof IStructuredSelection sel) {
            for (Object selected : sel) {
                if (selected instanceof IJavaProject) {
                    preselectedIpsElement = IIpsModel.get()
                            .getIpsProject(Wrappers.wrap(((IJavaProject)selected).getProject()).as(AProject.class));
                    break;
                } else if (selected instanceof IResource resource) {
                    preselectedIpsElement = IIpsModel.get()
                            .getIpsProject(Wrappers.wrap(resource.getProject()).as(AProject.class));
                    break;
                }
            }
        }
    }

}
