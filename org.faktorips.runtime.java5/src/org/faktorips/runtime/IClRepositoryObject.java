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

package org.faktorips.runtime;

import org.w3c.dom.Element;

/**
 * Objects that are loaded by the class loader repository need to implement this interface. The
 * class loader repository can by means of this interface assume that the object provided by the
 * repository knows how to initialize itself.
 * 
 * @author Peter Erzberger
 */
public interface IClRepositoryObject {

    /**
     * Initializes this object with the data stored in the XML element.
     * 
     * @throws NullPointerException if element is <code>null</code>.
     */
    public void initFromXml(Element element);

}
