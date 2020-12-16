/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.productcmpt.IProductCmptLinkContainer;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.commands.AbstractAddAndNewProductCmptCommand;
import org.faktorips.devtools.core.ui.dialogs.OpenIpsObjectSelectionDialog;
import org.faktorips.devtools.core.ui.dialogs.SingleTypeSelectIpsObjectContext;
import org.faktorips.devtools.core.ui.dialogs.StaticContentSelectIpsObjectContext;
import org.faktorips.devtools.core.ui.editors.productcmpt.link.AbstractAssociationViewItem;
import org.faktorips.devtools.core.ui.util.LinkCreatorUtil;
import org.faktorips.devtools.core.ui.util.TypedSelection;
import org.faktorips.devtools.core.ui.views.productstructureexplorer.Messages;

/**
 * Opens the wizard to create a new product component relation.
 * 
 * @author Thorsten Guenther
 */
public class AddProductCmptLinkCommand extends AbstractAddAndNewProductCmptCommand {

    public static final String COMMAND_ID = "org.faktorips.devtools.core.ui.commands.AddProductCmptLink"; //$NON-NLS-1$

    public AddProductCmptLinkCommand() {
        super();
    }

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection)selection;
            if (structuredSelection.getFirstElement() instanceof IProductCmptStructureReference) {
                addLinkOnReference(event);
                return null;
            } else if (structuredSelection.getFirstElement() instanceof AbstractAssociationViewItem) {
                addLinksOnAssociation(event);
                return null;
            } else {
                throw new RuntimeException();
            }
        }
        return null;
    }

    private void addLinksOnAssociation(ExecutionEvent event) {
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        TypedSelection<AbstractAssociationViewItem> typedSelection = new TypedSelection<AbstractAssociationViewItem>(
                AbstractAssociationViewItem.class, selection);
        if (!typedSelection.isValid()) {
            return;
        }

        IEditorPart editor = HandlerUtil.getActiveEditor(event);
        if (!(editor instanceof ProductCmptEditor)) {
            return;
        }

        ProductCmptEditor productCmptEditor = (ProductCmptEditor)editor;
        IProductCmpt productCmpt = productCmptEditor.getProductCmpt();
        try {
            AbstractAssociationViewItem associationViewItem = typedSelection.getFirstElement();
            IProductCmptType productCmptType = productCmpt.findProductCmptType(productCmpt.getIpsProject());
            IProductCmptTypeAssociation association = (IProductCmptTypeAssociation)productCmptType.findAssociation(
                    associationViewItem.getAssociationName(), productCmpt.getIpsProject());

            IProductCmptType targetProductCmptType = association.findTargetProductCmptType(productCmpt.getIpsProject());
            // Possible target product component source files
            IIpsSrcFile[] ipsSrcFiles = productCmpt.getIpsProject().findAllProductCmptSrcFiles(targetProductCmptType,
                    true);

            List<IProductCmptLink> existingLinks = getExistingLinks(productCmptEditor, association);
            Set<IIpsSrcFile> possibleTargets = getSelectableTargetProductCmpts(ipsSrcFiles, existingLinks);
            final StaticContentSelectIpsObjectContext context = new StaticContentSelectIpsObjectContext();
            context.setElements(possibleTargets.toArray(new IIpsSrcFile[ipsSrcFiles.length]));
            final OpenIpsObjectSelectionDialog dialog = new OpenIpsObjectSelectionDialog(
                    HandlerUtil.getActiveShell(event), Messages.AddLinkAction_selectDialogTitle, context, true);
            int rc = dialog.open();
            if (rc == Window.OK && !dialog.getSelectedObjects().isEmpty()) {
                addLinksToGenerationOrProductCmpt((IProductCmptGeneration)productCmptEditor.getActiveGeneration(),
                        association, dialog.getSelectedObjects());
            }

        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    protected List<IProductCmptLink> getExistingLinks(ProductCmptEditor productCmptEditor,
            IProductCmptTypeAssociation association) {
        IProductCmptGeneration activeGeneration = (IProductCmptGeneration)productCmptEditor.getActiveGeneration();
        IProductCmptLinkContainer container = LinkCreatorUtil.getLinkContainerFor(activeGeneration, association);
        return container.getLinksAsList(association.getName());
    }

    /**
     * Compares possible target product component source files and existing links and returns the
     * different source files as result
     */
    private Set<IIpsSrcFile> getSelectableTargetProductCmpts(IIpsSrcFile[] ipsSrcFiles,
            List<IProductCmptLink> existingLinks) {
        Set<IIpsSrcFile> result = new LinkedHashSet<IIpsSrcFile>();

        for (int i = 0; i < ipsSrcFiles.length; i++) {
            boolean exists = false;
            for (IProductCmptLink link : existingLinks) {
                if ((ipsSrcFiles[i].getQualifiedNameType().getName()).equals(link.getTarget())) {
                    exists = true;
                }
            }
            if (!exists) {
                result.add(ipsSrcFiles[i]);
            }
        }
        return result;
    }

    private void addLinksToGenerationOrProductCmpt(final IProductCmptGeneration activeProductCmptGeneration,
            final IProductCmptTypeAssociation association,
            final List<IIpsElement> selectedIpsElements) {

        IpsUIPlugin.getDefault().runWorkspaceModification(new IWorkspaceRunnable() {
            @Override
            public void run(IProgressMonitor monitor) throws CoreException {
                for (IIpsElement element : selectedIpsElements) {
                    if (element instanceof IIpsSrcFile) {
                        IIpsSrcFile ipsSrcFile = (IIpsSrcFile)element;
                        IProductCmpt targetProductCmpt = (IProductCmpt)ipsSrcFile.getIpsObject();

                        LinkCreatorUtil util = new LinkCreatorUtil(false);
                        util.createLink(association, activeProductCmptGeneration, targetProductCmpt.getQualifiedName());
                    }
                }
            }
        });
    }

    private void addLinkOnReference(ExecutionEvent event) {
        LinkCreatorUtil linkCreator = new LinkCreatorUtil(true);
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        TypedSelection<IProductCmptStructureReference> typedSelection = new TypedSelection<IProductCmptStructureReference>(
                IProductCmptStructureReference.class, selection);
        if (!typedSelection.isValid()) {
            return;
        }
        IIpsProject ipsProject = null;
        IProductCmptStructureReference structureReference = typedSelection.getFirstElement();
        ipsProject = structureReference.getIpsProject();
        if (ipsProject != null) {
            List<IProductCmpt> selectedResults = selectProductCmpt(ipsProject, structureReference,
                    HandlerUtil.getActiveShell(event));
            linkCreator.createLinks(selectedResults, structureReference);
        }
    }

    /**
     * returns a list of product cmpts because the link creator requests a list
     */
    private List<IProductCmpt> selectProductCmpt(IIpsProject ipsProject,
            IProductCmptStructureReference linkTarget,
            Shell shell) {

        List<IProductCmpt> selectedResults = new ArrayList<IProductCmpt>();
        OpenIpsObjectSelectionDialog dialog = getSelectDialog(ipsProject, linkTarget, shell);

        if (dialog.open() == Window.OK) {
            List<IIpsElement> selectedResult = dialog.getSelectedObjects();
            for (IIpsElement selects : selectedResult) {
                if (selects instanceof IIpsSrcFile) {
                    IIpsObject selectedIpsObject = ((IIpsSrcFile)selects).getIpsObject();
                    if (selectedIpsObject instanceof IProductCmpt) {
                        IProductCmpt selectResultCmpt = (IProductCmpt)selectedIpsObject;
                        selectedResults.add(selectResultCmpt);
                    }
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
                Messages.AddLinkAction_selectDialogTitle, context, true);
        return dialog;
    }

    private static class LinkViewerFilter extends ViewerFilter {

        private final LinkCandidateFilter filter;

        public LinkViewerFilter(IProductCmptStructureReference linkTarget) {
            filter = new LinkCandidateFilter(linkTarget, IpsPlugin.getDefault().getIpsPreferences()
                    .isWorkingModeBrowse());
        }

        @Override
        public boolean select(Viewer viewer, Object parentElement, Object element) {
            if (element instanceof IIpsSrcFile) {
                IIpsSrcFile srcFile = (IIpsSrcFile)element;
                return filter.filter(srcFile);
            } else {
                return false;
            }
        }
    }
}
