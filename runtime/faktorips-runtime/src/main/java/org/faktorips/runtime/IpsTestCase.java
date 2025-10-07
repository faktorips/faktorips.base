/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.faktorips.runtime.internal.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import junit.framework.TestCase;

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
        return getDocumentBuilder().parse(new InputSource(new InputStreamReader(is, StandardCharsets.UTF_8)));
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

    @SuppressWarnings("unused")
    protected DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
        return XmlUtil.getDocumentBuilder();
    }

}
