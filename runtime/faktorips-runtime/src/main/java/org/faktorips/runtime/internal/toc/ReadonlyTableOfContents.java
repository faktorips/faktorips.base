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

import static java.util.Comparator.comparing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.faktorips.runtime.IRuntimeObject;
import org.faktorips.runtime.ITable;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.runtime.model.IpsModel;
import org.faktorips.runtime.model.table.TableStructure;
import org.faktorips.runtime.model.table.TableStructureKind;
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

    /**
     * A map that contains per kindId a map with versionId and product components that are of the
     * kind.
     */
    private Map<String, Map<String, ProductCmptTocEntry>> kindIdTocEntryListMap;

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
        pcIdTocEntryMap = new HashMap<>(tocSize);
        pcNameTocEntryMap = new HashMap<>(tocSize);
        kindIdTocEntryListMap = new HashMap<>(tocSize);
        tableImplClassTocEntryMap = new HashMap<>(tocSize / 4);
        tableContentNameTocEntryMap = new HashMap<>(tocSize / 4);
        testCaseNameTocEntryMap = new HashMap<>();
        modelTypeNameTocEntryMap = new HashMap<>(tocSize / 4);
        enumContentImplClassTocEntryMap = new HashMap<>(tocSize / 4);
        enumXmlAdapterTocEntryMap = new HashMap<>(tocSize / 4);
        otherTocEntryMaps = new HashMap<>();
    }

    @Override
    protected void internalAddEntry(TocEntryObject entry) {
        switch (entry) {
            case ProductCmptTocEntry productCmptEntry -> {
                pcIdTocEntryMap.put(productCmptEntry.getIpsObjectId(), productCmptEntry);
                pcNameTocEntryMap.put(productCmptEntry.getIpsObjectQualifiedName(), productCmptEntry);
                getVersions(productCmptEntry.getKindId()).put(productCmptEntry.getVersionId(), productCmptEntry);
            }
            case TableContentTocEntry tableEntry -> {
                TableContentTocEntry previousTocEntry = tableImplClassTocEntryMap
                        .put(entry.getImplementationClassName(), tableEntry);
                removePreviousSingleContent(entry, previousTocEntry);
                tableContentNameTocEntryMap.put(entry.getIpsObjectQualifiedName(), tableEntry);
            }
            case FormulaTestTocEntry formulaTestEntry -> testCaseNameTocEntryMap
                    .put(formulaTestEntry.getIpsObjectQualifiedName(), formulaTestEntry);
            case TestCaseTocEntry testCaseEntry -> testCaseNameTocEntryMap
                    .put(testCaseEntry.getIpsObjectQualifiedName(), testCaseEntry);
            case ModelTypeTocEntry modelTypeEntry -> modelTypeNameTocEntryMap
                    .put(modelTypeEntry.getIpsObjectQualifiedName(), modelTypeEntry);
            case EnumContentTocEntry enumContentEntry -> addEnumContentTocEntry(enumContentEntry);
            case EnumXmlAdapterTocEntry enumXmlAdapterEntry -> enumXmlAdapterTocEntryMap.put(entry.getIpsObjectId(),
                    enumXmlAdapterEntry);
            case CustomTocEntryObject<?> tocEntry -> putTypedTocEntryToMap(tocEntry);
            default -> throw new IllegalArgumentException("Unknown entry type " + entry);
        }
    }

    /**
     * Returns whether this ToC is modifiable, which of course it isn't, because it is the read-only
     * ToC, but this is different for other classes in the hierarchy.
     *
     * @see #internalRemoveEntry(TocEntryObject)
     * @since 24.7
     */
    protected boolean isModifiable() {
        return false;
    }

    /**
     * Removes the given entry from this ToC, if this ToC {@link #isModifiable() is modifiable}.
     *
     * @return whether the entry actually was part of this ToC
     * @since 24.7
     */
    // CSOFF: CyclomaticComplexity
    protected boolean internalRemoveEntry(TocEntryObject entry) {
        if (!isModifiable()) {
            throw new UnsupportedOperationException("Table of contents is not modifiable");
        }

        return switch (entry) {
            case ProductCmptTocEntry prodEntry -> {
                boolean removed = pcIdTocEntryMap.remove(prodEntry.getIpsObjectId()) != null;
                pcNameTocEntryMap.remove(prodEntry.getIpsObjectQualifiedName());
                Map<String, ProductCmptTocEntry> versions = getVersions(prodEntry.getKindId());
                versions.remove(prodEntry.getVersionId());
                yield removed;
            }
            case TableContentTocEntry tableEntry -> {
                boolean removed = tableImplClassTocEntryMap.remove(tableEntry.getImplementationClassName()) != null;
                removed |= tableContentNameTocEntryMap.remove(tableEntry.getIpsObjectQualifiedName()) != null;
                yield removed;
            }
            case FormulaTestTocEntry formulaCaseEntry -> testCaseNameTocEntryMap.remove(entry.getIpsObjectQualifiedName()) != null;
            case TestCaseTocEntry testCaseEntry -> testCaseNameTocEntryMap.remove(entry.getIpsObjectQualifiedName()) != null;
            case ModelTypeTocEntry modelTypeEntry -> modelTypeNameTocEntryMap.remove(entry.getIpsObjectQualifiedName()) != null;
            case EnumContentTocEntry enumTypeEntry -> enumContentImplClassTocEntryMap.remove(entry.getImplementationClassName()) != null;
            case EnumXmlAdapterTocEntry enumXmlAdapterEntry -> enumXmlAdapterTocEntryMap.remove(entry.getIpsObjectId()) != null;
            case CustomTocEntryObject<?> tocEntry -> {
                Map<String, CustomTocEntryObject<?>> map = otherTocEntryMaps.get(tocEntry.getRuntimeObjectClass());
                yield map != null && map.remove(tocEntry.getIpsObjectQualifiedName()) != null;
            }
            default -> throw new IllegalArgumentException("Unknown entry type " + entry);
        };

    }
    // CSON: CyclomaticComplexity

    private void removePreviousSingleContent(TocEntryObject entry, TableContentTocEntry previousTocEntry) {
        if (previousTocEntry != null) {
            try {
                @SuppressWarnings("unchecked")
                TableStructure tableStructure = IpsModel.getTableStructure(
                        (Class<? extends ITable<?>>)getClassLoader().loadClass(entry.getImplementationClassName()));
                if (tableStructure.getKind().equals(TableStructureKind.SINGLE_CONTENT)) {
                    tableContentNameTocEntryMap.remove(previousTocEntry.getIpsObjectQualifiedName());
                }
            } catch (ClassNotFoundException e) {
                // don't know the table class, treat it as MULTIPLE_CONTENTS
            }
        }
    }

    private void addEnumContentTocEntry(TocEntryObject entry) {
        EnumContentTocEntry previousEntry = enumContentImplClassTocEntryMap.get(entry.getImplementationClassName());
        if (previousEntry == null || IpsStringUtils.isEmpty(previousEntry.getXmlResourceName())) {
            enumContentImplClassTocEntryMap.put(entry.getImplementationClassName(), (EnumContentTocEntry)entry);
        }
    }

    private <T> void putTypedTocEntryToMap(CustomTocEntryObject<T> tocEntry) {
        Map<String, CustomTocEntryObject<?>> otherTocEntryMap = otherTocEntryMaps
                .computeIfAbsent(tocEntry.getRuntimeObjectClass(), $ -> new HashMap<>());
        otherTocEntryMap.put(tocEntry.getIpsObjectQualifiedName(), tocEntry);
    }

    @Override
    public List<ProductCmptTocEntry> getProductCmptTocEntries() {
        return new ArrayList<>(pcIdTocEntryMap.values());
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
        return getVersions(kindId).get(versionId);
    }

    @Override
    public List<ProductCmptTocEntry> getProductCmptTocEntries(String kindId) {
        return new ArrayList<>(getVersions(kindId).values());
    }

    private Map<String, ProductCmptTocEntry> getVersions(String kindId) {
        return kindIdTocEntryListMap.computeIfAbsent(kindId, $ -> new TreeMap<>());
    }

    @Override
    public List<TableContentTocEntry> getTableTocEntries() {
        return new ArrayList<>(tableContentNameTocEntryMap.values());
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
        return new ArrayList<>(testCaseNameTocEntryMap.values());
    }

    @Override
    public TestCaseTocEntry getTestCaseTocEntryByQName(String qName) {
        return testCaseNameTocEntryMap.get(qName);
    }

    @Override
    public Set<ModelTypeTocEntry> getModelTypeTocEntries() {
        return new HashSet<>(modelTypeNameTocEntryMap.values());
    }

    @Override
    public List<EnumContentTocEntry> getEnumContentTocEntries() {
        return new ArrayList<>(enumContentImplClassTocEntryMap.values());
    }

    @Override
    public EnumContentTocEntry getEnumContentTocEntry(String className) {
        return enumContentImplClassTocEntryMap.get(className);
    }

    @Override
    public Set<EnumXmlAdapterTocEntry> getEnumXmlAdapterTocEntries() {
        return new HashSet<>(enumXmlAdapterTocEntryMap.values());
    }

    public List<TocEntryObject> getEntries() {
        SortedSet<TocEntryObject> sortedEntries = new TreeSet<>(
                comparing(TocEntryObject::getPackageName)
                        .thenComparing(TocEntryObject::getUnqualifiedName)
                        .thenComparing(TocEntry::getXmlElementTag));
        sortedEntries.addAll(pcIdTocEntryMap.values());
        sortedEntries.addAll(tableContentNameTocEntryMap.values());
        sortedEntries.addAll(testCaseNameTocEntryMap.values());
        sortedEntries.addAll(modelTypeNameTocEntryMap.values());
        sortedEntries.addAll(enumContentImplClassTocEntryMap.values());
        sortedEntries.addAll(enumXmlAdapterTocEntryMap.values());
        for (Map<String, CustomTocEntryObject<?>> otherTocEntryMap : otherTocEntryMaps.values()) {
            sortedEntries.addAll(otherTocEntryMap.values());
        }
        return new ArrayList<>(sortedEntries);
    }

    @Override
    public <T> CustomTocEntryObject<T> getCustomTocEntry(Class<T> type, String ipsObjectQualifiedName) {
        Map<String, CustomTocEntryObject<?>> otherTocEntryMap = otherTocEntryMaps.get(type);
        if (otherTocEntryMap != null) {
            return castCustomEntryObject(ipsObjectQualifiedName, otherTocEntryMap);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private <T> CustomTocEntryObject<T> castCustomEntryObject(String ipsObjectQualifiedName,
            Map<String, CustomTocEntryObject<?>> otherTocEntryMap) {
        return (CustomTocEntryObject<T>)otherTocEntryMap
                .get(ipsObjectQualifiedName);
    }

    @Override
    public <T extends IRuntimeObject> List<CustomTocEntryObject<T>> getTypedTocEntries(Class<T> type) {
        Map<String, CustomTocEntryObject<?>> otherTocEntryMap = otherTocEntryMaps.get(type);
        ArrayList<CustomTocEntryObject<T>> list = new ArrayList<>();
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
}
