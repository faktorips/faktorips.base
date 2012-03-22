/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.editors.productcmpt.ProductCmptEditorInput;
import org.faktorips.devtools.core.ui.util.TypedSelection;

public class OpenEditorHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection selection = HandlerUtil.getCurrentSelectionChecked(event);

        TypedSelection<IAdaptable> typedSelection = new TypedSelection<IAdaptable>(IAdaptable.class, selection);
        IAdaptable firstElement = typedSelection.getFirstElement();

        if (firstElement instanceof IProductCmptLink) {
            IProductCmptGeneration targetGeneration = getTargetProductCmptGeneration((IProductCmptLink)firstElement);
            openEditorForProductCmptGeneration(targetGeneration);
        }

        IFile file = (IFile)typedSelection.getFirstElement().getAdapter(IFile.class);
        openEditorForFile(file);

        return null;
    }

    private IProductCmptGeneration getTargetProductCmptGeneration(IProductCmptLink link) {
        IProductCmptGeneration targetProductCmptGeneration = null;
        try {
            IProductCmpt targetProductCmpt = link.findTarget(link.getIpsProject());
            if (targetProductCmpt != null) {
                targetProductCmptGeneration = targetProductCmpt.getGenerationEffectiveOn(link
                        .getProductCmptGeneration().getValidFrom());
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
        return targetProductCmptGeneration;
    }

    private void openEditorForProductCmptGeneration(IProductCmptGeneration generation) {
        IpsUIPlugin.getDefault().openEditor(ProductCmptEditorInput.createWithGeneration(generation));
    }

    private void openEditorForFile(IFile file) {
        IpsUIPlugin.getDefault().openEditor(file);
    }

}
