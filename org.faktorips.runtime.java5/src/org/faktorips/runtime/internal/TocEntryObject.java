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

package org.faktorips.runtime.internal;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TimeZone;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A toc entry that represents a product component, a table, a test case or an enum content
 * identified by the qualified name. Each entry gives access to the class implementing either the
 * product component, the table, the test case or the enum content.
 * 
 * @author Jan Ortmann
 */
public class TocEntryObject extends TocEntry {

    // Entry type constants
    public final static String PRODUCT_CMPT_ENTRY_TYPE = "productComponent";
    public final static String TABLE_ENTRY_TYPE = "table";
    public final static String TEST_CASE_ENTRY_TYPE = "testCase";
    public final static String FORMULA_TEST_ENTRY_TYPE = "formulaTest";
    public final static String ENUM_CONTENT_ENTRY_TYPE = "enumContent";
    public final static String ENUM_XML_ADAPTER_TYPE = "enumXmlAdapter";
    public final static String MODEL_TYPE_ENTRY_TYPE = "modelType";

    public static final String PROPERTY_ENTRYTYPE = "entryType";
    public static final String PROPERTY_IPS_OBJECT_ID = "ipsObjectId";
    public static final String PROPERTY_IPS_OBJECT_QNAME = "ipsObjectQualifiedName";
    public static final String PROPERTY_KIND_ID = "kindId";
    public static final String PROPERTY_VERSION_ID = "versionId";
    public static final String PROPERTY_VALID_TO = "validTo";
    public static final String PROPERTY_GENERATION_IMPL_CLASS_NAME = "generationImplClassName";

    /**
     * The identifier of the ips object (either the qualified name for a table or the runtime id for
     * a product component).
     */
    private String ipsObjectId;

    /** The qualified name of the ips object. */
    private String ipsObjectQualifiedName;

    /** Indicates the type of ips object that is represented by this entry. */
    private String entryType;

    /**
     * If this entry is a product component: the (runtime) id if of the product component kind,
     * empty string otherwise.
     */
    private String kindId;

    /**
     * If this entry is a product component: the version id if of the product component kind, emtpy
     * string otherwise.
     */
    private String versionId;

    /**
     * If this entry is for a product component: the date until this product component is valid
     */
    private DateTime validTo;

    /**
     * If this entry is for a product component type: the name of the implementation class for the
     * generation object
     */
    private String generationImplClassName;

    private List<TocEntryGeneration> generationEntries = new ArrayList<TocEntryGeneration>(0);

