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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;

/**
 * Default implementation of <code>ReadonlyTableOfContents</code>.
 * 
 * @author Jan Ortmann
 */
public class ReadonlyTableOfContents extends AbstractReadonlyTableOfContents {

    /**
     * A map that contains the runtime id of product components as key and the toc entry as value.
     */
    protected Map<String, ProductCmptTocEntry> pcIdTocEntryMap;

    /**
     * A map that contains the fully qualified name of product components as key and the toc entry
     * as value.
     */
    protected Map<String, ProductCmptTocEntry> pcNameTocEntryMap;

    /** A map that contains per kindId the list of product component ids that are of the kind. */
    protected Map<String, List<VersionIdTocEntry>> kindIdTocEntryListMap;

    /**
     * Maps a table class to the toc entry that contains information about a table object
     * represented by this class.
     */
    protected Map<String, TableContentTocEntry> tableImplClassTocEntryMap;

    /** Maps a qualified table name to the toc entry that contains information about a table object. */
    protected Map<String, TableContentTocEntry> tableContentNameTocEntryMap;

    /**
     * Maps a qualified test case name to the toc entry that contains information about a test case
     * object.
     */
    protected Map<String, TestCaseTocEntry> testCaseNameTocEntryMap;

    /**
     * Maps a qualified model type name to the toc entry that contains information about the model
     * type.
     */
    protected Map<String, ModelTypeTocEntry> modelTypeNameTocEntryMap;

    /** A map that contains the runtime id of enum contents as key and the toc entry as value. */
    protected Map<String, EnumContentTocEntry> enumContentImplClassTocEntryMap;

    /**
     * Maps the qualified name of an enumtype to a toc entry of an XmlAdapter. Only for enum type
     * that defer their content XmlAdapters and hence entries into this map are created.
     */
    protected Map<String, EnumXmlAdapterTocEntry> enumXmlAdapterTocEntryMap;

    /**
     * Creats a new toc.
     */
    public ReadonlyTableOfContents() {
        super();
    }

    @Override
    public void initFromXml(Element tocElement) {
        int size = tocElement.getChildNodes().getLength();
        initHashMaps(size);
        super.initFromXml(tocElement);
    }

    protected void initHashMaps(int tocSize) {
        /*
         * The size of the HashMaps is set to estimated values depending on the maximum size of the
         * table of contents
         */
        pcIdTocEntryMap = new HashMap<String, ProductCmptTocEntry>(tocSize);
        pcNameTocEntryMap = new HashMap<String, ProductCmptTocEntry>(tocSize);
        kindIdTocEntryListMap = new HashMap<String, List<VersionIdTocEntry>>(tocSize);
        tableImplClassTocEntryMap = new HashMap<String, TableContentTocEntry>(tocSize / 4);
        tableContentNameTocEntryMap = new HashMap<String, TableContentTocEntry>(tocSize / 4);
        testCaseNameTocEntryMap = new HashMap<String, TestCaseTocEntry>();
        modelTypeNameTocEntryMap = new HashMap<String, ModelTypeTocEntry>(tocSize / 4);
        enumContentImplClassTocEntryMap = new HashMap<String, EnumContentTocEntry>(tocSize / 4);
        enumXmlAdapterTocEntryMap = new HashMap<String, EnumXmlAdapterTocEntry>(tocSize / 4);
    }

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

    @Override
    public List<ProductCmptTocEntry> getProductCmptTocEntries() {
        return new ArrayList<ProductCmptTocEntry>(pcIdTocEntryMap.values());
    }

    @Override
    public ProductCmptTocEntry getProductCmptTocEntry(String id) {
        return pcIdTocEntryMap.get(id);
    }

    @Override
    public ProductCmptTocEntry getProductCmptTocEntry(String kindId, String versionId) {
        if (kindId == null) {
            return null;
        }
        if (versionId == null) {
            throw new RuntimeException("VersionId must not be null!");
        }
        List<VersionIdTocEntry> versions = getVersionList(kindId);
        for (VersionIdTocEntry each : versions) {
            if (versionId.equals(each.versionId)) {
                return each.tocEntry;
            }
        }
        return null;
    }

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

    @Override
    public List<TableContentTocEntry> getTableTocEntries() {
        return new ArrayList<TableContentTocEntry>(tableContentNameTocEntryMap.values());
    }

    @Override
    public TableContentTocEntry getTableTocEntryByClassname(String implementationClass) {
        return tableImplClassTocEntryMap.get(implementationClass);
    }

    @Override
    public TableContentTocEntry getTableTocEntryByQualifiedTableName(String qualifiedTableName) {
        return tableContentNameTocEntryMap.get(qualifiedTableName);
    }

    @Override
    public List<TestCaseTocEntry> getTestCaseTocEntries() {
        return new ArrayList<TestCaseTocEntry>(testCaseNameTocEntryMap.values());
    }

    @Override
    public TestCaseTocEntry getTestCaseTocEntryByQName(String qName) {
        return testCaseNameTocEntryMap.get(qName);
    }

    @Override
    public Set<ModelTypeTocEntry> getModelTypeTocEntries() {
        return new HashSet<ModelTypeTocEntry>(modelTypeNameTocEntryMap.values());
    }

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
