/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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
 * default implementation of the {@link TablePageElementLayout}
 * 
 * @author dicker
 * 
 */
public class DefaultTablePageElementLayout implements TablePageElementLayout {

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.faktorips.devtools.htmlexport.pages.elements.core.table.TablePageElementLayout#layoutCell
     * (int, int, org.faktorips.devtools.htmlexport.pages.elements.core.table.TableCellPageElement)
     */
    @Override
    public void layoutCell(int row, int column, TableCellPageElement cellPageElement) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.faktorips.devtools.htmlexport.pages.elements.core.table.TablePageElementLayout#layoutRow
     * (int, org.faktorips.devtools.htmlexport.pages.elements.core.table.TableRowPageElement)
     */
    @Override
    public void layoutRow(int row, TableRowPageElement rowPageElement) {
    }

}
