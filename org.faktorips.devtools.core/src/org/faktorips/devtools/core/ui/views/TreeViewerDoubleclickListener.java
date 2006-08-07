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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.PartInitException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptReference;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;
import org.faktorips.devtools.core.model.product.IProductCmptTypeRelationReference;

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
			else if (selectedObject instanceof IIpsElement) {
				openEditor((IIpsElement) selectedObject);
			}
			// for usage with StructureExplorer: open ProductComponents contained in StructureNodes
            else if (selectedObject instanceof IProductCmptReference) {
            	openEditor(((IProductCmptReference)selectedObject).getProductCmpt());
            }
            else if (selectedObject instanceof IProductCmptTypeRelationReference) {
            	openEditor(((IProductCmptTypeRelationReference)selectedObject).getRelation());
            }
            else if (selectedObject instanceof IProductCmptRelation) {
            	try {
            		IProductCmptRelation rel = (IProductCmptRelation)selectedObject;
					openEditor(rel.getIpsProject().findIpsObject(IpsObjectType.PRODUCT_CMPT, rel.getTarget()));
				} catch (CoreException e) {
					IpsPlugin.log(e);
				}
            }
			// for usage with Search: open the result of a reference-search which is an object-array
            else if (selectedObject instanceof Object[]) {
            	Object[] array = (Object[]) selectedObject;
            	if (array.length > 1 && array[0] instanceof IProductCmpt) {
            		openEditor((IProductCmpt)array[0]);
            	}
            	else if (array.length >= 1 && array[0] instanceof IPolicyCmptType) {
            		openEditor((IPolicyCmptType)array[0]);
            	}
            }
		}
	}

	protected void openEditor(IIpsElement e) {
		IIpsObject ipsObject= null;
		if(e instanceof IIpsObjectPart){
			ipsObject= ((IIpsObjectPart)e).getIpsObject();
		}else if(e instanceof IIpsObject){
			ipsObject= (IIpsObject)e;
		}
		if(ipsObject != null){
			try {
				IpsPlugin.getDefault().openEditor(ipsObject.getIpsSrcFile());
			} catch (PartInitException e1) {
				IpsPlugin.logAndShowErrorDialog(e1);
			}
		}
	}
}
