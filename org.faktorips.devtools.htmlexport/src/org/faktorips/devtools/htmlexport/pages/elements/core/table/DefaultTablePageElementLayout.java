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

/**
 * Default implementation of the {@link ITablePageElementLayout} with empty implementations of the
 * methods. You could override just the method you need and leave the other
 * 
 * @author dicker
 * 
 */
public class DefaultTablePageElementLayout implements ITablePageElementLayout {

    @Override
    public void layoutCell(int row, int column, TableCellPageElement cellPageElement) {
        // could be overridden
    }

    @Override
    public void layoutRow(int row, TableRowPageElement rowPageElement) {
        // could be overridden
    }

}
