/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
