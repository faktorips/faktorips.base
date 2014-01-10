/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * An interface that marks an object as being able to convert it's state to an XML element and vice
 * versa.
 * 
 * @author Jan Ortmann
 */
public interface XmlSupport {

    /**
     * Transforms the object to an XML element.
     * 
     * @param doc the XML document that can be used as a factory to create XML element.
     * 
     * @return the XML element representation
     */
    public Element toXml(Document doc);

    /**
     * (Re)Initializes the object's state with the data found in the XML element.
     * 
     * @param element the XML element containing the object's state
     */
    public void initFromXml(Element element);

}
