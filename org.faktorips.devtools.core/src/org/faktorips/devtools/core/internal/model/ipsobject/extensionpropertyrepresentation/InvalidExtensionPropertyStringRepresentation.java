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
 * This implementation of {@link InvalidExtensionPropertyRepresentation} takes a String from XML
 * initialization and try to store it the same way to XML. To do so it uses a
 * {@link StringExtensionPropertyDefinition}.
 */
public class InvalidExtensionPropertyStringRepresentation extends InvalidExtensionPropertyRepresentation {

    private final String propertyId;
    private final String value;

    public InvalidExtensionPropertyStringRepresentation(String propertyId, String value) {
        this.propertyId = propertyId;
        this.value = value;
    }

    @Override
    public void saveElementInXML(Element extPropertiesEl) {
        StringExtensionPropertyDefinition stringExtensionPropertyDefinition = new StringExtensionPropertyDefinition();
        ExtensionPropertyHandler.propertyToXml(propertyId, stringExtensionPropertyDefinition, value, extPropertiesEl);
    }

}
