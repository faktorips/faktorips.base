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

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractRootPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TablePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TableRowPageElement;

/**
 * Test fuer das HTML-Layout
 * 
 */
public class HtmlLayouterTest extends AbstractTestHtmlLayouter {
    public void testHtmlLayouterRootPageElement() throws Exception {
        AbstractRootPageElement pageElement = new AbstractRootPageElement() {

            @Override
            public String getPathToRoot() {
                return ""; //$NON-NLS-1$
            }

        };
        pageElement.setTitle("Test"); //$NON-NLS-1$

        String[] containments = { "<html", "</html>", "<head>", "</head>", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                "<title>" + pageElement.getTitle() + "</title>", "<body>", "</body>" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        assertContains(layout(pageElement), containments);
    }

    public void testHtmlLayouterTextPageElementEinfach() throws UnsupportedEncodingException {
        String text = "text beispiel"; //$NON-NLS-1$
        TextPageElement pageElement = new TextPageElement(text);

        assertEquals(text, layout(pageElement));
    }

    public void testHtmlLayouterTextPageElementInline() throws UnsupportedEncodingException {
        String text = "text beispiel"; //$NON-NLS-1$
        TextPageElement pageElement = new TextPageElement(text, TextType.INLINE);

        assertEquals("<span>" + text + "</span>", layout(pageElement)); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public void testHtmlLayouterTextPageElementBlock() throws UnsupportedEncodingException {
        String text = "text beispiel"; //$NON-NLS-1$
        TextPageElement pageElement = new TextPageElement(text, TextType.BLOCK);

        assertEquals("<div>" + text + "</div>", layout(pageElement)); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public void testHtmlLayouterListPageElement() throws UnsupportedEncodingException {

        String[] texte = { "Item 1", "Punkt 2", "blablabla", "letzter Punkt" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        List<PageElement> elementListe = createPageElementListe(texte);

        ListPageElement pageElement = new ListPageElement(elementListe);

        String html = layout(pageElement);
        assertContains(html, texte);

        String[] tags = { "<ul>", "<li>", "</li>", "</ul>" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        assertContains(html, tags);
    }

    public void testHtmlLayouterListPageElementUngeordnet() throws UnsupportedEncodingException {

        String[] texte = { "Item 1", "Punkt 2", "blablabla", "letzter Punkt" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        List<PageElement> elementListe = createPageElementListe(texte);

        ListPageElement pageElement = new ListPageElement(elementListe);
        pageElement.setOrdered(true);

        String html = layout(pageElement);
        assertContains(html, texte);

        String[] tags = { "<ol>", "<li>", "</li>", "</ol>" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        assertContains(html, tags);
    }

    public void testHtmlLayouterTablePageElement() throws UnsupportedEncodingException {
        int rows = 3;
        int cols = 4;

        String[][] texte = new String[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                texte[i][j] = i + "-" + j; //$NON-NLS-1$
            }
        }

        TablePageElement table = new TablePageElement();

        for (String[] zeile : texte) {
            table.addPageElements(new TableRowPageElement(createPageElementListe(zeile).toArray(new TextPageElement[0])));

        }

        String html = layout(table);
        for (String[] element : texte) {
            assertContains(html, element);
        }

        String[] tags = { "<table", "<tr>", "<td>", "</td>", "</tr>", "</table>" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        assertContains(html, tags);
    }
}
