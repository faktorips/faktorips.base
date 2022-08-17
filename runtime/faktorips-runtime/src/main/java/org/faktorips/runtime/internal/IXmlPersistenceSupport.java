/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal;

import org.faktorips.runtime.IClRepositoryObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Common interface for runtime classes that support XML persistence through the methods
 * {@link #initFromXml(Element)} and {@link #toXml(Document)}.
 * 
 * @author Stefan Widmaier, FaktorZehn AG
 */
public interface IXmlPersistenceSupport extends IClRepositoryObject {

    /**
     * Creates an {@link Element} (using the given document) that represents this object in XML. The
     * caller is responsible of adding the returned element to an other {@link Element} or
     * {@link Document} if required.
     * 
     * @param document the document to use for creating {@link Element}s
     * @return an {@link Element} that represents this object as XML element
     */
    Element toXml(Document document);
}
