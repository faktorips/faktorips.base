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

import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractRootPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.junit.Test;

public class HtmlRootPageElementLayouterTest extends AbstractHtmlPageElementLayouterTest {

    @Test
    public void testRootPage() throws Exception {
        String title = "Wurzel";
        String text = "Inhalt";

        AbstractRootPageElement pageElement = createRootPageElement();

        pageElement.setTitle(title);
        pageElement.addPageElements(new TextPageElement(text, TextType.BLOCK));

        HtmlRootPageElementLayouter elementLayouter = new HtmlRootPageElementLayouter(pageElement, layouter);

        elementLayouter.layout();

        // css
        assertXpathExists(layouter.generateText(), "/html/head/link[@rel='stylesheet'][@type='text/css'][@href]");

        // title
        assertXpathExists(layouter.generateText(), "/html/head[title='" + title + "']");

        // Inhalt
        assertXpathExists(layouter.generateText(), "/html/body[div='" + text + "']");

    }

    private AbstractRootPageElement createRootPageElement() {
        AbstractRootPageElement pageElement = new AbstractRootPageElement() {

            @Override
            public String getPathToRoot() {
                return "../";
            }
        };
        return pageElement;
    }

}
