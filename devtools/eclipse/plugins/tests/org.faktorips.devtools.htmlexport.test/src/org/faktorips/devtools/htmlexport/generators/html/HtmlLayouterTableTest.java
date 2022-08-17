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

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.RowTablePageElementLayout;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TablePageElement;
import org.junit.Assert;
import org.junit.Test;

public class HtmlLayouterTableTest extends AbstractHtmlLayouterTableTest {

    @Test
    public void testLeereTabelle() throws Exception {
        TablePageElement table = new TablePageElement(getContext());

        Assert.assertTrue(StringUtils.isEmpty(layout(table)));
    }

    @Test
    public void testTabelle() throws Exception {
        int rows = 3;
        int cols = 4;
        TablePageElement table = createTable(rows, cols);

        String layout = layout(table);

        Assert.assertFalse(layout, StringUtils.isEmpty(layout));

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

        Assert.assertFalse(layout, StringUtils.isEmpty(layout));

        assertXpathExists(layout, "/table/tr[1][@class='" + Style.TABLE_HEADLINE.name() + "']");
        for (int row = 1; row < rows; row++) {
            assertXpathExists(layout,
                    "/table/tr[" + (row + 1) + "][not(@class='" + Style.TABLE_HEADLINE.name() + "')]");
        }
    }

    @Test
    public void testTabelleBorder() throws Exception {
        int rows = 3;
        int cols = 4;
        TablePageElement table = createTable(rows, cols);
        table.setBorder(true);

        String layout = layout(table);
        Assert.assertFalse(layout, StringUtils.isEmpty(layout));
        assertXpathExists(layout, "/table[@class='" + Style.BORDER.name() + "']");

        getLayouter().clear();
        table.setBorder(false);

        layout = layout(table);
        Assert.assertFalse(layout, StringUtils.isEmpty(layout));
        assertXpathExists(layout, "/table[not(@class='" + Style.BORDER.name() + "')]");

    }
}
