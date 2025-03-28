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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;

import org.faktorips.runtime.internal.DateTime;
import org.w3c.dom.Element;

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
     * If this entry is a product component: the version id if of the product component kind, empty
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

    // CSOFF: ParameterNumber
    public ProductCmptTocEntry(String ipsObjectId, String ipsObjectQualifiedName, String kindId, String versionId,
            String xmlResourceName, String implementationClassName, String generationImplClassName, DateTime validTo) {
        // CSON: ParameterNumber
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
        return new ArrayList<>(generationEntries.values());
    }

    /**
     * Returns the number of generation entries.
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
        entries.forEach(entry -> generationEntries.put(entry.getValidFromInMillisec(TimeZone.getDefault()), entry));
    }

    /**
     * Returns the {@link GenerationTocEntry} successor of the one that is found for the provided
     * validity date. Returns <code>null</code> if either no entry is found for the provided date or
     * if the found one doesn't have a successor.
     *
     * @see #findNextGenerationEntry(Calendar) findNextGenerationEntry(Calendar) for null-safe
     *          processing
     */
    public GenerationTocEntry getNextGenerationEntry(Calendar validFrom) {
        return findNextGenerationEntry(validFrom).orElse(null);
    }

    /**
     * Returns an Optional containing the {@link GenerationTocEntry} successor of the one that is
     * found for the provided validity date. Returns an {@link Optional#empty() empty Optional} if
     * either no entry is found for the provided date or if the found one doesn't have a successor.
     */
    public Optional<GenerationTocEntry> findNextGenerationEntry(Calendar validFrom) {
        SortedMap<Long, GenerationTocEntry> map = generationEntries.headMap(validFrom.getTimeInMillis());
        if (map.isEmpty()) {
            return Optional.empty();
        }
        return map.isEmpty() ? Optional.empty() : Optional.ofNullable(generationEntries.get(map.lastKey()));
    }

    /**
     * Returns the {@link GenerationTocEntry} that is prior to the one that is found for the
     * provided validity date. Returns <code>null</code> if either no entry is found for the
     * provided date or if the found one doesn't have a predecessor.
     *
     * @see #findPreviousGenerationEntry(Calendar) findPreviousGenerationEntry(Calendar) for
     *          null-safe processing
     */
    public GenerationTocEntry getPreviousGenerationEntry(Calendar validFrom) {
        return findPreviousGenerationEntry(validFrom).orElse(null);
    }

    /**
     * Returns an Optional containing the {@link GenerationTocEntry} that is prior to the one that
     * is found for the provided validity date. Returns an {@link Optional#empty() empty Optional}
     * if either no entry is found for the provided date or if the found one doesn't have a
     * predecessor.
     */
    public Optional<GenerationTocEntry> findPreviousGenerationEntry(Calendar validFrom) {
        SortedMap<Long, GenerationTocEntry> map = generationEntries.tailMap(validFrom.getTimeInMillis() - 1);
        if (map.isEmpty()) {
            return Optional.empty();
        }
        Long key = map.firstKey();
        return Optional.ofNullable(generationEntries.get(key));

    }

    /**
     * Returns the latest {@link GenerationTocEntry} with repect to the generations validity date.
     *
     * @see #findLatestGenerationEntry() findLatestGenerationEntry() for null-safe processing
     */
    public GenerationTocEntry getLatestGenerationEntry() {
        return findLatestGenerationEntry().orElse(null);
    }

    /**
     * Returns an Optional containing the latest {@link GenerationTocEntry} with respect to the
     * generations validity date.
     */
    public Optional<GenerationTocEntry> findLatestGenerationEntry() {
        if (generationEntries.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(generationEntries.get(generationEntries.firstKey()));
    }

    /**
     * Returns the toc entry for the generation valid on the given effective date, or
     * <code>null</code> if no generation is effective on the given date or the effective is
     * <code>null</code>.
     *
     * @see #findGenerationEntry(Calendar) findGenerationEntry(Calendar) for null-safe processing
     */
    public GenerationTocEntry getGenerationEntry(Calendar effectiveDate) {
        return findGenerationEntry(effectiveDate).orElse(null);
    }

    /**
     * Returns an Optional containing the toc entry for the generation valid on the given effective
     * date, or an {@link Optional#empty() empty Optional} if no generation is effective on the
     * given date or the effective is <code>null</code>.
     */
    public Optional<GenerationTocEntry> findGenerationEntry(Calendar effectiveDate) {
        if (effectiveDate == null) {
            return Optional.empty();
        }
        SortedMap<Long, GenerationTocEntry> map = generationEntries.tailMap(effectiveDate.getTimeInMillis() + 1);
        if (map.isEmpty()) {
            return Optional.empty();
        }
        Long key = map.firstKey();
        return Optional.ofNullable(generationEntries.get(key));
    }

    /**
     * Return the Generation Toc Entry for the exact valid at {@link DateTime}. This is the most
     * effective way to get a generation toc entry
     *
     * @see #findGenerationEntry(DateTime) findGenerationEntry(DateTime) for null-safe processing
     */
    public GenerationTocEntry getGenerationEntry(DateTime validAt) {
        return findGenerationEntry(validAt).orElse(null);
    }

    /**
     * Return an Optional containing the Generation Toc Entry for the exact valid at
     * {@link DateTime}. This is the most effective way to get a generation toc entry.
     */
    public Optional<GenerationTocEntry> findGenerationEntry(DateTime validAt) {
        return Optional.ofNullable(generationEntries.get(validAt.toTimeInMillisecs(TimeZone.getDefault())));
    }

    @Override
    protected void addToXml(Element entryElement) {
        super.addToXml(entryElement);
        entryElement.setAttribute(PROPERTY_KIND_ID, kindId);
        entryElement.setAttribute(PROPERTY_VERSION_ID, versionId);
        Optional.ofNullable(validTo).ifPresent(v -> entryElement.setAttribute(PROPERTY_VALID_TO, v.toIsoFormat()));
        if (!getGenerationEntries().isEmpty()) {
            entryElement.setAttribute(PROPERTY_GENERATION_IMPL_CLASS_NAME, generationImplClassName);
            generationEntries.values().forEach(generationEntry -> entryElement
                    .appendChild(generationEntry.toXml(entryElement.getOwnerDocument())));
        }
    }

    @Override
    protected String getXmlElementTag() {
        return XML_TAG;
    }

    private static TreeMap<Long, GenerationTocEntry> createNewTreeMap() {
        return new TreeMap<>(Comparator.reverseOrder());
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
        return prime * result + ((versionId == null) ? 0 : versionId.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ProductCmptTocEntry other) || !super.equals(obj)) {
            return false;
        }
        return Objects.equals(generationEntries, other.generationEntries)
                && Objects.equals(generationImplClassName, other.generationImplClassName)
                && Objects.equals(kindId, other.kindId)
                && Objects.equals(validTo, other.validTo)
                && Objects.equals(versionId, other.versionId);
    }
}
