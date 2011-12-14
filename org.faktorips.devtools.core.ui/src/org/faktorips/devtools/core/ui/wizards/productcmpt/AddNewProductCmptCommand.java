/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.wizards.productcmpt;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.ui.editors.productcmpt.ProductCmptEditor;

public class AddNewProductCmptCommand extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        try {
            IWorkbenchWindow activeWorkbenchWindow = HandlerUtil.getActiveWorkbenchWindow(event);
            // Getting selection by HandlerUtil.getCurrentSelection(event) often returns null also
            // there is a selection
            ISelection currentSelection = activeWorkbenchWindow.getSelectionService().getSelection();
            if (currentSelection instanceof IStructuredSelection) {
                IStructuredSelection structuredSelection = (IStructuredSelection)currentSelection;
                Object firstElement = structuredSelection.getFirstElement();
                if (firstElement instanceof IProductCmptLink) {
                    IProductCmptLink productCmptLink = (IProductCmptLink)firstElement;
                    IProductCmptGeneration sourceGeneration = productCmptLink.getProductCmptGeneration();
                    IProductCmptTypeAssociation association = productCmptLink.findAssociation(productCmptLink
                            .getIpsProject());
                    IProductCmpt targetProductCmpt = productCmptLink.findTarget(productCmptLink.getIpsProject());
                    initWizard(sourceGeneration, association, targetProductCmpt, activeWorkbenchWindow.getShell());
                } else if (firstElement instanceof String) {
                    // association node in LinkSection
                    String associationName = (String)firstElement;
                    IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);
                    IProductCmptGeneration sourceGeneration = getGenerationFromActiveEditor(activeEditor);
                    if (sourceGeneration != null) {
                        IProductCmptType productCmptType = sourceGeneration.findProductCmptType(sourceGeneration
                                .getIpsProject());
                        IProductCmptTypeAssociation addToAssociation = (IProductCmptTypeAssociation)productCmptType
                                .findAssociation(associationName, sourceGeneration.getIpsProject());
                        initWizard(sourceGeneration, addToAssociation, null, activeWorkbenchWindow.getShell());
                    }
                } else {
                    // TODO only for debugging - remove!
                    throw new RuntimeException("Illegal selection");
                }
            } else {
                // TODO only for debugging - remove!
                throw new RuntimeException("No structured selection");
            }

        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
        return null;
    }

    public void initWizard(IProductCmptGeneration sourceGeneration,
            IProductCmptTypeAssociation association,
            IProductCmpt targetProductCmpt,
            Shell shell) {
        try {
            IProductCmptType targetProductCmptType = association.findTargetProductCmptType(sourceGeneration
                    .getIpsProject());
            NewProductCmptWizard newProductCmptWizard = new NewProductCmptWizard();
            newProductCmptWizard.initDefaults(sourceGeneration.getIpsSrcFile().getIpsPackageFragment(),
                    targetProductCmptType, targetProductCmpt);
            newProductCmptWizard.setAddToAssociation(sourceGeneration, association);
            WizardDialog dialog = new WizardDialog(shell, newProductCmptWizard);
            dialog.open();
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    private IProductCmptGeneration getGenerationFromActiveEditor(IEditorPart activeEditor) {
        if (activeEditor instanceof ProductCmptEditor) {
            ProductCmptEditor prodCmptEditor = (ProductCmptEditor)activeEditor;
            return (IProductCmptGeneration)prodCmptEditor.getActiveGeneration();
        } else {
            return null;
        }
    }

}
