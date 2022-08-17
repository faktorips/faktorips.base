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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.faktorips.devtools.htmlexport.TestUtil;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.generators.html.HtmlLayouter;
import org.faktorips.devtools.htmlexport.pages.elements.core.IPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.junit.Assert;
import org.xml.sax.SAXException;

public abstract class AbstractTestHtmlLayouter extends XMLTestCase {

    private HtmlLayouter layouter = new HtmlLayouter(new TestUtil().createMockDocumentationContext(), ".resources"); //$NON-NLS-1$

    public AbstractTestHtmlLayouter() {
        super();
    }

    public AbstractTestHtmlLayouter(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        getLayouter().clear();
    }

    protected DocumentationContext getContext() {
        return getLayouter().getContext();
    }

    protected List<IPageElement> createPageElementListe(String[] texte) {
        List<IPageElement> elemente = new ArrayList<>();
        for (String text : texte) {
            elemente.add(new TextPageElement(text, getContext()));
        }
        return elemente;
    }

    protected void assertContains(String html, String... containments) {
        for (String string : containments) {
            Assert.assertTrue("Nicht enthalten: " + string, html.contains(string)); //$NON-NLS-1$
        }
    }

    protected String layout(IPageElement pageElement) {
        pageElement.acceptLayouter(getLayouter());
        byte[] generate = getLayouter().generate();

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

    public HtmlLayouter getLayouter() {
        return layouter;
    }
}
