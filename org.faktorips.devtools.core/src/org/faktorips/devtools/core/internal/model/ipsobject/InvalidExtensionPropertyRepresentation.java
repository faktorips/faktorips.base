/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsobject;

import org.faktorips.devtools.core.model.extproperties.StringExtensionPropertyDefinition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This class is used to save invalid extension properties and makes sure they could be stored to
 * XML the same way as they were initialized.
 * <p>
 * The different extensions of this class could handle different kinds of input data depending on
 * the kind of XML initialization.
 */
public abstract class InvalidExtensionPropertyRepresentation {

    /**
     * Creates a new {@link InvalidExtensionPropertyRepresentation}. Use this if you have an
     * {@link Element} read from XML.
     */
    public static InvalidExtensionPropertyRepresentation createInvalidExtensionProperty(Element valElementElement) {
        return new InvalidExtensionPropertyXMLRepresentation(valElementElement);
    }

    /**
     * Creates a new {@link InvalidExtensionPropertyRepresentation}. Use this if you have no
     * {@link Element} but the id and the value as String.
     */
    public static InvalidExtensionPropertyRepresentation createInvalidExtensionProperty(String propertyId, String value) {
        return new InvalidExtensionPropertyStringRepresentation(propertyId, value);
    }

    /**
     * This method writes the extension property content to an XML element and appends the new
     * {@link Element} to the provided one.
     * 
     * @param extPropertiesElement The {@link Element} to which this method adds the new
     *            {@link Element}
     */
    public abstract void appendToXml(Element extPropertiesElement);

    /**
     * This implementation of {@link InvalidExtensionPropertyRepresentation} takes an
     * {@link Element} from the XML initialization and is able to save the {@link Element} in a new
     * document.
     */
    private static class InvalidExtensionPropertyXMLRepresentation extends InvalidExtensionPropertyRepresentation {

        private final Element valueElement;

        private InvalidExtensionPropertyXMLRepresentation(Element valueElement) {
            this.valueElement = valueElement;
        }

        @Override
        public void appendToXml(Element extPropertiesEl) {
            Document doc = extPropertiesEl.getOwnerDocument();
            Node importedNode = doc.importNode(valueElement, true);
            extPropertiesEl.appendChild(importedNode);
        }
    }

    /**
     * This implementation of {@link InvalidExtensionPropertyRepresentation} takes a String from the
     * XML initialization and try to store it the same way to XML. To do so it uses a
     * {@link StringExtensionPropertyDefinition}.
     */
    private static class InvalidExtensionPropertyStringRepresentation extends InvalidExtensionPropertyRepresentation {

        private final String propertyId;
        private final String value;

        private InvalidExtensionPropertyStringRepresentation(String propertyId, String value) {
            this.propertyId = propertyId;
            this.value = value;
        }

        @Override
        public void appendToXml(Element extPropertiesEl) {
            StringExtensionPropertyDefinition stringExtensionPropertyDefinition = new StringExtensionPropertyDefinition();
            ExtensionPropertyHandler.propertyToXml(propertyId, stringExtensionPropertyDefinition, value,
                    extPropertiesEl);
        }

    }

}
