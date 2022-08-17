/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime;

import org.faktorips.runtime.internal.IXmlPersistenceSupport;
import org.w3c.dom.Element;

/**
 * Objects that are loaded by the class loader repository need to implement this interface. The
 * class loader repository can by means of this interface assume that the object provided by the
 * repository knows how to initialize itself.
 * 
 * SW 09.2011: With the implementation of product variants an extension interface (
 * {@link IXmlPersistenceSupport}) was created. {@link IXmlPersistenceSupport} is not published as
 * should be this interface. Sadly this cannot be undone as it is extended by
 * {@link IProductComponentLink} and generated code relies on it.
 * 
 * @author Peter Erzberger
 */
public interface IClRepositoryObject {

    /**
     * Initializes this object with the data stored in the XML element.
     * 
     * @throws NullPointerException if element is <code>null</code>.
     */
    void initFromXml(Element element);

}
