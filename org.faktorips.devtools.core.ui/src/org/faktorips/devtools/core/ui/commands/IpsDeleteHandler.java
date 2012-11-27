/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.ui.util.TypedSelection;
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
        ArrayList<IResource> result = new ArrayList<IResource>();
        TypedSelection<IAdaptable> typedSelection = new TypedSelection<IAdaptable>(IAdaptable.class, sel);
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
        IResource resource = (IResource)adaptable.getAdapter(IResource.class);
        if (resource != null) {
            return resource;
        }
        IIpsElement ipsElement = (IIpsElement)adaptable.getAdapter(IIpsElement.class);
        if (ipsElement != null && !(ipsElement instanceof IIpsObjectPart)) {
            return getAdaptedResource(ipsElement);
        }
        return null;
    }

}