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

import javax.xml.parsers.DocumentBuilder;

import org.faktorips.runtime.internal.XmlUtil;
import org.w3c.dom.Document;

/**
 * A test case that makes it easier to write test cases that read data from an xml file.
 */
public abstract class XmlAbstractTestCase {

    /**
     * Returns the XML document that is associated with the test case. This document has the same
     * name as the test case class and the ending "+.xml".
     */
    public Document getTestDocument() {
        try {
            String className = getClass().getName();
            int index = className.lastIndexOf('.');
            if (index > -1) {
                className = className.substring(index + 1);
            }
            String resourceName = className + ".xml";
            return getTestDocument(resourceName);
            // CSOFF: IllegalCatch
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // CSON: IllegalCatch
    }

    public Document getTestDocument(String resourceName) {
        try {
            InputStream is = getClass().getResourceAsStream(resourceName);
            if (is == null) {
                throw new RuntimeException("Can't find resource " + resourceName);
            }
            return getDocumentBuilder().parse(is);
            // CSOFF: IllegalCatch
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // CSON: IllegalCatch
    }

    public final Document newDocument() {
        try {
            return getDocumentBuilder().newDocument();
            // CSOFF: IllegalCatch
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // CSON: IllegalCatch
    }

    public static final DocumentBuilder getDocumentBuilder() {
        return XmlUtil.getDocumentBuilder();
    }

}
