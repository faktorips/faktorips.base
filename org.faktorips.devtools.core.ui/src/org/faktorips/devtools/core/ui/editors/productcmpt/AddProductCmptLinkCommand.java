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

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.dialogs.OpenIpsObjectSelectionDialog;
import org.faktorips.devtools.core.ui.dialogs.SingleTypeSelectIpsObjectContext;
import org.faktorips.devtools.core.ui.dialogs.StaticContentSelectIpsObjectContext;
import org.faktorips.devtools.core.ui.util.LinkCreatorUtil;
import org.faktorips.devtools.core.ui.util.TypedSelection;
import org.faktorips.devtools.core.ui.views.productstructureexplorer.Messages;

/**
 * Opens the wizard to create a new product component relation.
 * 
 * @author Thorsten Guenther
 */
public class AddProductCmptLinkCommand extends AbstractHandler {

    public static final String COMMAND_ID = "org.faktorips.devtools.core.ui.commands.AddProductCmptLink"; //$NON-NLS-1$

    public AddProductCmptLinkCommand() {
        super();
    }

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        Shell shell = HandlerUtil.getActiveShell(event);
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection)selection;
            if (structuredSelection.getFirstElement() instanceof IProductCmptStructureReference) {
                return addLinkOnReference(selection, shell);
            } else {
                IEditorPart editor = HandlerUtil.getActiveEditor(event);
                return getAddLinkDialog(selection, shell, (ProductCmptEditor)editor);
            }
        }
        return null;
    }

    private OpenIpsObjectSelectionDialog getAddLinkDialog(ISelection selection,
            Shell shell,
            ProductCmptEditor productCmptEditor) {
        TypedSelection<String> typedSelection = new TypedSelection<String>(String.class, selection);

        IProductCmptTypeAssociation typeRelation;
        try {

            String associationName = typedSelection.getFirstElement();
            IProductCmpt productCmpt = productCmptEditor.getProductCmpt();
            IProductCmptType productCmptType = productCmpt.findProductCmptType(productCmpt.getIpsProject());

            typeRelation = (IProductCmptTypeAssociation)productCmptType.findAssociation(associationName,
                    productCmpt.getIpsProject());

            IProductCmptType targetProductCmptType = typeRelation
                    .findTargetProductCmptType(productCmpt.getIpsProject());
            IIpsSrcFile[] ipsSrcFiles = productCmpt.getIpsProject().findAllProductCmptSrcFiles(targetProductCmptType,
                    true);
            final StaticContentSelectIpsObjectContext context = new StaticContentSelectIpsObjectContext();
            context.setElements(ipsSrcFiles);
            final OpenIpsObjectSelectionDialog dialog = new OpenIpsObjectSelectionDialog(shell,
                    Messages.AddLinkAction_selectDialogTitle, context, true);
            int rc = dialog.open();
            if (rc == Window.OK) {
                if (dialog.getResult().length > 0) {
                    addLinkInEditor(productCmptEditor, typeRelation, associationName, dialog);

                }
            }

        } catch (CoreException e) {
            throw new CoreRuntimeException(e);

        }
        return null;
    }

    private void addLinkInEditor(ProductCmptEditor productCmptEditor,
            IProductCmptTypeAssociation typeRelation,
            String associationName,
            final OpenIpsObjectSelectionDialog dialog) throws CoreException {
        IProductCmptGeneration activeGeneration = (IProductCmptGeneration)productCmptEditor.getActiveGeneration();
        ArrayList<IIpsElement> result = dialog.getSelectedObjects();
        for (IIpsElement element : result) {
            if (element instanceof IIpsSrcFile) {
                IIpsSrcFile ipsSrcFile = (IIpsSrcFile)element;
                IProductCmpt selectedProductCmpt = (IProductCmpt)ipsSrcFile.getIpsObject();
                IProductCmptLink link = activeGeneration.newLink(typeRelation);
                link.setAssociation(associationName);
                link.setTarget(selectedProductCmpt.getQualifiedName());
                link.setMaxCardinality(1);
                link.setMinCardinality(0);

            }
        }
    }

    private boolean isEnabledInternal() {

        if (!super.isEnabled()) {
            return false;
        }

        IWorkbenchWindow activeWorkbenchWindow = IpsUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
        ISelection selection = activeWorkbenchWindow.getSelectionService().getSelection();
        if (selection.isEmpty() || !(selection instanceof IStructuredSelection)) {
            return false;
        }

        IStructuredSelection structuredSelection = (IStructuredSelection)selection;
        Object selectedElement = structuredSelection.getFirstElement();

        if (selectedElement instanceof String) {
            String associationName = (String)selectedElement;
            IProductCmpt productCmpt = ((ProductCmptEditor)activeWorkbenchWindow.getActivePage().getActiveEditor())
                    .getProductCmpt();
            return isEnabledString(associationName, productCmpt);
        } else {
            return false;
        }

    }

    private boolean isEnabledString(String associationName, IProductCmpt productCmpt) {

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
            typeAssociation = (IProductCmptTypeAssociation)productCmptType.findAssociation(associationName,
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

    @Override
    public void setEnabled(Object evaluationContext) {
        ISelection selection = IpsUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getSelectionService()
                .getSelection();
        if (selection.isEmpty() || !(selection instanceof IStructuredSelection)) {
            setBaseEnabled(false);
            return;
        }

        IStructuredSelection structuredSelection = (IStructuredSelection)selection;
        Object selectedElement = structuredSelection.getFirstElement();
        if (selectedElement instanceof IProductCmptReference) {
            setBaseEnabled(((IProductCmptReference)selectedElement).hasAssociationChildren());
        } else if (selectedElement instanceof String) {
            if (isEnabledInternal()) {

            }
        }

        TypedSelection<IProductCmptReference> typedSelection = new TypedSelection<IProductCmptReference>(
                IProductCmptReference.class, selection);
        if (typedSelection.isValid()) {
            setBaseEnabled(typedSelection.getFirstElement().hasAssociationChildren());
        } else if (isEnabledInternal()) {
            setBaseEnabled(true);

        } else {
            setBaseEnabled(true);
        }
        super.setEnabled(evaluationContext);
    }

    private Object addLinkOnReference(ISelection currentSelection, Shell shell) {
        LinkCreatorUtil linkCreator = new LinkCreatorUtil(true);
        TypedSelection<IProductCmptStructureReference> typedSelection = new TypedSelection<IProductCmptStructureReference>(
                IProductCmptStructureReference.class, currentSelection);
        if (!typedSelection.isValid()) {
            return null;
        }
        try {
            IIpsProject ipsProject = null;
            IProductCmptStructureReference structureReference = typedSelection.getFirstElement();
            ipsProject = structureReference.getWrappedIpsObject().getIpsProject();
            if (ipsProject != null) {
                List<IProductCmpt> selectedResults = selectProductCmpt(ipsProject, structureReference, shell);
                linkCreator.createLinks(selectedResults, structureReference);
            }
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
        return null;
    }

    /**
     * returns a list of product cmpts because the link creator requests a list
     */
    private List<IProductCmpt> selectProductCmpt(IIpsProject ipsProject,
            IProductCmptStructureReference linkTarget,
            Shell shell) throws CoreException {

        List<IProductCmpt> selectedResults = new ArrayList<IProductCmpt>();
        OpenIpsObjectSelectionDialog dialog = getSelectDialog(ipsProject, linkTarget, shell);
        // TODO set multi select in dialog
        if (dialog.open() == Window.OK) {
            IIpsElement selectedResult = dialog.getSelectedObject();
            if (selectedResult instanceof IIpsSrcFile) {
                IIpsObject selectedIpsObject = ((IIpsSrcFile)selectedResult).getIpsObject();
                if (selectedIpsObject instanceof IProductCmpt) {
                    IProductCmpt selectResultCmpt = (IProductCmpt)selectedIpsObject;
                    selectedResults.add(selectResultCmpt);
                }
            }
        }
        return selectedResults;
    }

    private OpenIpsObjectSelectionDialog getSelectDialog(IIpsProject ipsProject,
            IProductCmptStructureReference linkTarget,
            Shell shell) {
        SingleTypeSelectIpsObjectContext context = new SingleTypeSelectIpsObjectContext(ipsProject,
                IpsObjectType.PRODUCT_CMPT, new LinkViewerFilter(linkTarget));
        OpenIpsObjectSelectionDialog dialog = new OpenIpsObjectSelectionDialog(shell,
                Messages.AddLinkAction_selectDialogTitle, context);
        return dialog;
    }

    private static class LinkViewerFilter extends ViewerFilter {

        private final IProductCmptStructureReference linkTarget;
        private final LinkCreatorUtil linkCreator;

        public LinkViewerFilter(IProductCmptStructureReference linkTarget) {
            this.linkTarget = linkTarget;
            linkCreator = new LinkCreatorUtil(true);
        }

        @Override
        public boolean select(Viewer viewer, Object parentElement, Object element) {
            try {
                if (element instanceof IIpsSrcFile) {
                    IIpsSrcFile srcFile = (IIpsSrcFile)element;
                    if (!srcFile.getIpsObjectType().equals(IpsObjectType.PRODUCT_CMPT)) {
                        return false;
                    }
                    List<IProductCmpt> productCmptList = new ArrayList<IProductCmpt>(1);
                    productCmptList.add((IProductCmpt)srcFile.getIpsObject());
                    return linkCreator.canCreateLinks(linkTarget, productCmptList);
                } else {
                    return false;
                }
            } catch (CoreException e) {
                IpsPlugin.log(e);
                return false;
            }
        }
    }

}
