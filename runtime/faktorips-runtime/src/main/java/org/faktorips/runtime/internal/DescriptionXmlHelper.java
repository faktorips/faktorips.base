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

import org.faktorips.runtime.IpsEnumToXmlWriter;
import org.faktorips.values.DefaultInternationalString;
import org.faktorips.values.InternationalString;
import org.faktorips.values.LocalizedString;
import org.w3c.dom.Element;

import edu.umd.cs.findbugs.annotations.CheckForNull;

/**
 * Reads descriptions from and writes them to XML.
 */
public class DescriptionXmlHelper {

    public static final String XML_ELEMENT_DESCRIPTION = "Description";
    public static final String XML_ATTRIBUTE_LOCALE = InternationalStringXmlReaderWriter.XML_ATTR_LOCALE;

    private DescriptionXmlHelper() {
        // util
    }

    /**
     * Writes the given description as {@value #XML_ELEMENT_DESCRIPTION} child element(s) of the
     * given parent element.
     */
    public static void write(@CheckForNull InternationalString description, Element parentElement) {
        write(description, parentElement, false);
    }

    /**
     * Writes the given description as {@value #XML_ELEMENT_DESCRIPTION} child element(s) of the
     * given parent element, optionally skipping the actual values.
     *
     * @see IpsEnumToXmlWriter#toXml(org.w3c.dom.Document) and #writeValuesToXml for skipTextContent
     *          usage
     */
    public static void write(@CheckForNull InternationalString description,
            Element parentElement,
            boolean skipTextContent) {
        if (description != null) {
            for (LocalizedString localizedString : ((DefaultInternationalString)description).getLocalizedStrings()) {
                Element descriptionElement = parentElement.getOwnerDocument().createElement(XML_ELEMENT_DESCRIPTION);
                descriptionElement.setAttribute(XML_ATTRIBUTE_LOCALE, localizedString.getLocale().toString());
                if (!skipTextContent) {
                    descriptionElement.setTextContent(localizedString.getValue());
                }
                parentElement.appendChild(descriptionElement);
            }
        }
    }

    /**
     * Reads the description from the {@value #XML_ELEMENT_DESCRIPTION} child element(s) of the
     * given parent element.
     */
    public static InternationalString read(Element parentElement) {
        List<Element> descriptionElements = XmlUtil.getElements(parentElement, XML_ELEMENT_DESCRIPTION);
        List<LocalizedString> descriptions = new ArrayList<>(descriptionElements.size());
        for (Element descriptionElement : descriptionElements) {
            String localeCode = descriptionElement.getAttribute(XML_ATTRIBUTE_LOCALE);
            Locale locale = "".equals(localeCode) ? null : new Locale(localeCode); //$NON-NLS-1$
            String text = descriptionElement.getTextContent();
            descriptions.add(new LocalizedString(locale, text));
        }
        // FIXME: FIPS-5152 use the correct default locale from the repository
        return new DefaultInternationalString(descriptions,
                descriptions.isEmpty() ? null : descriptions.get(0).getLocale());
    }

}
