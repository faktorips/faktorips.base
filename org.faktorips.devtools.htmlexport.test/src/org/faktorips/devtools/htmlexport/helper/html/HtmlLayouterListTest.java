/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.htmlexport.helper.html;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.ListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.junit.Test;

public class HtmlLayouterListTest extends AbstractTestHtmlLayouter {

    private String[] items;

    @Test
    public void testList() throws Exception {
        ListPageElement liste = createList();

        String xml = layout(liste);

        for (int i = 0; i < items.length; i++) {
            assertXpathExists(xml, "/ul/li[" + (i + 1) + "][. = '" + items[i] + "']");
        }
    }

    @Test
    public void testOrderedList() throws Exception {
        ListPageElement liste = createList();
        liste.setOrdered(true);

        String xml = layout(liste);

        for (int i = 0; i < items.length; i++) {
            assertXpathExists(xml, "/ol/li[" + (i + 1) + "][. = '" + items[i] + "']");
        }
    }

    @Test
    public void testLeereListe() throws UnsupportedEncodingException {
        ListPageElement liste = new ListPageElement();

        String xml = layout(liste);
        assertTrue("Liste nicht leer: " + xml, StringUtils.isEmpty(xml));

    }

    protected ListPageElement createList() {
        items = new String[] { "Item 1", "Item 2", "Item 3" };
        PageElement[] pageElements = new PageElementUtils().createTextPageElements(Arrays.asList(items));

        ListPageElement liste = new ListPageElement(Arrays.asList(pageElements));
        return liste;
    }
}
