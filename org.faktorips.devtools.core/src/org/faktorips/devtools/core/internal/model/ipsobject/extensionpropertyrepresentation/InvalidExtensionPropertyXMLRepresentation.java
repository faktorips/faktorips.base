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
 * 
 * A concrete class extending {@link InvalidExtensionPropertyRepresentation} to save an element in
 * an XML document.
 */
public class InvalidExtensionPropertyXMLRepresentation extends InvalidExtensionPropertyRepresentation {

    private Element valueElement;

    public InvalidExtensionPropertyXMLRepresentation(Element valueElement) {
        this.valueElement = valueElement;
    }

    @Override
    public void saveElementInXML(Element extPropertiesEl) {
        Document doc = valueElement.getOwnerDocument();
        Node importedNode = doc.importNode(valueElement, true);
        extPropertiesEl.appendChild(importedNode);
    }
}
