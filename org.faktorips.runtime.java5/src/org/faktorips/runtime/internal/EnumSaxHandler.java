/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.runtime.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.faktorips.values.IInternationalString;
import org.faktorips.values.InternationalString;
import org.faktorips.values.LocalizedString;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SAX event handler for ips enumeration contents.
 * 
 * @author Peter Kuntz
 */
public class EnumSaxHandler extends DefaultHandler {

    private static final String ENUM_VALUE_NAME = "EnumValue";

    private static final String ENUM_ATTRIBUTE_VALUE_NAME = "EnumAttributeValue";

    private List<List<Object>> enumValues = new ArrayList<List<Object>>();

    private List<Object> enumValue;

    private StringBuilder stringBuilder;

    private List<LocalizedString> localizedStrings;

    private IInternationalString internationalString;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (ENUM_VALUE_NAME.equals(qName)) {
            enumValue = new ArrayList<Object>();
        } else if (ENUM_ATTRIBUTE_VALUE_NAME.equals(qName)) {
            stringBuilder = new StringBuilder();
        } else if (InternationalStringXmlReaderWriter.XML_TAG.equals(qName)) {
            localizedStrings = new ArrayList<LocalizedString>();
        } else if (InternationalStringXmlReaderWriter.XML_ELEMENT_LOCALIZED_STRING.equals(qName)) {
            Locale locale = new Locale(attributes.getValue(InternationalStringXmlReaderWriter.XML_ATTR_LOCALE));
            String text = attributes.getValue(InternationalStringXmlReaderWriter.XML_ATTR_TEXT);
            localizedStrings.add(new LocalizedString(locale, text));
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (ENUM_ATTRIBUTE_VALUE_NAME.equals(qName)) {
            if (enumValue == null) {
                throw new SAXException("The xml content for this enumeration is not valid. Encountered a tag \""
                        + ENUM_ATTRIBUTE_VALUE_NAME + "\" that is not embedded in a tag \"" + ENUM_ATTRIBUTE_VALUE_NAME
                        + "\"");
            } else {
                if (internationalString != null) {
                    enumValue.add(internationalString);
                    internationalString = null;
                } else {
                    enumValue.add(stringBuilder.toString());
                    stringBuilder = null;
                }
            }
        } else if (ENUM_VALUE_NAME.equals(qName)) {
            enumValues.add(enumValue);
            enumValue = null;
        } else if (InternationalStringXmlReaderWriter.XML_TAG.equals(qName)) {
            internationalString = new InternationalString(localizedStrings);
        }
    }

    @Override
    public void characters(char[] buf, int offset, int len) throws SAXException {
        if (stringBuilder != null) {
            char[] dest = new char[len];
            System.arraycopy(buf, offset, dest, 0, len);
            stringBuilder.append(dest);
        }
    }

    public List<List<Object>> getEnumValueList() {
        return enumValues;
    }

}
