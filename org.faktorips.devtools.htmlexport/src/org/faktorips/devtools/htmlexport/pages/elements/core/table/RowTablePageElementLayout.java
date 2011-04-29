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
 * The {@link RowTablePageElementLayout} layouts tablerows and adds specified {@link Style}s to a
 * {@link TableRowPageElement}
 * 
 * @author dicker
 * 
 */
public class RowTablePageElementLayout extends DefaultTablePageElementLayout {

    public final static RowTablePageElementLayout HEADLINE = new RowTablePageElementLayout(0, Style.TABLE_HEADLINE);

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
