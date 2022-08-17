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
 * The {@link RowTablePageElementLayout} layouts tablerows and adds specified {@link Style}s to a
 * {@link TableRowPageElement}
 * 
 * @author dicker
 * 
 */
public class RowTablePageElementLayout extends DefaultTablePageElementLayout {

    public static final RowTablePageElementLayout HEADLINE = new RowTablePageElementLayout(0, Style.TABLE_HEADLINE);

    private int[] rows;
    private Style[] styles;

    /**
     * adds the given {@link Style}s to all given rows
     * 
     */
    public RowTablePageElementLayout(int[] rows, Style... styles) {
        this.rows = rows;
        this.styles = styles;
    }

    /**
     * adds the given {@link Style}s to the rows
     * 
     */
    public RowTablePageElementLayout(int row, Style... styles) {
        this(new int[] { row }, styles);
    }

    @Override
    public void layoutRow(int row, TableRowPageElement rowPageElement) {
        if (isRelatedRow(row)) {
            rowPageElement.addStyles(styles);
        }
    }

    /**
     * @return true, if the given row is related
     */
    protected boolean isRelatedRow(int row) {
        for (int layoutedRow : rows) {
            if (layoutedRow == row) {
                return true;
            }
        }
        return false;
    }
}
