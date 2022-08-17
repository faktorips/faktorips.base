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

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.faktorips.datatype.AbstractPrimitiveDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.util.ArgumentCheck;

/**
 * A cell editor wich delegates to different row cell editors. This cell editor could be defined as
 * cell editor in a column, but depending on the value datatype in a row different cell editors will
 * be used to edit the value in a row, e.g. in the first row a drop down and in the second row a
 * text cell editor could be used.<br>
 * Usage:
 * <ol>
 * <li>Create this cell editor with a corresponding table viewer which uses this cell editor and the
 * column index this cell editor is adapt to.
 * <li>Set the cell editors for all rows.
 * </ol>
 * 
 * @author Joerg Ortmann
 */
public class DelegateCellEditor extends CellEditor {
    // Dummy indicator for the delegate cell editor
    public static final ValueDatatype DELEGATE_VALUE_DATATYPE = new DelegateValueDatatype();

    // contains the last active cell editor
    private IpsCellEditor currentCellEditor;

    // The table viewer this cell editor is used for
    private TableViewer tableViewer;

    // the column this cell editor is adapted
    private int column;

    // The list of cell editors for each row one cell editor
    private List<CellEditor> cellEditors;

    public DelegateCellEditor(TableViewer tableViewer, int column) {
        super();
        this.tableViewer = tableViewer;
        this.column = column;
    }

    /**
     * Returns the column this cell editor is specified for.
     */
    public int getColumn() {
        return column;
    }

    /**
     * Set the cell editors. Remark: For each row a cell editor must be specified. Otherwise a
     * argument exception will be thrown.
     */
    public void setCellEditors(CellEditor[] cellEditors) {
        ArgumentCheck.isTrue(cellEditors.length == tableViewer.getTable().getItems().length);
        this.cellEditors = Arrays.asList(cellEditors);
    }

    /*
     * Returns the current cell editor.
     */
    private IpsCellEditor getCurrent() {
        if (currentCellEditor == null) {
            currentCellEditor = getCurrentBySelectedRow();
        }
        return currentCellEditor;
    }

    @Override
    public Control getControl() {
        return getCurrent().getControl();
    }

    @Override
    public void removeListener(ICellEditorListener listener) {
        getCurrent().removeListener(listener);
    }

    @Override
    protected Control createControl(Composite parent) {
        return getCurrent().createControl(parent);
    }

    public boolean isMappedValue() {
        return getCurrent().isMappedValue();
    }

    @Override
    protected Object doGetValue() {
        IpsCellEditor current = getCurrent();
        return current.doGetValue();
    }

    @Override
    protected void doSetFocus() {
        IpsCellEditor current = getCurrent();
        current.doSetFocus();
    }

    @Override
    protected void doSetValue(Object value) {
        IpsCellEditor current = getCurrent();
        current.doSetValue(value);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        getControl().setVisible(false);
        currentCellEditor = null;
    }

    /*
     * Returns the cell editor using the current selected row in the table. First the current
     * selected row will be determined and then the corresponding cell editor will be returned. If
     * no cell editor is defined for the selected row index a runtime exception will be thrown.
     */
    private IpsCellEditor getCurrentBySelectedRow() {
        super.activate();
        int currentCellEditorRow = tableViewer.getTable().getSelectionIndex();
        if (currentCellEditorRow >= cellEditors.size()) {
            throw new RuntimeException(
                    "Undefined table cell editor! No table cell editor is specified for the selected row."); //$NON-NLS-1$
        }
        return ((IpsCellEditor)cellEditors.get(currentCellEditorRow));
    }

    // Dummy value datatype to indicate that the delegate cell editor is used for this datatype
    private static class DelegateValueDatatype extends AbstractPrimitiveDatatype {
        @Override
        public Object getValue(String value) {
            return null;
        }

        @Override
        public String getDefaultValue() {
            return null;
        }

        @Override
        public ValueDatatype getWrapperType() {
            return null;
        }

        @Override
        public boolean supportsCompare() {
            return false;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public String getQualifiedName() {
            return null;
        }
    }
}
