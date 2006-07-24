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

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.pctype.Attribute;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeRelation;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.ui.forms.IpsSection;

/**
 * This action is designed to be used with views and editors that contain a <code>TreeViewer</code>
 * and use it as a selectionprovider for this action.  </b>
 * IpsDeleteAction deletes all selected IpsObjectPartContainers in the ObjectModel without 
 * saving the changes. After deletion the topmost sibling (if any), or the topmost parent 
 * of the deleted items are selected in the tree.  </b>
 * This Action is primarily used in Editors, thus no changes to objects are saved (to the filesystem). 
 * See <code>IpsDeleteAndSaveAction</code> for an action that saves changes after deleting.
 * @see org.faktorips.devtools.core.ui.actions.IpsDeleteAndSaveAction
 * @author Thorsten Guenther
 * @author Stefan Widmaier
 */

public class IpsDeleteAction extends Action {
	private ISelectionProvider selectionProvider;
	
	/**
	 * Constructs a DeleteAction with the given SelectionProvider. This SelectionProvider
	 * is used to retrieve the selected objects that are deleted from the object model
	 * while executing the run() method.
	 */
	public IpsDeleteAction(ISelectionProvider provider){
		selectionProvider= provider;
	}
	
    /**
	 * {@inheritDoc}
	 */
	public void runWithEvent(Event event) {
		if(selectionProvider==null){
			return;
		}
		if(!(selectionProvider instanceof TreeViewer)){
			return;
		}
		ISelection selection= selectionProvider.getSelection();
		if(!(selection instanceof IStructuredSelection)){
			return;
		}
		Object[] items= ((IStructuredSelection) selection).toArray();
		
		Tree tree= ((TreeViewer)selectionProvider).getTree();
		if (tree.isDisposed()) {
			IpsPlugin.log(new IpsStatus("Tree disposed!")); //$NON-NLS-1$
			return;
		}
		// store the path to the item to select after all selected items are deleted.
		Indexer indexer= createIndexer(tree);
		
		// delete selected Objects
	    deleteResourceForSelection(items);
	    
	    // select the item in the tree whose path was stored before.
	    applyIndexer(tree, indexer);
	    
    }
	
    /**
     * Deletes the given objects from the object model. 
     * As a quick'n'dirty fix this implementation ignores <code>Attribute</code> objects 
     * to disable the deletion of attributes in the ModelExplorer.
     * TODO IpsDeleteAction should be used consistently in all editors and viewparts. 
     * On the one hand this means the editors (their Pages and Sections) need to be connected 
     * to the SelectionService, on the other hand problems with multiple instances of IpsObjects
     * (data synchronization) must be solved. <p/>
     * See Flyspray entry FS#330.
     */
	private void deleteResourceForSelection(Object[] items){
    	for (int i = 0; i < items.length; i++) {
            if (!(items[i] instanceof IProductCmptTypeRelation)) {
            	if (items[i] instanceof IIpsObjectPart) {
            		// ignore Attributes
            		if(!(items[i] instanceof Attribute)){
            			((IIpsObjectPart)items[i]).delete();
            		}
            	} else if (items[i] instanceof IIpsElement) {
            		IResource res;
            		if (items[i] instanceof IProductCmpt || items[i] instanceof ITableContents) {
            			res = ((IIpsObject)items[i]).getEnclosingResource();
            		}else if(items[i] instanceof IIpsObject){
            			res = ((IIpsObject)items[i]).getIpsSrcFile().getCorrespondingFile();
            		}else {
            			res = ((IIpsElement)items[i]).getCorrespondingResource();
            		}
            		if (res != null) {
            			try {
            				res.delete(true, null);
            			} catch (CoreException e) {
            				IpsPlugin.logAndShowErrorDialog(e);
            			}
            		}
            	}
            }
        }
	}
	private Indexer createIndexer(Tree tree){
		TreeItem[] items = tree.getSelection();
		Indexer start= null;
		if (items.length >= 1) {
			TreeItem parent = items[0].getParentItem();
			TreeItem getIndexFor = items[0];
			Indexer previous = null;
			while (parent != null) {
				previous = new Indexer(parent.indexOf(getIndexFor), previous);
				getIndexFor = parent;
				parent = parent.getParentItem();
			}
	    	start = new Indexer(tree.indexOf(getIndexFor), previous);
		}
		return start;
	}
	
	private void applyIndexer(Tree tree, Indexer indexer){
		tree.deselectAll();
		IpsSection section = getIpsSection(tree);
		if (section != null) {
			section.refresh();
		}
	
	    if (indexer != null)  {
	    	TreeItem toSetAt = tree.getItem(indexer.index);
	    	TreeItem item = toSetAt;
	    	Indexer next = indexer.next;
	    	int index = 0;
	    	while (next != null) {
	    		index = next.index;
	    		try {
	    			toSetAt = toSetAt.getItem(index);
	    		} catch (IllegalArgumentException e) {
	    			if (index > 0 && index -1 < toSetAt.getItemCount()) {
	    				toSetAt = toSetAt.getItem(index - 1);
	    			} else {
	    				toSetAt = null;
	    			}
	    			break;
	    		}
	    		next = next.next;
	    		
	    		if (next == null && index > 0) {
	    			toSetAt = item.getItem(index);
	    		}
	    		
	    		item = toSetAt;
	    	}
	    	
	    	if (toSetAt != null) {
	    		tree.setSelection(new TreeItem[] {toSetAt});
	    	}
	    }
	}
	
	/**
	 * Class used to store the path to the topmost deleted item. This path is used
	 * later to select the parent or next sibling (if any) in the tree.
	 * 
	 */
    private class Indexer {
    	public int index;
    	public Indexer next;
    	
    	public Indexer(int index, Indexer next) {
    		this.index = index;
    		this.next = next;
    	}
    }
    
    private IpsSection getIpsSection(Composite child) {
    	if (child == null) {
    		return null;
    	}
    	if (child instanceof IpsSection) {
    		return (IpsSection)child;
    	}
    	return getIpsSection(child.getParent());
    		
    }
    
}
