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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.junit.Test;

public class RegexTablePageElementLayoutTest {

    @Test
    public void testMoreThenOneElementInTableCell() {
        TableCellPageElement cellPageElement = mock(TableCellPageElement.class);
        when(cellPageElement.size()).thenReturn(2);

        String regex = "\\d+";
        Style[] styles = { Style.BIG, Style.BOLD };

        RegexTablePageElementLayout layout = new RegexTablePageElementLayout(regex, styles);

        when(cellPageElement.getSubElement(0)).thenReturn(new TextPageElement("123", null));
        layout.layoutCell(0, 0, cellPageElement);
        verify(cellPageElement, never()).addStyles(styles);
    }

    @Test
    public void testRegex() {
        TableCellPageElement cellPageElement = mock(TableCellPageElement.class);
        when(cellPageElement.size()).thenReturn(1);

        String regex = "\\d+";
        Style[] styles = { Style.BIG, Style.BOLD };

        RegexTablePageElementLayout layout = new RegexTablePageElementLayout(regex, styles);

        String[] textsStrings = { "123", "a", "", null, "1234d", "00000" };

        for (String text : textsStrings) {
            when(cellPageElement.getSubElement(0)).thenReturn(new TextPageElement(text, null));
            layout.layoutCell(0, 0, cellPageElement);
        }

        verify(cellPageElement, times(2)).addStyles(styles);
    }
}
