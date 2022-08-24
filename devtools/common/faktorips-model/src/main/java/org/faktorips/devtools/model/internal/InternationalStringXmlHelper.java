/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal;

import org.faktorips.devtools.model.IInternationalString;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Helper class to load {@link IInternationalString} from XML and sore the to XML.
 * <p>
 * The helper should not be initialized, just use the static utility methods.
 * <p>
 * With this helper you could use international strings in different manners. To achieve this you
 * store every international string into an element describing the context. For example if you want
 * to use an international string for the name and one international string for the description, you
 * have two context elements (name and description) both containing an international string. The xml
 * may look like this:
 * 
 * <pre>
 * &lt;name&gt;
 *   &lt;InternationalString&gt;
 *     &lt;LocalizedString locale="de" text="Der Name"/&gt;
 *   &lt;/InternationalString&gt;
 * &lt;/name&gt;
 * &lt;description&gt;
 *   &lt;InternationalString&gt;
 *     &lt;LocalizedString locale="de" text="Die Beschreibung"/&gt;
 *   &lt;/InternationalString&gt;
 * &lt;/description&gt;
 * </pre>
 * 
 * @author dirmeier
 */
public final class InternationalStringXmlHelper {

    private InternationalStringXmlHelper() {
        // do not instatiate
    }

    /**
     * Stores the given {@link IInternationalString} to a new XML element that is a child of the
     * given parent element. The name of the new element is given by the parameter xmlTagName
     * 
     * @param internationalString The international string to save
     * @param parentElement the parent XML element
     * @param xmlTagName the name of the new XML element holding the international string
     */
    public static void toXml(IInternationalString internationalString, Element parentElement, String xmlTagName) {
        Document doc = parentElement.getOwnerDocument();
        Element msgTextElement = doc.createElement(xmlTagName);
        parentElement.appendChild(msgTextElement);
        msgTextElement.appendChild(internationalString.toXml(doc));
    }

    /**
     * Loads an {@link IInternationalString} from an XML element. This method loads the first child
     * element with the XML tag name {@link InternationalString#XML_TAG}.
     * 
     * @param internationalString An instance of international string that will be initialized with
     *            the XML content
     * @param element The XML element holding the international string element
     */
    public static void initFromXml(IInternationalString internationalString, Element element) {
        NodeList childNodes = element.getElementsByTagName(InternationalString.XML_TAG);
        for (int j = 0; j < childNodes.getLength(); j++) {
            if (childNodes.item(j) instanceof Element) {
                Element internationalStringElement = (Element)childNodes.item(j);
                internationalString.initFromXml(internationalStringElement);
                break;
            }
        }
    }

    /**
     * Checks if the given element is an element, which holds an {@link IInternationalString}.
     * 
     * @param element the element to check
     * @return true if the element is an IInternationalString, false otherwise.
     */
    public static boolean isInternationalStringElement(Element element) {
        if (element == null) {
            return false;
        }
        NodeList childNodes = element.getElementsByTagName(InternationalString.XML_TAG);
        return childNodes.getLength() > 0;
    }
}
