/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.faktorips.values.DefaultInternationalString;
import org.faktorips.values.InternationalString;
import org.faktorips.values.LocalizedString;
import org.faktorips.values.ObjectUtil;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SAX event handler for ips enumeration contents.
 *
 * @author Peter Kuntz
 */
public class EnumSaxHandler extends DefaultHandler {

    private static final String XML_ATTR_IS_NULL = "isNull";

    private static final String ENUM_VALUE_NAME = "EnumValue";

    private static final String ENUM_ATTRIBUTE_VALUE_NAME = "EnumAttributeValue";

    private List<List<Object>> enumValues = new ArrayList<>();

    private List<Object> enumValue;

    private StringBuilder stringBuilder;

    private List<LocalizedString> localizedStrings;

    private InternationalString internationalString;

    private Locale defaultLocale;

    private boolean isNull;

    private Locale descriptionLocale;

    private List<LocalizedString> descriptions = new ArrayList<>();

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (ENUM_VALUE_NAME.equals(qName)) {
            enumValue = new ArrayList<>();
        } else if (ENUM_ATTRIBUTE_VALUE_NAME.equals(qName)) {
            stringBuilder = new StringBuilder();
            isNull = Boolean.parseBoolean(attributes.getValue(XML_ATTR_IS_NULL));
        } else if (InternationalStringXmlReaderWriter.XML_TAG.equals(qName)) {
            localizedStrings = new ArrayList<>();
            String language = attributes.getValue(InternationalStringXmlReaderWriter.XML_ATTR_DEFAULT_LOCALE);
            if (language != null) {
                defaultLocale = Locale.of(language);
            }
        } else if (InternationalStringXmlReaderWriter.XML_ELEMENT_LOCALIZED_STRING.equals(qName)) {
            Locale locale = Locale.of(attributes.getValue(InternationalStringXmlReaderWriter.XML_ATTR_LOCALE));
            String text = attributes.getValue(InternationalStringXmlReaderWriter.XML_ATTR_TEXT);
            localizedStrings.add(new LocalizedString(locale, text));
        } else if (DescriptionXmlHelper.XML_ELEMENT_DESCRIPTION.equals(qName)) {
            String language = attributes.getValue(DescriptionXmlHelper.XML_ATTRIBUTE_LOCALE);
            if (language != null) {
                descriptionLocale = Locale.of(language);
            }
            stringBuilder = new StringBuilder();
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
                    enumValue.add(isNull ? null : stringBuilder.toString());
                    stringBuilder = null;
                    isNull = false;
                }
            }
        } else if (ENUM_VALUE_NAME.equals(qName)) {
            enumValues.add(enumValue);
            enumValue = null;
        } else if (InternationalStringXmlReaderWriter.XML_TAG.equals(qName)) {
            internationalString = new DefaultInternationalString(localizedStrings, ObjectUtil.defaultIfNull(
                    defaultLocale, Locale.getDefault()));
        } else if (DescriptionXmlHelper.XML_ELEMENT_DESCRIPTION.equals(qName)) {
            if (enumValue == null) {
                descriptions.add(new LocalizedString(descriptionLocale, stringBuilder.toString()));
            }
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

    public EnumContent getEnumContent() {
        Locale locale;
        if (descriptions.isEmpty()) {
            // without a description no way to know what the correct locale may be, see FIPS-5152
            locale = Locale.getDefault();
        } else {
            locale = descriptions.get(0).getLocale();
        }
        DefaultInternationalString description = new DefaultInternationalString(descriptions, locale);
        return new EnumContent(enumValues, description);
    }
}
