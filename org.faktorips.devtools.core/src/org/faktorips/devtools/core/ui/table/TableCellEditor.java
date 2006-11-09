/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.table;

import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.ui.editors.tablecontents.ContentPage;

/**
 * This CellEditor can be configured to ceate new rows if needed and append them to the end of the
 * table. @see #setRowCreating(boolean)
 * 
 * 
 * 
 * 
 * Todo Superklasse:
 * - editfield als textcontrol verwenden
 * - prinzipielle moeglichkeit des Popupmenues
 *      .null-value support
 * 
 * Todo Unterklasse:
 * - textcontrol mit keylistenern versehen
 * - popupmenue
 * FIXME:
 *  - popupmenu
 * 
 * @author Stefan Widmaier
 */
public abstract class TableCellEditor extends CellEditor{
    private final TableViewer tableViewer;
    private final int columnIndex;
    
    private Control control;
    
    /**
     * True if this CellEditor creates new rows if requested and deletes empty rows at the 
     * bottom of the table, false otherwise (default).
     */
    private boolean rowCreating= false;
    
    /**
     * Constructs a CellEditor that is used in the given <code>TableViewer</code>. The CellEditor
     * displays the given control when a cell is edited. The given columnIndex indicates in which
     * column of the table this editor is used.
     * <p>
     * The created CellEditor does not create rows automatically and must be configured to do so
     * @see #setRowCreating(boolean).
     * 
     * @param tableViewer The TableViewer this CellEditor is used in.
     * @param columnIndex The index of the column which cells this editor edits.
     * @param control The control to be displayed in a cell when editing.
     */
    public TableCellEditor(TableViewer tableViewer, int columnIndex, Control control) {
        // do not call super-constructor.
        Assert.isTrue(control != null);
        deactivate();
        
        this.tableViewer= tableViewer;
        this.columnIndex= columnIndex;
        this.control= control;

        initKeyListener(control);
        initTraverseListener(control);
    }

    /**
     * This method is never called, since the super-constructor is not used to create this cell
     * editor. Returns the control given at instanciation. {@inheritDoc}
     */
    protected Control createControl(Composite parent) {
        return control;
    }
    
