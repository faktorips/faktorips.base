/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.wizards.deepcopy;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureTblUsageReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTypeAssociationReference;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;

/**
 * Listener to support special check-/uncheck-handling for CheckboxTreeViewer.
 * <p>
 * Applying this listener to a CheckboxTreeViewer you get the follwing behaviour: - Check a child
 * node which parent is not checked - the parent get checked, too. - Uncheck a node with checked
 * children - the children get unchecked, too.
 * <p>
 * This behaviour is slightly different to the one of the ContainerCheckedTreeViewer, which unchecks
 * the parent if no child is checked any more.
 * 
 * @author Thorsten Guenther
 */
public class CheckStateListener implements ICheckStateListener {

    private WizardPage page;

    /**
     * Creates a new CheckStateListener to handle the check state correctly.
     * 
     * @param page The reference and review page to update or null, if instantiated by any other
     *            class. If not null, this page is notified of changes in selection state.
     */
    public CheckStateListener(WizardPage page) {
        this.page = page;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkStateChanged(CheckStateChangedEvent event) {
        updateCheckState((CheckboxTreeViewer)event.getSource(), event.getElement(), event.getChecked());
    }

    /**
     * Updates the check state for the given treeViewer, stating at the given modified object.
     */
    public void updateCheckState(CheckboxTreeViewer treeViewer, Object modified, boolean checked) {
        Tree t = (Tree)treeViewer.getControl();
        TreeItem[] items = t.getItems();

        TreeItem toStartAt = find(items, modified);

        if (toStartAt == null) {
            return;
        }

        if (checked) {
            checkParents(toStartAt.getParentItem());
            checkRelationChildren(toStartAt);
        } else {
            uncheckChildren(toStartAt.getItems());
            uncheckRelationParent(toStartAt.getParentItem());
        }

        updateGrayState(items);

        if (page != null) {
            if (modified instanceof IProductCmptReference || modified instanceof IProductCmptStructureTblUsageReference) {
                // set page complete, implicit validate all components in tree,
                // because maybe the validate state of the parent objects has changed
                page.canFlipToNextPage();
            }
        }
    }

    private Object getTreeItemContent(TreeItem in) {
        if (in.getData() instanceof IProductCmptReference) {
            return ((IProductCmptReference)in.getData()).getProductCmpt();
        } else if (in.getData() instanceof IProductCmptTypeAssociationReference) {
            return ((IProductCmptTypeAssociationReference)in.getData()).getAssociation();
        }

        return null;
    }

    /**
     * If the given treeitem reprensents a relation, it is unchecked if all direct children are
     * unchecked.
     */
    private void uncheckRelationParent(TreeItem parent) {
        if (parent != null && getTreeItemContent(parent) instanceof IProductCmptTypeAssociation) {
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
     * If the given item represents a relation, all of its direct children are checked.
     */
    private void checkRelationChildren(TreeItem node) {
        Object treeItemContent = getTreeItemContent(node);
        if (treeItemContent instanceof IProductCmpt) {
            return;
        }

        TreeItem[] items = node.getItems();
        for (TreeItem item : items) {
            item.setChecked(true);
        }
    }

    /**
     * Updates the gray-state of the tree items. An item becomes gray if it is checked and at least
     * one of its children is unchecked.
     */
    private boolean updateGrayState(TreeItem[] items) {
        boolean gray = false;
        boolean unchecked = false;

        for (TreeItem item : items) {
            if (item.getChecked()) {
                boolean myGray = updateGrayState(item.getItems());
                item.setGrayed(myGray);
                gray = gray || myGray;
            } else {
                setNotGrayedRecursive(new TreeItem[] { item });
                unchecked = true;
            }
        }

        return gray || unchecked;
    }

    /**
     * Set all given TreeItems and all children non-grayed.
     */
    private void setNotGrayedRecursive(TreeItem[] items) {
        for (TreeItem item : items) {
            item.setGrayed(false);
            setNotGrayedRecursive(item.getItems());
        }
    }

    /**
     * Uncheck the given tree items and all its direct or indirect children.
     */
    private void uncheckChildren(TreeItem[] items) {
        for (TreeItem item : items) {
            item.setChecked(false);
            uncheckChildren(item.getItems());
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
     * Finds the first occurrence of the object <code>toFind</code> in the tree using the depth
     * first algorithm (first, check the childrens of the current node, then check its siblings).
     * <p>
     * To identify the treeitem, its data is compared using identity (Operator <code>==</code>), not
     * the equals-method.
     * 
     * @param toSearch The treeitems to search
     * @param toFind The object to find.
     * @return The found tree item or null, if none can be found.
     */
    private TreeItem find(TreeItem[] toSearch, Object toFind) {
        TreeItem found = null;
        for (int i = 0; i < toSearch.length && found == null; i++) {
            if (toSearch[i].getData() != null && toSearch[i].getData().equals(toFind)) {
                return toSearch[i];
            }
            found = find(toSearch[i].getItems(), toFind);
        }

        return found;
    }
}
