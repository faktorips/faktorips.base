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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.faktorips.devtools.core.ui.controller.fields.FormattingComboField;

/**
 * Supports the navigation in a <code>Table</code> / <code>TableViewer</code> using the
 * <code>SWT.TRAVERSE_ESCAPE</code>, <code>SWT.TRAVERSE_RETURN</code>,
 * <code>SWT.TRAVERSE_TAB_NEXT</code>, <code>SWT.TRAVERSE_TAB_PREVIOUS</code>,
 * <code>SWT.ARROW_DOWN</code>, <code>SWT.ARROW_UP</code> keys.
 *
 * @author Stefan Widmaier, Alexander Weickmann
 */
public abstract class TableTraversalStrategy extends AbstractTraversalStrategy {
    /** Index of the table column this <code>CellEditor</code> was created for. */
    private final int columnIndex;

    public TableTraversalStrategy(IpsCellEditor cellEditor, int colIndex) {
        super(cellEditor);
        columnIndex = colIndex;
    }

    @Override
    public void keyTraversed(TraverseEvent e) {
        switch (e.detail) {
            case SWT.TRAVERSE_ESCAPE -> {
                getCellEditor().deactivate();
                e.doit = false;
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
        if (avoidArrowUpDownForRowTraversal()) {
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

    /**
     * If this cell editor is a Combo-based cell editor, let arrow up and down be used for selecting
     * values inside the drop down. Thus avoid creating or deleting rows, as this closes the
     * combo.(FS#1585 and JIRA-3371)
     */
    private boolean avoidArrowUpDownForRowTraversal() {
        return getCellEditor() instanceof EditFieldCellEditor
                && ((EditFieldCellEditor)getCellEditor()).getEditField() instanceof FormattingComboField;
    }

    @Override
    public void focusLost(FocusEvent e) {
        fireApplyEditorValue();
    }

    /**
     * Edits the previous row relative to the current selection of the <code>TableViewer</code> this
     * <code>TableCellEditor</code> is used in. Does nothing if the first row of the table is
     * selected.
     */
    private void editPreviousRow() {
        fireApplyEditorValue();
        editCell(getPreviousRow(), columnIndex);
    }

    /**
     * Edits the previous column relative to the column this <code>TableCellEditor</code> is used
     * for. If there is no previous column, the last cell of the previous row is edited. If in turn
     * the previous row does not exist the first cell of the topmost row of the table is edited
     * (first cell of the table).
     */
    private void editPreviousColumn() {
        if ((getPreviousColumn() >= columnIndex) && (getCurrentRow() > 0)) {
            fireApplyEditorValue();
            editCell(getPreviousRow(), getPreviousColumn());
        } else {
            editCell(getCurrentRow(), getPreviousColumn());
        }
    }

    /**
     * Edits the next row relative to the current selection of the <code>TableViewer</code> this
     * <code>TableCellEditor</code> is used in. If no following row exists, this method does
     * nothing.
     */
    protected void editNextRow() {
        if (!isAtNewRow()) {
            fireApplyEditorValue();
            editCell(getNextRow(), columnIndex);
        }
    }

    /**
     * Edits the next column relative to the column this <code>TableCellEditor</code> is used for.
     * If there is no next column, the first cell of the next row is edited. If in turn no following
     * row exists this method does nothing.
     */
    protected void editNextColumn() {
        if (isAtNewColumn()) {
            fireApplyEditorValue();
            editCell(getNextRow(), getNextColumn());
        } else {
            editCell(getCurrentRow(), getNextColumn());
        }
    }

    /**
     * Returns the index of the next row. Returns the current row index if no following row exists.
     */
    protected int getNextRow() {
        if (isAtNewRow()) {
            return getCurrentRow();
        }
        return getCurrentRow() + 1;
    }

    /**
     * Returns the index of the next column. If no following row exists the current column index
     * will be returned.
     */
    protected int getNextColumn() {
        return (isAtNewColumn()) ? columnIndex : columnIndex + 1;
    }

    /**
     * Returns the index of the previous row. If the first row is currently selected the index of
     * the first row (0) will be returned.
     */
    protected int getPreviousRow() {
        if (getCurrentRow() - 1 < 0) {
            return 0;
        }
        return getCurrentRow() - 1;
    }

    /**
     * Returns the index of the previous column. If there is no previous column there are two
     * possible behaviors:
     * <ul>
     * <li>If the first row is being edited the first column (0) will be returned.
     * <li>For any other row the highest column index will be returned.
     * </ul>
     * .
     */
    protected int getPreviousColumn() {
        int previousColumn = columnIndex - 1;
        if (previousColumn < 0) {
            previousColumn = (getCurrentRow() == 0) ? 0 : getColumnCount() - 1;
        }
        return previousColumn;
    }

    /** Returns <code>true</code> if the next row does not exist, <code>false</code> otherwise. */
    private boolean isAtNewRow() {
        return getCurrentRow() + 1 == getRowCount();
    }

    /**
     * Returns <code>true</code> if the next column does not exist, <code>false</code> otherwise.
     */
    protected boolean isAtNewColumn() {
        int nextColIndex = columnIndex + 1;
        return nextColIndex >= getColumnCount();
    }

    /**
     * Edits the table cell in the given column of the given row. Expects valid values for
     * <code>rowIndex</code> and <code>columnIndex</code>. Out-of-bound values will cause the table
     * viewer to loose focus.
     * <p>
     * For optimization reasons this method only informs the table viewer of a cell edit if the cell
     * has changed.
     *
     * @param rowIndex The index of the table row that shall be edited.
     * @param columnIndex The index of the table column that shall be edited.
     */
    protected abstract void editCell(int rowIndex, int columnIndex);

    /** Returns the index of the row that is currently being edited. */
    protected abstract int getCurrentRow();

    protected abstract int getRowCount();

    protected abstract int getColumnCount();

    protected int getColumnIndex() {
        return columnIndex;
    }

}
