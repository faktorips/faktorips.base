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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.widgets.Table;
import org.faktorips.devtools.model.enums.IEnumValueContainer;
import org.faktorips.devtools.model.tablecontents.IRow;
import org.faktorips.devtools.model.tablecontents.ITableContents;

/**
 * Supports the navigation in a <code>Table</code> / <code>TableViewer</code> using the
 * <code>SWT.TRAVERSE_ESCAPE</code>, <code>SWT.TRAVERSE_RETURN</code>,
 * <code>SWT.TRAVERSE_TAB_NEXT</code>, <code>SWT.TRAVERSE_TAB_PREVIOUS</code>,
 * <code>SWT.ARROW_DOWN</code>, <code>SWT.ARROW_UP</code> keys.
 *
 * @author Stefan Widmaier, Alexander Weickmann
 */
public class TableViewerTraversalStrategy extends TableTraversalStrategy {
    private TableViewer tableViewer;
    /**
     * Flag that is <code>true</code> if this {@link TraversalStrategy} creates new rows if
     * requested.
     */
    private boolean rowCreating;

    /**
     * A list containing the indices of all columns that shall be skipped when navigating trough the
     * table.
     */
    private List<Integer> skippedColumns;

    public TableViewerTraversalStrategy(IpsCellEditor cellEditor, TableViewer viewer, int colIndex) {
        super(cellEditor, colIndex);
        tableViewer = viewer;
        skippedColumns = new ArrayList<>(getRowCount());
    }

    @Override
    public void focusGained(FocusEvent e) {
        if (skippedColumns.contains(getColumnIndex())) {
            getCellEditor().deactivate();
        }
    }

    @Override
    public void focusLost(FocusEvent e) {
        if (!(skippedColumns.contains(getColumnIndex()))) {
            fireApplyEditorValue();
        }
    }

    /**
     * Edits the next row relative to the current selection of the <code>TableViewer</code> this
     * <code>TableCellEditor</code> is used in. If no following row exists, two behaviors are
     * possible:
     * <ul>
     * <li>If this <code>TableCellEditor</code> is configured to create rows a new row is created
     * and the current column in the new row is edited.
     * <li>If this <code>TableCellEditor</code> is configured to not create rows the current column
     * of the last row is edited.
     * </ul>
     */
    @Override
    protected void editNextRow() {
        fireApplyEditorValue();
        if (getNextRow() != getCurrentRow() && isAtNewRow()) {
            appendTableRow();
        }
        editCell(getNextRow(), getColumnIndex());
    }

    /**
     * Edits the next column relative to the column this <code>TableCellEditor</code> is used for.
     * If there is no next column, the first cell of the next row is edited. If in turn no following
     * row exists two behaviors are possible:
     * <ul>
     * <li>If this <code>TableCellEditor</code> is configured to create rows a new row is created
     * and the first cell edited.
     * <li>If this <code>TableCellEditor</code> is configured to not create rows the last cell of
     * the last row of the table is edited.
     * </ul>
     *
     */
    @Override
    protected void editNextColumn() {
        if (isAtNewColumn()) {
            fireApplyEditorValue();
            appendRowIfNecessary();
            editCell(getNextRow(), getNextColumn());
        } else {
            editCell(getCurrentRow(), getNextColumn());
        }
    }

    private void appendRowIfNecessary() {
        if (isAtNewRow()) {
            if (isRowCreating()) {
                /*
                 * in GTK / Linux appending a new row resetting the selected column to -1 so we safe
                 * the currently selected column
                 */
                Object newRow = appendTableRow();
                tableViewer.setSelection(new StructuredSelection(newRow), true);
            } else {
                /*
                 * This will ensure that if the cursor is in the last row and last column, hitting
                 * tab will set the focus to the table so that if tab is pressed again, the next UI
                 * element will be focused
                 */
                tableViewer.getTable().forceFocus();
            }
        }
    }

    /**
     * Returns the index of the next row. If no following row exists and this
     * <code>TableCellEditor</code> wasn't configured to create new rows, the index of the last row
     * will be returned.
     */
    @Override
    protected int getNextRow() {
        if (isAtNewRow() && isRowCreating()) {
            return getCurrentRow();
        }
        return getCurrentRow() + 1;
    }

    /**
     * Returns the index of the next column. If no following row exists the current column index
     * will be returned.
     * <p>
     * Takes skipped columns into account.
     */
    @Override
    protected int getNextColumn() {
        int nextColumn = getColumnIndex();
        do {
            nextColumn = (nextColumn + 1) % getColumnCount();
        } while (skippedColumns.contains(nextColumn));

        return nextColumn;
    }

