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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import org.faktorips.runtime.IRuntimeObject;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.w3c.dom.Element;

/**
 * Default implementation of <code>ReadonlyTableOfContents</code>.
 * 
 * @author Jan Ortmann
 */
public class ReadonlyTableOfContents extends AbstractReadonlyTableOfContents {

    /**
     * A map that contains the runtime id of product components as key and the TOC entry as value.
     */
    private Map<String, ProductCmptTocEntry> pcIdTocEntryMap;

    /**
     * A map that contains the fully qualified name of product components as key and the TOC entry
     * as value.
     */
    private Map<String, ProductCmptTocEntry> pcNameTocEntryMap;

    /** A map that contains per kindId the list of product component IDs that are of the kind. */
    private Map<String, List<VersionIdTocEntry>> kindIdTocEntryListMap;

    /**
     * Maps a table class to the TOC entry that contains information about a table object
     * represented by this class.
     */
    private Map<String, TableContentTocEntry> tableImplClassTocEntryMap;

    /**
     * Maps a qualified table name to the TOC entry that contains information about a table object.
     */
    private Map<String, TableContentTocEntry> tableContentNameTocEntryMap;

    /**
     * Maps a qualified test case name to the TOC entry that contains information about a test case
     * object.
     */
    private Map<String, TestCaseTocEntry> testCaseNameTocEntryMap;

    /**
     * Maps a qualified model type name to the TOC entry that contains information about the model
     * type.
     */
    private Map<String, ModelTypeTocEntry> modelTypeNameTocEntryMap;

    /** A map that contains the runtime id of enum contents as key and the TOC entry as value. */
    private Map<String, EnumContentTocEntry> enumContentImplClassTocEntryMap;

    /**
     * Maps the qualified name of an enumeration type to a TOC entry of an XmlAdapter. Only for enum
     * type that defer their content XmlAdapters and hence entries into this map are created.
     */
    private Map<String, EnumXmlAdapterTocEntry> enumXmlAdapterTocEntryMap;

    private Map<Class<?>, Map<String, CustomTocEntryObject<?>>> otherTocEntryMaps;

    /**
     * Creates a new TOC.
     */
    public ReadonlyTableOfContents() {
        super(ReadonlyTableOfContents.class.getClassLoader());
    }

    /**
     * Creates a new TOC that uses the given {@link ClassLoader} to find {@link ITocEntryFactory}
     * implementations via {@link ServiceLoader}.
     * 
     * @param classLoader the {@link ClassLoader} used to find {@link ITocEntryFactory}
     *            implementations
     */
    public ReadonlyTableOfContents(ClassLoader classLoader) {
        super(classLoader);
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
        otherTocEntryMaps = new HashMap<Class<?>, Map<String, CustomTocEntryObject<?>>>();
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
            addEnumContentTocEntry(entry);
            return;
        }
        if (entry instanceof EnumXmlAdapterTocEntry) {
            enumXmlAdapterTocEntryMap.put(entry.getIpsObjectId(), (EnumXmlAdapterTocEntry)entry);
            return;
        }
        if (entry instanceof CustomTocEntryObject<?>) {
            putTypedTocEntryToMap((CustomTocEntryObject<?>)entry);
            return;
        }

        throw new IllegalArgumentException("Unknown entry type " + entry);
    }

    private void addEnumContentTocEntry(TocEntryObject entry) {
        EnumContentTocEntry previousEntry = enumContentImplClassTocEntryMap.get(entry.getImplementationClassName());
        if (previousEntry == null || IpsStringUtils.isEmpty(previousEntry.getXmlResourceName())) {
            enumContentImplClassTocEntryMap.put(entry.getImplementationClassName(), (EnumContentTocEntry)entry);
        }
    }

    private <T> void putTypedTocEntryToMap(CustomTocEntryObject<T> tocEntry) {
        Map<String, CustomTocEntryObject<?>> otherTocEntryMap = otherTocEntryMaps
                .computeIfAbsent(tocEntry.getRuntimeObjectClass(), $ -> new HashMap<String, CustomTocEntryObject<?>>());
        otherTocEntryMap.put(tocEntry.getIpsObjectQualifiedName(), tocEntry);
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
        return kindIdTocEntryListMap.computeIfAbsent(kindId, $ -> new ArrayList<VersionIdTocEntry>(1));
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
    public List<EnumContentTocEntry> getEnumContentTocEntries() {
        return new ArrayList<EnumContentTocEntry>(enumContentImplClassTocEntryMap.values());
    }

    @Override
    public EnumContentTocEntry getEnumContentTocEntry(String className) {
        return enumContentImplClassTocEntryMap.get(className);
    }

    @Override
    public Set<EnumXmlAdapterTocEntry> getEnumXmlAdapterTocEntries() {
        return new HashSet<EnumXmlAdapterTocEntry>(enumXmlAdapterTocEntryMap.values());
    }

    public List<TocEntryObject> getEntries() {
        List<TocEntryObject> results = new ArrayList<TocEntryObject>();
        results.addAll(pcIdTocEntryMap.values());
        results.addAll(tableContentNameTocEntryMap.values());
        results.addAll(testCaseNameTocEntryMap.values());
        results.addAll(modelTypeNameTocEntryMap.values());
        results.addAll(enumContentImplClassTocEntryMap.values());
        results.addAll(enumXmlAdapterTocEntryMap.values());
        for (Map<String, CustomTocEntryObject<?>> otherTocEntryMap : otherTocEntryMaps.values()) {
            results.addAll(otherTocEntryMap.values());
        }
        return results;
    }

    @Override
    public <T> CustomTocEntryObject<T> getCustomTocEntry(Class<T> type, String ipsObjectQualifiedName) {
        Map<String, CustomTocEntryObject<?>> otherTocEntryMap = otherTocEntryMaps.get(type);
        if (otherTocEntryMap != null) {
            @SuppressWarnings("unchecked")
            CustomTocEntryObject<T> typedTocEntryObject = (CustomTocEntryObject<T>)otherTocEntryMap
                    .get(ipsObjectQualifiedName);
            return typedTocEntryObject;
        }
        return null;
    }

    @Override
    public <T extends IRuntimeObject> List<CustomTocEntryObject<T>> getTypedTocEntries(Class<T> type) {
        Map<String, CustomTocEntryObject<?>> otherTocEntryMap = otherTocEntryMaps.get(type);
        ArrayList<CustomTocEntryObject<T>> list = new ArrayList<CustomTocEntryObject<T>>();
        if (otherTocEntryMap != null) {
            Collection<CustomTocEntryObject<?>> values = otherTocEntryMap.values();
            for (CustomTocEntryObject<?> typedTocEntryObject : values) {
                @SuppressWarnings("unchecked")
                CustomTocEntryObject<T> tocEntryObject = (CustomTocEntryObject<T>)typedTocEntryObject;
                list.add(tocEntryObject);
            }
        }
        return list;
    }

    private class VersionIdTocEntry {

        private String versionId;
        private ProductCmptTocEntry tocEntry;

        public VersionIdTocEntry(String versionId, ProductCmptTocEntry entry) {
            this.versionId = versionId;
            this.tocEntry = entry;
        }

    }

}
