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

package org.faktorips.devtools.htmlexport.pages.standard;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.faktorips.devtools.htmlexport.generators.IGenerator;
import org.faktorips.devtools.htmlexport.generators.ILayouter;
import org.faktorips.devtools.htmlexport.generators.html.HtmlLayouter;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.test.documentor.AbstractHtmlExportTest;
import org.xml.sax.SAXException;

public abstract class AbstractXmlUnitHtmlExportTest extends AbstractHtmlExportTest {

    private static class HtmlExportXmlUnitTest extends XMLTestCase {
        @Override
        public void assertXpathExists(String xml, String xPath) throws IOException, SAXException {
            String xmlWithoutDoctypeDeclaration = prepareXml(xml);

            try {
                super.assertXpathExists(xPath, xmlWithoutDoctypeDeclaration);
            } catch (XpathException e) {
                throw new RuntimeException("Fehler bei XPath: " + xPath, e);
            }
        }

        @Override
        public void assertXpathNotExists(String xml, String xPath) throws IOException, SAXException {
            String xmlWithoutDoctypeDeclaration = prepareXml(xml);

            try {
                super.assertXpathNotExists(xPath, xmlWithoutDoctypeDeclaration);
            } catch (XpathException e) {
                throw new RuntimeException("Fehler bei XPath: " + xPath, e);
            }
        }

        private String prepareXml(String xml) {
            return xml.replaceFirst("<html .+\n", "<html>\n").replaceFirst("<!DOCTYPE .+\n", "").trim();
        }
    }

    private ILayouter layouter;

    public AbstractXmlUnitHtmlExportTest() {
        super();
    }

    public AbstractXmlUnitHtmlExportTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        layouter = new HtmlLayouter("");
    }

    protected void assertXPathExists(PageElement pageElement, String xPath) throws Exception {
        new HtmlExportXmlUnitTest().assertXpathExists(createXml(pageElement), xPath);
    }

    protected void assertXPathNotExists(PageElement pageElement, String xPath) throws Exception {
        new HtmlExportXmlUnitTest().assertXpathNotExists(createXml(pageElement), xPath);
    }

    private String createXml(PageElement pageElement) throws UnsupportedEncodingException {
        pageElement.build();

        layouter.clear();
        pageElement.acceptLayouter(layouter);

        return new String(layouter.generate(), IGenerator.CHARSET);
    }

}