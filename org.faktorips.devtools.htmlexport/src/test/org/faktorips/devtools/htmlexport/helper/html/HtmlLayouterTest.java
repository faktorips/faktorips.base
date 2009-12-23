package org.faktorips.devtools.htmlexport.helper.html;

import java.util.List;

import org.faktorips.devtools.htmlexport.pages.elements.ListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.RootPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.TextType;

public class HtmlLayouterTest extends AbstractHtmlLayouterTest {

    public void testHtmlLayouterRootPageElement() throws Exception {
        RootPageElement pageElement = new RootPageElement();
        pageElement.setTitle("Test");

        String[] containments = { "<html", "</html>", "<head>", "</head>", "<title>" + pageElement.getTitle() + "</title>", "<body>", "</body>" };
        assertContains(layout(pageElement), containments);
    }

    public void testHtmlLayouterTextPageElementEinfach() throws Exception {
        String text = "text beispiel";
        TextPageElement pageElement = new TextPageElement(text);

        assertEquals(text, layout(pageElement));
    }

    public void testHtmlLayouterTextPageElementInline() throws Exception {
        String text = "text beispiel";
        TextPageElement pageElement = new TextPageElement(text, TextType.INLINE);

        assertEquals("<span>" + text + "</span>", layout(pageElement));
    }

    public void testHtmlLayouterTextPageElementBlock() throws Exception {
        String text = "text beispiel";
        TextPageElement pageElement = new TextPageElement(text, TextType.BLOCK);

        assertEquals("<div>" + text + "</div>", layout(pageElement));
    }

    public void testHtmlLayouterTextPageElementUeberschrift() throws Exception {
        String text = "text bespiel";
        TextPageElement pageElement = new TextPageElement(text, TextType.HEADING_3);

        assertEquals("<h3>" + text + "</h3>", layout(pageElement));
    }


    /*
     * da die Links ipsobjekte brauchen, muessen sie als plugin test getestet werden
     */
    /*
    public void testHtmlLayouterLinkPageElement() throws Exception {
        String text = "text beispiel";
        
        LinkPageElement pageElement = new LinkPageElement();

        assertEquals("<h3>" + text + "</h3>", layout(pageElement));
    }
    */

    public void testHtmlLayouterListPageElement() throws Exception {

        String[] texte = { "Item 1", "Punkt 2", "blablabla", "letzter Punkt" };
        List<PageElement> elementListe = createPageElementListe(texte);

        ListPageElement pageElement = new ListPageElement(elementListe);

        String html = layout(pageElement);
        assertContains(html, texte);
        
        String[] tags = { "<ul>", "<li>", "</li>", "</ul>" };
        assertContains(html, tags);
    }

    public void testHtmlLayouterListPageElementUngeordnet() throws Exception {

        String[] texte = { "Item 1", "Punkt 2", "blablabla", "letzter Punkt" };
        List<PageElement> elementListe = createPageElementListe(texte);

        ListPageElement pageElement = new ListPageElement(elementListe);
        pageElement.setOrdered(false);
        
        String html = layout(pageElement);
        assertContains(html, texte);
        
        String[] tags = { "<ol>", "<li>", "</li>", "</ol>" };
        assertContains(html, tags);
    }
}
