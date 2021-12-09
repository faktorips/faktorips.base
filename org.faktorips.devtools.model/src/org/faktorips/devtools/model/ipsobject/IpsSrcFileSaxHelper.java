/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.ipsobject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.runtime.Assert;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Helper class to parse IPS source files via SAX.
 * 
 * @author Joerg Ortmann
 */
public abstract class IpsSrcFileSaxHelper {

    /**
     * Reads and returns all attributes of the first (root) node of the given source file.
     * 
     * @throws CoreRuntimeException if an error occurs.
     */
    public static Map<String, String> getHeaderAttributes(IIpsSrcFile file) throws CoreRuntimeException {
        AttributeReadHandler handler = new AttributeReadHandler(file.getIpsObjectType().getXmlElementName());
        parseContent(file, handler);
        return handler.getAttributeValue();
    }

    private static void parseContent(IIpsSrcFile ipsSrcFile, AttributeReadHandler handler) throws CoreRuntimeException {
        if (!ipsSrcFile.exists()) {
            return;
        }
        InputStream is = ipsSrcFile.getContentFromEnclosingResource();
        try {
            SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
            saxParser.parse(new InputSource(is), handler);
        } catch (SAXFinishedException ignored) {
            // nothing to do
        } catch (Exception e) {
            throw new CoreRuntimeException(new IpsStatus(e));
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ignored) {
                // nothing to do
            }
        }
    }

    /*
     * Exception to indicate the end of parsing. Remark (SAX 2.0 documentation): this is the only
     * way to abort the parser because parse method is synchronous, it will not return until parsing
     * has ended. If a client application wants to terminate parsing early, it should throw an
     * exception.
     */
    private static class SAXFinishedException extends SAXException {
        private static final long serialVersionUID = 1L;

        public SAXFinishedException() {
            super("Parser finished"); //$NON-NLS-1$
        }
    }

    /**
     * Handler to read the attributes of the first node and abort further parsing.
     */
    private static class AttributeReadHandler extends DefaultHandler {

        private String xmlRootElementName;
        private Map<String, String> attributeValue = new HashMap<>(10);

        public AttributeReadHandler(String xmlRootElementName) {
            Assert.isNotNull(xmlRootElementName);
            this.xmlRootElementName = xmlRootElementName;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes)
                throws SAXException {
            if (xmlRootElementName.equals(qName)) {
                int numOfattributes = attributes.getLength();
                for (int i = 0; i < numOfattributes; i++) {
                    attributeValue.put(attributes.getQName(i), attributes.getValue(i));
                }
                throw new SAXFinishedException();
            }
        }

        /**
         * Returns all attributes, found in the first (root) node.
         */
        public Map<String, String> getAttributeValue() {
            return attributeValue;
        }
    }

}
