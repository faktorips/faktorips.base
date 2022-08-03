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
        pageElement.addPageElements(new TextPageElement(text, TextType.BLOCK, getContext()));

        HtmlRootPageElementLayouter elementLayouter = new HtmlRootPageElementLayouter(pageElement, layouter);

        elementLayouter.layout();

        // css
        assertXpathExists(layouter.generateText(), "/html/head/link[@rel='stylesheet'][@type='text/css'][@href]");

        // title
        assertXpathExists(layouter.generateText(), "/html/head[starts-with(title, '" + title + " (')]");

        // Inhalt
        assertXpathExists(layouter.generateText(), "/html/body[div='" + text + "']");

    }

    private AbstractRootPageElement createRootPageElement() {
        return new AbstractRootPageElement(getContext()) {

            @Override
            public String getPathToRoot() {
                return "../";
            }

            @Override
            public boolean isContentUnit() {
                return false;
            }
        };
    }

}
