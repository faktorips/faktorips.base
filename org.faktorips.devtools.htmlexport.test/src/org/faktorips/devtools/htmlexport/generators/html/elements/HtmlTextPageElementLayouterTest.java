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

import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.junit.Test;

public class HtmlTextPageElementLayouterTest extends AbstractHtmlPageElementLayouterTest {
    private static final String TESTTEXT = "Ich bin der Testtext";

    @Test
    public void testEinfacherText() {
        TextPageElement pageElement = new TextPageElement(TESTTEXT);

        HtmlTextPageElementLayouter elementLayouter = new HtmlTextPageElementLayouter(pageElement, layouter);

        elementLayouter.layout();

        assertEquals(TESTTEXT, layouter.generateText());
    }

    @Test
    public void testMitStyle() {
        TextPageElement pageElement = new TextPageElement(TESTTEXT);
        pageElement.addStyles(Style.BOLD);

        HtmlTextPageElementLayouter elementLayouter = new HtmlTextPageElementLayouter(pageElement, layouter);

        elementLayouter.layout();

        assertEquals("<span class=\"BOLD\">" + TESTTEXT + "</span>", layouter.generateText());
    }

    @Test
    public void testMitTextType() {
        TextPageElement pageElement = new TextPageElement(TESTTEXT);
        pageElement.setType(TextType.HEADING_4);

        HtmlTextPageElementLayouter elementLayouter = new HtmlTextPageElementLayouter(pageElement, layouter);

        elementLayouter.layout();

        assertEquals("<h4>" + TESTTEXT + "</h4>", layouter.generateText());
    }

    @Test
    public void testMitTextTypeUndStyle() {
        TextPageElement pageElement = new TextPageElement(TESTTEXT);
        pageElement.setType(TextType.HEADING_3);
        pageElement.addStyles(Style.ITALIC);

        HtmlTextPageElementLayouter elementLayouter = new HtmlTextPageElementLayouter(pageElement, layouter);

        elementLayouter.layout();

        assertEquals("<h3 class=\"ITALIC\">" + TESTTEXT + "</h3>", layouter.generateText());
    }

    @Test
    public void testAnchor() {
        TextPageElement pageElement = new TextPageElement(TESTTEXT);
        pageElement.setAnchor("anker");

        HtmlTextPageElementLayouter elementLayouter = new HtmlTextPageElementLayouter(pageElement, layouter);

        elementLayouter.layout();

        assertEquals("<a id=\"anker\"/>" + TESTTEXT, layouter.generateText());
    }

}
