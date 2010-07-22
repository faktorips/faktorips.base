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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;

import org.faktorips.runtime.internal.DateTime;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A {@link TocEntryObject} for {@link org.faktorips.runtime.IProductComponent}s
 * 
 * @author dirmeier
 */
public class ProductCmptTocEntry extends TocEntryObject {

    public static final String PROPERTY_KIND_ID = "kindId";
    public static final String PROPERTY_VERSION_ID = "versionId";
    public static final String PROPERTY_VALID_TO = "validTo";
    public static final String PROPERTY_GENERATION_IMPL_CLASS_NAME = "generationImplClassName";
    public static final String XML_TAG = "ProductComponent";

    private TreeMap<Long, GenerationTocEntry> generationEntries = createNewTreeMap();
    /**
     * If this entry is a product component: the (runtime) id if of the product component kind,
     * empty string otherwise.
     */
    private final String kindId;
    /**
     * If this entry is a product component: the version id if of the product component kind, emtpy
     * string otherwise.
     */
    private final String versionId;
    /**
     * If this entry is for a product component: the date until this product component is valid
     */
    private final DateTime validTo;
    /**
     * If this entry is for a product component type: the name of the implementation class for the
     * generation object
     */
    private final String generationImplClassName;

    public static ProductCmptTocEntry createFromXml(Element entryElement) {
        String ipsObjectId = entryElement.getAttribute(PROPERTY_IPS_OBJECT_ID);
        String ipsObjectName = entryElement.getAttribute(PROPERTY_IPS_OBJECT_QNAME);
        String xmlResourceName = entryElement.getAttribute(PROPERTY_XML_RESOURCE);
        String implementationClassName = entryElement.getAttribute(PROPERTY_IMPLEMENTATION_CLASS);
        String generationImplClassName = entryElement.getAttribute(PROPERTY_GENERATION_IMPL_CLASS_NAME);

        DateTime validTo = DateTime.parseIso(entryElement.getAttribute(PROPERTY_VALID_TO));
        String kindId = entryElement.getAttribute(PROPERTY_KIND_ID);
        String versionId = entryElement.getAttribute(PROPERTY_VERSION_ID);

        ProductCmptTocEntry newEntry = new ProductCmptTocEntry(ipsObjectId, ipsObjectName, kindId, versionId,
                xmlResourceName, implementationClassName, generationImplClassName, validTo);

        NodeList nl = entryElement.getElementsByTagName(GenerationTocEntry.XML_TAG);
        for (int i = 0; i < nl.getLength(); i++) {
            GenerationTocEntry entry = GenerationTocEntry.createFromXml(newEntry, (Element)nl.item(i));
            newEntry.generationEntries.put(entry.getValidFromInMillisec(TimeZone.getDefault()), entry);
        }
        return newEntry;
    }

