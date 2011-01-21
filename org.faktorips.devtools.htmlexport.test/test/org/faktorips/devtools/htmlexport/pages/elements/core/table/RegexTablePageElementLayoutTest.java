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
import static org.mockito.Mockito.when;
import junit.framework.TestCase;

import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;

public class RegexTablePageElementLayoutTest extends TestCase {

    public void testMoreThenOneElementInTableCell() {
        TableCellPageElement cellPageElement = mock(TableCellPageElement.class);
        when(cellPageElement.size()).thenReturn(2);

        String regex = "\\d+";
        Style[] styles = { Style.BIG, Style.BOLD };

        RegexTablePageElementLayout layout = new RegexTablePageElementLayout(regex, styles);

        when(cellPageElement.getSubElement(0)).thenReturn(new TextPageElement("123"));
        layout.layoutCell(0, 0, cellPageElement);
        verify(cellPageElement, never()).addStyles(styles);
    }

    public void testRegex() {
        TableCellPageElement cellPageElement = mock(TableCellPageElement.class);
        when(cellPageElement.size()).thenReturn(1);

        String regex = "\\d+";
        Style[] styles = { Style.BIG, Style.BOLD };

        RegexTablePageElementLayout layout = new RegexTablePageElementLayout(regex, styles);

        String[] textsStrings = { "123", "a", "", null, "1234d", "00000" };

        for (String text : textsStrings) {
            when(cellPageElement.getSubElement(0)).thenReturn(new TextPageElement(text));
            layout.layoutCell(0, 0, cellPageElement);
        }

        verify(cellPageElement, times(2)).addStyles(styles);
    }
}