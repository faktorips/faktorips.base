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
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.StructuredSelection;
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

    // contains all checked elements
    private Object[] checkedElements;

    private String selectedItemPath;

    private TreeItem selectedTreeItem;

    private interface TreeItemActionStrategy {
        public void execute(TreeItem treeItem);
    }

    private class SelectStrategy implements TreeItemActionStrategy {
        @Override
        public void execute(TreeItem treeItem) {
            treeViewer.setSelection(new StructuredSelection(treeItem.getData()));
        }
    }

    private class ExpandStrategy implements TreeItemActionStrategy {
        @Override
        public void execute(TreeItem treeItem) {
            treeViewer.setExpandedState(treeItem.getData(), true);
        }
    }

    public TreeViewerExpandStateStorage(TreeViewer treeViewer) {
        this.treeViewer = treeViewer;
    }

    public void storeExpandedStatus() {
        selectedItemPath = null;
        TreeItem[] selectedTreeItems = treeViewer.getTree().getSelection();
        if (selectedTreeItems.length > 0) {
            selectedTreeItem = selectedTreeItems[0];
        }
        expandedItems = new ArrayList<String>();
        storeCheckedElements();
        checkExpandedStatus(expandedItems, treeViewer.getTree().getItems(), ""); //$NON-NLS-1$
        if (treeViewer instanceof CheckboxTreeViewer) {
            checkedElements = ((CheckboxTreeViewer)treeViewer).getCheckedElements();
        }
    }

    public void restoreExpandedStatus() {
        treeViewer.collapseAll();
        for (String itemPath : expandedItems) {
            searchAndExpandInTree(itemPath, treeViewer.getTree().getItems(), ""); //$NON-NLS-1$
        }

        restoreCheckedStatus();

        if (selectedItemPath != null) {
            searchAndSelectInTree(selectedItemPath, treeViewer.getTree().getItems(), ""); //$NON-NLS-1$
        }
    }

    private void searchAndSelectInTree(String itemPath, TreeItem[] items, String parent) {
        searchAndProcessInTree(itemPath, items, parent, new SelectStrategy());
    }

    private void searchAndExpandInTree(String itemPath, TreeItem[] items, String parent) {
        searchAndProcessInTree(itemPath, items, parent, new ExpandStrategy());
    }

    private void storeCheckedElements() {
        if (!(treeViewer instanceof CheckboxTreeViewer)) {
            return;
        }
        checkedElements = ((CheckboxTreeViewer)treeViewer).getCheckedElements();
    }

    private void restoreCheckedStatus() {
        if (!(treeViewer instanceof CheckboxTreeViewer)) {
            return;
        }
        CheckboxTreeViewer checkboxTreeViewer = (CheckboxTreeViewer)treeViewer;

        // 1. get all element
        checkboxTreeViewer.setAllChecked(true);
        List<Object> elementsToUncheck = new ArrayList<Object>();
        elementsToUncheck.addAll(Arrays.asList(checkboxTreeViewer.getCheckedElements()));
        // 2. remove all previous checked elements
        for (Object checkedElement : checkedElements) {
            elementsToUncheck.remove(checkedElement);
        }
        // 3. the result contains all unchecked elements
        for (Object object : elementsToUncheck) {
            if (!(object instanceof TestCaseTypeAssociation)) {
                // don't change check state of TestCaseTypeAssociation
                // otherwise all child's are also unchecked
                checkboxTreeViewer.setChecked(object, false);
            }
        }
    }

    private boolean searchAndProcessInTree(String itemPath,
            TreeItem childs[],
            String parent,
            TreeItemActionStrategy strategy) {
        for (TreeItem child : childs) {
            if (child.isDisposed()) {
                continue;
            }
            String pathOfChild = getItemPath(parent, child);
            if (pathOfChild != null && !pathOfChild.startsWith(parent)) {
                return false;
            }

            if (itemPath.equals(pathOfChild)) {
                strategy.execute(child);
                return true;
            }
            TreeItem subChilds[] = child.getItems();
            if (searchAndProcessInTree(itemPath, subChilds, pathOfChild, strategy)) {
                return true;
            }
        }
        return false;
    }

    private void checkExpandedStatus(ArrayList<String> expandedItems, TreeItem childs[], String parent) {
        for (TreeItem item : childs) {
            String itemPath = getItemPath(parent, item);
            if (item.isDisposed() || itemPath == null) {
                continue;
            }
            if (item.getExpanded()) {
                expandedItems.add(itemPath);
                checkExpandedStatus(expandedItems, item.getItems(), itemPath);
            }
            if (item == selectedTreeItem) {
                selectedItemPath = itemPath;
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