    public ProductCmptTocEntry(String ipsObjectId, String ipsObjectQualifiedName, String kindId, String versionId,
            String xmlResourceName, String implementationClassName, String generationImplClassName, DateTime validTo) {
        super(ipsObjectId, ipsObjectQualifiedName, xmlResourceName, implementationClassName);
        this.kindId = kindId;
        this.versionId = versionId;
        this.validTo = validTo;
        this.generationImplClassName = generationImplClassName;
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
    public List<GenerationTocEntry> getGenerationEntries() {
        return new ArrayList<GenerationTocEntry>(generationEntries.values());
    }

    /**
     * Returns the number of genertion entries.
     */
    public int getNumberOfGenerationEntries() {
        return generationEntries == null ? 0 : generationEntries.size();
    }

    /**
     * Returns the name of the generation implementation class
     */
    public String getGenerationImplClassName() {
        return generationImplClassName;
    }

    /**
     * Sets the generation entries.
     */
    public void setGenerationEntries(List<GenerationTocEntry> entries) {
        generationEntries = createNewTreeMap();
        for (GenerationTocEntry entry : entries) {
            generationEntries.put(entry.getValidFromInMillisec(TimeZone.getDefault()), entry);
        }
    }

    /**
     * Returns the {@link GenerationTocEntry} successor of the one that is found for the provided
     * validity date. Returns <code>null</code> if either no entry is found for the provided date or
     * if the found one doesn't have a successor.
     */
    public GenerationTocEntry getNextGenerationEntry(Calendar validFrom) {
        SortedMap<Long, GenerationTocEntry> map = generationEntries.headMap(validFrom.getTimeInMillis());
        if (map.isEmpty()) {
            return null;
        }
        Long key = map.lastKey();
        return generationEntries.get(key);
    }

    /**
     * Returns the {@link GenerationTocEntry} that is prior to the one that is found for the
     * provided validity date. Returns <code>null</code> if either no entry is found for the
     * provided date or if the found one doesn't have a predecessor.
     */
    public GenerationTocEntry getPreviousGenerationEntry(Calendar validFrom) {
        SortedMap<Long, GenerationTocEntry> map = generationEntries.tailMap(validFrom.getTimeInMillis() - 1);
        if (map.isEmpty()) {
            return null;
        }
        Long key = map.firstKey();
        return generationEntries.get(key);

    }

    /**
     * Returns the latest {@link GenerationTocEntry} with repect to the generations validity date.
     */
    public GenerationTocEntry getLatestGenerationEntry() {
        if (generationEntries.isEmpty()) {
            return null;
        }
        return generationEntries.get(generationEntries.firstKey());
    }

    /**
     * Returns the toc entry for the generation valid on the given effective date, or
     * <code>null</code> if no generation is effective on the given date or the effective is
     * <code>null</code>.
     */
    public GenerationTocEntry getGenerationEntry(Calendar effectiveDate) {
        if (effectiveDate == null) {
            return null;
        }
        SortedMap<Long, GenerationTocEntry> map = generationEntries.tailMap(effectiveDate.getTimeInMillis() + 1);
        if (map.isEmpty()) {
            return null;
        }
        Long key = map.firstKey();
        return generationEntries.get(key);
    }

    /**
     * Return the Generation Toc Entry for the exact valid at {@link DateTime}. This is the most
     * effective way to get a generation toc entry
     * 
     */
    public GenerationTocEntry getGenerationEntry(DateTime validAt) {
        return generationEntries.get(validAt.toTimeInMillisecs(TimeZone.getDefault()));
    }

    @Override
    protected void addToXml(Element entryElement) {
        super.addToXml(entryElement);
        entryElement.setAttribute(PROPERTY_KIND_ID, kindId);
        entryElement.setAttribute(PROPERTY_VERSION_ID, versionId);
        if (validTo != null) {
            entryElement.setAttribute(PROPERTY_VALID_TO, validTo.toIsoFormat());
        }
        entryElement.setAttribute(PROPERTY_GENERATION_IMPL_CLASS_NAME, generationImplClassName);
        for (GenerationTocEntry generationEntry : generationEntries.values()) {
            entryElement.appendChild(generationEntry.toXml(entryElement.getOwnerDocument()));
        }
    }

    @Override
    protected String getXmlElementTag() {
        return XML_TAG;
    }

    private static TreeMap<Long, GenerationTocEntry> createNewTreeMap() {
        return new TreeMap<Long, GenerationTocEntry>(new InverseLongComparator());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        // using only the size of the generation entries is a quite weak implementation. But because
        // we know that most time the other properties are not the same we prefer the performance
        // of this implementation.
        result = prime * result + ((generationEntries == null) ? 0 : generationEntries.size());
        result = prime * result + ((generationImplClassName == null) ? 0 : generationImplClassName.hashCode());
        result = prime * result + ((kindId == null) ? 0 : kindId.hashCode());
        result = prime * result + ((validTo == null) ? 0 : validTo.hashCode());
        result = prime * result + ((versionId == null) ? 0 : versionId.hashCode());
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
        if (!(obj instanceof ProductCmptTocEntry)) {
            return false;
        }
        ProductCmptTocEntry other = (ProductCmptTocEntry)obj;
        if (generationEntries == null) {
            if (other.generationEntries != null) {
                return false;
            }
        } else if (!generationEntries.equals(other.generationEntries)) {
            return false;
        }
        if (generationImplClassName == null) {
            if (other.generationImplClassName != null) {
                return false;
            }
        } else if (!generationImplClassName.equals(other.generationImplClassName)) {
            return false;
        }
        if (kindId == null) {
            if (other.kindId != null) {
                return false;
            }
        } else if (!kindId.equals(other.kindId)) {
            return false;
        }
        if (validTo == null) {
            if (other.validTo != null) {
                return false;
            }
        } else if (!validTo.equals(other.validTo)) {
            return false;
        }
        if (versionId == null) {
            if (other.versionId != null) {
                return false;
            }
        } else if (!versionId.equals(other.versionId)) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        return true;
    }

    static class InverseLongComparator implements Comparator<Long> {

        public int compare(Long first, Long second) {
            return -1 * first.compareTo(second);
        }

    }

}