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

import org.w3c.dom.Element;

/**
 * A {@link TocEntryObject} representing a enum xml adapter element
 * 
 * @author dirmeier
 */
public class EnumXmlAdapterTocEntry extends TocEntryObject {

    public static final String XML_TAG = "EnumXmlAdapter";

    public static EnumXmlAdapterTocEntry createFromXml(Element entryElement) {
        String ipsObjectId = entryElement.getAttribute(PROPERTY_IPS_OBJECT_ID);
        String implementationClassName = entryElement.getAttribute(PROPERTY_IMPLEMENTATION_CLASS);
        String qualifiedName = entryElement.getAttribute(PROPERTY_IPS_OBJECT_QNAME);
        return new EnumXmlAdapterTocEntry(ipsObjectId, qualifiedName, implementationClassName);
    }

    public EnumXmlAdapterTocEntry(String ipsObjectId, String qualifiedName, String implementationClassName) {
        super(ipsObjectId, qualifiedName, "", implementationClassName);
    }

    @Override
    protected String getXmlElementTag() {
        return XML_TAG;
    }

}
