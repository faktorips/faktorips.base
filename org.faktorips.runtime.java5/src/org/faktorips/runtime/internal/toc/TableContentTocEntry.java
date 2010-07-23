/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.runtime.internal.toc;

import org.w3c.dom.Element;

/**
 * A {@link TocEntryObject} for table contents
 * 
 * @author dirmeier
 */
public class TableContentTocEntry extends TocEntryObject {

    public static final String XML_TAG = "TableContent";

    public static TableContentTocEntry createFromXml(Element entryElement) {
        String ipsObjectId = entryElement.getAttribute(PROPERTY_IPS_OBJECT_ID);
        String ipsObjectQualifiedName = entryElement.getAttribute(PROPERTY_IPS_OBJECT_QNAME);
        String xmlResourceName = entryElement.getAttribute(PROPERTY_XML_RESOURCE);
        String implementationClassName = entryElement.getAttribute(PROPERTY_IMPLEMENTATION_CLASS);
        return new TableContentTocEntry(ipsObjectId, ipsObjectQualifiedName, xmlResourceName, implementationClassName);
    }

    public TableContentTocEntry(String ipsObjectId, String ipsObjectQualifiedName, String xmlResourceName,
            String implementationClassName) {
        super(ipsObjectId, ipsObjectQualifiedName, xmlResourceName, implementationClassName);
    }

    @Override
    protected String getXmlElementTag() {
        return XML_TAG;
    }

}
