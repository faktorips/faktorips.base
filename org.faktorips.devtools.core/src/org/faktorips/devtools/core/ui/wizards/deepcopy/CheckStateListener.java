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

package org.faktorips.devtools.core.ui.wizards.deepcopy;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeRelation;

/**
 * Listener to support special check-/uncheck-handling for CheckboxTreeViewer.
 * <p>
 * Applying this listener to a CheckboxTreeViewer you get the follwing behaviour:
 * - Check a child node which parent is not checked - the parent get checked, too.
 * - Uncheck a node with checked children - the children get unchecked, too.
 * <p>
 * This behaviour is slightly different to the one of the ContainerCheckedTreeViewer, 
 * which unchecks the parent if no child is checked any more. 
 * 
 * @author Thorsten Guenther
 */
public class CheckStateListener implements ICheckStateListener {

	private ReferenceAndPreviewPage page;
	
	/**
	 * Creates a new CheckStateListener to handle the check state correctly.
	 * 
	 * @param page The reference and review page to update or null, if instantiated
	 * by any other class. If not null, this page is notified of changes in selection 
	 * state.
	 */
	public CheckStateListener(ReferenceAndPreviewPage page) {
		this.page = page;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void checkStateChanged(CheckStateChangedEvent event) {
		
		CheckboxTreeViewer treeViewer = (CheckboxTreeViewer)event.getSource();
		Tree t = (Tree)treeViewer.getControl();
		TreeItem[] items = t.getItems();
		Object modified = event.getElement();
		
		TreeItem toStartAt = find(items, modified);
		
		if (toStartAt == null) {
			return;
		}
		
		if (event.getChecked()) {
			checkParents(toStartAt.getParentItem());
			checkRelationChildren(toStartAt);
		}
		else {
			uncheckChildren(toStartAt.getItems());
			uncheckRelationParent(toStartAt.getParentItem());
		}
		
		updateGrayState(items);
		
		if (page != null) {
			page.setPageComplete();
		}
	}

	/**
	 * If the given treeitem reprensents a relation, it is unchecked if all
	 * direct children are unchecked.
	 */
	private void uncheckRelationParent(TreeItem parent) {
		if (parent != null && parent.getData() instanceof IProductCmptTypeRelation) {
			TreeItem[] children = parent.getItems();
			boolean checkedChild = false;
			for (int i = 0; i < children.length && !checkedChild; i++) {
				checkedChild = children[i].getChecked();
			}
			
			if (!checkedChild) {
				parent.setChecked(false);
			}
		}
	}

	/**
	 * If the given item represents a relation, all of its direct children 
	 * are checked.
	 */
	private void checkRelationChildren(TreeItem node) {
		if (!(node.getData() instanceof IProductCmptTypeRelation)) {
			return;
		}
		
		TreeItem[] items = node.getItems();
		for (int i = 0; i < items.length; i++) {
			items[i].setChecked(true);
		}
	}

	/**
	 * Updates the gray-state of the tree items. An item becomes gray if it is
	 * checked and at least one of its children is unchecked.
	 */
	private boolean updateGrayState(TreeItem[] items) {
		boolean gray = false;
		boolean unchecked = false;
		

		for (int i = 0; i < items.length; i++) {
			if (items[i].getChecked()) {
				boolean myGray = updateGrayState(items[i].getItems());
 				items[i].setGrayed(myGray);
 				gray = gray || myGray;
			}
			else {
				items[i].setGrayed(false);
				unchecked = true;
			}
		}

		return gray || unchecked;
	}

	/**
	 * Uncheck the given tree items and all its direct or indirect children.
	 */
	private void uncheckChildren(TreeItem[] items) {
		for (int i = 0; i < items.length; i++) {
			items[i].setChecked(false);
			uncheckChildren(items[i].getItems());
		}
	}

	/**
	 * Checks the given tree item and all its parents.
	 */
	private void checkParents(TreeItem item) {
		if (item == null) {
			return;
		}
		item.setChecked(true);
		checkParents(item.getParentItem());
	}
	
	/**
	 * Finds the first occurence of the object <code>toFind</code> in the tree using
	 * the depth first algorithm (first, check the childrens of the current node, then 
	 * check its siblings).
	 * <p>
	 * To identify the treeitem, its data is compared using identity (Operator <code>==</code>),
	 * not the equals-method.
	 * 
	 * @param toSearch The treeitems to search
	 * @param toFind The object to find.
	 * @return The found tree item or null, if none can be found.
	 */
	private TreeItem find(TreeItem[] toSearch, Object toFind) {
		TreeItem found = null;
		for (int i = 0; i < toSearch.length && found == null; i++) {
			if (toSearch[i].getData() == toFind) {
				return toSearch[i];
			}
			found = find(toSearch[i].getItems(), toFind);
		}
		
		return found;
	}
}

