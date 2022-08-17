/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.productcmpt;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.faktorips.devtools.core.ui.commands.AbstractAddAndNewProductCmptCommand;
import org.faktorips.devtools.core.ui.editors.productcmpt.ProductCmptEditor;
import org.faktorips.devtools.core.ui.editors.productcmpt.link.AbstractAssociationViewItem;
import org.faktorips.devtools.core.ui.util.TypedSelection;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;

public class AddNewProductCmptCommand extends AbstractAddAndNewProductCmptCommand {

    public static final String COMMAND_ID = "org.faktorips.devtools.core.ui.wizards.productcmpt.newProductCmpt"; //$NON-NLS-1$

    public static final String PARAMETER_SELECTED_ASSOCIATION = "org.faktorips.devtools.core.ui.wizards.productcmpt.newProductCmpt.selectedAssociation"; //$NON-NLS-1$

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        ISelection selection = HandlerUtil.getCurrentSelection(event);
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection)selection;
            Object firstElement = structuredSelection.getFirstElement();
            if (firstElement instanceof AbstractAssociationViewItem) {
                addNewLinkInEditor(event);
            } else if (firstElement instanceof IProductCmptStructureReference) {
                addNewLinkOnReference(event);
            } else {
                throw new RuntimeException();
            }
        }
        return null;
    }

    private void addNewLinkInEditor(ExecutionEvent event) {
        // association node in LinkSection
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        TypedSelection<AbstractAssociationViewItem> typedSelection = new TypedSelection<>(
                AbstractAssociationViewItem.class, selection);
        if (!typedSelection.isValid()) {
            return;
        }

        IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);
        if (!(activeEditor instanceof ProductCmptEditor)) {
            return;
        }
        IProductCmptGeneration sourceGeneration = getGenerationFromActiveEditor(activeEditor);
        if (sourceGeneration != null) {
            IProductCmptType productCmptType = sourceGeneration.findProductCmptType(sourceGeneration.getIpsProject());
            IProductCmptTypeAssociation addToAssociation = (IProductCmptTypeAssociation)productCmptType.findAssociation(
                    typedSelection.getFirstElement().getAssociationName(), sourceGeneration.getIpsProject());
            if (addToAssociation != null) {
                initWizard(sourceGeneration, addToAssociation, null, HandlerUtil.getActiveShell(event));
            }
        }
    }

    private void addNewLinkOnReference(ExecutionEvent event) {
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        TypedSelection<IProductCmptStructureReference> typedSelection = new TypedSelection<>(
                IProductCmptStructureReference.class, selection);
        if (!typedSelection.isValid()) {
            return;
        }

        IProductCmptStructureReference structureReference = typedSelection.getFirstElement();
        IProductCmptGeneration generation = structureReference
                .getAdapter(IProductCmptGeneration.class);
        if (generation != null) {
            String selectedAssociationParameter = event.getParameter(PARAMETER_SELECTED_ASSOCIATION);
            IProductCmptTypeAssociation association;
            if (selectedAssociationParameter != null) {
                association = (IProductCmptTypeAssociation)generation.findProductCmptType(generation.getIpsProject())
                        .findAssociation(selectedAssociationParameter, generation.getIpsProject());
                initWizard(generation, association, null, HandlerUtil.getActiveShell(event));
            } else {
                association = structureReference
                        .getAdapter(IProductCmptTypeAssociation.class);
                if (association != null) {
                    initWizard(generation, association, null, HandlerUtil.getActiveShell(event));
                }
            }
        }
    }

    private void initWizard(IProductCmptGeneration sourceGeneration,
            IProductCmptTypeAssociation association,
            IProductCmpt targetProductCmpt,
            Shell shell) {
        IProductCmptType targetProductCmptType = association
                .findTargetProductCmptType(sourceGeneration.getIpsProject());
        NewProductWizard newProductCmptWizard = new NewProductCmptWizard();
        newProductCmptWizard.initDefaults(sourceGeneration.getIpsSrcFile().getIpsPackageFragment(),
                targetProductCmptType, targetProductCmpt);
        newProductCmptWizard.setAddToAssociation(sourceGeneration, association);
        WizardDialog dialog = new WizardDialog(shell, newProductCmptWizard);
        dialog.open();
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