    /**
     * Returns the index of the previous column. If there is no previous column there are two
     * possible behaviors:
     * <ul>
     * <li>If the first row is being edited the first column (0) will be returned.
     * <li>For any other row the highest column index will be returned.
     * </ul>
     * <p>
     * Takes skipped columns into account.
     */
    @Override
    protected int getPreviousColumn() {
        int previousColumn = getColumnIndex();
        do {
            previousColumn = (previousColumn - 1) % getColumnCount();
            if (previousColumn < 0) {
                previousColumn = getCurrentRow() == 0 ? getFirstNotSkippedColumn() : getColumnCount() - 1;
            }
        } while (skippedColumns.contains(previousColumn));

        return previousColumn;
    }

    private int getFirstNotSkippedColumn() {
        int firstNotSkippedColumn = 0;
        for (int i : skippedColumns) {
            if (i > firstNotSkippedColumn) {
                break;
            }
            firstNotSkippedColumn++;
        }
        return firstNotSkippedColumn;
    }

    /**
     * Returns <code>true</code> if the next row does not exist yet, <code>false</code> otherwise.
     */
    private boolean isAtNewRow() {
        return getCurrentRow() + 1 == getRowCount();
    }

    /**
     * Returns <code>true</code> if the next column does not exist yet, <code>false</code>
     * otherwise. Takes skipped columns into account.
     */
    @Override
    protected boolean isAtNewColumn() {
        int nextNotSkipped = getColumnIndex() + 1;
        while (skippedColumns.contains(nextNotSkipped)) {
            nextNotSkipped++;
        }
        return nextNotSkipped >= getColumnCount();
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
    @Override
    protected void editCell(int rowIndex, int columnIndex) {
        if (columnIndex != getColumnIndex() || rowIndex != getCurrentRow()) {
            Object element = tableViewer.getElementAt(rowIndex);
            if (element != null) {
                scrollToColumn(columnIndex);
                tableViewer.editElement(element, columnIndex);
            }
        }
    }

    private void scrollToColumn(int columnIndex) {
        Table table = tableViewer.getTable();
        table.showColumn(table.getColumn(columnIndex));
    }

    /**
     * Appends a new <code>IRow</code> to the table if the tableviewer's input is a
     * <code>TableContents</code>.
     * <p>
     * Appends a new <code>IEnumValue</code> to the table if the tableviewer's input is an
     * <code>EnumValueContainer</code>.
     * <p>
     * Does nothing otherwise.
     */
    private Object appendTableRow() {
        return switch (tableViewer.getInput()) {
            case ITableContents tableContents -> {
                IRow newRow = tableContents.getTableRows().newRow();
                tableViewer.refresh();
                yield newRow;
            }
            case IEnumValueContainer enumValueContainer -> enumValueContainer.newEnumValue();
            default -> null;
        };
    }

    /** Returns the index of the row that is currently being edited. */
    @Override
    protected int getCurrentRow() {
        return tableViewer.getTable().getSelectionIndex();
    }

    @Override
    protected int getRowCount() {
        return tableViewer.getTable().getItemCount();
    }

    @Override
    protected int getColumnCount() {
        return tableViewer.getTable().getColumnCount();
    }

    /**
     * Adds the column identified by the given column index to the list of columns that are skipped
     * while navigating trough the table.
     * <p>
     * Returns <code>true</code> if the index was added, <code>false</code> otherwise (e.g. if the
     * given index is already skipped).
     */
    public boolean addSkippedColumnIndex(int columnIndex) {
        return skippedColumns.contains(columnIndex) ? false : skippedColumns.add(columnIndex);
    }

    /**
     * Removes the column identified by the given column index from the list of columns that are
     * skipped while navigating trough the table.
     * <p>
     * Returns <code>true</code> if the index was found and removed, <code>false</code> otherwise
     * (e.g. if the given index was not skipped).
     */
    public boolean removeSkippedColumnIndex(int columnIndex) {
        return skippedColumns.remove(Integer.valueOf(columnIndex));
    }

    /**
     * Clears the skipped columns causing any columns that are currently being skipped to be no
     * longer skipped.
     */
    public void clearSkippedColumns() {
        skippedColumns.clear();
    }

    /**
     * Indicates whether this <code>TableCellEditor</code> creates new rows when requested (
     * <code>true</code>) or not (<code>false</code>).
     */
    public boolean isRowCreating() {
        return rowCreating;
    }

    /**
     * Configures this <code>TableCellEditor</code> to create new rows dynamically. New rows are
     * created when the user tries to navigate into a cell / row that is not in the table (yet).
     * <p>
     * If <code>false</code> is given this <code>TableCellEditor</code> will not create any rows and
     * simply edit the last cell of the table. On the other hand, if <code>true</code> is given a
     * new row is created when needed.
     *
     * @param rowCreating Flag indicating whether new rows shall be created by this cell editor when
     *            the need to do so arises.
     */
    public void setRowCreating(boolean rowCreating) {
        this.rowCreating = rowCreating;
    }
}
