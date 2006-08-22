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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.ui.forms.IpsSection;

/**
 * This action is designed for use with views and editors that contain a <code>TreeViewer</code>
 * as selectionprovider.  <p>
 * IpsDeleteAction deletes all selected IpsObjectPartContainers in the object-model without 
 * saving the changes to sourcefiles. After deletion the topmost sibling (if any), or the topmost
 * parent of the deleted items are selected in the tree.  <p>
 * This Action is primarily used in Editors, thus no changes to objects are saved (to the filesystem). 
 * See <code>IpsDeleteAndSaveAction</code> for an action that saves changes after deletion.
 * @see org.faktorips.devtools.core.ui.actions.IpsDeleteAndSaveAction
 * TODO Refactoring: Implement IpsAction; make action independant of GUI/Tree.
 * @author Thorsten Guenther
 * @author Stefan Widmaier
 */

public abstract class IpsDeleteAction extends Action implements ISelectionChangedListener{
    
    /**
     * The selectionProvider given at instanciation. This is used to retrieve the currently
     * selected objects when the run() method is called.
     */
	protected ISelectionProvider selectionProvider;
	
	/**
	 * Creates a DeleteAction with the given SelectionProvider. This SelectionProvider
	 * is used to retrieve the selected objects that are deleted from the object model
	 * while executing the run() method.
     * <p>
     * The given <code>ISelectionProvider</code> must not be <code>null</code>.
	 */
	public IpsDeleteAction(ISelectionProvider provider){
		selectionProvider= provider;
        provider.addSelectionChangedListener(this);
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
		
		Tree tree= ((TreeViewer)selectionProvider).getTree();
		if (tree.isDisposed()) {
			IpsPlugin.log(new IpsStatus("Tree disposed!")); //$NON-NLS-1$
			return;
		}
		// store the path to the item to select after all selected items are deleted.
		Indexer indexer= createIndexer(tree);
		
		// delete selected Objects if possible
        deleteSelection((IStructuredSelection)selection);
	    
	    // select the item in the tree whose path was stored before.
	    applyIndexer(tree, indexer);
	    
    }
	
    protected abstract void deleteSelection(IStructuredSelection selection);
    
    protected abstract void setEnabledState(ISelection selection);
    
    

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
	 * after deletion to select the parent or next sibling (if any) in the tree.
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
    
    public void selectionChanged(SelectionChangedEvent event) {
        setEnabledState(event.getSelection());
    }
    
}
