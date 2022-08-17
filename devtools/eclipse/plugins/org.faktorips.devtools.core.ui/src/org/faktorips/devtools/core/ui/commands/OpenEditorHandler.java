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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.util.TypedSelection;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.IProductCmptLinkContainer;

public class OpenEditorHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection selection = HandlerUtil.getCurrentSelectionChecked(event);

        TypedSelection<IAdaptable> typedSelection = new TypedSelection<>(IAdaptable.class, selection);
        IAdaptable firstElement = typedSelection.getFirstElement();

        IProductCmptLink link = IpsObjectPartTester.castOrAdaptToPart(firstElement, IProductCmptLink.class);
        if (link != null) {
            openEditorForLink(link);
            return null;
        }

        IProductCmptGeneration generation = typedSelection.getFirstElement().getAdapter(
                IProductCmptGeneration.class);
        if (generation != null) {
            IpsUIPlugin.getDefault().openEditor(generation);
            return null;
        }

        IIpsObject ipsObject = typedSelection.getFirstElement().getAdapter(IIpsObject.class);
        if (ipsObject != null) {
            IpsUIPlugin.getDefault().openEditor(ipsObject);
            return null;
        }

        IFile file = typedSelection.getFirstElement().getAdapter(IFile.class);
        IpsUIPlugin.getDefault().openEditor(file);
        return null;
    }

    private void openEditorForLink(IProductCmptLink link) {
        IProductCmptGeneration targetGeneration = getTargetProductCmptGeneration(link);
        openEditorForProductCmptGeneration(targetGeneration);
    }

    private IProductCmptGeneration getTargetProductCmptGeneration(IProductCmptLink link) {
        IProductCmptGeneration targetProductCmptGeneration = null;
        IProductCmpt targetProductCmpt = link.findTarget(link.getIpsProject());
        if (targetProductCmpt != null) {
            IProductCmptLinkContainer productCmptLinkContainer = link.getProductCmptLinkContainer();
            if (productCmptLinkContainer instanceof IProductCmptGeneration) {
                IProductCmptGeneration productCmptGeneration = (IProductCmptGeneration)productCmptLinkContainer;
                targetProductCmptGeneration = targetProductCmpt
                        .getBestMatchingGenerationEffectiveOn(productCmptGeneration.getValidFrom());
            } else {
                targetProductCmptGeneration = targetProductCmpt.getLatestProductCmptGeneration();
            }
        }
        return targetProductCmptGeneration;
    }

    private void openEditorForProductCmptGeneration(IProductCmptGeneration generation) {
        IpsUIPlugin.getDefault().openEditor(generation);
    }

}
