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

import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
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
public class ExtensionPropertyValue {

    private Object value;

    private final Element valueElement;

    private final String propertyId;

    public ExtensionPropertyValue(String propertyId, Element valueElement) {
        this.propertyId = propertyId;
        this.valueElement = valueElement;
    }

    /**
     * This method writes the extension property content to an XML element and appends the new
     * {@link Element} to the provided one. TODO
     * 
     * @param extPropertiesElement The {@link Element} to which this method adds the new
     *            {@link Element}
     */
    public void appendToXml(IpsObjectPartContainer part, Element extPropertiesEl) {
        IExtensionPropertyDefinition extensionPropertyDefinition = part.getExtensionPropertyDefinition(propertyId);
        if (extensionPropertyDefinition == null) {
            if (valueElement != null) {
                Document doc = extPropertiesEl.getOwnerDocument();
                Node importedNode = doc.importNode(valueElement, true);
                extPropertiesEl.appendChild(importedNode);
            }
        } else {
            propertyToXml(propertyId, extensionPropertyDefinition, value, extPropertiesEl);
        }
    }

    private void propertyToXml(String propertyId,
            IExtensionPropertyDefinition propertyDefinition,
            Object value,
            Element extPropertiesEl) {
        Document ownerDocument = extPropertiesEl.getOwnerDocument();
        Element valueEl = createValueElement(propertyId, propertyDefinition, value, ownerDocument);
        extPropertiesEl.appendChild(valueEl);
    }

    private Element createValueElement(String propertyId,
            IExtensionPropertyDefinition propertyDefinition,
            Object value,
            Document ownerDocument) {
        Element valueEl = ownerDocument.createElement(IpsObjectPartContainer.XML_VALUE_ELEMENT);
        valueEl.setAttribute(IpsObjectPartContainer.XML_ATTRIBUTE_EXTPROPERTYID, propertyId);
        valueEl.setAttribute(IpsObjectPartContainer.XML_ATTRIBUTE_ISNULL, value == null ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
        if (value != null) {
            propertyDefinition.valueToXml(valueEl, value);
        }
        return valueEl;
    }

    public void setValue(Object object) {
        this.value = object;
    }

    public Object getValue() {
        return value;
    }

    // /**
    // * This implementation of {@link ExtensionPropertyValue} takes a String from the
    // * XML initialization and try to store it the same way to XML. To do so it uses a
    // * {@link StringExtensionPropertyDefinition}.
    // */
    // static class ExtensionPropertyValueString extends ExtensionPropertyValue {
    //
    // private final String propertyId;
    // private final String value;
    //
    // public ExtensionPropertyValueString(String propertyId, String value) {
    // this.propertyId = propertyId;
    // this.value = value;
    // }
    //
    // @Override
    // public void appendToXml(Element extPropertiesEl) {
    // StringExtensionPropertyDefinition stringExtensionPropertyDefinition = new
    // StringExtensionPropertyDefinition();
    // ExtensionPropertyHandler.propertyToXml(propertyId, stringExtensionPropertyDefinition, value,
    // extPropertiesEl);
    // }
    // }

}
