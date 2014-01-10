/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.helper.html;

import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.junit.Test;

public class HtmlLayouterStyleTest extends AbstractTestHtmlLayouter {

    @Test
    public void testStyleBold() {
        String text = "text beispiel"; //$NON-NLS-1$
        TextPageElement pageElement = new TextPageElement(text);
        pageElement.addStyles(Style.BOLD);

        assertEquals("<span class=\"BOLD\">" + text + "</span>", layout(pageElement)); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void testStyleItalic() {
        String text = "text beispiel"; //$NON-NLS-1$
        TextPageElement pageElement = new TextPageElement(text);
        pageElement.addStyles(Style.ITALIC);

        assertEquals("<span class=\"ITALIC\">" + text + "</span>", layout(pageElement)); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void testStyleBoldAndItalic() {
        String text = "text beispiel"; //$NON-NLS-1$
        TextPageElement pageElement = new TextPageElement(text);
        pageElement.addStyles(Style.ITALIC);
        pageElement.addStyles(Style.BOLD);

        assertTrue(layout(pageElement).matches("<span class=\"(ITALIC BOLD|BOLD ITALIC)\">" + text + "</span>")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void testStyleCenter() {
        String text = "text beispiel"; //$NON-NLS-1$
        TextPageElement pageElement = new TextPageElement(text, TextType.BLOCK);
        pageElement.addStyles(Style.CENTER);

        assertEquals("<div class=\"CENTER\">" + text + "</div>", layout(pageElement)); //$NON-NLS-1$ //$NON-NLS-2$
    }
}
