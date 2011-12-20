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

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.editors.productcmpt.ProductCmptEditor;

public class AddNewProductCmptCommand extends AbstractHandler implements IElementUpdater {

    @Override
    public void updateElement(UIElement element, Map parameters) {
        ISelectionService selectionService = IpsUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow()
                .getSelectionService();
        if (selectionService != null) {
            IStructuredSelection selection = (IStructuredSelection)selectionService.getSelection();
            if (selection == null) {
                return;
            }
            if (selection.getFirstElement() instanceof String) {
                String selectedString = (String)selection.getFirstElement();
                element.setText(NLS.bind("Add new {0}...", selectedString));
            } else if (selection.getFirstElement() instanceof IAdaptable) {
                IAdaptable adaptable = (IAdaptable)selection.getFirstElement();
                IWorkbenchAdapter workbenchAdapter = (IWorkbenchAdapter)adaptable.getAdapter(IWorkbenchAdapter.class);
                if (workbenchAdapter != null) {
                    element.setText(NLS.bind("Add new {0}...", workbenchAdapter.getLabel(adaptable)));
                }
            }
        }
    }

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
                addNewLinkOnReference((IProductCmptStructureReference)firstElement, shell);
            }
        } else {
            // TODO only for debugging - remove!
            throw new RuntimeException("No structured selection");
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
                initWizard(sourceGeneration, addToAssociation, null, activeWorkbenchWindow.getShell());
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
        initWizard(generation, association, null, shell);
        structureReference.getWrappedIpsObject();
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
