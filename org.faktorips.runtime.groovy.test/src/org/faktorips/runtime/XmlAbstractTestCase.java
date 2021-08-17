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

import java.io.InputStream;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * A test case that makes it easier to write test cases that read data from an xml file.
 */
public abstract class XmlAbstractTestCase {

    /**
     * Returns the xml document that is associated with the test case. This document has the same
     * name as the test case class and the ending "+.xml".
     */
    public final Document getTestDocument() throws Exception {
        String className = getClass().getName();
        int index = className.lastIndexOf('.');
        if (index > -1) {
            className = className.substring(index + 1);
        }
        String resourceName = className + ".xml";

        try (InputStream is = getClass().getResourceAsStream(resourceName)) {
            return getDocumentBuilder().parse(Objects.requireNonNull(is, "Can't find resource " + resourceName));
        }
    }

    public final Document newDocument() {
        try {
            return getDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public static final DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setErrorHandler(new ErrorHandler() {
            @Override
            public void error(SAXParseException e) throws SAXException {
                throw e;
            }

            @Override
            public void fatalError(SAXParseException e) throws SAXException {
                throw e;
            }

            @Override
            public void warning(SAXParseException e) throws SAXException {
                throw e;
            }
        });
        return builder;
    }

}
