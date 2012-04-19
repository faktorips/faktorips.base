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
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.editors.productcmpt.ProductCmptEditor;

public class AddNewProductCmptCommand extends AbstractHandler {

    public static final String COMMAND_ID = "org.faktorips.devtools.core.ui.wizards.productcmpt.newProductCmpt"; //$NON-NLS-1$

    public static final String PARAMETER_SELECTED_ASSOCIATION = "org.faktorips.devtools.core.ui.wizards.productcmpt.newProductCmpt.selectedAssociation"; //$NON-NLS-1$

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchWindow activeWorkbenchWindow = HandlerUtil.getActiveWorkbenchWindow(event);
        // Getting selection by HandlerUtil.getCurrentSelection(event) often returns null also
        // there is a selection
        ISelection currentSelection = activeWorkbenchWindow.getSelectionService().getSelection();
        if (currentSelection instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection)currentSelection;
            Object firstElement = structuredSelection.getFirstElement();
            if (firstElement instanceof String) {
                IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);
                addNewLinkInEditor(activeEditor, activeWorkbenchWindow, firstElement);
            } else if (firstElement instanceof IProductCmptStructureReference) {
                Shell shell = HandlerUtil.getActiveShell(event);
                String selectedAssociationParameter = event.getParameter(PARAMETER_SELECTED_ASSOCIATION);
                if (selectedAssociationParameter != null) {
                    addNewLinkOnReference((IProductCmptStructureReference)firstElement, selectedAssociationParameter,
                            shell);
                } else {
                    addNewLinkOnReference((IProductCmptStructureReference)firstElement, shell);
                }
            }
        }
        return null;
    }

    private void addNewLinkInEditor(IEditorPart activeEditor,
            IWorkbenchWindow activeWorkbenchWindow,
            Object firstElement) {
        // association node in LinkSection
        String associationName = (String)firstElement;
        IProductCmptGeneration sourceGeneration = getGenerationFromActiveEditor(activeEditor);
        if (sourceGeneration != null) {
            try {
                IProductCmptType productCmptType = sourceGeneration.findProductCmptType(sourceGeneration
                        .getIpsProject());
                IProductCmptTypeAssociation addToAssociation = (IProductCmptTypeAssociation)productCmptType
                        .findAssociation(associationName, sourceGeneration.getIpsProject());
                if (addToAssociation != null) {
                    initWizard(sourceGeneration, addToAssociation, null, activeWorkbenchWindow.getShell());
                }
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
        }
    }

    private void addNewLinkOnReference(IProductCmptStructureReference structureReference, Shell shell) {
        IProductCmptGeneration generation = (IProductCmptGeneration)structureReference
                .getAdapter(IProductCmptGeneration.class);
        IProductCmptTypeAssociation association = (IProductCmptTypeAssociation)structureReference
                .getAdapter(IProductCmptTypeAssociation.class);
        if (generation != null && association != null) {
            initWizard(generation, association, null, shell);
        }
    }

    private void addNewLinkOnReference(IProductCmptStructureReference structureReference,
            String selectedAssociationParameter,
            Shell shell) {
        try {
            IProductCmptGeneration generation = (IProductCmptGeneration)structureReference
                    .getAdapter(IProductCmptGeneration.class);
            if (generation != null) {
                IProductCmptTypeAssociation association = (IProductCmptTypeAssociation)generation.findProductCmptType(
                        generation.getIpsProject()).findAssociation(selectedAssociationParameter,
                        generation.getIpsProject());
                if (association != null) {
                    initWizard(generation, association, null, shell);
                }
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }

    }

    private void initWizard(IProductCmptGeneration sourceGeneration,
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

    @Override
    public void setEnabled(Object evaluationContext) {
        IWorkbenchWindow activeWorkbenchWindow = IpsUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
        ISelection selection = activeWorkbenchWindow.getSelectionService().getSelection();
        if (selection.isEmpty() || !(selection instanceof IStructuredSelection)) {
            setBaseEnabled(false);
            return;
        }

        IStructuredSelection structuredSelection = (IStructuredSelection)selection;
        Object selectedElement = structuredSelection.getFirstElement();
        if (selectedElement instanceof IProductCmptReference) {
            setBaseEnabled(((IProductCmptReference)selectedElement).hasAssociationChildren());
        } else if (selectedElement instanceof String) {
            setBaseEnabled(isEnabledInternal(selectedElement, activeWorkbenchWindow));
        } else {
            setBaseEnabled(true);
        }
    }

    /**
     * Queries to the target type of the selected element, target type must be found.
     * 
     */
    private boolean isEnabledInternal(Object selectedElement, IWorkbenchWindow activeWorkbenchWindow) {

        IProductCmpt productCmpt = ((ProductCmptEditor)activeWorkbenchWindow.getActivePage().getActiveEditor())
                .getProductCmpt();

        IProductCmptType productCmptType = null;
        try {
            productCmptType = productCmpt.findProductCmptType(productCmpt.getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
        if (productCmptType == null) {
            return false;
        }
        IProductCmptTypeAssociation typeAssociation = null;
        try {
            typeAssociation = (IProductCmptTypeAssociation)productCmptType.findAssociation((String)selectedElement,
                    productCmpt.getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
        if (typeAssociation == null) {
            return false;
        }

        IProductCmptType targetProductCmptType = null;
        try {
            targetProductCmptType = typeAssociation.findTargetProductCmptType(productCmpt.getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
        return targetProductCmptType != null;

    }

}
