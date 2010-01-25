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
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.CheckboxTreeViewer;
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

    // contains all checked elements
    private Object[] checkedElements;

    public TreeViewerExpandStateStorage(TreeViewer treeViewer) {
        this.treeViewer = treeViewer;
    }

    public void storeExpandedStatus() {
        selection = treeViewer.getSelection();
        expandedItems = new ArrayList<String>();
        storeCheckedElements();
        checkExpandedStatus(expandedItems, treeViewer.getTree().getItems(), ""); //$NON-NLS-1$
        if (treeViewer instanceof CheckboxTreeViewer) {
            checkedElements = ((CheckboxTreeViewer)treeViewer).getCheckedElements();
        }
    }

    public void restoreExpandedStatus() {
        treeViewer.collapseAll();
        for (Iterator<String> iter = expandedItems.iterator(); iter.hasNext();) {
            String itemPath = iter.next();
            TreeItem childs[] = treeViewer.getTree().getItems();
            searchAndExpandInTree(itemPath, childs, ""); //$NON-NLS-1$
        }

        restoreCheckedStatus();

        if (selection.isEmpty()) {
            return;
        }
        treeViewer.setSelection(selection);
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
        for (int i = 0; i < checkedElements.length; i++) {
            elementsToUncheck.remove(checkedElements[i]);
        }
        // 3. the result contains all unchecked elements
        for (Iterator<Object> iterator = elementsToUncheck.iterator(); iterator.hasNext();) {
            Object object = iterator.next();
            if (!(object instanceof TestCaseTypeAssociation)) {
                // don't change check state of TestCaseTypeAssociation
                // otherwise all child's are also unchecked
                checkboxTreeViewer.setChecked(object, false);
            }
        }
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
