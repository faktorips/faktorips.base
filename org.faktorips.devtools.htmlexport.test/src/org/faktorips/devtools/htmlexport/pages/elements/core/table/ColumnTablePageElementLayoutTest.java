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

package org.faktorips.devtools.htmlexport.pages.elements.core.table;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.junit.Test;

public class ColumnTablePageElementLayoutTest {

    @Test
    public void testSetStylesInChosenColumns() {
        TableCellPageElement cellPageElement = mock(TableCellPageElement.class);

        int[] columns = { 1, 3, 5 };
        Style[] styles = { Style.BIG, Style.BOLD };

        ColumnTablePageElementLayout layout = new ColumnTablePageElementLayout(columns, styles);

        for (int column : columns) {
            layout.layoutCell(1, column, cellPageElement);
        }

        verify(cellPageElement, times(3)).addStyles(styles);
    }

    @Test
    public void testSetNoStylesInChosenColumns() {
        TableCellPageElement cellPageElement = mock(TableCellPageElement.class);

        int column = 3;
        Style[] styles = { Style.BIG, Style.BOLD };

        ColumnTablePageElementLayout layout = new ColumnTablePageElementLayout(column, styles);

        for (int i = 0; i < 10; i++) {
            if (i == column) {
                continue;
            }
            layout.layoutCell(1, i, cellPageElement);
        }

        verify(cellPageElement, never()).addStyles(styles);
    }
}
