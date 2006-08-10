/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
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
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.ui.actions.OpenEditorAction;

/**
 * Doubleclicklistener for a TreeViewer that opens an editor for the selected/clicked Object.
 * The listener expands or collapses the tree view in case IPSPackageFragments,
 * IPSPackageFragmentRoots or IPSProjects are clicked.
 * 
 * @author Stefan Widmaier
 */
public class TreeViewerDoubleclickListener implements IDoubleClickListener {
	private TreeViewer tree;

	public TreeViewerDoubleclickListener(TreeViewer tree) {
		this.tree = tree;
	}
	
	public void doubleClick(DoubleClickEvent event) {

		OpenEditorAction action= new OpenEditorAction(tree);
		action.run();
		
		if (event.getSelection() instanceof StructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) event.getSelection();
			Object selectedObject = selection.getFirstElement();

			if (selectedObject instanceof IIpsPackageFragment
					|| selectedObject instanceof IIpsPackageFragmentRoot
					|| selectedObject instanceof IIpsProject
					|| selectedObject instanceof IFolder
					|| selectedObject instanceof IProject) {
				List list = Arrays.asList(tree.getVisibleExpandedElements());
				if (list.contains(selectedObject)) {
					tree.collapseToLevel(selectedObject, 1);
				} else {
					tree.expandToLevel(selectedObject, 1);
				}
			}
		}
	}
}
