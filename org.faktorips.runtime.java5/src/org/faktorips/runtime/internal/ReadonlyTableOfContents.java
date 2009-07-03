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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Default implementation of <code>ReadonlyTableOfContents</code>.
 * 
 * @author Jan Ortmann
 */
public class ReadonlyTableOfContents extends AbstractReadonlyTableOfContents {

    /**
     * A map that contains the runtime id of product components as key and the toc entry as value.
     */
    protected Map<String, TocEntryObject> pcIdTocEntryMap = new HashMap<String, TocEntryObject>(1000);

    /**
     * A map that contains the fully qualified name of product components as key and the toc entry
     * as value.
     */
    protected Map<String, TocEntryObject> pcNameTocEntryMap = new HashMap<String, TocEntryObject>(1000);

    /** A map that contains per kindId the list of product component ids that are of the kind. */
    protected Map<String, List<VersionIdTocEntry>> kindIdTocEntryListMap = new HashMap<String, List<VersionIdTocEntry>>(
            500);

    /**
     * Maps a table class to the toc entry that contains information about a table object
     * represented by this class.
     */
    protected Map<String, TocEntryObject> tableImplClassTocEntryMap = new HashMap<String, TocEntryObject>(100);

    /** Maps a qualified table name to the toc entry that contains information about a table object. */
    protected Map<String, TocEntryObject> tableContentNameTocEntryMap = new HashMap<String, TocEntryObject>(100);

    /**
     * Maps a qualified test case name to the toc entry that contains information about a test case
     * object.
     */
    protected Map<String, TocEntryObject> testCaseNameTocEntryMap = new HashMap<String, TocEntryObject>(10);

    /**
     * Maps a qualified model type name to the toc entry that contains information about the model
     * type.
     */
    protected Map<String, TocEntryObject> modelTypeNameTocEntryMap = new HashMap<String, TocEntryObject>(100);

    /** A map that contains the runtime id of enum contents as key and the toc entry as value. */
    protected Map<String, TocEntryObject> enumContentNameTocEntryMap = new HashMap<String, TocEntryObject>(100);
    
    /**
     * Maps the qualified name of an enumtype to a toc entry of an XmlAdapter. Only for enum type that defer
     * their content XmlAdapters and hence entries into this map are created.
     */
    protected Map<String, TocEntryObject> enumXmlAdapterTocEntryMap = new HashMap<String, TocEntryObject>(100);

    /**
     * Creats a new toc.
     */
    public ReadonlyTableOfContents() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    protected void internalAddEntry(TocEntryObject entry) {
        if (entry.isProductCmptTypeTocEntry()) {
            pcIdTocEntryMap.put(entry.getIpsObjectId(), entry);
            pcNameTocEntryMap.put(entry.getIpsObjectQualifiedName(), entry);
            List<VersionIdTocEntry> versions = getVersionList(entry.getKindId());
            versions.add(new VersionIdTocEntry(entry.getVersionId(), entry));
            return;
        }

        if (entry.isTableTocEntry()) {
            /*
             * TODO store the first or last entry of multiple toc entries with the same class name?
             * This stores only the last found toc entry.
             */
            tableImplClassTocEntryMap.put(entry.getImplementationClassName(), entry);
            tableContentNameTocEntryMap.put(entry.getIpsObjectId(), entry);
            return;
        }

        if (entry.isTestCaseTocEntry()) {
            testCaseNameTocEntryMap.put(entry.getIpsObjectQualifiedName(), entry);
            return;
        }

        if (entry.isFormulaTestTocEntry()) {
            testCaseNameTocEntryMap.put(entry.getIpsObjectQualifiedName(), entry);
            return;
        }

        if (entry.isModelTypeTocEntry()) {
            modelTypeNameTocEntryMap.put(entry.getIpsObjectQualifiedName(), entry);
            return;
        }

        if (entry.isEnumContentTypeTocEntry()) {
            enumContentNameTocEntryMap.put(entry.getIpsObjectQualifiedName(), entry);
            return;
        }
        if (entry.isEnumXmlAdapterTocEntry()) {
            enumXmlAdapterTocEntryMap.put(entry.getIpsObjectQualifiedName(), entry);
            return;
        }
        throw new IllegalArgumentException("Unknown entry type " + entry);
    }

