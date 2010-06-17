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
import java.util.HashMap;
import java.util.HashSet;
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
    protected Map<String, ProductCmptTocEntry> pcIdTocEntryMap = new HashMap<String, ProductCmptTocEntry>(1000);

    /**
     * A map that contains the fully qualified name of product components as key and the toc entry
     * as value.
     */
    protected Map<String, ProductCmptTocEntry> pcNameTocEntryMap = new HashMap<String, ProductCmptTocEntry>(1000);

    /** A map that contains per kindId the list of product component ids that are of the kind. */
    protected Map<String, List<VersionIdTocEntry>> kindIdTocEntryListMap = new HashMap<String, List<VersionIdTocEntry>>(
            500);

    /**
     * Maps a table class to the toc entry that contains information about a table object
     * represented by this class.
     */
    protected Map<String, TableContentTocEntry> tableImplClassTocEntryMap = new HashMap<String, TableContentTocEntry>(
            100);

    /** Maps a qualified table name to the toc entry that contains information about a table object. */
    protected Map<String, TableContentTocEntry> tableContentNameTocEntryMap = new HashMap<String, TableContentTocEntry>(
            100);

    /**
     * Maps a qualified test case name to the toc entry that contains information about a test case
     * object.
     */
    protected Map<String, TestCaseTocEntry> testCaseNameTocEntryMap = new HashMap<String, TestCaseTocEntry>(10);

    /**
     * Maps a qualified model type name to the toc entry that contains information about the model
     * type.
     */
    protected Map<String, ModelTypeTocEntry> modelTypeNameTocEntryMap = new HashMap<String, ModelTypeTocEntry>(100);

    /** A map that contains the runtime id of enum contents as key and the toc entry as value. */
    protected Map<String, EnumContentTocEntry> enumContentImplClassTocEntryMap = new HashMap<String, EnumContentTocEntry>(
            100);

    /**
     * Maps the qualified name of an enumtype to a toc entry of an XmlAdapter. Only for enum type
     * that defer their content XmlAdapters and hence entries into this map are created.
     */
    protected Map<String, EnumXmlAdapterTocEntry> enumXmlAdapterTocEntryMap = new HashMap<String, EnumXmlAdapterTocEntry>(
            100);

    /**
     * Creats a new toc.
     */
    public ReadonlyTableOfContents() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void internalAddEntry(TocEntryObject entry) {
        if (entry instanceof ProductCmptTocEntry) {
            ProductCmptTocEntry prodEntry = (ProductCmptTocEntry)entry;
            pcIdTocEntryMap.put(prodEntry.getIpsObjectId(), prodEntry);
            pcNameTocEntryMap.put(prodEntry.getIpsObjectQualifiedName(), prodEntry);
            List<VersionIdTocEntry> versions = getVersionList(prodEntry.getKindId());
            versions.add(new VersionIdTocEntry(prodEntry.getVersionId(), prodEntry));
            return;
        }

        if (entry instanceof TableContentTocEntry) {
            /*
             * TODO store the first or last entry of multiple toc entries with the same class name?
             * This stores only the last found toc entry.
             */
            tableImplClassTocEntryMap.put(entry.getImplementationClassName(), (TableContentTocEntry)entry);
            tableContentNameTocEntryMap.put(entry.getIpsObjectQualifiedName(), (TableContentTocEntry)entry);
            return;
        }

        if (entry instanceof TestCaseTocEntry) {
            testCaseNameTocEntryMap.put(entry.getIpsObjectQualifiedName(), (TestCaseTocEntry)entry);
            return;
        }

        if (entry instanceof FormulaTestTocEntry) {
            testCaseNameTocEntryMap.put(entry.getIpsObjectQualifiedName(), (FormulaTestTocEntry)entry);
            return;
        }

        if (entry instanceof ModelTypeTocEntry) {
            modelTypeNameTocEntryMap.put(entry.getIpsObjectQualifiedName(), (ModelTypeTocEntry)entry);
            return;
        }

        if (entry instanceof EnumContentTocEntry) {
            enumContentImplClassTocEntryMap.put(entry.getImplementationClassName(), (EnumContentTocEntry)entry);
            return;
        }
        if (entry instanceof EnumXmlAdapterTocEntry) {
            enumXmlAdapterTocEntryMap.put(entry.getIpsObjectId(), (EnumXmlAdapterTocEntry)entry);
            return;
        }
        throw new IllegalArgumentException("Unknown entry type " + entry);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ProductCmptTocEntry> getProductCmptTocEntries() {
        return new ArrayList<ProductCmptTocEntry>(pcIdTocEntryMap.values());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProductCmptTocEntry getProductCmptTocEntry(String id) {
        return pcIdTocEntryMap.get(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProductCmptTocEntry getProductCmptTocEntry(String kindId, String versionId) {
        if (kindId == null) {
            return null;
        }
        if (versionId == null) {
            throw new RuntimeException("Not implemented yet!");
        }
        List<VersionIdTocEntry> versions = getVersionList(kindId);
        for (VersionIdTocEntry each : versions) {
            if (versionId.equals(each.versionId)) {
                return each.tocEntry;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ProductCmptTocEntry> getProductCmptTocEntries(String kindId) {
        List<ProductCmptTocEntry> result = new ArrayList<ProductCmptTocEntry>();
        List<VersionIdTocEntry> versionList = getVersionList(kindId);
        for (VersionIdTocEntry each : versionList) {
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
    @Override
    public List<TableContentTocEntry> getTableTocEntries() {
        return new ArrayList<TableContentTocEntry>(tableContentNameTocEntryMap.values());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableContentTocEntry getTableTocEntryByClassname(String implementationClass) {
        return tableImplClassTocEntryMap.get(implementationClass);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableContentTocEntry getTableTocEntryByQualifiedTableName(String qualifiedTableName) {
        return tableContentNameTocEntryMap.get(qualifiedTableName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TestCaseTocEntry> getTestCaseTocEntries() {
        return new ArrayList<TestCaseTocEntry>(testCaseNameTocEntryMap.values());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestCaseTocEntry getTestCaseTocEntryByQName(String qName) {
        return testCaseNameTocEntryMap.get(qName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<ModelTypeTocEntry> getModelTypeTocEntries() {
        return new HashSet<ModelTypeTocEntry>(modelTypeNameTocEntryMap.values());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EnumContentTocEntry getEnumContentTocEntry(String className) {
        return enumContentImplClassTocEntryMap.get(className);
    }

    @Override
    public Set<EnumXmlAdapterTocEntry> getEnumXmlAdapterTocEntries() {
        return new HashSet<EnumXmlAdapterTocEntry>(enumXmlAdapterTocEntryMap.values());
    }

    private class VersionIdTocEntry {

        private String versionId;
        private ProductCmptTocEntry tocEntry;

        public VersionIdTocEntry(String versionId, ProductCmptTocEntry entry) {
            this.versionId = versionId;
            this.tocEntry = entry;
        }

    }

    public List<TocEntryObject> getEntries() {
        List<TocEntryObject> results = new ArrayList<TocEntryObject>();
        results.addAll(pcIdTocEntryMap.values());
        results.addAll(tableContentNameTocEntryMap.values());
        results.addAll(testCaseNameTocEntryMap.values());
        results.addAll(modelTypeNameTocEntryMap.values());
        results.addAll(enumContentImplClassTocEntryMap.values());
        results.addAll(enumXmlAdapterTocEntryMap.values());
        return results;
    }

}
