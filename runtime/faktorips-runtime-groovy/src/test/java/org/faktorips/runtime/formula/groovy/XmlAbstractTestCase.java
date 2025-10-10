/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.formula.groovy;

import java.io.InputStream;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilder;

import org.faktorips.runtime.internal.XmlUtil;
import org.w3c.dom.Document;

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
        return getDocumentBuilder().newDocument();
    }

    public static final DocumentBuilder getDocumentBuilder() {
        return XmlUtil.getDocumentBuilder();
    }

}
