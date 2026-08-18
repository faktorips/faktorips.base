/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public abstract class AbstractHtmlExportXmlUnitLayouterTest {

    public void assertXpathExists(String xml, String xPath) throws IOException, SAXException {
        String prepared = prepareXml(xml);
        try {
            NodeList nodes = (NodeList) XPathFactory.newInstance().newXPath()
                    .evaluate(xPath, toDocument(prepared), XPathConstants.NODESET);
            if (nodes.getLength() == 0) {
                throw new AssertionError("Fehler in Auswertung: " + xPath + " in:\n" + xml); //$NON-NLS-1$
            }
        } catch (XPathExpressionException e) {
            throw new RuntimeException("Fehler in XPath: " + xPath, e); //$NON-NLS-1$
        }
    }

    public void assertXpathNotExists(String xml, String xPath) throws IOException, SAXException {
        String prepared = prepareXml(xml);
        try {
            NodeList nodes = (NodeList) XPathFactory.newInstance().newXPath()
                    .evaluate(xPath, toDocument(prepared), XPathConstants.NODESET);
            if (nodes.getLength() > 0) {
                throw new AssertionError("Fehler in Auswertung: " + xPath + " in:\n" + xml); //$NON-NLS-1$
            }
        } catch (XPathExpressionException e) {
            throw new RuntimeException("Fehler bei XPath: " + xPath, e); //$NON-NLS-1$
        }
    }

    private Document toDocument(String xml) throws SAXException, IOException {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(new InputSource(new StringReader(xml)));
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    private String prepareXml(String xml) {
        return xml.replaceFirst("<html .+>", "<html>").replaceFirst("<!DOCTYPE .+\n", "").trim(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }
}
