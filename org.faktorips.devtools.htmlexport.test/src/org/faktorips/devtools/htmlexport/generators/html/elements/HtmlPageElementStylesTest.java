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
