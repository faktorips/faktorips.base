/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.table;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.faktorips.devtools.core.ui.controls.tableedit.FormattedCellEditingSupport;

/**
 * This class is designed for tables/trees with multiple editable columns (and non-editable
 * columns). This {@link LinkedColumnsTraversalStrategy} holds references to
 * {@link LinkedColumnsTraversalStrategy}s of all other editable columns. Using this
 * {@link TraversalStrategy} will ensure that all editable cells can be reached when traversing -
 * the user can jump from one editable column to another even if there is a locked column in
 * between.
 * <p>
 * Again this class (as {@link AbstractPermanentTraversalStrategy}) is designed to be used with a
 * {@link FormattedCellEditingSupport} that holds one instance of this class during its lifetime.
 * <p>
 * This {@link TraversalStrategy} supports two kinds of traversal in the ColumnViewer:
 * <ul>
 * <li>Column-Traversal(Tab,Shift+Tab) will jump to the next cell in the same line, or the first
 * cell in the next line (reading order) et vice versa.</li>
 * <li>Row-Traversal(Enter/Down,Up) will jump to the next editable cell strictly in the same column.
 * It will skip rows that are not editable for the given column even if there are editable cells in
 * the row (e.g. other columns that would be reached with Column-Traversal). Et vice versa.</li>
 * </ul>
 * <p>
 * The generic Type T defines the type of the object displayed by the columns, provided by the
 * content provider
 *
 * @author Stefan Widmaier
 */
public abstract class LinkedColumnsTraversalStrategy<T> extends AbstractPermanentTraversalStrategy<T> {

    private LinkedColumnsTraversalStrategy<T> follower = null;
    private LinkedColumnsTraversalStrategy<T> predecessor = null;

    public LinkedColumnsTraversalStrategy(CellTrackingEditingSupport<T> editingSupport) {
        super(editingSupport);
    }

    @Override
    public void keyTraversed(TraverseEvent e) {
        switch (e.detail) {
            case SWT.TRAVERSE_ESCAPE -> {
                if (getCurrentCellEditor() != null) {
                    getCurrentCellEditor().deactivate();
                    e.doit = false;
                }
            }
            case SWT.TRAVERSE_RETURN -> {
                editNextRow();
                e.doit = false;
            }
            case SWT.TRAVERSE_TAB_NEXT -> {
                editNextColumn();
                e.doit = false;
            }
            case SWT.TRAVERSE_TAB_PREVIOUS -> {
                editPreviousColumn();
                e.doit = false;
            }
        }
    }

    @Override
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

    private void editNextColumnFor(T currentViewItem) {
        if (hasNextColumnTraversalStrategy()) {
            getNextColumnTraversalStrategy().editCellOrColumnFollowerFor(currentViewItem);
        } else {
            getFirstColumnTraversalStrategy().editCellOrColumnFollowerFor(getNextVisibleViewItem(currentViewItem));
        }
    }

    private void editPreviousColumnFor(T currentViewItem) {
        if (hasPreviousColumnTraversalStrategy()) {
            getPreviousColumnTraversalStrategy().editCellOrColumnPredecessorFor(currentViewItem);
        } else {
            getLastColumnTraversalStrategy()
                    .editCellOrColumnPredecessorFor(getPreviousVisibleViewItem(currentViewItem));
        }
    }

    private void editCellOrColumnFollowerFor(T currentViewItem) {
        if (currentViewItem == null) {
            return;
        }
        if (canEdit(currentViewItem)) {
            getViewer().editElement(currentViewItem, getColumnIndex());
        } else {
            editNextColumnFor(currentViewItem);
        }
    }

    private void editCellOrColumnPredecessorFor(T currentViewItem) {
        if (currentViewItem == null) {
            return;
        }
        if (canEdit(currentViewItem)) {
            getViewer().editElement(currentViewItem, getColumnIndex());
        } else {
            editPreviousColumnFor(currentViewItem);
        }
    }

    private void editNextRow() {
        T nextItem = getNextVisibleViewItem(getCurrentViewItem());
        if (nextItem != null) {
            editCellOrRowFollowerFor(nextItem);
        }
    }

    private void editPreviousRow() {
        T previousItem = getPreviousVisibleViewItem(getCurrentViewItem());
        if (previousItem != null) {
            editCellOrRowPredecessorFor(previousItem);
        }
    }

    private void editCellOrRowFollowerFor(T currentViewItem) {
        if (currentViewItem == null) {
            return;
        }
        if (canEdit(currentViewItem)) {
            getViewer().editElement(currentViewItem, getColumnIndex());
        } else {
            editCellOrRowFollowerFor(getNextVisibleViewItem(currentViewItem));
        }
    }

    private void editCellOrRowPredecessorFor(T currentViewItem) {
        if (currentViewItem == null) {
            return;
        }
        if (canEdit(currentViewItem)) {
            getViewer().editElement(currentViewItem, getColumnIndex());
        } else {
            editCellOrRowPredecessorFor(getPreviousVisibleViewItem(currentViewItem));
        }
    }

    protected boolean canEdit(T currentViewItem) {
        return getEditingSupport().canEdit(currentViewItem);
    }

    protected abstract int getColumnIndex();

    protected abstract T getPreviousVisibleViewItem(T currentViewItem);

    protected abstract T getNextVisibleViewItem(T currentViewItem);

    private LinkedColumnsTraversalStrategy<T> getNextColumnTraversalStrategy() {
        return follower;
    }

    private LinkedColumnsTraversalStrategy<T> getPreviousColumnTraversalStrategy() {
        return predecessor;
    }

    private LinkedColumnsTraversalStrategy<T> getFirstColumnTraversalStrategy() {
        if (hasPreviousColumnTraversalStrategy()) {
            return getPreviousColumnTraversalStrategy().getFirstColumnTraversalStrategy();
        } else {
            return this;
        }
    }

    private LinkedColumnsTraversalStrategy<T> getLastColumnTraversalStrategy() {
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

    protected LinkedColumnsTraversalStrategy<T> getFollower() {
        return follower;
    }

    /**
     * Sets the {@link LinkedColumnsTraversalStrategy} of the following editable column. Sets this
     * Strategy as predecessor of the given follower if not already so.
     *
     * @param foll the new following {@link LinkedColumnsTraversalStrategy}
     */
    public void setFollower(LinkedColumnsTraversalStrategy<T> foll) {
        follower = foll;
        if (foll != null && foll.getPredecessor() != this) {
            foll.setPredecessor(this);
        }
    }

    protected LinkedColumnsTraversalStrategy<T> getPredecessor() {
        return predecessor;
    }

    /**
     * Sets the {@link LinkedColumnsTraversalStrategy} of the previous editable column. Sets this
     * Strategy as follower of the given follower if not already so.
     *
     * @param pred the new previous {@link LinkedColumnsTraversalStrategy}
     */
    public void setPredecessor(LinkedColumnsTraversalStrategy<T> pred) {
        predecessor = pred;
        if (pred != null && pred.getFollower() != this) {
            pred.setFollower(this);
        }
    }

    @Override
    public void focusLost(FocusEvent e) {
        fireApplyEditorValue();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // nothing to do
    }

    @Override
    public void focusGained(FocusEvent e) {
        // nothing to do
    }

    protected ColumnViewer getViewer() {
        return getEditingSupport().getViewer();
    }
}
