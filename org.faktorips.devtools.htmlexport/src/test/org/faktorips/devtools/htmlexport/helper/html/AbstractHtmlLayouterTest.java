package org.faktorips.devtools.htmlexport.helper.html;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;

public abstract class AbstractHtmlLayouterTest extends TestCase {

    HtmlLayouter layouter = new HtmlLayouter(".resources");

    public AbstractHtmlLayouterTest() {
        super();
    }

    public AbstractHtmlLayouterTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        layouter.clear();
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

    protected String layout(PageElement pageElement)  {
        pageElement.acceptLayouter(layouter);
        byte[] generate = layouter.generate();
    
        String html;
		try {
			html = new String(generate, "UTF-8").trim();
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
        System.out.println(html);
        return html;
    }

}