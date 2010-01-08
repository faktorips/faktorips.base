package org.faktorips.devtools.htmlexport.helper.html;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;

import junit.framework.TestCase;

public abstract class AbstractHtmlLayouterTest extends TestCase {

    HtmlLayouter layouter = new HtmlLayouter();

    public AbstractHtmlLayouterTest() {
        super();
    }

    public AbstractHtmlLayouterTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        layouter.clean();
        System.out.println("====================================================");
    }

    protected List<PageElement> createPageElementListe(String[] texte) {
        List<PageElement> elemente = new ArrayList<PageElement>();
        for (String text : texte) {
            elemente.add(new TextPageElement(text));
        }
        return elemente;
    }

    protected void assertContains(String html, String... containments) {
        for (String string : containments) {
            assertTrue("Nicht enthalten: " + string, html.contains(string));
        }
    }

    protected String layout(PageElement pageElement) throws UnsupportedEncodingException {
        pageElement.acceptLayouter(layouter);
        byte[] generate = layouter.generate();
    
        String html = new String(generate, "UTF-8").trim();
        System.out.println(html);
        return html;
    }

}