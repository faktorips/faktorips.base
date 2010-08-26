/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.model.ipsobject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsStatus;
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

    /*
     * Exception to indicate the end of parsing. Remark (SAX 2.0 documentation): this is the only
     * way to abort the parser because parse method is synchronous, it will not return until parsing
     * has ended. If a client application wants to terminate parsing early, it should throw an
     * exception.
     */
    private static class SAXFinishedException extends SAXException {
        public SAXFinishedException() {
            super("Parser finished"); //$NON-NLS-1$
        }

        private static final long serialVersionUID = 1L;
    }

    /**
     * Handler to read the attributes of the first node and abort further parsing.
     */
    private static class AttributeReadHandler extends DefaultHandler {

        private String xmlRootElementName;
        private Map<String, String> attributeValue = new HashMap<String, String>(10);

        public AttributeReadHandler(String xmlRootElementName) {
            Assert.isNotNull(xmlRootElementName);
            this.xmlRootElementName = xmlRootElementName;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
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

    /**
     * Reads and returns all attributes of the first (root) node of the given source file.
     * 
     * @throws CoreException if an error occurs.
     */
    public static Map<String, String> getHeaderAttributes(IIpsSrcFile file) throws CoreException {
        AttributeReadHandler handler = new AttributeReadHandler(file.getIpsObjectType().getXmlElementName());
        parseContent(file, handler);
        return handler.getAttributeValue();
    }

    private static void parseContent(IIpsSrcFile ipsSrcFile, AttributeReadHandler handler) throws CoreException {
        if (!ipsSrcFile.exists()) {
            return;
        }
        InputStream is = ipsSrcFile.getContentFromEnclosingResource();
        try {
            SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
            saxParser.parse(new InputSource(is), handler);
        } catch (SAXFinishedException ignored) {
        } catch (Exception e) {
            throw new CoreException(new IpsStatus(e));
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ignored) {
            }
        }
    }

}
