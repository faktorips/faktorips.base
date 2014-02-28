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

public class ExtensionPropertyDefinitionXMLRepresentation extends ExtensionPropertyRepresentation {

    private Element valueElement;

    public ExtensionPropertyDefinitionXMLRepresentation(Element valueElement) {
        this.valueElement = valueElement;
    }

    @Override
    public void saveElementInXML(Document doc, Element extPropertiesEl) {
        Node importedNode = doc.importNode(valueElement, true);
        extPropertiesEl.appendChild(importedNode);
    }

    @Override
    public Object getValueFromXml(Element valueElement) {
        return this.valueElement;
    }
}
