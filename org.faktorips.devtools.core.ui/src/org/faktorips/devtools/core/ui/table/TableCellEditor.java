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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
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
import org.faktorips.devtools.core.ui.editors.tablecontents.ContentPage;
import org.faktorips.util.ArgumentCheck;

/**
 * Base class for <code>CellEditor</code>s for Tables. Supports the navigation in a
 * table/tableviewer using the SWT.TRAVERSE_ESCAPE, SWT.TRAVERSE_RETURN, SWT.TRAVERSE_TAB_NEXT,
 * SWT.TRAVERSE_TAB_PREVIOUS, SWT.ARROW_DOWN, SWT.ARROW_UP keys. This cell editor is created with a
 * control that is displayed when the user edits a table cell.
 * <p>
 * This CellEditor can be configured to ceate new rows if needed and append them to the end of the
 * table. @see #setRowCreating(boolean)
 * 
 * @author Stefan Widmaier
 */
public abstract class TableCellEditor extends CellEditor {
    /*
     * The tableviewer this CellEditor is used in.
     */
    private final TableViewer tableViewer;

    /*
     * Index of the column this CellEditor was created for.
     */
    private final int columnIndex;

    /*
     * The control to be displayed when the user edits a table cell.
     */
    private Control control;

    /*
     * True if this CellEditor creates new rows if requested and deletes empty rows at the bottom of
     * the table, false otherwise (default).
     */
    private boolean rowCreating = false;

    /**
     * Constructs a CellEditor that is used in the given <code>TableViewer</code>. The CellEditor
     * displays the given control when a cell is edited. The given columnIndex indicates in which
     * column of the table this editor is used.
     * <p>
     * The created CellEditor does not create rows automatically and must be configured to do so
     * {@link #setRowCreating(boolean)}.
     * 
     * @param tableViewer The TableViewer this CellEditor is used in.
     * @param columnIndex The index of the column which cells this editor edits.
     * @param control The control to be displayed in a cell when editing.
     */
    public TableCellEditor(TableViewer tableViewer, int columnIndex, Control control) {
        // do not call super-constructor.
        ArgumentCheck.notNull(control);
        deactivate();

        this.tableViewer = tableViewer;
        this.columnIndex = columnIndex;
        this.control = control;

        initKeyListener(control);
        initTraverseListener(control);
    }

    /**
     * Returns <code>true</code> if the cell editor supports mapping between an id and a text which
     * will be displayed (e.g. combo boxes stores intern an id but the user can select the value by
     * using names).
     */
    public abstract boolean isMappedValue();

    /**
     * This method is never called, since the super-constructor is not used to create this cell
     * editor. Returns the control given at instanciation. {@inheritDoc}
     */
    @Override
    protected Control createControl(Composite parent) {
        return control;
    }

