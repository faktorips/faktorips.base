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

package org.faktorips.devtools.core.ui.editors.tablecontents;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.TableItem;
import org.faktorips.devtools.core.model.tablecontents.IRow;

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
            if (element instanceof TableItem) {
                row = (IRow)((TableItem)element).getData();
            } else if (element instanceof IRow) {
                row = (IRow)element;
            }
            if (row != null) {
                if (value == null || !value.equals(row.getValue(columnIndex))) {
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
