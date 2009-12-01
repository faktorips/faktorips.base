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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.TraverseEvent;
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
public class TableTraversalStrategy extends AbstractTraversalStrategy {
    private TableViewer tableViewer;
    /** Index of the table column this <tt>CellEditor</tt> was created for. */
    private final int columnIndex;
    /**
     * A list containing the indices of all columns that shall be skipped when navigating trough the
     * table.
     */
    private List<Integer> skippedColumns;

    public TableTraversalStrategy(TableCellEditor cellEditor, TableViewer viewer, int colIndex) {
        super(cellEditor);
        tableViewer = viewer;
        columnIndex = colIndex;
        skippedColumns = new ArrayList<Integer>(tableViewer.getTable().getItemCount());
    }

    @Override
    public void keyTraversed(TraverseEvent e) {
        if (e.detail == SWT.TRAVERSE_ESCAPE) {
            getCellEditor().deactivate();
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

    @Override
    public void keyPressed(KeyEvent e) {
        // FS#1585: if this cell editor is a ComboCellEditor then the arrow down and up
        // feature to create or delete rows are not supported, because otherwise the
        // selection of a new value inside the drop down doesn't work correctly
        if (getCellEditor() instanceof ComboCellEditor) {
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

    @Override
    public void focusLost(FocusEvent e) {
        // Skipped columns do not fire the event.
        if (!(skippedColumns.contains(columnIndex))) {
            fireApplyEditorValue();
        }
    }

    /**
     * Edits the previous row relative to the current selection of the <tt>TableViewer</tt> this
     * <tt>TableCellEditor</tt> is used in. Does nothing if the first row of the table is selected.
     */
    private void editPreviousRow() {
        fireApplyEditorValue();
        editCell(getPreviousRow(), columnIndex);
    }

    /**
     * Edits the previous column relative to the column this <tt>TableCellEditor</tt> is used for.
     * If there is no previous column, the last cell of the previous row is edited. If in turn the
     * previous row does not exist the first cell of the topmost row of the table is edited (first
     * cell of the table).
     */
    private void editPreviousColumn() {
        if (getPreviousColumn() > columnIndex) {
            fireApplyEditorValue();
            editCell(getPreviousRow(), getPreviousColumn());
        } else {
            editCell(getCurrentRow(), getPreviousColumn());
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
    private void editNextRow() {
        fireApplyEditorValue();
        if (getNextRow() != getCurrentRow() && isAtNewRow()) {

            appendTableRow();
        }
        editCell(getNextRow(), columnIndex);
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
    private void editNextColumn() {
        if (isAtNewColumn()) {
            fireApplyEditorValue();
            if (isAtNewRow()) {
                appendTableRow();
            }
            editCell(getNextRow(), getNextColumn());
        }
        editCell(getCurrentRow(), getNextColumn());
    }

    private void fireApplyEditorValue() {
        getCellEditor().fireApplyEditorValue();
    }

    /**
     * Returns the index of the next row. If no following row exists and this
     * <tt>TableCellEditor</tt> wasn't configured to create new rows, the index of the last row will
     * be returned.
     */
    private int getNextRow() {
        if (isAtNewRow() && !getCellEditor().isRowCreating()) {
            return getCurrentRow();
        }
        return getCurrentRow() + 1;
    }

    /**
     * Returns the index of the next column. If no following row exists the first column (0) will be
     * returned.
     * <p>
     * Takes skipped columns into account.
     */
    private int getNextColumn() {
        int nextColumn = (isAtNewColumn()) ? 0 : columnIndex + 1;
        while (skippedColumns.contains(nextColumn)) {
            nextColumn++;
        }
        return nextColumn;
    }

    /**
     * Returns the index of the previous row. If the first row is currently selected the index of
     * the first row (0) will be returned.
     */
    private int getPreviousRow() {
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
     * <p>
     * Takes skipped columns into account.
     */
    private int getPreviousColumn() {
        int previousColumn = columnIndex - 1;
        if (previousColumn < 0) {
            previousColumn = (getCurrentRow() == 0) ? 0 : tableViewer.getTable().getColumnCount() - 1;
        }
        while (skippedColumns.contains(previousColumn)) {
            previousColumn--;
            if (previousColumn < 0) {
                previousColumn = (getCurrentRow() == 0) ? 0 : tableViewer.getTable().getColumnCount() - 1;
            }
        }
        return previousColumn;
    }

    /** Returns <tt>true</tt> if the next row does not exist yet, <tt>false</tt> otherwise. */
    private boolean isAtNewRow() {
        return getCurrentRow() + 1 == tableViewer.getTable().getItemCount();
    }

    /**
     * Returns <tt>true</tt> if the next column does not exist yet, <tt>false</tt> otherwise. Takes
     * skipped columns into account.
     */
    private boolean isAtNewColumn() {
        int nextNotSkipped = columnIndex + 1;
        while (skippedColumns.contains(nextNotSkipped)) {
            nextNotSkipped++;
        }
        return nextNotSkipped >= tableViewer.getTable().getColumnCount();
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
    private void editCell(int rowIndex, int columnIndex) {
        if (columnIndex != this.columnIndex || rowIndex != getCurrentRow()) {
            tableViewer.editElement(tableViewer.getElementAt(rowIndex), columnIndex);
        }
    }

    // FS#1607, TODO not necessary?
    // see this#deactivate()
    // /** Saves the current user input. */
    // private void saveCurrentValue() {
    // Object[] properties = tableViewer.getColumnProperties();
    // if (properties == null) {
    // // TODO since Eclipse 3.3 there is an alternative way for cell editing, do we have to
    // // support this here? @see ViewerColumn#setEditingSupport(EditingSupport)
    // return;
    // }
    // if (columnIndex < properties.length) {
    // Table table = tableViewer.getTable();
    // ICellModifier cellModifier = tableViewer.getCellModifier();
    // cellModifier.modify(table.getItem(getCurrentRow()), (String)properties[columnIndex],
    // getValue());
    // }
    // }

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
    private int getCurrentRow() {
        return tableViewer.getTable().getSelectionIndex();
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
}
