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