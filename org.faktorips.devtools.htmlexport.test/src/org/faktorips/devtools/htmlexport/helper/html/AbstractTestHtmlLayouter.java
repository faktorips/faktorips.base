/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.faktorips.devtools.htmlexport.TestUtil;
import org.faktorips.devtools.htmlexport.generators.html.HtmlLayouter;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.xml.sax.SAXException;

public abstract class AbstractTestHtmlLayouter extends XMLTestCase {

    protected HtmlLayouter layouter = new HtmlLayouter(new TestUtil().createMockDocumentationContext(), ".resources"); //$NON-NLS-1$

    public AbstractTestHtmlLayouter() {
        super();
    }

    public AbstractTestHtmlLayouter(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        layouter.clear();
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
            assertTrue("Nicht enthalten: " + string, html.contains(string)); //$NON-NLS-1$
        }
    }

    protected String layout(PageElement pageElement) {
        pageElement.acceptLayouter(layouter);
        byte[] generate = layouter.generate();

        String html;
        try {
            html = new String(generate, "UTF-8").trim(); //$NON-NLS-1$
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return html;
    }

    @Override
    public void assertXpathExists(String xml, String xPath) throws IOException, SAXException {
        String xmlWithoutDoctypeDeclaration = prepareXml(xml);

        try {
            super.assertXpathExists(xPath, xmlWithoutDoctypeDeclaration);
        } catch (XpathException e) {
            throw new RuntimeException("Fehler bei XPath: " + xPath, e); //$NON-NLS-1$
        }
    }

    @Override
    public void assertXpathNotExists(String xml, String xPath) throws IOException, SAXException {
        String xmlWithoutDoctypeDeclaration = prepareXml(xml);

        try {
            super.assertXpathNotExists(xPath, xmlWithoutDoctypeDeclaration);
        } catch (XpathException e) {
            throw new RuntimeException("Fehler bei XPath: " + xPath, e); //$NON-NLS-1$
        }
    }

    private String prepareXml(String xml) {
        return xml.replaceFirst("<html .+>", "<html>").replaceFirst("<!DOCTYPE .+\n", "").trim(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }
}
