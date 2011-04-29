/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
        if (rowPageElement.hasStyle(Style.TABLE_HEADLINE)) {
            return;
        }
        if (ignoreFirstRow && row == 0) {
            return;
        }
        rowPageElement.addStyles(getStyle(row));
    }

    private Style getStyle(int row) {
        if (row % 2 == 1) {
            return Style.TABLE_ROW_EVEN;
        }
        return Style.TABLE_ROW_UNEVEN;
    }
}
