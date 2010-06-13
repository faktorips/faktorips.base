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

package org.faktorips.runtime;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.faktorips.runtime.internal.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Base class for IPS test cases.
 * 
 * @author Jan Ortmann
 */
public abstract class IpsTestCase extends TestCase {

    /**
     * Creates a new test case.
     */
    public IpsTestCase(String name) {
        super(name);
    }

    /**
     * Implementation of the JUnit <code>Test</code> method that reads the test case data (input,
     * expected output) from the test's resource, executes the business functions and compares the
     * expected result with the actual result.
     * 
     * {@inheritDoc}
     */
    @Override
    protected void runTest() throws Throwable {
        Document doc = getXmlDocument(getName());
        if (doc == null && getName().startsWith("test")) {
            doc = getXmlDocument(getName().substring(4));
        }
        if (doc == null) {
            throw new RuntimeException("Can't find resource for test case " + getName());
        }
        run(doc);
    }

    private Document getXmlDocument(String name) throws SAXException, IOException, ParserConfigurationException {
        InputStream is = getClass().getResourceAsStream(name + ".xml");
        if (is == null) {
            is = getClass().getResourceAsStream(name + ".ipstestcase");
            if (is == null) {
                return null;
            }
        }
        return getDocumentBuilder().parse(is);
    }

    protected void run(Document doc) throws Exception {
        run(doc.getDocumentElement());
    }

    protected void run(Element testCaseEl) throws Exception {
        readInput(XmlUtil.getFirstElement(testCaseEl));
        execBusinessFcts();
        readExpectedResult(XmlUtil.getFirstElement(testCaseEl, "ExpectedResult"));
        execAsserts();
    }

    /**
     * Reads the input for the test from the given Xml element.
     */
    protected abstract void readInput(Element inputEl);

    /**
     * Reads the expected result from the given Xml element.
     */
    protected abstract void readExpectedResult(Element expResultEl);

    /**
     * Executes the business function(s) to test.
     * 
     * @throws Exception Any exception thrown by the business function is considered as an error.
     */
    protected abstract void execBusinessFcts() throws Exception;

    /**
     * Compares the actual output (created by the business function) with the expected result.
     */
    protected abstract void execAsserts() throws Exception;

    protected DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setErrorHandler(new ErrorHandler() {
            public void error(SAXParseException e) throws SAXException {
                throw e;
            }

            public void fatalError(SAXParseException e) throws SAXException {
                throw e;
            }

            public void warning(SAXParseException e) throws SAXException {
                throw e;
            }
        });
        return builder;
    }

}