    /**
     * Initializes a <code>TraverseListener</code> for this CellEditor's control. This listener is
     * used for navigation the table with tab, shift-tab, enter and escape.
     */
    protected void initTraverseListener(Control control) {
        control.addTraverseListener(new TraverseListener() {
            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_ESCAPE) {
                    e.doit = false;
                    deactivate();
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
     * Initializes a <code>KeyListener</code> for this CellEditor's control. This listener is used
     * for navigation the table with arrow-up and arrow-down.
     */
    protected void initKeyListener(Control control) {
        control.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.keyCode == SWT.ARROW_DOWN) {
                    editNextRow();
                } else if (e.keyCode == SWT.ARROW_UP) {
                    editPreviousRow();
                }
            }
        });
    }

    /**
     * Edits the next row relative to the current selection of the tableviewer this celleditor is
     * used in. If no following row exists two behaviours are possible:
     * <ul>
     * <li>If this celleditor is configured to create rows ({@link #isRowCreating()}) a new row is
     * created and the current column in the new row is edited.</li>
     * <li>If this celleditor is configured to not create rows the current column of the last row is
     * edited.</li>
     * <li></li>
     * </ul>
     * 
     */
    private void editNextRow() {
        int nextRow = tableViewer.getTable().getSelectionIndex() + 1;
        nextRow = requestRow(nextRow);
        saveCurrentValue();
        editCell(nextRow, columnIndex);
    }

    /**
     * Edits the previous row relative to the current selection of the tableviewer this celleditor
     * is used in. Does nothing if the first row of the table is selected.
     * 
     */
    private void editPreviousRow() {
        int previousRow = tableViewer.getTable().getSelectionIndex() - 1;
        if (previousRow < 0) {
            previousRow = 0;
        }
        saveCurrentValue();
        editCell(previousRow, columnIndex);
    }

    /**
     * Edits the next column relative to the column this celleditor is used for. If there is no next
     * column (celleditor in last column), the first cell of the next row is edited. If in turn no
     * following row exists two behaviours are possible:
     * <ul>
     * <li>If this celleditor is configured to create rows ({@link #isRowCreating()}) a new row is
     * created and the first cell edited.</li>
     * <li>If this celleditor is configured to not create rows the last cell of the last row of the
     * table is edited.</li>
     * <li></li>
     * </ul>
     * 
     */
    private void editNextColumn() {
        int rowIndex = tableViewer.getTable().getSelectionIndex();
        int nextColumnIndex = columnIndex + 1;
        if (nextColumnIndex >= tableViewer.getTable().getColumnCount()) {
            nextColumnIndex = 0;
            rowIndex++;
            // Avoid tab-traversing in last row if row creating is disabled
            if (!isRowCreating() && rowIndex == tableViewer.getTable().getItemCount()) {
                nextColumnIndex = tableViewer.getTable().getColumnCount() - 1;
                rowIndex = tableViewer.getTable().getItemCount() - 1;
            }
        }
        rowIndex = requestRow(rowIndex);
        saveCurrentValue();
        editCell(rowIndex, nextColumnIndex);
    }

    private void saveCurrentValue() {
        Object[] properties = tableViewer.getColumnProperties();

        if (columnIndex < properties.length) {
            Table table = tableViewer.getTable();
            tableViewer.getCellModifier().modify(table.getItem(table.getSelectionIndex()),
                    (String)tableViewer.getColumnProperties()[columnIndex], getValue());
        }
    }

    /**
     * Edits the previous column relative to the column this celleditor is used for. If there is no
     * previous column (celleditor in first column), the last cell of the previous row is edited. If
     * in turn the previous row does not exist the first cell of the topmost row of the table is
     * edited (first cell of the table).
     */
    private void editPreviousColumn() {
        int rowIndex = tableViewer.getTable().getSelectionIndex();
        int previousColumnIndex = columnIndex - 1;
        if (previousColumnIndex < 0) {
            if (rowIndex == 0) {
                previousColumnIndex = 0;
            } else {
                previousColumnIndex = tableViewer.getTable().getColumnCount() - 1;
                rowIndex--;
            }
        }
        saveCurrentValue();
        editCell(rowIndex, previousColumnIndex);
    }

    /**
     * Edits the table cell in the given column of the given row. Expects valid values for
     * nextRowIndex and nextColumnIndex. Out-of-bound values will cause the tableviewer to loose
     * focus.
     * <p>
     * For optimization reasons this method only informs the tableviewer of a cell edit if the cell
     * changed.
     * 
     * @param columnIndex The index of the column (value) that should be edited in the currently
     *            selected row.
     */
    private void editCell(int nextRowIndex, int nextColumnIndex) {
        if (nextColumnIndex != columnIndex || nextRowIndex != tableViewer.getTable().getSelectionIndex()) {
            tableViewer.editElement(tableViewer.getElementAt(nextRowIndex), nextColumnIndex);
        }
    }

    /**
     * Converts the given rowIndex to a valid index and returns it. This method may also create a
     * new row if this cellEditor is configured to do so.
     * <p>
     * If the given rowindex is less than zero, 0 is returned. If rowindex is valid (greater or
     * equal than zero and less than the number of rows in the table) the given rowIndex is simply
     * returned.
     * <p>
     * If the rowindex is greater or equal than the number of tableitems actions are possible:
     * <ul>
     * <li>If isRowCreating()==true a new row is created and its index returned.</li>
     * <li>If isRowCreating()==false no row is created, instead the index of last row of the table
     * is returned.</li>
     * </ul>
     * The mechanism for deleting dynamically created rows is realized in the
     * <code>ContentPage</code> of the TableContentsEditor.
     * 
     * @see ContentPage
     * 
     * @param nextRow
     * @return The index of the last row of the table (whether it was newly created or not).
     */
    private int requestRow(int nextRow) {
        // transform to valid range
        if (nextRow > tableViewer.getTable().getItemCount()) {
            nextRow = tableViewer.getTable().getItemCount();
        }
        if (nextRow < 0) {
            nextRow = 0;
        }

        if (nextRow == tableViewer.getTable().getItemCount()) {
            if (isRowCreating()) {
                saveCurrentValue();
                appendTableRow();
                return nextRow;
            } else {
                return tableViewer.getTable().getItemCount() - 1;
            }
        } else {
            return nextRow;
        }
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
     * Configures this CellEditor to create and delete rows dynamically. New rows are created when
     * the user tries to navigate into a cell/row that is not in the table (yet). Rows are deleted
     * if they are at the bottom of the table and at the same time empty.
     * <p>
     * If false is given this CellEditor will not create any rows and simply edit the last cell of
     * the table. If true is given every Key or Traverse-Event will create a new row if needed.
     * 
     * @return
     */
    public boolean isRowCreating() {
        return rowCreating;
    }

    /**
     * Indicates if this CellEditor creates new rows if requested (true) or not (false).
     * 
     * @param appendRows
     */
    public void setRowCreating(boolean appendRows) {
        rowCreating = appendRows;
    }

    /**
     * Reimplementation of the <code>CellEditor</code> method. Needed to access the control.
     * <p>
     * Hides this cell editor's control. Does nothing if this cell editor is not visible.
     */
    @Override
    public void deactivate() {
        if (control != null && !control.isDisposed()) {
            control.setVisible(false);
        }
    }

    /**
     * Reimplementation of the <code>CellEditor</code> method. Needed to access the control.
     * <p>
     * Disposes of this cell editor and frees any associated SWT resources.
     */
    @Override
    public void dispose() {
        if (control != null && !control.isDisposed()) {
            control.dispose();
        }
        control = null;
    }

    /**
     * Reimplementation of the <code>CellEditor</code> method. Needed to access the control.
     * <p>
     * Returns the control used to implement this cell editor.
     * 
     * @return the control, or <code>null</code> if this cell editor has no control
     */
    @Override
    public Control getControl() {
        return control;
    }

    /**
     * Reimplementation of the <code>CellEditor</code> method. Needed to access the control.
     * <p>
     * Returns whether this cell editor is activated.
     * 
     * @return <code>true</code> if this cell editor's control is currently visible, and
     *         <code>false</code> if not visible
     */
    @Override
    public boolean isActivated() {
        return control != null && control.isVisible();
    }
}
