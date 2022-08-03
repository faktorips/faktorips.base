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
 * The {@link AlternateRowTablePageElementLayout} layouts the even and uneven rows of a table with
 * different {@link Style}s The first row can be ignored.
 * 
 * @author dicker
 * 
 */
public class AlternateRowTablePageElementLayout extends DefaultTablePageElementLayout {

    /**
     * true if first row should be ignored (e.g. when first line is a headline!)
     */
    protected boolean ignoreFirstRow;

    /**
     * creates an {@link AlternateRowTablePageElementLayout}
     * 
     */
    public AlternateRowTablePageElementLayout(boolean ignoreFirstRow) {
        super();
        this.ignoreFirstRow = ignoreFirstRow;
    }

    @Override
    public void layoutRow(int row, TableRowPageElement rowPageElement) {
        if (rowPageElement.hasStyle(Style.TABLE_HEADLINE) || (ignoreFirstRow && row == 0)) {
            return;
        }
        rowPageElement.addStyles(getStyle(row));
    }

    private Style getStyle(int row) {
        if (row % 2 == 0) {
            return Style.TABLE_ROW_UNEVEN;
        }
        return Style.TABLE_ROW_EVEN;
    }
}
