/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport;

import java.io.IOException;

import junit.framework.AssertionFailedError;

import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.xml.sax.SAXException;

public abstract class AbstractHtmlExportXmlUnitLayouterTest extends XMLTestCase {

    @Override
    public void assertXpathExists(String xml, String xPath) throws IOException, SAXException {
        String xmlWithoutDoctypeDeclaration = prepareXml(xml);

        try {
            super.assertXpathExists(xPath, xmlWithoutDoctypeDeclaration);
        } catch (XpathException e) {
            throw new RuntimeException("Fehler in XPath: " + xPath, e); //$NON-NLS-1$

        } catch (AssertionFailedError e) {
            throw new AssertionFailedError("Fehler in Auswertung: " + xPath + " in:\n" + xml); //$NON-NLS-1$ 
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