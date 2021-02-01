/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.elements.core.table;

import org.faktorips.devtools.htmlexport.pages.elements.core.Style;

/**
 * The {@link RowTablePageElementLayout} layouts table columns and adds specified {@link Style}s to
 * the {@link TableCellPageElement}s of a column.
 * 
 * @author dicker
 * 
 */
public class ColumnTablePageElementLayout extends DefaultTablePageElementLayout {
    private int[] columns;
    private Style[] styles;

    /**
     * adds the given {@link Style}s to all cells of the given columns
     * 
     */
    public ColumnTablePageElementLayout(int[] columns, Style... styles) {
        this.columns = columns;
        this.styles = styles;
    }

    /**
     * adds the given {@link Style}s to all cells of the given column
     * 
     */
    public ColumnTablePageElementLayout(int column, Style... styles) {
        this(new int[] { column }, styles);
    }

    protected boolean isRelatedColumn(int column) {
        for (int layoutedColumn : columns) {
            if (layoutedColumn == column) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void layoutCell(int row, int column, TableCellPageElement cellPageElement) {
        if (isRelatedColumn(column)) {
            cellPageElement.addStyles(styles);
        }
    }
}
