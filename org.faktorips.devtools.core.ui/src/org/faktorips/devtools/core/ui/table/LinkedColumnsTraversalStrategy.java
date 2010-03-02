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

package org.faktorips.devtools.core.ui.table;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.TraverseEvent;

/**
 * This class is designed for tables/trees with multiple editable columns (and non-editable
 * columns). This {@link LinkedColumnsTraversalStrategy} holds references to
 * {@link LinkedColumnsTraversalStrategy}s of all other editable columns. Using this
 * {@link TraversalStrategy} will ensure that all editable cells can be reached when traversing -
 * the user can jump from one editable column to another even if there is a locked column in
 * between.
 * <p/>
 * Again this class (as {@link AbstractPermanentTraversalStrategy}) is designed to be used with a
 * {@link EditingSupport} that holds one instance of this class during its lifetime.
 * 
 * <p/>
 * This {@link TraversalStrategy} supports two kinds of traversal in the ColumnViewer:
 * <ul>
 * <li>Column-Traversal(Tab,Shift+Tab) will jump to the next cell in the same line, or the first
 * cell in the next line (reading order) et vice versa.</li>
 * <li>Row-Traversal(Enter/Down,Up) will jump to the next editable cell strictly in the same column.
 * It will skip rows that are not editable for the given column even if there are editable cells in
 * the row (e.g. other columns that would be reached with Column-Traversal). Et vice versa.</li>
 * </ul>
 * 
 * @author Stefan Widmaier
 */
public abstract class LinkedColumnsTraversalStrategy extends AbstractPermanentTraversalStrategy {

    private LinkedColumnsTraversalStrategy follower = null;
    private LinkedColumnsTraversalStrategy predecessor = null;

    private ColumnViewer viewer;

    public LinkedColumnsTraversalStrategy(ColumnViewer viewer, EditingSupport editingSupport) {
        super(editingSupport);
        this.viewer = viewer;
    }

    public void keyTraversed(TraverseEvent e) {
        if (e.detail == SWT.TRAVERSE_ESCAPE) {
            getCurrentCellEditor().deactivate();
            e.doit = false;
        } else if (e.detail == SWT.TRAVERSE_RETURN) {
            editNextRow();
            e.doit = false;
        } else if (e.detail == SWT.TRAVERSE_TAB_NEXT) {
            editNextColumn();
            e.doit = false;
        } else if (e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
            editPreviousColumn();
            e.doit = false;
        }
    }

    public void keyPressed(KeyEvent e) {
        // FS#1585: if this cell editor is a ComboCellEditor then the arrow down and up
        // feature to create or delete rows are not supported, because otherwise the
        // selection of a new value inside the drop down doesn't work correctly
        if (getCurrentCellEditor() instanceof ComboCellEditor) {
            return;
        }
        if (e.keyCode == SWT.ARROW_DOWN) {
            editNextRow();
            e.doit = false;
        } else if (e.keyCode == SWT.ARROW_UP) {
            editPreviousRow();
            e.doit = false;
        }
    }

    private void editNextColumn() {
        editNextColumnFor(getCurrentViewItem());
    }

    private void editPreviousColumn() {
        editPreviousColumnFor(getCurrentViewItem());
    }

    private void editNextColumnFor(Object currentViewItem) {
        if (hasNextColumnTraversalStrategy()) {
            getNextColumnTraversalStrategy().editCellOrColumnFollowerFor(currentViewItem);
        } else {
            getFirstColumnTraversalStrategy().editCellOrColumnFollowerFor(getNextVisibleViewItem(currentViewItem));
        }
    }

    private void editPreviousColumnFor(Object currentViewItem) {
        if (hasPreviousColumnTraversalStrategy()) {
            getPreviousColumnTraversalStrategy().editCellOrColumnPredecessorFor(currentViewItem);
        } else {
            getLastColumnTraversalStrategy()
                    .editCellOrColumnPredecessorFor(getPreviousVisibleViewItem(currentViewItem));
        }
    }

    private void editCellOrColumnFollowerFor(Object currentViewItem) {
        if (currentViewItem == null) {
            return;
        }
        if (canEdit(currentViewItem)) {
            viewer.editElement(currentViewItem, getColumnIndex());
        } else {
            editNextColumnFor(currentViewItem);
        }
    }

