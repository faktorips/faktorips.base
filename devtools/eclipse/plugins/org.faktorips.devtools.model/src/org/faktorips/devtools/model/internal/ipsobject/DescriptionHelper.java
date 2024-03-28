/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsobject;

import org.faktorips.devtools.model.util.XmlUtil;
import org.faktorips.runtime.internal.DescriptionXmlHelper;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * @author Jan Ortmann
 */
public class DescriptionHelper {

    public static final String XML_ELEMENT_NAME = DescriptionXmlHelper.XML_ELEMENT_DESCRIPTION;
    private static final String XML_ATTRIBUTE_NAME = "description"; //$NON-NLS-1$

    private DescriptionHelper() {
        // Helper class not to be instantiated.
    }

    /**
     * Adds the description to the element.
     */
    public static final void setDescription(Element parentElement, String description) {
        Element descEl = XmlUtil.getFirstElement(parentElement, XML_ELEMENT_NAME);
        if (descEl == null) {
            descEl = parentElement.getOwnerDocument().createElement(XML_ELEMENT_NAME);
            parentElement.appendChild(descEl);
        }
        if (IpsStringUtils.isNotEmpty(description)) {
            Text text = XmlUtil.getTextNode(descEl);
            if (text == null) {
                text = descEl.getOwnerDocument().createTextNode(description);
            } else {
                text.setData(description);
            }
            descEl.appendChild(text);
        }
    }

    /**
     * Returns the description from the XML element. Returns an empty string if the element does not
     * contain a description.
     */
    public static final String getDescription(Element element) {
        Element descEl = XmlUtil.getFirstElement(element, XML_ELEMENT_NAME);
        if (descEl == null) {
            // conversion of old files
            return element.getAttribute(XML_ATTRIBUTE_NAME);
        }
        Text text = XmlUtil.getTextNode(descEl);
        if (text == null) {
            return ""; //$NON-NLS-1$
        }
        return text.getData();
    }

    /**
     * Returns the first child element that is not the description element or <code>null</code> if
     * the parentEl does not contain such an element.
     */
    public static final Element getFirstNoneDescriptionElement(Element parentEl) {
        NodeList nl = parentEl.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            if (node instanceof Element && !node.getNodeName().equals(XML_ELEMENT_NAME)) {
                return (Element)node;
            }
        }
        return null;
    }

}
