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

import java.util.List;

import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractRootPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.IPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TablePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TableRowPageElement;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test fuer das HTML-Layout
 * 
 */
public class HtmlLayouterTest extends AbstractTestHtmlLayouter {
    @Test
    public void testHtmlLayouterRootPageElement() throws Exception {
        AbstractRootPageElement pageElement = new AbstractRootPageElement(getContext()) {

            @Override
            public String getPathToRoot() {
                return ""; //$NON-NLS-1$
            }

            @Override
            public boolean isContentUnit() {
                return false;
            }

        };
        pageElement.setTitle("Test"); //$NON-NLS-1$

        String[] containments = { "<html", "</html>", "<head>", "</head>", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                "<title>" + pageElement.getTitle() + " (", "<body>", "</body>" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        assertContains(layout(pageElement), containments);
    }

    @Test
    public void testHtmlLayouterTextPageElementEinfach() {
        String text = "text beispiel"; //$NON-NLS-1$
        TextPageElement pageElement = new TextPageElement(text, getContext());

        Assert.assertEquals(text, layout(pageElement));
    }

    @Test
    public void testHtmlLayouterTextPageElementInline() {
        String text = "text beispiel"; //$NON-NLS-1$
        TextPageElement pageElement = new TextPageElement(text, TextType.INLINE, getContext());

        Assert.assertEquals("<span>" + text + "</span>", layout(pageElement)); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void testHtmlLayouterTextPageElementBlock() {
        String text = "text beispiel"; //$NON-NLS-1$
        TextPageElement pageElement = new TextPageElement(text, TextType.BLOCK, getContext());

        Assert.assertEquals("<div>" + text + "</div>", layout(pageElement)); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void testHtmlLayouterListPageElement() {
        String[] texte = { "Item 1", "Punkt 2", "blablabla", "letzter Punkt" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        List<IPageElement> elementListe = createPageElementListe(texte);

        ListPageElement pageElement = new ListPageElement(elementListe, getContext());

        String html = layout(pageElement);
        assertContains(html, texte);

        String[] tags = { "<ul>", "<li>", "</li>", "</ul>" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        assertContains(html, tags);
    }

    @Test
    public void testHtmlLayouterListPageElementUngeordnet() {
        String[] texte = { "Item 1", "Punkt 2", "blablabla", "letzter Punkt" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        List<IPageElement> elementListe = createPageElementListe(texte);

        ListPageElement pageElement = new ListPageElement(elementListe, getContext());
        pageElement.setOrdered(true);

        String html = layout(pageElement);
        assertContains(html, texte);

        String[] tags = { "<ol>", "<li>", "</li>", "</ol>" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        assertContains(html, tags);
    }

    @Test
    public void testHtmlLayouterTablePageElement() {
        int rows = 3;
        int cols = 4;

        String[][] texte = new String[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                texte[i][j] = i + "-" + j; //$NON-NLS-1$
            }
        }

        TablePageElement table = new TablePageElement(getContext());

        for (String[] zeile : texte) {
            table.addPageElements(new TableRowPageElement(createPageElementListe(zeile).toArray(new TextPageElement[0]), getContext()));

        }

        String html = layout(table);
        for (String[] element : texte) {
            assertContains(html, element);
        }

        String[] tags = { "<table", "<tr>", "<td>", "</td>", "</tr>", "</table>" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        assertContains(html, tags);
    }
}
