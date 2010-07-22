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

package org.faktorips.devtools.core.internal.model.ipsobject;

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.core.util.XmlUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * @author Jan Ortmann
 */
public class DescriptionHelper {

    private final static String XML_ATTRIBUTE_NAME = "description"; //$NON-NLS-1$
    public final static String XML_ELEMENT_NAME = "Description"; //$NON-NLS-1$

    /**
     * Adds the description to the element.
     */
    public final static void setDescription(Element parentElement, String description) {
        Element descEl = XmlUtil.getFirstElement(parentElement, XML_ELEMENT_NAME);
        if (descEl == null) {
            descEl = parentElement.getOwnerDocument().createElement(XML_ELEMENT_NAME);
            parentElement.appendChild(descEl);
        }
        if (StringUtils.isNotEmpty(description)) {
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
    public final static String getDescription(Element element) {
        Element descEl = XmlUtil.getFirstElement(element, XML_ELEMENT_NAME);
        if (descEl == null) {
            return element.getAttribute(XML_ATTRIBUTE_NAME); // conversion of old files
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
    public final static Element getFirstNoneDescriptionElement(Element parentEl) {
        NodeList nl = parentEl.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            if (node instanceof Element && !node.getNodeName().equals(XML_ELEMENT_NAME)) {
                return (Element)node;
            }
        }
        return null;
    }

    private DescriptionHelper() {
        // Helper class not to be instantiated.
    }

}
