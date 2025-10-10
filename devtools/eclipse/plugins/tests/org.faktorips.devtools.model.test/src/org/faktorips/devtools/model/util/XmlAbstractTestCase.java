/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.util;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;

import org.faktorips.runtime.internal.XmlUtil;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * A test case that makes it easier to write test cases that read data from an XML file.
 */
public abstract class XmlAbstractTestCase {

    /**
     * Returns the XML document that is associated with the test case. This document has the same
     * name as the test case class and the ending ".XML".
     */
    public Document getTestDocument() {
        try {
            String className = getClass().getName();
            int index = className.lastIndexOf('.');
            if (index > -1) {
                className = className.substring(index + 1);
            }
            String resourceName = className + ".xml"; //$NON-NLS-1$
            InputStream is = getClass().getResourceAsStream(resourceName);
            if (is == null) {
                throw new RuntimeException("Can't find resource " + resourceName); //$NON-NLS-1$
            }
            return getDocumentBuilder().parse(is);
        } catch (SAXException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public final Document newDocument() {
        return getDocumentBuilder().newDocument();
    }

    public static final DocumentBuilder getDocumentBuilder() {
        return XmlUtil.getDocumentBuilder();
    }

}
