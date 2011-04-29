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

package org.faktorips.runtime.internal.toc;

import org.w3c.dom.Element;

/**
 * A toc entry that represents a product component, a table, a test case or an enum content
 * identified by the qualified name. Each entry gives access to the class implementing either the
 * product component, the table, the test case or the enum content.
 * 
 * @author Jan Ortmann
 */
public abstract class TocEntryObject extends TocEntry {

    public static final String PROPERTY_ENTRYTYPE = "entryType";
    public static final String PROPERTY_IPS_OBJECT_ID = "ipsObjectId";
    public static final String PROPERTY_IPS_OBJECT_QNAME = "ipsObjectQualifiedName";

    /**
     * The identifier of the ips object (either the qualified name for a table or the runtime id for
     * a product component).
     */
    private String ipsObjectId;

    /** The qualified name of the ips object. */
    private String ipsObjectQualifiedName;

    public static TocEntryObject createFromXml(Element entryElement) {
        String entryName = entryElement.getNodeName();

        if (ProductCmptTocEntry.XML_TAG.equals(entryName)) {
            return ProductCmptTocEntry.createFromXml(entryElement);
        } else if (TableContentTocEntry.XML_TAG.equals(entryName)) {
            return TableContentTocEntry.createFromXml(entryElement);
        } else if (TestCaseTocEntry.TEST_XML_TAG.equals(entryName)) {
            return TestCaseTocEntry.createFromXml(entryElement);
        } else if (EnumContentTocEntry.XML_TAG.equals(entryName)) {
            return EnumContentTocEntry.createFromXml(entryElement);
        } else if (EnumXmlAdapterTocEntry.XML_TAG.equals(entryName)) {
            return EnumXmlAdapterTocEntry.createFromXml(entryElement);
        } else if (FormulaTestTocEntry.FORMULA_TEST_XML_TAG.equals(entryName)) {
            return FormulaTestTocEntry.createFromXml(entryElement);
        } else if (ProductCmptTypeTocEntry.XML_TAG.equals(entryName)) {
            return ProductCmptTypeTocEntry.createFromXml(entryElement);
        } else if (PolicyCmptTypeTocEntry.XML_TAG.equals(entryName)) {
            return PolicyCmptTypeTocEntry.createFromXml(entryElement);
        } else {
            throw new IllegalArgumentException("Unknown element: " + entryName);
        }

    }

    protected TocEntryObject(String ipsObjectId, String ipsObjectQualifiedName, String xmlResourceName,
            String implementationClassName) {
        super(implementationClassName, xmlResourceName);
        this.ipsObjectId = ipsObjectId;
        this.ipsObjectQualifiedName = ipsObjectQualifiedName;
    }

    /**
     * Returns the id for the object (either the runtime id if the object is a product component or
     * the qualified name if the object is a table
     */
    public String getIpsObjectId() {
        return ipsObjectId;
    }

    /**
     * Returns the qualified name of the object.
     */
    public String getIpsObjectQualifiedName() {
        return ipsObjectQualifiedName;
    }

    @Override
    protected void addToXml(Element entryElement) {
        super.addToXml(entryElement);
        entryElement.setAttribute(PROPERTY_IPS_OBJECT_ID, ipsObjectId);
        entryElement.setAttribute(PROPERTY_IPS_OBJECT_QNAME, ipsObjectQualifiedName);
    }

    @Override
    public String toString() {
        return new StringBuffer().append("TocEntry(").append(getXmlElementTag()).append(':').append(ipsObjectId)
                .append(')').toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((ipsObjectId == null) ? 0 : ipsObjectId.hashCode());
        result = prime * result + ((ipsObjectQualifiedName == null) ? 0 : ipsObjectQualifiedName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof TocEntryObject)) {
            return false;
        }
        TocEntryObject other = (TocEntryObject)obj;
        if (ipsObjectId == null) {
            if (other.ipsObjectId != null) {
                return false;
            }
        } else if (!ipsObjectId.equals(other.ipsObjectId)) {
            return false;
        }
        if (ipsObjectQualifiedName == null) {
            if (other.ipsObjectQualifiedName != null) {
                return false;
            }
        } else if (!ipsObjectQualifiedName.equals(other.ipsObjectQualifiedName)) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        return true;
    }

}
