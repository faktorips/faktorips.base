/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.model.ipsobject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsStatus;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Helper class to parse ips source files via SAX.
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
    private static class SAXFinishedException extends SAXException{
        public SAXFinishedException() {
            super("Parser finished"); //$NON-NLS-1$
        }

        private static final long serialVersionUID = 1L;
    }
    
    /*
     * Handler to read the attributes of the first node and abort further parsing.
     */
    private static class AttributeReadHandler extends DefaultHandler {
        private String xmlRootElementName;
        private Map attributeValue = new HashMap(10);
        
        public AttributeReadHandler(String xmlRootElementName) {
            Assert.isNotNull(xmlRootElementName);
            this.xmlRootElementName = xmlRootElementName;
        }
        
        /**
         * {@inheritDoc}
         */
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (xmlRootElementName.equals(qName)){
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
        public Map getAttributeValue() {
            return attributeValue;
        }
    }
    
    /**
     * Reads and returns all attributes of the first (root) node of the given soure file.
     * 
     * @throws CoreException if an error occurs.
     */
    public static Map getHeaderAttributes(IIpsSrcFile file) throws CoreException {
        AttributeReadHandler handler = new AttributeReadHandler(file.getIpsObjectType().getXmlElementName());
        parseContent(file, handler);
        return handler.getAttributeValue();
    }
    
    private static void parseContent(IIpsSrcFile ipsSrcFile, AttributeReadHandler handler) throws CoreException {
        if (!ipsSrcFile.exists()){
            return;
        }
        IFile file = ipsSrcFile.getCorrespondingFile();
        if (file == null){
            return;
        }
        InputStream is = null;
        try {
            is = file.getContents();
            SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
            saxParser.parse(new InputSource(is), handler);
        } catch (SAXFinishedException ignored) {
        } catch (Exception e) {
            throw new CoreException(new IpsStatus(e));
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException ignored) {
            }
        }
    }
}
