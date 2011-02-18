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

import org.faktorips.devtools.htmlexport.pages.elements.core.LinkPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.junit.Before;
import org.junit.Test;

public class HtmlLinkPageElementLayouterTest extends AbstractHtmlPageElementLayouterTest {

    private static final String PATH_TO_ROOT = "../../";
    private static final String FILE_EXTENSION = ".html";

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        layouter.setPathToRoot(PATH_TO_ROOT);
    }

    @Test
    public void testLink() throws Exception {
        String path = "xyz/sub/file";
        String target = "frame";
        String text = "Linktext";

        LinkPageElement pageElement = new LinkPageElement(path, target, text);

        HtmlLinkPageElementLayouter elementLayouter = new HtmlLinkPageElementLayouter(pageElement, layouter);

        elementLayouter.layout();

        String generateText = layouter.generateText();

        assertXpathExists(generateText, "/a[@target='" + target + "'][@href='" + PATH_TO_ROOT + path + FILE_EXTENSION
                + "'][.='" + text + "']");
    }

    @Test
    public void testLinkMitStyle() throws Exception {
        String path = "xyz/sub/file";
        String target = "frame";
        String text = "Linktext";

        LinkPageElement pageElement = new LinkPageElement(path, target, text);
        pageElement.addStyles(Style.BOLD);

        HtmlLinkPageElementLayouter elementLayouter = new HtmlLinkPageElementLayouter(pageElement, layouter);

        elementLayouter.layout();

        assertXpathExists(layouter.generateText(), "/a[@class='BOLD']");
    }

    @Test
    public void testLinkMitBlockStyle() throws Exception {
        String path = "xyz/sub/file";
        String target = "frame";
        String text = "Linktext";

        LinkPageElement pageElement = new LinkPageElement(path, target, text);
        pageElement.addStyles(Style.BLOCK);

        HtmlLinkPageElementLayouter elementLayouter = new HtmlLinkPageElementLayouter(pageElement, layouter);

        elementLayouter.layout();

        assertXpathExists(layouter.generateText(), "/div/a[.='" + text + "']");
    }

    @Test
    public void testLinkMitAnker() throws Exception {
        String path = "xyz/sub/file";
        String target = "frame";
        String text = "Linktext";
        String linkAnchor = "anker";

        LinkPageElement pageElement = new LinkPageElement(path, target, text);
        pageElement.setLinkAnchor(linkAnchor);

        HtmlLinkPageElementLayouter elementLayouter = new HtmlLinkPageElementLayouter(pageElement, layouter);

        elementLayouter.layout();

        String generateText = layouter.generateText();

        assertXpathExists(generateText, "/a[.='" + text + "'][@href='" + PATH_TO_ROOT + path + FILE_EXTENSION + "#"
                + linkAnchor + "']");
    }

}
