/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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
