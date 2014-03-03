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

import org.faktorips.devtools.core.internal.model.ipsobject.ExtensionPropertyHandler;
import org.faktorips.devtools.core.model.extproperties.StringExtensionPropertyDefinition;
import org.w3c.dom.Element;

/**
 * 
 * A concrete class extending {@link InvalidExtensionPropertyRepresentation} to save an element in a
 * XML document by using StringextensionPropertiyDefinitions.
 */
public class InvalidExtensionPropertyStringRepresentation extends InvalidExtensionPropertyRepresentation {
    private ExtensionPropertyHandler extensionPropertyHandler;

    public InvalidExtensionPropertyStringRepresentation(ExtensionPropertyHandler extensionPropertyHandler) {
        this.extensionPropertyHandler = extensionPropertyHandler;
    }

    @Override
    public void saveElementInXML(Element extPropertiesEl) {
        StringExtensionPropertyDefinition stringExtensionPropertyDefinition = new StringExtensionPropertyDefinition();
        extensionPropertyHandler.propertyToXml(extPropertiesEl, stringExtensionPropertyDefinition);
    }
}