    public final static TocEntryObject createFromXml(Element entryElement) {
        String entryType = entryElement.getAttribute(PROPERTY_ENTRYTYPE);
        String ipsObjectId = entryElement.getAttribute(PROPERTY_IPS_OBJECT_ID);
        String ipsObjectName = entryElement.getAttribute(PROPERTY_IPS_OBJECT_QNAME);
        String xmlResourceName = entryElement.getAttribute(PROPERTY_XML_RESOURCE);
        String implementationClassName = entryElement.getAttribute(PROPERTY_IMPLEMENTATION_CLASS);
        DateTime validTo = DateTime.parseIso(entryElement.getAttribute(PROPERTY_VALID_TO));
        String generationImplClassName = entryElement.getAttribute(PROPERTY_GENERATION_IMPL_CLASS_NAME);

        TocEntryObject newEntry;

        Builder builder = new Builder(ipsObjectId, implementationClassName, entryType);

        if (PRODUCT_CMPT_ENTRY_TYPE.equals(entryType)) {
            String kindId = entryElement.getAttribute(PROPERTY_KIND_ID);
            String versionId = entryElement.getAttribute(PROPERTY_VERSION_ID);
            newEntry = builder.xmlResourceName(xmlResourceName).ipsObjectQualifiedName(ipsObjectName).kindId(kindId)
                    .versionId(versionId).validTo(validTo).build();
        } else if (TABLE_ENTRY_TYPE.equals(entryType)) {
            newEntry = builder.xmlResourceName(xmlResourceName).ipsObjectQualifiedName(ipsObjectName).build();
        } else if (TEST_CASE_ENTRY_TYPE.equals(entryType)) {
            newEntry = builder.xmlResourceName(xmlResourceName).ipsObjectQualifiedName(ipsObjectName).build();
        } else if (ENUM_CONTENT_ENTRY_TYPE.equals(entryType)) {
            newEntry = builder.xmlResourceName(xmlResourceName).ipsObjectQualifiedName(ipsObjectName).build();
        } else if (ENUM_XML_ADAPTER_TYPE.equals(entryType)) {
            newEntry = builder.build();
        } else if (FORMULA_TEST_ENTRY_TYPE.equals(entryType)) {
            String kindId = entryElement.getAttribute(PROPERTY_KIND_ID);
            String versionId = entryElement.getAttribute(PROPERTY_VERSION_ID);
            newEntry = builder.xmlResourceName(xmlResourceName).ipsObjectQualifiedName(ipsObjectName).kindId(kindId)
                    .versionId(versionId).build();
        } else if (MODEL_TYPE_ENTRY_TYPE.equals(entryType)) {
            // TODO check if ipsObjectId is really needed
            newEntry = builder.xmlResourceName(xmlResourceName).ipsObjectQualifiedName(ipsObjectName)
                    .generationImplClassName(generationImplClassName).build();
        } else {
            throw new IllegalArgumentException("Unknown entry type " + entryType);
        }

        NodeList nl = entryElement.getElementsByTagName(AbstractReadonlyTableOfContents.TOC_ENTRY_XML_ELEMENT);
        newEntry.generationEntries = new ArrayList<TocEntryGeneration>(nl.getLength());
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeName().equals(AbstractReadonlyTableOfContents.TOC_ENTRY_XML_ELEMENT)) {
                newEntry.generationEntries.add(TocEntryGeneration.createFromXml(newEntry, (Element)nl.item(i)));
            }
        }

        sortGenerations(newEntry.generationEntries);

        return newEntry;
    }

    private static void sortGenerations(List<TocEntryGeneration> generations) {
        Collections.sort(generations, new TocEntryGeneratorComparator());
    }

    /**
     * Creates an entry that referes to a product component.
     */
    public final static TocEntryObject createProductCmptTocEntry(String ipsObjectId,
            String ipsObjectQualifiedName,
            String kindId,
            String versionId,
            String xmlResourceName,
            String implementationClassName,
            DateTime validTo) {
        return new Builder(ipsObjectId, implementationClassName, PRODUCT_CMPT_ENTRY_TYPE).xmlResourceName(
                xmlResourceName).ipsObjectQualifiedName(ipsObjectQualifiedName).kindId(kindId).versionId(versionId)
                .validTo(validTo).build();
    }

    /**
     * Creates an entry that references to a formula test.
     */
    public static TocEntryObject createFormulaTestTocEntry(String ipsObjectId,
            String ipsObjectQualifiedName,
            String kindId,
            String versionId,
            String xmlResourceName,
            String implementationClassName) {
        return new Builder(ipsObjectId, implementationClassName, FORMULA_TEST_ENTRY_TYPE).xmlResourceName(
                xmlResourceName).ipsObjectQualifiedName(ipsObjectQualifiedName).kindId(kindId).versionId(versionId)
                .build();
    }

    /**
     * Creates an entry that referes to a table.
     */
    public final static TocEntryObject createTableTocEntry(String ipsObjectId,
            String ipsObjectQualifiedName,
            String xmlResourceName,
            String implementationClassName) {
        return new Builder(ipsObjectId, implementationClassName, TABLE_ENTRY_TYPE).xmlResourceName(xmlResourceName)
                .ipsObjectQualifiedName(ipsObjectQualifiedName).build();
    }

    /**
     * Creates an entry that referes to a test case.
     */
    public final static TocEntryObject createTestCaseTocEntry(String ipsObjectId,
            String ipsObjectQualifiedName,
            String xmlResourceName,
            String implementationClassName) {
        return new Builder(ipsObjectId, implementationClassName, TEST_CASE_ENTRY_TYPE).xmlResourceName(xmlResourceName)
                .ipsObjectQualifiedName(ipsObjectQualifiedName).build();
    }

    /**
     * Creates an entry that refers to an enum content.
     */
    public final static TocEntryObject createEnumContentTocEntry(String ipsObjectId,
            String ipsObjectQualifiedName,
            String xmlResourceName,
            String implementationClassName) {
        return new Builder(ipsObjectId, implementationClassName, ENUM_CONTENT_ENTRY_TYPE).xmlResourceName(
                xmlResourceName).ipsObjectQualifiedName(ipsObjectQualifiedName).build();
    }

    /**
     * Creates an entry that refers to a model type.
     */
    public final static TocEntryObject createModelTypeTocEntry(String ipsObjectId,
            String ipsObjectQualifiedName,
            String xmlResourceName,
            String implementationClassName,
            String generationImplClassName) {
        return new Builder(ipsObjectId, implementationClassName, MODEL_TYPE_ENTRY_TYPE)
                .xmlResourceName(xmlResourceName).ipsObjectQualifiedName(ipsObjectQualifiedName)
                .generationImplClassName(generationImplClassName).build();
    }

    /**
     * Creates an entry that referes to an enumeration xml adapter.
     */
    public final static TocEntryObject createEnumXmlAdapterTocEntry(String ipsObjectId, String implementationClassName) {
        return new Builder(ipsObjectId, implementationClassName, ENUM_XML_ADAPTER_TYPE).build();
    }

    /**
     * 
     */
    private TocEntryObject(Builder builder) {
        super(builder.implementationClassName, builder.xmlResourceName);
        this.ipsObjectId = builder.ipsObjectId;
        this.ipsObjectQualifiedName = builder.ipsObjectQualifiedName;
        this.kindId = builder.kindId;
        this.versionId = builder.versionId;
        this.entryType = builder.entryType;
        this.validTo = builder.validTo;
        this.generationImplClassName = builder.generationImplClassName;
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

    /**
     * Returns the id of the product component kind, if this entry describes a product component,
     * otherwise an empty string.
     */
    public String getKindId() {
        return kindId;
    }

    /**
     * Returns the version id if this entry describes a product component, otherwise an empty
     * string.
     */
    public String getVersionId() {
        return versionId;
    }

    /**
     * Returns the validTo date or null if the object doesn't supports a valid to attribute.
     */
    public DateTime getValidTo() {
        return validTo;
    }

    /**
     * Returns the generation entries or an empty array if this entry does not contain any
     * generation entries.
     */
    List<TocEntryGeneration> getGenerationEntries() {
        return generationEntries;
    }

    /**
     * Returns the number of genertion entries.
     */
    public int getNumberOfGenerationEntries() {
        return generationEntries == null ? 0 : generationEntries.size();
    }

    /**
     * Sets the generation entries.
     */
    public void setGenerationEntries(List<TocEntryGeneration> entries) {
        generationEntries = entries;
        sortGenerations(generationEntries);
    }

    /**
     * Returns the {@link TocEntryGeneration} successor of the one that is found for the provided
     * validity date. Returns <code>null</code> if either no entry is found for the provided date or
     * if the found one doesn't have a successor.
     */
    public TocEntryGeneration getNextGenerationEntry(Calendar validFrom) {
        Integer index = getGenerationEntryIndex(validFrom);
        if (index == null) {
            return null;
        }
        int next = index.intValue() - 1;
        if (next >= 0 && generationEntries.size() > 0) {
            return generationEntries.get(next);
        }
        return null;
    }

    /**
     * Returns the {@link TocEntryGeneration} that is prior to the one that is found for the
     * provided validity date. Returns <code>null</code> if either no entry is found for the
     * provided date or if the found one doesn't have a predecessor.
     */
    public TocEntryGeneration getPreviousGenerationEntry(Calendar validFrom) {
        Integer index = getGenerationEntryIndex(validFrom);
        if (index == null) {
            return null;
        }
        int previous = index.intValue() + 1;
        if (previous < generationEntries.size()) {
            return generationEntries.get(previous);
        }
        return null;
    }

    /**
     * Returns the latest {@link TocEntryGeneration} with repect to the generations validity date.
     */
    public TocEntryGeneration getLatestGenerationEntry() {
        if (generationEntries.size() > 0) {
            return generationEntries.get(0);
        }
        return null;
    }

    /**
     * Returns the toc entry for the generation valid on the given effective date, or
     * <code>null</code> if no generation is effective on the given date or the effective is
     * <code>null</code>.
     */
    public TocEntryGeneration getGenerationEntry(Calendar effectiveDate) {
        Integer index = getGenerationEntryIndex(effectiveDate);
        if (index == null) {
            return null;
        }
        return generationEntries.get(index);
    }

    private Integer getGenerationEntryIndex(Calendar validFrom) {
        if (validFrom == null) {
            return null;
        }
        long effectiveTime = validFrom.getTimeInMillis();
        for (int i = 0; i < generationEntries.size(); i++) {
            long genValidFrom = generationEntries.get(i).getValidFromInMillisec(validFrom.getTimeZone());
            if (effectiveTime >= genValidFrom) {
                return new Integer(i);
            }
        }
        return null;
    }

    /**
     * @return <code>true</code> if this is an entry representing a product component.
     */
    public boolean isProductCmptTypeTocEntry() {
        return PRODUCT_CMPT_ENTRY_TYPE.equals(entryType);
    }

    /**
     * @return <code>true</code> if this is an entry representing a table.
     */
    public boolean isTableTocEntry() {
        return TABLE_ENTRY_TYPE.equals(entryType);
    }

    /**
     * @return <code>true</code> if this is an entry representing a test case.
     */
    public boolean isTestCaseTocEntry() {
        return TEST_CASE_ENTRY_TYPE.equals(entryType);
    }

    /**
     * @return <code>true</code> if this is an entry representing a formula test.
     */
    public boolean isFormulaTestTocEntry() {
        return FORMULA_TEST_ENTRY_TYPE.equals(entryType);
    }

    /**
     * @return <code>true</code> if this is an entry representing a model type.
     */
    public boolean isModelTypeTocEntry() {
        return MODEL_TYPE_ENTRY_TYPE.equals(entryType);
    }

    /**
     * @return <code>true</code> if this is an entry representing an enum content.
     */
    public boolean isEnumContentTypeTocEntry() {
        return ENUM_CONTENT_ENTRY_TYPE.equals(entryType);
    }

    /**
     * @return <code>true</code> if this is an entry representing an enum content.
     */
    public boolean isEnumXmlAdapterTocEntry() {
        return ENUM_XML_ADAPTER_TYPE.equals(entryType);
    }

    /**
     * Transforms the toc entry to xml.
     * 
     * @param doc The document used as factory for new element.
     */
    public Element toXml(Document doc) {
        Element entryElement = doc.createElement(AbstractReadonlyTableOfContents.TOC_ENTRY_XML_ELEMENT);
        super.addToXml(entryElement);

        entryElement.setAttribute(PROPERTY_ENTRYTYPE, entryType);
        entryElement.setAttribute(PROPERTY_IPS_OBJECT_ID, ipsObjectId);
        entryElement.setAttribute(PROPERTY_IPS_OBJECT_QNAME, ipsObjectQualifiedName);
        entryElement.setAttribute(PROPERTY_KIND_ID, kindId);
        entryElement.setAttribute(PROPERTY_VERSION_ID, versionId);
        entryElement.setAttribute(PROPERTY_VALID_TO, validTo != null ? validTo.toIsoFormat() : "");
        entryElement.setAttribute(PROPERTY_GENERATION_IMPL_CLASS_NAME, generationImplClassName);

        for (TocEntryGeneration generationEntry : generationEntries) {
            entryElement.appendChild(generationEntry.toXml(doc));
        }

        return entryElement;
    }

    /**
     * Overridden.
     */
    @Override
    public String toString() {
        return new StringBuffer().append("TocEntry(").append(entryType).append(':').append(ipsObjectId).append(')')
                .toString();
    }

    private static class TocEntryGeneratorComparator implements Comparator<TocEntryGeneration> {

        /**
         * {@inheritDoc}
         */
        public int compare(TocEntryGeneration first, TocEntryGeneration second) {

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

    /**
     * A builder for {@link TocEntryObject} described in 'Effective Java' Item 2
     * 
     * @author INSERT YOUR NAME
     */
    public static class Builder {

        private String ipsObjectId;

        private String ipsObjectQualifiedName;

        private String entryType;

        private String kindId;

        private String versionId;

        private DateTime validTo;

        private String generationImplClassName;

        private String implementationClassName;

        private String xmlResourceName;

        public Builder(String ipsObjectId, String implementationClassName, String entryType) {
            this.ipsObjectId = ipsObjectId;
            this.implementationClassName = implementationClassName;
            this.entryType = entryType;
        }

        public Builder xmlResourceName(String xmlResourceName) {
            this.xmlResourceName = xmlResourceName;
            return this;
        }

        public Builder ipsObjectQualifiedName(String ipsObjectQualifiedName) {
            this.ipsObjectQualifiedName = ipsObjectQualifiedName;
            return this;
        }

        public Builder kindId(String kindId) {
            this.kindId = kindId;
            return this;
        }

        public Builder versionId(String versionId) {
            this.versionId = versionId;
            return this;
        }

        public Builder validTo(DateTime validTo) {
            this.validTo = validTo;
            return this;
        }

        public Builder generationImplClassName(String generationImplClassName) {
            this.generationImplClassName = generationImplClassName;
            return this;
        }

        public TocEntryObject build() {
            return new TocEntryObject(this);
        }
    }
}
