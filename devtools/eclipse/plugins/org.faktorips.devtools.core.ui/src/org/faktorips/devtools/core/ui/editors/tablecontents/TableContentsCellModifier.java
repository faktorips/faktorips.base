/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.tablecontents;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.TableItem;
import org.faktorips.devtools.model.tablecontents.IRow;

/**
 * A CellModifyer that can modify and retrieve values from <code>IRow</code> objects (
 * <code>ITableContents</code>).
 * 
 * @author Stefan Widmaier
 */
public class TableContentsCellModifier implements ICellModifier {

    private TableViewer tableViewer;
    private ContentPage page;

    public TableContentsCellModifier(TableViewer tableViewer, ContentPage page) {
        this.tableViewer = tableViewer;
        this.page = page;
    }

    /**
     * Returns the page's data changeable property.
     */
    @Override
    public boolean canModify(Object element, String property) {
        return page.isDataChangeable();
    }

    /**
     * Returns the value of the property of the given element. May return null.
     */
    @Override
    public String getValue(Object element, String property) {
        int columnIndex = getColumnIndexForProperty(property);
        if (columnIndex >= 0) {
            if (element instanceof IRow) {
                IRow row = (IRow)element;
                return row.getValue(columnIndex);
            }
        }
        return null;
    }

    /**
     * Refreshes the given element in the table after the modification.
     */
    @Override
    public void modify(Object element, String property, Object value) {
        int columnIndex = getColumnIndexForProperty(property);
        if (columnIndex >= 0) {
            IRow row = null;
            if (element instanceof TableItem && !((TableItem)element).isDisposed()) {
                row = (IRow)((TableItem)element).getData();
            } else if (element instanceof IRow) {
                row = (IRow)element;
            }
            if (row != null) {
                if (!Objects.equals(row.getValue(columnIndex), value)) {
                    row.setValue(columnIndex, (String)value);
                }
                if (page != null) {
                    page.refreshTable(row);
                }
            }
        }
    }

    private int getColumnIndexForProperty(String columnProperty) {
        List<Object> columnProperties = Arrays.asList(tableViewer.getColumnProperties());
        return columnProperties.indexOf(columnProperty);
    }

}
