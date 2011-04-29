/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.views.productstructureexplorer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.dialogs.OpenIpsObjectSelectionDialog;
import org.faktorips.devtools.core.ui.dialogs.SingleTypeSelectIpsObjectContext;
import org.faktorips.devtools.core.ui.util.LinkCreatorUtil;

/**
 * Add a new link in the product structure explorer
 * 
 * @author dirmeier
 */
public class AddLinkAction extends Action {

    private final TreeViewer treeViewer;

    private static LinkCreatorUtil linkCreator;

    public AddLinkAction(TreeViewer tree) {
        super(Messages.AddLinkAction_add, IpsUIPlugin.getImageHandling().createImageDescriptor("Add.gif")); //$NON-NLS-1$
        treeViewer = tree;
        linkCreator = new LinkCreatorUtil(true);
    }

    @Override
    public void run() {
        Object targetObject = getTargetObject(treeViewer);
        if (targetObject == null) {
            return;
        }
        try {
            IIpsProject ipsProject = null;
            if (targetObject instanceof IProductCmptStructureReference) {
                IProductCmptStructureReference structureReference = (IProductCmptStructureReference)targetObject;
                ipsProject = structureReference.getWrappedIpsObject().getIpsProject();
                if (ipsProject != null) {
                    List<IProductCmpt> selectedResults = selectProductCmpt(ipsProject, structureReference);
                    linkCreator.createLinks(selectedResults, structureReference);
                }
            }
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
    }

    /**
     * returns a list of product cmpts because the link creator requests a list
     */
    private List<IProductCmpt> selectProductCmpt(IIpsProject ipsProject, IProductCmptStructureReference linkTarget)
            throws CoreException {

        List<IProductCmpt> selectedResults = new ArrayList<IProductCmpt>();
        OpenIpsObjectSelectionDialog dialog = getSelectDialog(ipsProject, linkTarget);
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
            IProductCmptStructureReference linkTarget) {
        SingleTypeSelectIpsObjectContext context = new SingleTypeSelectIpsObjectContext(ipsProject,
                IpsObjectType.PRODUCT_CMPT, new LinkViewerFilter(linkTarget));
        OpenIpsObjectSelectionDialog dialog = new OpenIpsObjectSelectionDialog(treeViewer.getControl().getShell(),
                Messages.AddLinkAction_selectDialogTitle, context);
        return dialog;
    }

    private Object getTargetObject(Viewer viewer) {
        if (viewer.getSelection() instanceof IStructuredSelection) {
            IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
            return selection.getFirstElement();
        }
        return null;
    }

    private static class LinkViewerFilter extends ViewerFilter {

        private final IProductCmptStructureReference linkTarget;

        public LinkViewerFilter(IProductCmptStructureReference linkTarget) {
            this.linkTarget = linkTarget;
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
