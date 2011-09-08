/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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
    public Element toXml(Document document);
}
