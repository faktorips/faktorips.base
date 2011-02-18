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

import java.util.Arrays;
import java.util.Collections;

import org.faktorips.devtools.htmlexport.pages.elements.core.ListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.junit.Test;

public class HtmlListPageElementLayouterTest extends AbstractHtmlPageElementLayouterTest {

    @Test
    public void testLeereListe() throws Exception {

        ListPageElement pageElement = new ListPageElement(Collections.<PageElement> emptyList());

        HtmlListPageElementLayouter elementLayouter = new HtmlListPageElementLayouter(pageElement, layouter);

        elementLayouter.layout();

        String generateText = layouter.generateText();

        assertEquals(0, generateText.length());
    }

    @Test
    public void testListe() throws Exception {

        String[] itemTexts = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };

        PageElement[] elements = new PageElementUtils().createTextPageElements(Arrays.asList(itemTexts));

        ListPageElement listPageElement = new ListPageElement(Arrays.asList(elements));

        HtmlListPageElementLayouter elementLayouter = new HtmlListPageElementLayouter(listPageElement, layouter);

        elementLayouter.layout();

        String generateText = layouter.generateText();

        assertXpathExists(generateText, "/ul");

        for (int i = 0; i < itemTexts.length; i++) {
            assertXpathExists(generateText, "/ul/li[" + (i + 1) + "][.='" + itemTexts[i] + "']");
        }

        assertXpathNotExists(generateText, "/ul/li[6]");

        layouter.clear();

        listPageElement.setOrdered(true);

        elementLayouter.layout();

        generateText = layouter.generateText();

        assertXpathExists(generateText, "/ol");

        for (int i = 0; i < itemTexts.length; i++) {
            assertXpathExists(generateText, "/ol/li[" + (i + 1) + "][.='" + itemTexts[i] + "']");
        }

        assertXpathNotExists(generateText, "/ol/li[6]");
    }
}
