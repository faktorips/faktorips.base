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

package org.faktorips.devtools.htmlexport.generators.html.elements;

import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TableCellPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TablePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TableRowPageElement;
import org.junit.Test;

public class HtmlTablePageElementLayouterTest extends AbstractHtmlPageElementLayouterTest {

    @Test
    public void testLeereTabelle() {

        TablePageElement pageElement = new TablePageElement(true);

        HtmlTablePageElementLayouter elementLayouter = new HtmlTablePageElementLayouter(pageElement, layouter);

        elementLayouter.layout();

        String generateText = layouter.generateText();

        assertEquals(0, generateText.length());
    }

    @Test
    public void testTabelle() throws Exception {

        int rows = 5;
        int cols = 4;

        TablePageElement tablePageElement = new TablePageElement(true);

        for (int row = 0; row < rows; row++) {
            TableCellPageElement[] cellPageElements = new TableCellPageElement[cols];
            for (int col = 0; col < cols; col++) {
                cellPageElements[col] = new TableCellPageElement(new TextPageElement(createCellText(row, col)));
            }
            TableRowPageElement rowPageElement = new TableRowPageElement(cellPageElements);
            tablePageElement.addPageElements(rowPageElement);
        }

        HtmlTablePageElementLayouter elementLayouter = new HtmlTablePageElementLayouter(tablePageElement, layouter);

        elementLayouter.layout();

        String generateText = layouter.generateText();

        assertXpathExists(generateText, "/table");
        assertXpathExists(generateText, "/table[contains(@class, 'BORDER')]");

        for (int row = 0; row < rows; row++) {
            assertXpathExists(generateText, "/table/tr[" + (row + 1) + "]");
            for (int col = 0; col < cols; col++) {
                assertXpathExists(generateText, "/table/tr[" + (row + 1) + "]/td[" + (col + 1) + "][.='"
                        + createCellText(row, col) + "']");
            }
            assertXpathNotExists(generateText, "/table/tr[" + (row + 1) + "]/td[" + (cols + 2) + "]");
        }
        assertXpathNotExists(generateText, "/table/tr[" + (rows + 2) + "]");
    }

    protected String createCellText(int row, int col) {
        String cellText = row + "-" + col;
        return cellText;
    }
}
