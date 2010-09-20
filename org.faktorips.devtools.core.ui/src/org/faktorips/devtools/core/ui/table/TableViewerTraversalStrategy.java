/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.table;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.events.FocusEvent;
import org.faktorips.devtools.core.model.enums.IEnumValueContainer;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;

/**
 * Supports the navigation in a <tt>Table</tt> / <tt>TableViewer</tt> using the
 * <tt>SWT.TRAVERSE_ESCAPE</tt>, <tt>SWT.TRAVERSE_RETURN</tt>, <tt>SWT.TRAVERSE_TAB_NEXT</tt>,
 * <tt>SWT.TRAVERSE_TAB_PREVIOUS</tt>, <tt>SWT.ARROW_DOWN</tt>, <tt>SWT.ARROW_UP</tt> keys.
 * 
 * @author Stefan Widmaier, Alexander Weickmann
 */
public class TableViewerTraversalStrategy extends TableTraversalStrategy {
    private TableViewer tableViewer;
    /**
     * Flag that is <tt>true</tt> if this {@link TraversalStrategy} creates new rows if requested.
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
        skippedColumns = new ArrayList<Integer>(getRowCount());
    }

    @Override
    public void focusLost(FocusEvent e) {
        // Skipped columns do not fire the event.
        if (!(skippedColumns.contains(getColumnIndex()))) {
            fireApplyEditorValue();
        }
    }

    /**
     * Edits the next row relative to the current selection of the <tt>TableViewer</tt> this
     * <tt>TableCellEditor</tt> is used in. If no following row exists, two behaviors are possible:
     * <ul>
     * <li>If this <tt>TableCellEditor</tt> is configured to create rows a new row is created and
     * the current column in the new row is edited.
     * <li>If this <tt>TableCellEditor</tt> is configured to not create rows the current column of
     * the last row is edited.
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
     * Edits the next column relative to the column this <tt>TableCellEditor</tt> is used for. If
     * there is no next column, the first cell of the next row is edited. If in turn no following
     * row exists two behaviors are possible:
     * <ul>
     * <li>If this <tt>TableCellEditor</tt> is configured to create rows a new row is created and
     * the first cell edited.
     * <li>If this <tt>TableCellEditor</tt> is configured to not create rows the last cell of the
     * last row of the table is edited.
     * </ul>
     * 
     */
    @Override
    protected void editNextColumn() {
        if (isAtNewColumn()) {
            fireApplyEditorValue();
            if (isAtNewRow()) {
                if (isRowCreating()) {
                    /*
                     * in GTK / Linux appending a new row resetting the selected column to -1 so we
                     * safe the currently selected column
                     */
                    int currentRow = getCurrentRow();
                    appendTableRow();
                    tableViewer.getTable().select(currentRow);
                } else {
                    /*
                     * This will ensure that if the cursor is in the last row and last column,
                     * hitting tab will set the focus to the table so that if tab is pressed again,
                     * the next UI element will be focused
                     */
                    tableViewer.getTable().forceFocus();
                }
            }
            editCell(getNextRow(), getNextColumn());
        } else {
            editCell(getCurrentRow(), getNextColumn());
        }
    }

    /**
     * Returns the index of the next row. If no following row exists and this
     * <tt>TableCellEditor</tt> wasn't configured to create new rows, the index of the last row will
     * be returned.
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

    /** Returns <tt>true</tt> if the next row does not exist yet, <tt>false</tt> otherwise. */
    private boolean isAtNewRow() {
        return getCurrentRow() + 1 == getRowCount();
    }

    /**
     * Returns <tt>true</tt> if the next column does not exist yet, <tt>false</tt> otherwise. Takes
     * skipped columns into account.
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
     * <tt>rowIndex</tt> and <tt>columnIndex</tt>. Out-of-bound values will cause the table viewer
     * to loose focus.
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
                tableViewer.editElement(element, columnIndex);
            }
        }
    }

    /**
     * Appends a new <tt>IRow</tt> to the table if the tableviewer's input is a
     * <tt>TableContents</tt>.
     * <p>
     * Appends a new <tt>IEnumValue</tt> to the table if the tableviewer's input is an
     * <tt>EnumValueContainer</tt>.
     * <p>
     * Does nothing otherwise.
     */
    private void appendTableRow() {
        if (tableViewer.getInput() instanceof ITableContents) {
            ITableContents tableContents = (ITableContents)tableViewer.getInput();
            IRow newRow = ((ITableContentsGeneration)tableContents.getFirstGeneration()).newRow();
            tableViewer.add(newRow);

        } else if (tableViewer.getInput() instanceof IEnumValueContainer) {
            IEnumValueContainer enumValueContainer = (IEnumValueContainer)tableViewer.getInput();
            try {
                enumValueContainer.newEnumValue();
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
        }
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
     * Returns <tt>true</tt> if the index was added, <tt>false</tt> otherwise (e.g. if the given
     * index is already skipped).
     */
    public boolean addSkippedColumnIndex(int columnIndex) {
        return skippedColumns.contains(columnIndex) ? false : skippedColumns.add(columnIndex);
    }

    /**
     * Removes the column identified by the given column index from the list of columns that are
     * skipped while navigating trough the table.
     * <p>
     * Returns <tt>true</tt> if the index was found and removed, <tt>false</tt> otherwise (e.g. if
     * the given index was not skipped).
     */
    public boolean removeSkippedColumnIndex(int columnIndex) {
        return skippedColumns.remove(new Integer(columnIndex));
    }

    /**
     * Clears the skipped columns causing any columns that are currently being skipped to be no
     * longer skipped.
     */
    public void clearSkippedColumns() {
        skippedColumns.clear();
    }

    /**
     * Indicates whether this <tt>TableCellEditor</tt> creates new rows when requested (
     * <tt>true</tt>) or not (<tt>false</tt>).
     */
    public boolean isRowCreating() {
        return rowCreating;
    }

    /**
     * Configures this <tt>TableCellEditor</tt> to create new rows dynamically. New rows are created
     * when the user tries to navigate into a cell / row that is not in the table (yet).
     * <p>
     * If <tt>false</tt> is given this <tt>TableCellEditor</tt> will not create any rows and simply
     * edit the last cell of the table. On the other hand, if <tt>true</tt> is given a new row is
     * created when needed.
     * 
     * @param rowCreating Flag indicating whether new rows shall be created by this cell editor when
     *            the need to do so arises.
     */
    public void setRowCreating(boolean rowCreating) {
        this.rowCreating = rowCreating;
    }
}
