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
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.faktorips.devtools.core.model.enums.IEnumValueContainer;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.util.ArgumentCheck;

/**
 * Base class for all <tt>CellEditor</tt>s for tables. Supports the navigation in a <tt>Table</tt> /
 * <tt>TableViewer</tt> using the <tt>SWT.TRAVERSE_ESCAPE</tt>, <tt>SWT.TRAVERSE_RETURN</tt>,
 * <tt>SWT.TRAVERSE_TAB_NEXT</tt>, <tt>SWT.TRAVERSE_TAB_PREVIOUS</tt>, <tt>SWT.ARROW_DOWN</tt>,
 * <tt>SWT.ARROW_UP</tt> keys.
 * <p>
 * This cell editor is created with a control that is displayed when the user edits a table cell.
 * <p>
 * This CellEditor can be configured to create new rows if needed and append them to the end of the
 * table.
 * 
 * @author Stefan Widmaier, Alexander Weickmann
 */
public abstract class TableCellEditor extends CellEditor {

    /** The <tt>TableViewer</tt> this <tt>CellEditor</tt> is used in. */
    private final TableViewer tableViewer;

    /** Index of the table column this <tt>CellEditor</tt> was created for. */
    private final int columnIndex;

    /** The control to be displayed when the user edits a table cell. */
    private Control control;

    /**
     * Flag that is <tt>true</tt> if this <tt>CellEditor</tt> creates new rows if requested and
     * deletes empty rows at the bottom of the table, false otherwise (default).
     */
    private boolean rowCreating;

    /**
     * A list containing the indices of all columns that shall be skipped when navigating trough the
     * table.
     */
    private List<Integer> skippedColumns;

    /**
     * Constructs a <tt>TableCellEditor</tt> that is used in the given <tt>TableViewer</tt>. The
     * given control is displayed when a cell is edited. The given <tt>columnIndex</tt> indicates in
     * which column of the table this editor is used.
     * <p>
     * The created <tt>TableCellEditor</tt> does not create rows automatically and must be
     * configured to do so.
     * 
     * @see #setRowCreating(boolean)
     * 
     * @param tableViewer The <tt>TableViewer</tt> this <tt>TableCellEditor</tt> is used in.
     * @param columnIndex The index of the column which cells this editor edits.
     * @param control The control to be displayed in a cell when editing.
     * 
     * @throws NullPointerException If <tt>control</tt> is <tt>null</tt>.
     */
    public TableCellEditor(TableViewer tableViewer, int columnIndex, Control control) {
        // Do not call super-constructor.
        ArgumentCheck.notNull(control);
        deactivate();

        this.tableViewer = tableViewer;
        this.columnIndex = columnIndex;
        this.control = control;
        skippedColumns = new ArrayList<Integer>(tableViewer.getTable().getItemCount());

        initKeyListener();
        initFocusListener();
        initTraverseListener();
    }

    /**
     * Returns <tt>true</tt> if this <tt>TableCellEditor</tt> supports mapping between an id and a
     * text which will be displayed (e.g. a combo box internally stores an id but the user can
     * select the value by using names).
     * <p>
     * Returns <tt>false</tt> otherwise.
     */
    public abstract boolean isMappedValue();

    /**
     * {@inheritDoc}
     * <p>
     * This method is never called, since the super-constructor is not used to create this
     * <tt>CellEditor</tt>. Returns the control given at instantiation.
     */
    @Override
    protected Control createControl(Composite parent) {
        return control;
    }

    /**
     * Initializes a <tt>TraverseListener</tt> for this <tt>CellEditor</tt>'s control. This listener
     * is used for navigating trough the table using the <tt>Tab</tt>, <tt>Shift+Tab</tt>,
     * <tt>Enter</tt> and <tt>Escape</tt> keys.
     * <ul>
     * <li><tt>Tab</tt>: Edits the next column.
     * <li><tt>Shift+Tab</tt>: Edits the previous column.
     * <li><tt>Enter</tt>: Edits the next row.
     * <li><tt>Escape</tt>: Deactivates the editing mode.
     * </ul>
     */
    protected void initTraverseListener() {
        control.addTraverseListener(new TraverseListener() {
            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_ESCAPE) {
                    deactivate();
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
        });
    }

    /**
     * Initializes a <tt>KeyListener</tt> for this <tt>CellEditor</tt>'s control. This listener is
     * used for navigating trough the table using the <tt>Arrow-Up</tt> and <tt>Arrow-Down</tt>
     * keys.
     * <ul>
     * <li><tt>Arrow-Up</tt>: Edits the previous row.
     * <li><tt>Arrow-Down</tt>: Edits the next row.
     * </ul>
     */
    protected void initKeyListener() {
        control.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.keyCode == SWT.ARROW_DOWN) {
                    editNextRow();
                    e.doit = false;
                } else if (e.keyCode == SWT.ARROW_UP) {
                    editPreviousRow();
                    e.doit = false;
                }
            }
        });
    }

    /**
     * Initializes a <tt>FocusListener</tt> for this <tt>CellEditor</tt>'s control. This listener is
     * responsible for firing the <tt>applyEditorValue</tt> event when focus is lost.
     */
    protected void initFocusListener() {
        control.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {

            }

            public void focusLost(FocusEvent e) {
                // Skipped columns do not fire the event.
                if (!(skippedColumns.contains(columnIndex))) {
                    fireApplyEditorValue();
                }
            }
        });
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

    /**
     * Returns the index of the next row. If no following row exists and this
     * <tt>TableCellEditor</tt> wasn't configured to create new rows, the index of the last row will
     * be returned.
     */
    private int getNextRow() {
        if (isAtNewRow() && !rowCreating) {
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

    /** Returns the index of the row that is currently being edited. */
    private int getCurrentRow() {
        return tableViewer.getTable().getSelectionIndex();
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

    /** Saves the current user input. */
    private void saveCurrentValue() {
        Object[] properties = tableViewer.getColumnProperties();
        if (columnIndex < properties.length) {
            Table table = tableViewer.getTable();
            ICellModifier cellModifier = tableViewer.getCellModifier();
            cellModifier.modify(table.getItem(getCurrentRow()), (String)properties[columnIndex], getValue());
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

    /**
     * {@inheritDoc}
     * <p>
     * Copied from super class. We needed to do this because we do not call the super constructor
     * and so the <tt>control</tt> attribute of the super class is always <tt>null</tt>. This method
     * accesses the duplicated attribute in this class.
     */
    @Override
    public void deactivate() {
        if (control != null && !(control.isDisposed())) {
            control.setVisible(false);
            saveCurrentValue();
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Copied from super class. We needed to do this because we do not call the super constructor
     * and so the <tt>control</tt> attribute of the super class is always <tt>null</tt>. This method
     * accesses the duplicated attribute in this class.
     */
    @Override
    public void dispose() {
        if (control != null && !(control.isDisposed())) {
            control.dispose();
        }
        control = null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Copied from super class. We needed to do this because we do not call the super constructor
     * and so the <tt>control</tt> attribute of the super class is always <tt>null</tt>. This method
     * accesses the duplicated attribute in this class.
     */
    @Override
    public Control getControl() {
        return control;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Copied from super class. We needed to do this because we do not call the super constructor
     * and so the <tt>control</tt> attribute of the super class is always <tt>null</tt>. This method
     * accesses the duplicated attribute in this class.
     */
    @Override
    public boolean isActivated() {
        return control != null && control.isVisible();
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
