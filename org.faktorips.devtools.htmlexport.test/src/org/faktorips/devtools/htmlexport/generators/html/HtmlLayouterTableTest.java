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

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.RowTablePageElementLayout;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TablePageElement;
import org.junit.Test;

public class HtmlLayouterTableTest extends AbstractHtmlLayouterTableTest {

    @Test
    public void testLeereTabelle() throws Exception {
        TablePageElement table = new TablePageElement();

        assertTrue(StringUtils.isEmpty(layout(table)));
    }

    @Test
    public void testTabelle() throws Exception {
        int rows = 3;
        int cols = 4;
        TablePageElement table = createTable(rows, cols);

        String layout = layout(table);

        assertFalse(layout, StringUtils.isEmpty(layout));

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                assertXpathExists(layout,
                        "/table/tr[" + (row + 1) + "][td[" + (col + 1) + "]='" + createCellContent(row, col) + "']");
            }
        }
    }

    @Test
    public void testTabelleHeadline() throws Exception {
        int rows = 3;
        int cols = 4;
        TablePageElement table = createTable(rows, cols);
        table.addLayouts(RowTablePageElementLayout.HEADLINE);

        String layout = layout(table);

        assertFalse(layout, StringUtils.isEmpty(layout));

        assertXpathExists(layout, "/table/tr[1][@class='" + Style.TABLE_HEADLINE.name() + "']");
        for (int row = 1; row < rows; row++) {
            assertXpathExists(layout, "/table/tr[" + (row + 1) + "][not(@class='" + Style.TABLE_HEADLINE.name() + "')]");
        }
    }

    @Test
    public void testTabelleBorder() throws Exception {
        int rows = 3;
        int cols = 4;
        TablePageElement table = createTable(rows, cols);
        table.setBorder(true);

        String layout = layout(table);
        assertFalse(layout, StringUtils.isEmpty(layout));
        assertXpathExists(layout, "/table[@class='" + Style.BORDER.name() + "']");

        layouter.clear();
        table.setBorder(false);

        layout = layout(table);
        assertFalse(layout, StringUtils.isEmpty(layout));
        assertXpathExists(layout, "/table[not(@class='" + Style.BORDER.name() + "')]");

    }
}
