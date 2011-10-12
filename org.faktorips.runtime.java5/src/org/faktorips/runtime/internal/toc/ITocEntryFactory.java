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

package org.faktorips.runtime.internal.toc;

import org.w3c.dom.Element;

/**
 * A ITocEntryFactory implementation is used to load {@link TocEntryObject}s of a given type
 * identified by their XML tag.
 * 
 * @author schwering
 */
public interface ITocEntryFactory<T extends TocEntryObject> {

    /**
     * Creates a {@link TocEntryObject} from it's XML representation.
     * 
     * @param entryElement the XML element representing the {@link TocEntryObject}
     * @return a {@link TocEntryObject}
     */
    T createFromXml(Element entryElement);

    /**
     * Returns the XML tag identifying a {@link TocEntryObject} this factory can create.
     * 
     * @return the XML tag identifying a {@link TocEntryObject} this factory can create
     */
    String getXmlTag();
}
