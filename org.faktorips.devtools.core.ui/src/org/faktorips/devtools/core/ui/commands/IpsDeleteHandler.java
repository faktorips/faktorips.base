/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.commands;

import java.util.ArrayList;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.ltk.ui.refactoring.resource.DeleteResourcesWizard;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.faktorips.devtools.core.ui.util.TypedSelection;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.util.ArgumentCheck;

public class IpsDeleteHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        final Shell shell = HandlerUtil.getActiveShell(event);
        ArgumentCheck.notNull(shell, this);
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        IResource[] selectedResources = getSelectedResources(selection);
        if (selectedResources.length > 0) {
            DeleteResourcesWizard refactoringWizard = new DeleteResourcesWizard(selectedResources);
            RefactoringWizardOpenOperation op = new RefactoringWizardOpenOperation(refactoringWizard);
            try {
                op.run(shell, Messages.IpsDeleteHandler_deleteResources);
            } catch (InterruptedException e) {
                // do nothing
            }
        }
        return null;
    }

    private IResource[] getSelectedResources(ISelection sel) {
        ArrayList<IResource> result = new ArrayList<>();
        TypedSelection<IAdaptable> typedSelection = TypedSelection.createAnyCount(IAdaptable.class, sel);
        if (typedSelection.isValid()) {
            for (IAdaptable adaptable : typedSelection.getElements()) {
                IResource resource = getAdaptedResource(adaptable);
                if (resource != null) {
                    result.add(resource);
                }
            }
        }
        return result.toArray(new IResource[result.size()]);
    }

    private IResource getAdaptedResource(IAdaptable adaptable) {
        if (adaptable instanceof IResource) {
            return (IResource)adaptable;
        }
        IResource resource = adaptable.getAdapter(IResource.class);
        if (resource != null) {
            return resource;
        }
        IIpsElement ipsElement = adaptable.getAdapter(IIpsElement.class);
        if (ipsElement != null && !(ipsElement instanceof IIpsObjectPart)) {
            return getAdaptedResource(ipsElement);
        }
        return null;
    }

}
