/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.runtime.internal;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SAX event handler for ips enumeration contents.
 * 
 * @author Peter Kuntz
 */
public class EnumSaxHandler extends DefaultHandler {

    private final static String ENUM_VALUE_NAME = "EnumValue";
    private final static String ENUM_ATTRIBUTE_VALUE_NAME = "EnumAttributeValue";

    private List<List<String>> enumValues = new ArrayList<List<String>>();

    private List<String> enumAttributeValues;

    private boolean nextAttributeValue = false;

    private StringBuilder enumValue = new StringBuilder();

    /**
     * {@inheritDoc}
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (ENUM_VALUE_NAME.equals(qName)) {
            enumAttributeValues = new ArrayList<String>();
            return;
        }
        if (ENUM_ATTRIBUTE_VALUE_NAME.equals(qName)) {
            nextAttributeValue = true;
            enumValue.setLength(0);
            return;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (ENUM_VALUE_NAME.equals(qName)) {
            enumValues.add(enumAttributeValues);
            enumAttributeValues = null;
            return;
        }
        if (ENUM_ATTRIBUTE_VALUE_NAME.equals(qName)) {
            nextAttributeValue = false;
            enumAttributeValues.add(enumValue.toString());
            return;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void characters(char[] buf, int offset, int len) throws SAXException {
        if (nextAttributeValue) {
            if (enumAttributeValues == null) {
                throw new SAXException("The xml content for this enumeration is not valid. Encountered a tag \""
                        + ENUM_ATTRIBUTE_VALUE_NAME + "\" that is not embedded in a tag \"" + ENUM_ATTRIBUTE_VALUE_NAME
                        + "\"");
            }
            enumValue.append(new String(buf).substring(offset, offset + len));
        }
    }

    public List<List<String>> getEnumValueList() {
        return enumValues;
    }
}