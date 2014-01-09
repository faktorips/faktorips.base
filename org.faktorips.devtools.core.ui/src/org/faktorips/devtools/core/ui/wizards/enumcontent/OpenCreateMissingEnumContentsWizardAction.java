/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.enumcontent;

import java.util.Iterator;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;

/**
 * This action opens up a wizard that enables the user to create missing <tt>IEnumContent</tt>s.
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.4
 */
public class OpenCreateMissingEnumContentsWizardAction implements IObjectActionDelegate {

    private IWorkbenchWindow workbenchWindow;

    /** The preselected <tt>IIpsElement</tt>. */
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

        if (selection instanceof IStructuredSelection) {
            IStructuredSelection sel = (IStructuredSelection)selection;
            for (Iterator<?> iter = sel.iterator(); iter.hasNext();) {
                Object selected = iter.next();
                if (selected instanceof IJavaProject) {
                    preselectedIpsElement = IpsPlugin.getDefault().getIpsModel()
                            .getIpsProject(((IJavaProject)selected).getProject());
                    break;
                } else if (selected instanceof IResource) {
                    IResource resource = (IResource)selected;
                    preselectedIpsElement = IpsPlugin.getDefault().getIpsModel().getIpsProject(resource.getProject());
                    break;
                }
            }
        }
    }

}
