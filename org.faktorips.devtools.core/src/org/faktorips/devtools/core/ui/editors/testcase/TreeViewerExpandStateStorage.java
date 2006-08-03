/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.testcase;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Class to store and restore the expand state of all items within a tree viewer.
 * 
 * @author Joerg Ortmann
 */
public class TreeViewerExpandStateStorage {
	// Stores the expanded items
	private ArrayList expandedItems = new ArrayList();
	
	// Tree viewer which will be analysed and restored
	private TreeViewer treeViewer;

	public TreeViewerExpandStateStorage(TreeViewer treeViewer) {
		this.treeViewer = treeViewer;
	}
	
	public void storeExpandedStatus(){
		expandedItems = new ArrayList();
		TreeItem childs[] = treeViewer.getTree().getItems();
		checkExpandedStatus(expandedItems, childs, "");
	}
	
	public void restoreExpandedStatus(){
		for (Iterator iter = expandedItems.iterator(); iter.hasNext();) {
			String itemPath = (String) iter.next();
			TreeItem childs[] = treeViewer.getTree().getItems();
			searchAndExpandInTree(itemPath, childs, "");
		}
	}
	
	private boolean searchAndExpandInTree(String itemPath, TreeItem childs[], String parent){
		for (int i = 0; i < childs.length; i++) {
			String pathOfChild = parent + "/" + childs[i].getText();
			if (itemPath.equals(pathOfChild)){
				childs[i].setExpanded(true);
				return true;
			}
			TreeItem subChilds[] = childs[i].getItems();
			if (searchAndExpandInTree(itemPath, subChilds, pathOfChild)){
				return true;
			}
		}
		return false;
	}
	
	private void checkExpandedStatus(ArrayList expandedItems, TreeItem childs[], String parent){
		for (int i = 0; i < childs.length; i++) {
			TreeItem item = childs[i];
			if (item.getExpanded()){
				String itemPath = parent + "/" + item.getText();
				expandedItems.add(itemPath);
				checkExpandedStatus(expandedItems, item.getItems(), itemPath);
			}
		}
	}
}