    /**
     * Inititializes a <code>TraverseListener</code> for this CellEditor's control. 
     * This listener is used for navigation the table with tab, shift-tab, enter and escape.
     */
    protected void initTraverseListener(Control control){
        control.addTraverseListener(new TraverseListener() {
            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_ESCAPE){
                    e.doit= false;
                    deactivate();
                }else if (e.detail == SWT.TRAVERSE_RETURN) {
                    editNextRow();
                    e.doit= false;
                }else if (e.detail == SWT.TRAVERSE_TAB_NEXT) {
                    editNextColumn();
                    e.doit= false;
                }else if (e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
                    editPreviousColumn();
                    e.doit= false;
                }
            }
        });
    }
    /**
     * Inititializes a <code>KeyListener</code> for this CellEditor's control. 
     * This listener is used for navigation the table with arrow-up and arrow-down.
     */
    protected void initKeyListener(Control control){
        control.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if(e.keyCode==SWT.ARROW_DOWN) {
                    editNextRow();
                }else if(e.keyCode==SWT.ARROW_UP){
                    editPreviousRow();
                }else{
                    
                }
            }
        });
    }
    
    
    private void editNextRow() {
        int nextRow= tableViewer.getTable().getSelectionIndex() + 1;
        if(nextRow>tableViewer.getTable().getItemCount()){
            return;
        }
        nextRow= requestRow(nextRow);
        editCell(nextRow, columnIndex);
    }


    private void editPreviousRow() {
        int previousRow= tableViewer.getTable().getSelectionIndex() - 1;
        editCell(previousRow, columnIndex);
    }
    
    private void editNextColumn() {
        editColumn(columnIndex+1);
    }
    
    private void editPreviousColumn() {
        editColumn(columnIndex-1);
    }
    
    /**
     * Edits the table cell in the current row (current selection of the table) at the given column
     * index. If the columnindex is out of bounds (greater than the table's number of columns or
     * less than zero), this method reacts with a change of the row. If the columnindex is less than
     * zero the last cell of the previous row (if existent) is edited. If the columnindex is greater
     * than the columnnumber the first cell of the next row (if existent) is edited.
     * If this cellEditor is configured to create new rows a new row is created in case the end of the
     * table was reached.
     * <p>
     * For optimization reasons this method only informs the tableviewer of a cell edit if the
     * current cell changed.
     * 
     * @param columnIndex The index of the column (value) that should be edited in the currently
     *            selected row.
     */
    private void editColumn(int nextColumnIndex) {
        int nextRowIndex= tableViewer.getTable().getSelectionIndex();
        // end of line: edit first cell of next row
        if(nextColumnIndex >= tableViewer.getTable().getColumnCount()){
            nextRowIndex++;
            nextColumnIndex= 0;
            nextRowIndex= requestRow(nextRowIndex);
            editCell(nextRowIndex, nextColumnIndex);
        }else if(nextColumnIndex<0){
            nextRowIndex--;
            nextColumnIndex= tableViewer.getTable().getColumnCount()-1;
            if(nextRowIndex<0){
                // beginning of table: edit first cell
                editCell(0, 0);
            }else{
                editCell(nextRowIndex, nextColumnIndex);
            }
        }else{
            editCell(nextRowIndex, nextColumnIndex);
        }
    }

    /**
     * Returns the index of the last valid row. May also create a new row if this cellEditor is 
     * configured to do so.
     * <p>
     * If a rowindex greater or equal than the number of tableitems is given two actions
     * are possible:
     * <ul>
     * <li> If isRowCreating()==true a new row is created and its index returned. </li>
     * <li> If isRowCreating()==false no row is created, instead the index of last row of the table
     * is returned. </li>
     * </ul>
     * A given rowindex less than zero is treated as zero.
     * <p>
     * The mechanism for deleting dynamically created rows is realized in the <code>ContentPage</code>
     * of the TableContentsEditor.
     * @see ContentPage
     * 
     * @param nextRow
     * @return
     */
    private int requestRow(int nextRow) {
        // transform to valid range
        if(nextRow>tableViewer.getTable().getItemCount()){
            nextRow= tableViewer.getTable().getItemCount();
        }
        if(nextRow<0){
            nextRow= 0;
        }
        
        if(nextRow==tableViewer.getTable().getItemCount()){
            if(isRowCreating()){
                appendTableRow();
                return nextRow;
            }else{
                return tableViewer.getTable().getItemCount();
            }
        }else{
            return nextRow;
        }
    }
    /**
     * Opens the cellEditor at the given row index and column index. Assumes both rowIndex
     * and colIndex contain valid values.
     * 
     * @param rowIndex
     * @param colIndex
     */
    private void editCell(int rowIndex, int colIndex){
        // optimization: only edit if cell (row or column) changed
        if(colIndex!=columnIndex || rowIndex!=tableViewer.getTable().getSelectionIndex()){
            if (tableViewer.getElementAt(rowIndex) != null){
                tableViewer.editElement(tableViewer.getElementAt(rowIndex), colIndex);
            }
        }
    }
    
    
   /**
    * Appends new row to the table and returns it. Rows can only be appended if the input of the 
    * TableViewer is a <code>TableContents</code>. Returns null otherwise.
    * 
    */
    private void appendTableRow() {
        if(tableViewer.getInput() instanceof ITableContents){
            ITableContents tableContents= (ITableContents)tableViewer.getInput();
            IRow newRow= ((ITableContentsGeneration)tableContents.getFirstGeneration()).newRow();
            tableViewer.add(newRow);
        }
    }        

    /**
     * Configures this CellEditor to create and delete rows dynamically.  
     * New rows are created when the user tries to navigate into a cell/row that is not
     * in the table (yet). Rows are deleted if they are at the bottom of the table and 
     * at the same time empty.
     * <p>
     * If false is given this CellEditor will not create any rows and simply edit the last cell
     * of the table. If true is given every Key or Traverse-Event will create a new row if needed.
     * 
     * @return
     */
    public boolean isRowCreating() {
        return rowCreating;
    }
    
    /**
     * Indicates if this CellEditor creates new rows if requested (true) or not (false).
     * @param appendRows
     */
    public void setRowCreating(boolean appendRows) {
        this.rowCreating = appendRows;
    }
    
    /*
     * FIXME Update nach KeyEvent statt nach verlassen der Zelle
     *  warscheinlich nicht noetig, da TableEditorImpl modifiery verstaendigt, wenn Zelle focus verliert!
     * Ist allerdings noetig fuer on-the-fly editieren (bei jedem Tastendruck) fuer echtzeit problemmarker und 
     * contentassist. (-> modifyListener, der Modifyer informiert; doSetValue muss deswegen beim setzten des Textes
     * modifyListener entfernen)
     */
//    private void notifyModifier(Object newValue) {
//        if(newValue!=null){
//            Object selectedElement= ((IStructuredSelection) fTableViewer.getSelection()).getFirstElement();
//            fTableViewer.getCellModifier().modify(selectedElement, fProperty, newValue);
//            
//        }
//    }
    

    /**
     * Reimplementation of the <code>CellEditor</code> method. Needed to access the
     * control.
     * <p>
     * Hides this cell editor's control. Does nothing if this 
     * cell editor is not visible.
     */
    public void deactivate() {
        if (control != null && !control.isDisposed())
            control.setVisible(false);
    }

    /**
     * Reimplementation of the <code>CellEditor</code> method. Needed to access the
     * control.
     * <p>
     * Disposes of this cell editor and frees any associated SWT resources.
     */
    public void dispose() {
        if (control != null && !control.isDisposed()) {
            control.dispose();
        }
        control = null;
    }
    
    /**
     * Reimplementation of the <code>CellEditor</code> method. Needed to access the
     * control.
     * <p>
     * Returns the control used to implement this cell editor.
     *
     * @return the control, or <code>null</code> if this cell editor has no control
     */
    public Control getControl() {
        return control;
    }

    /**
     * Reimplementation of the <code>CellEditor</code> method. Needed to access the
     * control.
     * <p>
     * Returns whether this cell editor is activated.
     *
     * @return <code>true</code> if this cell editor's control is
     *   currently visible, and <code>false</code> if not visible
     */
    public boolean isActivated() {
        return control != null && control.isVisible();
    }
}
