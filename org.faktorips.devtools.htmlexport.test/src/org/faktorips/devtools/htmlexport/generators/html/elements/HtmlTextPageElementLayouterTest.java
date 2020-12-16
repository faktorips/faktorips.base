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

import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.junit.Assert;
import org.junit.Test;

public class HtmlTextPageElementLayouterTest extends AbstractHtmlPageElementLayouterTest {
    private static final String TESTTEXT = "Ich bin der Testtext";

    @Test
    public void testEinfacherText() {
        TextPageElement pageElement = new TextPageElement(TESTTEXT, getContext());

        HtmlTextPageElementLayouter elementLayouter = new HtmlTextPageElementLayouter(pageElement, layouter);

        elementLayouter.layout();

        Assert.assertEquals(TESTTEXT, layouter.generateText());
    }

    @Test
    public void testMitStyle() {
        TextPageElement pageElement = new TextPageElement(TESTTEXT, getContext());
        pageElement.addStyles(Style.BOLD);

        HtmlTextPageElementLayouter elementLayouter = new HtmlTextPageElementLayouter(pageElement, layouter);

        elementLayouter.layout();

        Assert.assertEquals("<span class=\"BOLD\">" + TESTTEXT + "</span>", layouter.generateText());
    }

    @Test
    public void testMitTextType() {
        TextPageElement pageElement = new TextPageElement(TESTTEXT, getContext());
        pageElement.setType(TextType.HEADING_4);

        HtmlTextPageElementLayouter elementLayouter = new HtmlTextPageElementLayouter(pageElement, layouter);

        elementLayouter.layout();

        Assert.assertEquals("<h4>" + TESTTEXT + "</h4>", layouter.generateText());
    }

    @Test
    public void testMitTextTypeUndStyle() {
        TextPageElement pageElement = new TextPageElement(TESTTEXT, getContext());
        pageElement.setType(TextType.HEADING_3);
        pageElement.addStyles(Style.ITALIC);

        HtmlTextPageElementLayouter elementLayouter = new HtmlTextPageElementLayouter(pageElement, layouter);

        elementLayouter.layout();

        Assert.assertEquals("<h3 class=\"ITALIC\">" + TESTTEXT + "</h3>", layouter.generateText());
    }

    @Test
    public void testAnchor() {
        TextPageElement pageElement = new TextPageElement(TESTTEXT, getContext());
        pageElement.setAnchor("anker");

        HtmlTextPageElementLayouter elementLayouter = new HtmlTextPageElementLayouter(pageElement, layouter);

        elementLayouter.layout();

        Assert.assertEquals("<a id=\"anker\"/>" + TESTTEXT, layouter.generateText());
    }

}
