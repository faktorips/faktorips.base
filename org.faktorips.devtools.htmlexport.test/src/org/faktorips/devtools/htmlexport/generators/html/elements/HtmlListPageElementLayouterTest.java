/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.generators.html.elements;

import java.util.Arrays;
import java.util.Collections;

import org.faktorips.devtools.htmlexport.pages.elements.core.IPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.junit.Assert;
import org.junit.Test;

public class HtmlListPageElementLayouterTest extends AbstractHtmlPageElementLayouterTest {

    @Test
    public void testLeereListe() throws Exception {

        ListPageElement pageElement = new ListPageElement(Collections.<IPageElement> emptyList(), getContext());

        HtmlListPageElementLayouter elementLayouter = new HtmlListPageElementLayouter(pageElement, layouter);

        elementLayouter.layout();

        String generateText = layouter.generateText();

        Assert.assertEquals(0, generateText.length());
    }

    @Test
    public void testListe() throws Exception {

        String[] itemTexts = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };

        IPageElement[] elements = new PageElementUtils(getContext()).createTextPageElements(Arrays.asList(itemTexts));

        ListPageElement listPageElement = new ListPageElement(Arrays.asList(elements), getContext());

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
