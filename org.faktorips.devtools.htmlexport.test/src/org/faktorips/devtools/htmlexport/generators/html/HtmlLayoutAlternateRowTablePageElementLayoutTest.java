/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.htmlexport.generators.html;

import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.AlternateRowTablePageElementLayout;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TablePageElement;
import org.junit.Test;

public class HtmlLayoutAlternateRowTablePageElementLayoutTest extends AbstractHtmlLayouterTableTest {

    @Test
    public void testAlternateRows() throws Exception {
        int rows = 5;
        TablePageElement table = createTable(rows, 2);
        table.addLayouts(new AlternateRowTablePageElementLayout(false));
        String xml = layout(table);

        for (int i = 0; i < rows; i++) {
            if (i % 2 == 1) {
                assertXpathExists(xml, "/table/tr[" + (i + 1) + "][@class='" + Style.TABLE_ROW_EVEN.name() + "']");
            } else {
                assertXpathExists(xml, "/table/tr[" + (i + 1) + "][@class='" + Style.TABLE_ROW_UNEVEN.name() + "']");
            }
        }
    }

    @Test
    public void testAlternateRowsIgnoreFirstRow() throws Exception {
        int rows = 5;
        TablePageElement table = createTable(rows, 2);
        table.addLayouts(new AlternateRowTablePageElementLayout(true));
        String xml = layout(table);

        for (int i = 0; i < rows; i++) {
            if (i == 0) {
                assertXpathExists(xml, "/table/tr[1][not(@class)]");
            } else if (i % 2 == 1) {
                assertXpathExists(xml, "/table/tr[" + (i + 1) + "][@class='" + Style.TABLE_ROW_EVEN.name() + "']");
            } else {
                assertXpathExists(xml, "/table/tr[" + (i + 1) + "][@class='" + Style.TABLE_ROW_UNEVEN.name() + "']");
            }
        }

    }

}
