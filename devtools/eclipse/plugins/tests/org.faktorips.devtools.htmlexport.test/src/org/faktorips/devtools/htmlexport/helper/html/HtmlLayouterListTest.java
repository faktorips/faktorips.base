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

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.IPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.junit.Assert;
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
    public void testLeereListe() {
        ListPageElement liste = new ListPageElement(getContext());

        String xml = layout(liste);
        Assert.assertTrue("Liste nicht leer: " + xml, StringUtils.isEmpty(xml));

    }

    protected ListPageElement createList() {
        items = new String[] { "Item 1", "Item 2", "Item 3" };
        IPageElement[] pageElements = new PageElementUtils(getContext()).createTextPageElements(Arrays.asList(items));

        return new ListPageElement(Arrays.asList(pageElements), getContext());
    }
}
