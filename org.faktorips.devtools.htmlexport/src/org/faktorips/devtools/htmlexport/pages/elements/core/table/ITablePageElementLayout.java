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

/**
 * The {@link ITablePageElementLayout} is a layout for tables
 * 
 * 
 * 
 * @author dicker
 * 
 */
public interface ITablePageElementLayout {

    /**
     * layouts the given {@link TableRowPageElement}.
     * <p>
     * Use the given row to layout a specific {@link TableRowPageElement}
     * </p>
     * <p>
     * This method is called by the {@link TablePageElement}
     * </p>
     * 
     */
    public void layoutRow(int row, TableRowPageElement rowPageElement);

    /**
     * layouts the given {@link TableCellPageElement}
     * <p>
     * Use the given row and columns to layout a specific {@link TableCellPageElement}. If you want
     * to layout all cells of a column check the value of columns
     * </p>
     * <p>
     * This method is called by the {@link TableRowPageElement}
     * </p>
     * 
     */
    public void layoutCell(int row, int column, TableCellPageElement cellPageElement);
}
