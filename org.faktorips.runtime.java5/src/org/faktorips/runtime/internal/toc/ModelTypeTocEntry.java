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

public class ModelTypeTocEntry extends TocEntryObject implements IModelTypeTocEntry {

    public static ModelTypeTocEntry createFromXml(Element entryElement) {
        String ipsObjectId = entryElement.getAttribute(PROPERTY_IPS_OBJECT_ID);
        String ipsObjectQualifiedName = entryElement.getAttribute(PROPERTY_IPS_OBJECT_QNAME);
        String xmlResourceName = entryElement.getAttribute(PROPERTY_XML_RESOURCE);
        String implementationClassName = entryElement.getAttribute(PROPERTY_IMPLEMENTATION_CLASS);
        return new ModelTypeTocEntry(ipsObjectId, ipsObjectQualifiedName, xmlResourceName, implementationClassName);
    }

    public ModelTypeTocEntry(String ipsObjectId, String ipsObjectQualifiedName, String xmlResourceName,
            String implementationClassName) {
        super(implementationClassName, xmlResourceName, ipsObjectId, ipsObjectQualifiedName);
        entryType = MODEL_TYPE_ENTRY_TYPE;
    }

}
