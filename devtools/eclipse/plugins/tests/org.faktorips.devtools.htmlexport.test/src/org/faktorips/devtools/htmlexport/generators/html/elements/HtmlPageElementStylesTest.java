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

import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.junit.Assert;
import org.junit.Test;

public class HtmlPageElementStylesTest extends AbstractHtmlPageElementLayouterTest {

    @Test
    public void testNoStyles() {
        TextPageElement pageElement = new TextPageElement("testtext", getContext());

        HtmlTextPageElementLayouter elementLayouter = new HtmlTextPageElementLayouter(pageElement, layouter);

        String classes = elementLayouter.getClasses();

        Assert.assertTrue(IpsStringUtils.isEmpty(classes));

    }

    @Test
    public void testStyleConcatenation() {
        TextPageElement pageElement = new TextPageElement("testtext", getContext());
        pageElement.addStyles(Style.ITALIC);
        pageElement.addStyles(Style.BOLD);

        HtmlTextPageElementLayouter elementLayouter = new HtmlTextPageElementLayouter(pageElement, layouter);

        String classes = elementLayouter.getClasses();

        Assert.assertTrue(classes.matches("(ITALIC BOLD|BOLD ITALIC)"));

    }

}