    /**
     * {@inheritDoc}
     */
    public List<TocEntryObject> getProductCmptTocEntries() {
        return new ArrayList<TocEntryObject>(pcIdTocEntryMap.values());
    }

    /**
     * {@inheritDoc}
     */
    public TocEntryObject getProductCmptTocEntry(String id) {
        return pcIdTocEntryMap.get(id);
    }

    /**
     * {@inheritDoc}
     */
    public TocEntryObject getProductCmptTocEntry(String kindId, String versionId) {
        if (kindId == null) {
            return null;
        }
        if (versionId == null) {
            throw new RuntimeException("Not implemented yet!");
        }
        List<VersionIdTocEntry> versions = getVersionList(kindId);
        for (Iterator<VersionIdTocEntry> it = versions.iterator(); it.hasNext();) {
            VersionIdTocEntry each = it.next();
            if (versionId.equals(each.versionId)) {
                return each.tocEntry;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public List<TocEntryObject> getProductCmptTocEntries(String kindId) {
        List<TocEntryObject> result = new ArrayList<TocEntryObject>();
        List<VersionIdTocEntry> versionList = getVersionList(kindId);
        for (Iterator<VersionIdTocEntry> it = versionList.iterator(); it.hasNext();) {
            VersionIdTocEntry each = it.next();
            result.add(each.tocEntry);
        }
        return result;
    }

    private List<VersionIdTocEntry> getVersionList(String kindId) {
        List<VersionIdTocEntry> versions = kindIdTocEntryListMap.get(kindId);
        if (versions == null) {
            versions = new ArrayList<VersionIdTocEntry>(1);
            kindIdTocEntryListMap.put(kindId, versions);
        }
        return versions;
    }

    /**
     * {@inheritDoc}
     */
    public List<TocEntryObject> getTableTocEntries() {
        return new ArrayList<TocEntryObject>(tableContentNameTocEntryMap.values());
    }

    /**
     * {@inheritDoc}
     */
    public TocEntryObject getTableTocEntryByClassname(String implementationClass) {
        return tableImplClassTocEntryMap.get(implementationClass);
    }

    /**
     * {@inheritDoc}
     */
    public TocEntryObject getTableTocEntryByQualifiedTableName(String qualifiedTableName) {
        return tableContentNameTocEntryMap.get(qualifiedTableName);
    }

    /**
     * {@inheritDoc}
     */
    public List<TocEntryObject> getTestCaseTocEntries() {
        return new ArrayList<TocEntryObject>(testCaseNameTocEntryMap.values());
    }

    /**
     * {@inheritDoc}
     */
    public TocEntryObject getTestCaseTocEntryByQName(String qName) {
        return testCaseNameTocEntryMap.get(qName);
    }

    /**
     * {@inheritDoc}
     */
    public Set<TocEntryObject> getModelTypeTocEntries() {
        return new HashSet<TocEntryObject>(modelTypeNameTocEntryMap.values());
    }

    /**
     * {@inheritDoc}
     */
    public Set<TocEntryObject> getEnumContentTocEntries() {
        return new HashSet<TocEntryObject>(enumContentNameTocEntryMap.values());
    }

    @Override
    public Set<TocEntryObject> getEnumXmlAdapterTocEntries() {
        return new HashSet<TocEntryObject>(enumXmlAdapterTocEntryMap.values());
    }

    private class VersionIdTocEntry {

        private String versionId;
        private TocEntryObject tocEntry;

        public VersionIdTocEntry(String versionId, TocEntryObject entry) {
            this.versionId = versionId;
            this.tocEntry = entry;
        }

    }


}
