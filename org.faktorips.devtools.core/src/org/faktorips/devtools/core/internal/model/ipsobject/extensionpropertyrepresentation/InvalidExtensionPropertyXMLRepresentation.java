/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsobject.extensionpropertyrepresentation;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This implementation of {@link InvalidExtensionPropertyRepresentation} takes an {@link Element}
 * from the XML initialization and is able to save the {@link Element} in a new document.
 */
public class InvalidExtensionPropertyXMLRepresentation extends InvalidExtensionPropertyRepresentation {

    private final Element valueElement;

    public InvalidExtensionPropertyXMLRepresentation(Element valueElement) {
        this.valueElement = valueElement;
    }

    @Override
    public void saveElementInXML(Element extPropertiesEl) {
        Document doc = extPropertiesEl.getOwnerDocument();
        Node importedNode = doc.importNode(valueElement, true);
        extPropertiesEl.appendChild(importedNode);
    }
}
