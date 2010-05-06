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

/**
 * The class represents an entry in the repository's table of contents.
 */
public abstract class TocEntry implements ITocEntry {

    // The qualified name of the resource that contains the ips object's xml representation,
    // e.g. org.faktips.samples.products.motor.internal.MotorProduct2005.
    private String xmlResourceName = "";

    // the ips object's implementation class
    private String implementationClassName = "";

    public TocEntry(String implementationClassName, String xmlResource) {
        this.implementationClassName = implementationClassName;
        this.xmlResourceName = xmlResource;
    }

    public String getXmlResourceName() {
        return xmlResourceName;
    }

    public String getImplementationClassName() {
        return implementationClassName;
    }

    /**
     * Adds this instance's property values to the xml element.
     */
    protected void addToXml(Element element) {
        element.setAttribute(PROPERTY_XML_RESOURCE, xmlResourceName);
        element.setAttribute(PROPERTY_IMPLEMENTATION_CLASS, implementationClassName);
    }

    public final Element toXml(Document doc) {
        Element entryElement = doc.createElement(getXmlElementTag());
        addToXml(entryElement);
        return entryElement;
    }

    /**
     * Getting the xml element tag for this toc entry
     */
    protected abstract String getXmlElementTag();

}
