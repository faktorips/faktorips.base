/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.helper.html;

import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.junit.Assert;
import org.junit.Test;

public class HtmlLayouterStyleTest extends AbstractTestHtmlLayouter {

    @Test
    public void testStyleBold() {
        String text = "text beispiel"; //$NON-NLS-1$
        TextPageElement pageElement = new TextPageElement(text, getContext());
        pageElement.addStyles(Style.BOLD);

        Assert.assertEquals("<span class=\"BOLD\">" + text + "</span>", layout(pageElement)); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void testStyleItalic() {
        String text = "text beispiel"; //$NON-NLS-1$
        TextPageElement pageElement = new TextPageElement(text, getContext());
        pageElement.addStyles(Style.ITALIC);

        Assert.assertEquals("<span class=\"ITALIC\">" + text + "</span>", layout(pageElement)); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void testStyleBoldAndItalic() {
        String text = "text beispiel"; //$NON-NLS-1$
        TextPageElement pageElement = new TextPageElement(text, getContext());
        pageElement.addStyles(Style.ITALIC);
        pageElement.addStyles(Style.BOLD);

        Assert.assertTrue(layout(pageElement).matches("<span class=\"(ITALIC BOLD|BOLD ITALIC)\">" + text + "</span>")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void testStyleCenter() {
        String text = "text beispiel"; //$NON-NLS-1$
        TextPageElement pageElement = new TextPageElement(text, TextType.BLOCK, getContext());
        pageElement.addStyles(Style.CENTER);

        Assert.assertEquals("<div class=\"CENTER\">" + text + "</div>", layout(pageElement)); //$NON-NLS-1$ //$NON-NLS-2$
    }
}
