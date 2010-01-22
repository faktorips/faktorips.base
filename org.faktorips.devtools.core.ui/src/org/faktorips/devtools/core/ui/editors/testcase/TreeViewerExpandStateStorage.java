/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.testcase;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Class to store and restore the expand state of all items within a tree viewer.
 * 
 * @author Joerg Ortmann
 */
public class TreeViewerExpandStateStorage {
    // Stores the expanded items
    private ArrayList<String> expandedItems = new ArrayList<String>();

    // Tree viewer which will be analyzed and restored
    private TreeViewer treeViewer;

    // Contains the currently selected tree item
    private ISelection selection;

    public TreeViewerExpandStateStorage(TreeViewer treeViewer) {
        this.treeViewer = treeViewer;
    }

    public void storeExpandedStatus() {
        selection = treeViewer.getSelection();
        expandedItems = new ArrayList<String>();
        TreeItem childs[] = treeViewer.getTree().getItems();
        checkExpandedStatus(expandedItems, childs, ""); //$NON-NLS-1$
    }

    public void restoreExpandedStatus() {
        treeViewer.collapseAll();
        for (Iterator<String> iter = expandedItems.iterator(); iter.hasNext();) {
            String itemPath = iter.next();
            TreeItem childs[] = treeViewer.getTree().getItems();
            searchAndExpandInTree(itemPath, childs, ""); //$NON-NLS-1$
        }
        if (selection.isEmpty()) {
            return;
        }
        treeViewer.setSelection(selection);
    }

    private boolean searchAndExpandInTree(String itemPath, TreeItem childs[], String parent) {
        for (int i = 0; i < childs.length; i++) {
            if (childs[i].isDisposed()) {
                continue;
            }
            String pathOfChild = getItemPath(parent, childs[i]);
            if (pathOfChild != null && !pathOfChild.startsWith(parent)) {
                return false;
            }

            if (itemPath.equals(pathOfChild)) {
                treeViewer.setExpandedState(childs[i].getData(), true);
                return true;
            }
            TreeItem subChilds[] = childs[i].getItems();
            if (searchAndExpandInTree(itemPath, subChilds, pathOfChild)) {
                return true;
            }
        }
        return false;
    }

    private void checkExpandedStatus(ArrayList<String> expandedItems, TreeItem childs[], String parent) {
        for (int i = 0; i < childs.length; i++) {
            TreeItem item = childs[i];
            String itemPath = getItemPath(parent, item);
            if (item.isDisposed() || itemPath == null) {
                continue;
            }
            if (item.getExpanded()) {
                expandedItems.add(itemPath);
                checkExpandedStatus(expandedItems, item.getItems(), itemPath);
            }
        }
    }

    private String getItemPath(String parent, TreeItem item) {
        if (item.getData() == null) {
            return null;
        }
        Object obj = item.getData();
        if (obj instanceof TestCaseTypeAssociation) {
            // special case for presentation object,
            // use always the model object to identify the path
            obj = ((TestCaseTypeAssociation)obj).getTestParameter();
        }
        return parent + "//" + System.identityHashCode(obj);
    }
}
