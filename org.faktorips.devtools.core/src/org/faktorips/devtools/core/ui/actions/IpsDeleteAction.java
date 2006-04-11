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

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeRelation;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;


public class IpsDeleteAction extends IpsAction {
	TreeViewer source;
	
    public IpsDeleteAction(TreeViewer selectionProvider) {
        super(selectionProvider);
    	source = selectionProvider;
    }

    public void run(IStructuredSelection selection) {
    	Tree tree = source.getTree();
    	TreeItem[] items = tree.getSelection();
    	Indexer start = null;
    	
    	// store the path to the item to select after all selected items are deleted.
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

    	
        List selectedObjects = selection.toList();

        for (Iterator iter = selectedObjects.iterator(); iter.hasNext();) {
            Object selected = iter.next();

            if (!(selected instanceof IProductCmptTypeRelation)) {
            	if (selected instanceof IIpsObjectPart) {
            		((IIpsObjectPart)selected).delete();
            	} else if (selected instanceof IIpsElement) {
            		IResource res;
            		if (selected instanceof IProductCmpt || selected instanceof ITableContents) {
            			res = ((IIpsObject)selected).getEnclosingResource();
            		} else {
            			res = ((IIpsElement)selected).getCorrespondingResource();
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

        // select the item the path to was stored before.
        if (start != null)  {
        	TreeItem toSetAt = tree.getItem(start.index);
        	TreeItem item = toSetAt;
        	Indexer next = start.next;
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
        			toSetAt = item.getItem(index -1);
        		}
        		
        		item = toSetAt;
        	}
        	
        	if (toSetAt != null) {
        		tree.setSelection(new TreeItem[] {toSetAt});
        	}
        }
    }
    
    private class Indexer {
    	public int index;
    	public Indexer next;
    	
    	public Indexer(int index, Indexer next) {
    		this.index = index;
    		this.next = next;
    	}
    }
}
