/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.helper.html;

import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;

public class HtmlLayouterStyleTest extends AbstractTestHtmlLayouter {

    public void testStyleBold() {
        String text = "text beispiel"; //$NON-NLS-1$
        TextPageElement pageElement = new TextPageElement(text);
        pageElement.addStyles(Style.BOLD);

        assertEquals("<span class=\"BOLD\">" + text + "</span>", layout(pageElement)); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public void testStyleItalic() {
        String text = "text beispiel"; //$NON-NLS-1$
        TextPageElement pageElement = new TextPageElement(text);
        pageElement.addStyles(Style.ITALIC);

        assertEquals("<span class=\"ITALIC\">" + text + "</span>", layout(pageElement)); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public void testStyleBoldAndItalic() {
        String text = "text beispiel"; //$NON-NLS-1$
        TextPageElement pageElement = new TextPageElement(text);
        pageElement.addStyles(Style.ITALIC);
        pageElement.addStyles(Style.BOLD);

        assertTrue(layout(pageElement).matches("<span class=\"(ITALIC BOLD|BOLD ITALIC)\">" + text + "</span>")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public void testStyleCenter() {
        String text = "text beispiel"; //$NON-NLS-1$
        TextPageElement pageElement = new TextPageElement(text, TextType.BLOCK);
        pageElement.addStyles(Style.CENTER);

        assertEquals("<div class=\"CENTER\">" + text + "</div>", layout(pageElement)); //$NON-NLS-1$ //$NON-NLS-2$
    }
}
