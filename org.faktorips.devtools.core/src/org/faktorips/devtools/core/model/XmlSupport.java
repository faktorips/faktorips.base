/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.model;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * An interface that marks an object as being able to convert it's state to an Xml element and vice
 * versa.
 * 
 * @author Jan Ortmann
 */
public interface XmlSupport {

    /**
     * Transforms the object to an xml element.
     * 
     * @param doc the xml document that can be used as a factory to create xml elment.
     * 
     * @return the xml element representation
     */
    public Element toXml(Document doc);

    /**
     * (Re)Initializes the object's state with the data found in the xml element.
     * 
     * @param element the xml element containing the object's state
     */
    public void initFromXml(Element element);

}
