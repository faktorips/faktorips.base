/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IEditorPart;
import org.faktorips.devtools.core.ui.actions.OpenEditorAction;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * Doubleclicklistener for a TreeViewer that opens an editor for the selected/clicked Object. The
 * listener expands or collapses the tree view in case IPSPackageFragments, IPSPackageFragmentRoots
 * or IPSProjects are clicked.
 * 
 * @author Stefan Widmaier
 */
public class TreeViewerDoubleclickListener implements IDoubleClickListener {
    private TreeViewer tree;

    public TreeViewerDoubleclickListener(TreeViewer tree) {
        this.tree = tree;
    }

    @Override
    public void doubleClick(DoubleClickEvent event) {
        openEditorsForSelection();

        collapsOrExpandTree(event);
    }

    protected void collapsOrExpandTree(DoubleClickEvent event) {
        if (event.getSelection() instanceof StructuredSelection) {
            IStructuredSelection selection = (IStructuredSelection)event.getSelection();
            Object selectedObject = selection.getFirstElement();

            if (selectedObject instanceof IIpsPackageFragment || selectedObject instanceof IIpsPackageFragmentRoot
                    || selectedObject instanceof IIpsProject || selectedObject instanceof IFolder
                    || selectedObject instanceof IProject) {
                List<Object> list = Arrays.asList(tree.getVisibleExpandedElements());
                if (list.contains(selectedObject)) {
                    tree.collapseToLevel(selectedObject, 1);
                } else {
                    tree.expandToLevel(selectedObject, 1);
                }
            }
        }
    }

    protected IEditorPart openEditorsForSelection() {
        OpenEditorAction action = new OpenEditorAction(tree);
        return action.openEditor();
    }
}
