/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.testcase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

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

    /** Stores the expanded items */
    private ArrayList<String> expandedItems = new ArrayList<>();

    /** Tree viewer which will be analyzed and restored */
    private TreeViewer treeViewer;

    /** contains all checked elements */
    private Object[] checkedElements;

    private String selectedItemPath;

    private TreeItem selectedTreeItem;

    public TreeViewerExpandStateStorage(TreeViewer treeViewer) {
        this.treeViewer = treeViewer;
    }

    public void storeExpandedStatus() {
        selectedItemPath = null;
        TreeItem[] selectedTreeItems = treeViewer.getTree().getSelection();
        if (selectedTreeItems.length > 0) {
            selectedTreeItem = selectedTreeItems[0];
        }
        expandedItems = new ArrayList<>();
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
        searchAndProcessInTree(itemPath, items, parent,
                treeItem -> treeViewer.setSelection(new StructuredSelection(treeItem.getData())));
    }

    private void searchAndExpandInTree(String itemPath, TreeItem[] items, String parent) {
        searchAndProcessInTree(itemPath, items, parent,
                treeItem -> treeViewer.setExpandedState(treeItem.getData(), true));
    }

    private void storeCheckedElements() {
        if (!(treeViewer instanceof CheckboxTreeViewer)) {
            return;
        }
        checkedElements = ((CheckboxTreeViewer)treeViewer).getCheckedElements();
    }

    @SuppressWarnings("deprecation")
    private void restoreCheckedStatus() {
        if (!(treeViewer instanceof CheckboxTreeViewer checkboxTreeViewer)) {
            return;
        }
        // 1. get all element
        checkboxTreeViewer.setAllChecked(true);
        List<Object> elementsToUncheck = new ArrayList<>();
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
            TreeItem[] childs,
            String parent,
            Consumer<TreeItem> strategy) {

        for (TreeItem child : childs) {
            if (child.isDisposed()) {
                continue;
            }
            String pathOfChild = getItemPath(parent, child);
            if (pathOfChild != null && !pathOfChild.startsWith(parent)) {
                return false;
            }

            if (itemPath.equals(pathOfChild)) {
                strategy.accept(child);
                return true;
            }
            TreeItem[] subChilds = child.getItems();
            if (searchAndProcessInTree(itemPath, subChilds, pathOfChild, strategy)) {
                return true;
            }
        }
        return false;
    }

    private void checkExpandedStatus(ArrayList<String> expandedItems, TreeItem[] childs, String parent) {
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
        return parent + "//" + System.identityHashCode(obj); //$NON-NLS-1$
    }
}
