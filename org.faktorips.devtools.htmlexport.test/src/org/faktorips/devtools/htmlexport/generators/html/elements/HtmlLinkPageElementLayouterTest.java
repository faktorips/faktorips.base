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

import org.faktorips.devtools.htmlexport.helper.path.TargetType;
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
        TargetType target = TargetType.CONTENT;
        String text = "Linktext";

        LinkPageElement pageElement = new LinkPageElement(path, target, text, null);

        HtmlLinkPageElementLayouter elementLayouter = new HtmlLinkPageElementLayouter(pageElement, layouter);

        elementLayouter.layout();

        String generateText = layouter.generateText();

        assertXpathExists(generateText, "/a[@target='" + target.getId() + "'][@href='" + PATH_TO_ROOT + path
                + FILE_EXTENSION + "'][.='" + text + "']");
    }

    @Test
    public void testLinkMitStyle() throws Exception {
        String path = "xyz/sub/file";
        TargetType target = TargetType.CONTENT;
        String text = "Linktext";

        LinkPageElement pageElement = new LinkPageElement(path, target, text, null);
        pageElement.addStyles(Style.BOLD);

        HtmlLinkPageElementLayouter elementLayouter = new HtmlLinkPageElementLayouter(pageElement, layouter);

        elementLayouter.layout();

        assertXpathExists(layouter.generateText(), "/a[@class='BOLD']");
    }

    @Test
    public void testLinkMitBlockStyle() throws Exception {
        String path = "xyz/sub/file";
        TargetType target = TargetType.CONTENT;
        String text = "Linktext";

        LinkPageElement pageElement = new LinkPageElement(path, target, text, null);
        pageElement.addStyles(Style.BLOCK);

        HtmlLinkPageElementLayouter elementLayouter = new HtmlLinkPageElementLayouter(pageElement, layouter);

        elementLayouter.layout();

        assertXpathExists(layouter.generateText(), "/div/a[.='" + text + "']");
    }

    @Test
    public void testLinkMitAnker() throws Exception {
        String path = "xyz/sub/file";
        TargetType target = TargetType.CONTENT;
        String text = "Linktext";
        String linkAnchor = "anker";

        LinkPageElement pageElement = new LinkPageElement(path, target, text, null);
        pageElement.setLinkAnchor(linkAnchor);

        HtmlLinkPageElementLayouter elementLayouter = new HtmlLinkPageElementLayouter(pageElement, layouter);

        elementLayouter.layout();

        String generateText = layouter.generateText();

        assertXpathExists(generateText, "/a[.='" + text + "'][@href='" + PATH_TO_ROOT + path + FILE_EXTENSION + "#"
                + linkAnchor + "']");
    }

}
