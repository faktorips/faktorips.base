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
 * This class is used to save invalid extension properties and makes sure they could be stored to
 * XML the same way as they were initialized.
 * <p>
 * The different extensions of this class could handle different kinds of input data depending on
 * the kind of XML initialization.
 */
public abstract class InvalidExtensionPropertyRepresentation {

    public abstract void saveElementInXML(Element extPropertiesEl);

}
