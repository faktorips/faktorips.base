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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.faktorips.runtime.internal.DateTime;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ProductCmptTocEntry extends TocEntryObject implements IProductCmptTocEntry {

    protected List<GenerationTocEntry> generationEntries = new ArrayList<GenerationTocEntry>(0);
    /**
     * If this entry is a product component: the (runtime) id if of the product component kind,
     * empty string otherwise.
     */
    protected final String kindId;
    /**
     * If this entry is a product component: the version id if of the product component kind, emtpy
     * string otherwise.
     */
    protected final String versionId;
    /**
     * If this entry is for a product component: the date until this product component is valid
     */
    protected final DateTime validTo;
    /**
     * If this entry is for a product component type: the name of the implementation class for the
     * generation object
     */
    protected final String generationImplClassName;

    public static IProductCmptTocEntry createFromXml(Element entryElement) {
        String ipsObjectId = entryElement.getAttribute(PROPERTY_IPS_OBJECT_ID);
        String ipsObjectName = entryElement.getAttribute(PROPERTY_IPS_OBJECT_QNAME);
        String xmlResourceName = entryElement.getAttribute(PROPERTY_XML_RESOURCE);
        String implementationClassName = entryElement.getAttribute(PROPERTY_IMPLEMENTATION_CLASS);
        String generationImplClassName = entryElement.getAttribute(PROPERTY_GENERATION_IMPL_CLASS_NAME);

        DateTime validTo = DateTime.parseIso(entryElement.getAttribute(IProductCmptTocEntry.PROPERTY_VALID_TO));
        String kindId = entryElement.getAttribute(IProductCmptTocEntry.PROPERTY_KIND_ID);
        String versionId = entryElement.getAttribute(IProductCmptTocEntry.PROPERTY_VERSION_ID);

        ProductCmptTocEntry newEntry = new ProductCmptTocEntry(ipsObjectId, ipsObjectName, kindId, versionId,
                xmlResourceName, implementationClassName, generationImplClassName, validTo);

        NodeList nl = entryElement.getElementsByTagName(GenerationTocEntry.XML_TAG);
        newEntry.generationEntries = new ArrayList<GenerationTocEntry>(nl.getLength());
        for (int i = 0; i < nl.getLength(); i++) {
            newEntry.generationEntries.add(GenerationTocEntry.createFromXml(newEntry, (Element)nl.item(i)));
        }
        Collections.sort(newEntry.generationEntries, new TocEntryGeneratorComparator());
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
        return generationEntries;
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
        generationEntries = entries;
        Collections.sort(generationEntries, new TocEntryGeneratorComparator());
    }

    /**
     * Returns the {@link GenerationTocEntry} successor of the one that is found for the provided
     * validity date. Returns <code>null</code> if either no entry is found for the provided date or
     * if the found one doesn't have a successor.
     */
    public GenerationTocEntry getNextGenerationEntry(Calendar validFrom) {
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
     * Returns the {@link GenerationTocEntry} that is prior to the one that is found for the
     * provided validity date. Returns <code>null</code> if either no entry is found for the
     * provided date or if the found one doesn't have a predecessor.
     */
    public GenerationTocEntry getPreviousGenerationEntry(Calendar validFrom) {
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
     * Returns the latest {@link GenerationTocEntry} with repect to the generations validity date.
     */
    public GenerationTocEntry getLatestGenerationEntry() {
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
    public GenerationTocEntry getGenerationEntry(Calendar effectiveDate) {
        Integer index = getGenerationEntryIndex(effectiveDate);
        if (index == null) {
            return null;
        }
        return generationEntries.get(index);
    }

    Integer getGenerationEntryIndex(Calendar validFrom) {
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

    @Override
    protected void addToXml(Element entryElement) {
        super.addToXml(entryElement);
        entryElement.setAttribute(PROPERTY_KIND_ID, kindId);
        entryElement.setAttribute(PROPERTY_VERSION_ID, versionId);
        if (validTo != null) {
            entryElement.setAttribute(PROPERTY_VALID_TO, validTo.toIsoFormat());
        }
        entryElement.setAttribute(PROPERTY_GENERATION_IMPL_CLASS_NAME, generationImplClassName);
        for (GenerationTocEntry generationEntry : generationEntries) {
            entryElement.appendChild(generationEntry.toXml(entryElement.getOwnerDocument()));
        }
    }

    @Override
    protected String getXmlElementTag() {
        return XML_TAG;
    }

}