    private void editCellOrColumnPredecessorFor(Object currentViewItem) {
        if (currentViewItem == null) {
            return;
        }
        if (canEdit(currentViewItem)) {
            viewer.editElement(currentViewItem, getColumnIndex());
        } else {
            editPreviousColumnFor(currentViewItem);
        }
    }

    /* ROW Operations */

    private void editNextRow() {
        Object nextItem = getNextVisibleViewItem(getCurrentViewItem());
        if (nextItem != null) {
            editCellOrRowFollowerFor(nextItem);
        }
    }

    private void editPreviousRow() {
        Object previousItem = getPreviousVisibleViewItem(getCurrentViewItem());
        if (previousItem != null) {
            editCellOrRowPredecessorFor(previousItem);
        }
    }

    private void editCellOrRowFollowerFor(Object currentViewItem) {
        if (currentViewItem == null) {
            return;
        }
        if (canEdit(currentViewItem)) {
            viewer.editElement(currentViewItem, getColumnIndex());
        } else {
            editCellOrRowFollowerFor(getNextVisibleViewItem(currentViewItem));
        }
    }

    private void editCellOrRowPredecessorFor(Object currentViewItem) {
        if (currentViewItem == null) {
            return;
        }
        if (canEdit(currentViewItem)) {
            viewer.editElement(currentViewItem, getColumnIndex());
        } else {
            editCellOrRowPredecessorFor(getPreviousVisibleViewItem(currentViewItem));
        }
    }

    protected abstract int getColumnIndex();

    protected abstract boolean canEdit(Object currentViewItem);

    protected abstract Object getPreviousVisibleViewItem(Object currentViewItem);

    protected abstract Object getNextVisibleViewItem(Object currentViewItem);

    private LinkedColumnsTraversalStrategy getNextColumnTraversalStrategy() {
        return follower;
    }

    private LinkedColumnsTraversalStrategy getPreviousColumnTraversalStrategy() {
        return predecessor;
    }

    private LinkedColumnsTraversalStrategy getFirstColumnTraversalStrategy() {
        if (hasPreviousColumnTraversalStrategy()) {
            return getPreviousColumnTraversalStrategy().getFirstColumnTraversalStrategy();
        } else {
            return this;
        }
    }

    private LinkedColumnsTraversalStrategy getLastColumnTraversalStrategy() {
        if (hasNextColumnTraversalStrategy()) {
            return getNextColumnTraversalStrategy().getLastColumnTraversalStrategy();
        } else {
            return this;
        }
    }

    private boolean hasNextColumnTraversalStrategy() {
        return getNextColumnTraversalStrategy() != null;
    }

    private boolean hasPreviousColumnTraversalStrategy() {
        return getPreviousColumnTraversalStrategy() != null;
    }

    protected LinkedColumnsTraversalStrategy getFollower() {
        return follower;
    }

    /**
     * Sets the {@link LinkedColumnsTraversalStrategy} of the following editable column. Sets this
     * Strategy as predecessor of the given follower if not already so.
     * 
     * @param foll the new following {@link LinkedColumnsTraversalStrategy}
     */
    public void setFollower(LinkedColumnsTraversalStrategy foll) {
        follower = foll;
        if (foll != null && foll.getPredecessor() != this) {
            foll.setPredecessor(this);
        }
    }

    protected LinkedColumnsTraversalStrategy getPredecessor() {
        return predecessor;
    }

    /**
     * Sets the {@link LinkedColumnsTraversalStrategy} of the previous editable column. Sets this
     * Strategy as follower of the given follower if not already so.
     * 
     * @param pred the new previous {@link LinkedColumnsTraversalStrategy}
     */
    public void setPredecessor(LinkedColumnsTraversalStrategy pred) {
        predecessor = pred;
        if (pred != null && pred.getFollower() != this) {
            pred.setFollower(this);
        }
    }

    public void focusLost(FocusEvent e) {
        fireApplyEditorValue();
    }

    public void keyReleased(KeyEvent e) {
        // nothing to do
    }

    public void focusGained(FocusEvent e) {
        // nothing to do
    }

    protected ColumnViewer getViewer() {
        return viewer;
    }
}
