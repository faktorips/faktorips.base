/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.abstracttest.test;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.faktorips.util.IoUtil;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * A test case that makes it easier to write test cases that read data from an xml file.
 */
public abstract class XmlAbstractTestCase {

    public XmlAbstractTestCase() {
        super();
    }

    /**
     * Returns the xml document that is associated with the test case. This document has the same
     * name as the test case class and the ending "+.xml".
     */
    public Document getTestDocument() {
        InputStream is = null;
        try {
            String resourceName = getXmlResourceName();
            is = getClass().getResourceAsStream(resourceName);
            if (is == null) {
                throw new RuntimeException("Can't find resource " + resourceName);
            }
            return getDocumentBuilder().parse(is);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            IoUtil.close(is);
        }
    }

    /**
     * Returns the xml file name that is associated with the test case. This file name has the same
     * name as the test case class and the ending "+.xml".
     */
    public String getXmlResourceName() {
        String className = getClass().getName();
        int index = className.lastIndexOf('.');
        if (index > -1) {
            className = className.substring(index + 1);
        }
        return className + ".xml";
    }

    public final Document newDocument() {
        try {
            return getDocumentBuilder().newDocument();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public final static DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
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
