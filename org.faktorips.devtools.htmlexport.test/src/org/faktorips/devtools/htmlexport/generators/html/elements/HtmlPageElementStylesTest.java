/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.generators.html.elements;

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.junit.Test;

public class HtmlPageElementStylesTest extends AbstractHtmlPageElementLayouterTest {

    @Test
    public void testNoStyles() {
        TextPageElement pageElement = new TextPageElement("testtext");

        HtmlTextPageElementLayouter elementLayouter = new HtmlTextPageElementLayouter(pageElement, layouter);

        String classes = elementLayouter.getClasses();

        assertTrue(StringUtils.isEmpty(classes));

    }

    @Test
    public void testStyleConcatenation() {
        TextPageElement pageElement = new TextPageElement("testtext");
        pageElement.addStyles(Style.ITALIC);
        pageElement.addStyles(Style.BOLD);

        HtmlTextPageElementLayouter elementLayouter = new HtmlTextPageElementLayouter(pageElement, layouter);

        String classes = elementLayouter.getClasses();

        assertTrue(classes.matches("(ITALIC BOLD|BOLD ITALIC)"));

    }

}
