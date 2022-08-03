/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal.toc;

import java.util.Objects;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The class represents an entry in the repository's table of contents.
 */
public abstract class TocEntry {

    public static final String PROPERTY_XML_RESOURCE = "xmlResource";
    public static final String PROPERTY_IMPLEMENTATION_CLASS = "implementationClass";

    // The qualified name of the resource that contains the ips object's xml representation,
    // e.g. org.faktips.samples.products.motor.internal.MotorProduct2005.
    private String xmlResourceName = "";

    // the ips object's implementation class
    private String implementationClassName = "";

    public TocEntry(String implementationClassName, String xmlResource) {
        this.implementationClassName = implementationClassName;
        xmlResourceName = xmlResource;
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
        if (xmlResourceName != null && !xmlResourceName.isEmpty()) {
            element.setAttribute(PROPERTY_XML_RESOURCE, xmlResourceName);
        }
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

    @Override
    public int hashCode() {
        return Objects.hash(implementationClassName, xmlResourceName);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || !(obj instanceof TocEntry)) {
            return false;
        }
        TocEntry other = (TocEntry)obj;
        return Objects.equals(implementationClassName, other.implementationClassName)
                && Objects.equals(xmlResourceName, other.xmlResourceName);
    }

}
