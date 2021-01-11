/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.actions;

import java.util.Iterator;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.wizards.enumcontent.CreateMissingEnumContentsWizard;

/**
 * This action opens up a wizard that enables the user to create missing {@link IEnumContent}.
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.4
 */
public class CreateMissingEnumContentsAction extends IpsAction {

    private IWorkbenchWindow workbenchWindow;

    public CreateMissingEnumContentsAction(ISelectionProvider selectionProvider, IWorkbenchWindow workbenchWindow) {
        super(selectionProvider);
        this.workbenchWindow = workbenchWindow;
        setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("CreateMissingEnumContents.gif")); //$NON-NLS-1$
        setText(Messages.CreateMissingEnumContentsAction_text);
    }

    @Override
    public void run(IStructuredSelection selection) {
        if (selection.isEmpty()) {
            return;
        }

        IIpsElement preselectedIpsElement = null;
        IStructuredSelection sel = selection;
        for (Iterator<?> iter = sel.iterator(); iter.hasNext();) {
            Object selected = iter.next();
            if (selected instanceof IJavaProject) {
                preselectedIpsElement = IpsPlugin.getDefault().getIpsModel()
                        .getIpsProject(((IJavaProject)selected).getProject());
                break;
            } else if (selected instanceof IIpsElement) {
                preselectedIpsElement = (IIpsElement)selected;
                break;
            }
        }
        CreateMissingEnumContentsWizard wizard = new CreateMissingEnumContentsWizard(preselectedIpsElement);
        wizard.open(workbenchWindow.getShell());
    }

}
