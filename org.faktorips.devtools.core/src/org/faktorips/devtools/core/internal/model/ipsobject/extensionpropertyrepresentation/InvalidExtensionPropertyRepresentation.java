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

import org.w3c.dom.Element;

/**
 * 
 * This helper class shall save an invalid element so that invalid input isn't lost. The element
 * should be saved adequately in the XML document.
 */
public abstract class InvalidExtensionPropertyRepresentation {

    public abstract void saveElementInXML(Element extPropertiesEl);

}
