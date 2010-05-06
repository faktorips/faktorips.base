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

package org.faktorips.runtime.internal.toc;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public interface ITocEntryObject extends ITocEntry {

    public static final String PROPERTY_IPS_OBJECT_ID = "ipsObjectId";
    public static final String PROPERTY_IPS_OBJECT_QNAME = "ipsObjectQualifiedName";

    /**
     * Returns the id for the object (either the runtime id if the object is a product component or
     * the qualified name if the object is a table
     */
    public String getIpsObjectId();

    /**
     * Returns the qualified name of the object.
     */
    public String getIpsObjectQualifiedName();

    /**
     * Transforms the toc entry to xml.
     * 
     * @param doc The document used as factory for new element.
     */
    public Element toXml(Document doc);

}