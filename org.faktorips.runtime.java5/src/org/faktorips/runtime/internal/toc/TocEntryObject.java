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

import java.util.Comparator;
import java.util.TimeZone;

import org.w3c.dom.Element;

/**
 * A toc entry that represents a product component, a table, a test case or an enum content
 * identified by the qualified name. Each entry gives access to the class implementing either the
 * product component, the table, the test case or the enum content.
 * 
 * @author Jan Ortmann
 */
public abstract class TocEntryObject extends TocEntry implements ITocEntryObject {

    public static final String PROPERTY_ENTRYTYPE = "entryType";

    /**
     * The identifier of the ips object (either the qualified name for a table or the runtime id for
     * a product component).
     */
    private String ipsObjectId;

    /** The qualified name of the ips object. */
    private String ipsObjectQualifiedName;

    public static ITocEntryObject createFromXml(Element entryElement) {
        String entryName = entryElement.getNodeName();

        if (IProductCmptTocEntry.XML_TAG.equals(entryName)) {
            return ProductCmptTocEntry.createFromXml(entryElement);
        } else if (ITableContentTocEntry.XML_TAG.equals(entryName)) {
            return TableContentTocEntry.createFromXml(entryElement);
        } else if (ITestCaseTocEntry.XML_TAG.equals(entryName)) {
            return TestCaseTocEntry.createFromXml(entryElement);
        } else if (IEnumContentTocEntry.XML_TAG.equals(entryName)) {
            return EnumContentTocEntry.createFromXml(entryElement);
        } else if (IEnumXmlAdapterTocEntry.XML_TAG.equals(entryName)) {
            return EnumXmlAdapterTocEntry.createFromXml(entryElement);
        } else if (IFormulaTestTocEntry.XML_TAG.equals(entryName)) {
            return FormulaTestTocEntry.createFromXml(entryElement);
        } else if (IModelTypeTocEntry.XML_TAG.equals(entryName)) {
            return ModelTypeTocEntry.createFromXml(entryElement);
        } else {
            throw new IllegalArgumentException("Unknown element: " + entryName);
        }

    }

    protected TocEntryObject(String implementationClassName, String xmlResourceName, String ipsObjectId,
            String ipsObjectQualifiedName) {
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

    /**
     * Overridden.
     */
    @Override
    public String toString() {
        return new StringBuffer().append("TocEntry(").append(getXmlElementTag()).append(':').append(ipsObjectId)
                .append(')').toString();
    }

    static class TocEntryGeneratorComparator implements Comparator<GenerationTocEntry> {

        /**
         * {@inheritDoc}
         */
        public int compare(GenerationTocEntry first, GenerationTocEntry second) {

            long firstValidFrom = first.getValidFromInMillisec(TimeZone.getDefault());
            long secondValidFrom = second.getValidFromInMillisec(TimeZone.getDefault());

            if (firstValidFrom > secondValidFrom) {
                return -1;
            }

            if (firstValidFrom == secondValidFrom) {
                return 0;
            }

            return 1;
        }

    }

}
