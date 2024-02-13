/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal;

import static java.util.function.Predicate.not;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;

import org.faktorips.runtime.GenerationId;
import org.faktorips.runtime.ICacheFactory;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.ITable;
import org.faktorips.runtime.internal.toc.CustomTocEntryObject;
import org.faktorips.runtime.internal.toc.EnumContentTocEntry;
import org.faktorips.runtime.internal.toc.GenerationTocEntry;
import org.faktorips.runtime.internal.toc.IReadonlyTableOfContents;
import org.faktorips.runtime.internal.toc.ModelTypeTocEntry;
import org.faktorips.runtime.internal.toc.ProductCmptTocEntry;
import org.faktorips.runtime.internal.toc.TableContentTocEntry;
import org.faktorips.runtime.internal.toc.TestCaseTocEntry;
import org.faktorips.runtime.internal.toc.TocEntry;
import org.faktorips.runtime.internal.toc.TocEntryObject;
import org.faktorips.runtime.test.IpsTest2;
import org.faktorips.runtime.test.IpsTestCaseBase;
import org.faktorips.runtime.xml.IIpsXmlAdapter;

/**
 * Abstract base implementation of runtime repository that uses a table of contents to lazily load
 * the product data. This implementation also manages the caches.
 *
 * @author Jan Ortmann
 */
public abstract class AbstractTocBasedRuntimeRepository extends AbstractCachingRuntimeRepository {

    private volatile IReadonlyTableOfContents toc;

    public AbstractTocBasedRuntimeRepository(String name, ICacheFactory cacheFactory, ClassLoader cl) {
        super(name, cacheFactory, cl);
    }

    protected abstract IReadonlyTableOfContents loadTableOfContents();

    private void setTableOfContents(IReadonlyTableOfContents toc) {
        this.toc = toc;
    }

    protected IReadonlyTableOfContents getTableOfContents() {
        return toc;
    }

    protected abstract <T> IpsEnum<T> createEnumValues(EnumContentTocEntry tocEntry, Class<T> clazz);

    @Override
    protected final IProductComponent getProductComponentInternal(String kindId, String versionId) {
        ProductCmptTocEntry entry = toc.getProductCmptTocEntry(kindId, versionId);
        if (entry == null) {
            return null;
        }
        return getProductComponent(entry.getIpsObjectId());
    }

    @Override
    public void getAllProductComponents(String kindId, List<IProductComponent> result) {
        for (ProductCmptTocEntry entry : toc.getProductCmptTocEntries(kindId)) {
            result.add(getProductComponent(entry.getIpsObjectId()));
        }
    }

    @Override
    public void getAllProductComponents(List<IProductComponent> result) {
        for (TocEntryObject entry : toc.getProductCmptTocEntries()) {
            result.add(getProductComponent(entry.getIpsObjectId()));
        }
    }

    @Override
    public void getAllEnumClasses(LinkedHashSet<Class<?>> result) {
        for (TocEntryObject entry : toc.getEnumContentTocEntries()) {
            result.add(getClass(entry.getImplementationClassName(), getClassLoader()));
        }
    }

    @Override
    public void getAllProductComponentIds(List<String> result) {
        addAllNewIds(toc.getProductCmptTocEntries(), result);
    }

    private void addAllNewIds(List<? extends TocEntryObject> tocEntries, List<String> result) {
        tocEntries.stream()
                .map(TocEntryObject::getIpsObjectId)
                .filter(not(result::contains))
                .forEach(result::add);
    }

    @Override
    public void getAllTableIds(List<String> result) {
        addAllNewIds(toc.getTableTocEntries(), result);
    }

    protected abstract IProductComponent createProductCmpt(ProductCmptTocEntry tocEntry);

    private IProductComponentGeneration getProductComponentGenerationAtValidFromOrNull(String productCmptId,
            Optional<GenerationTocEntry> generationTocEntry) {
        return generationTocEntry
                .map(GenerationTocEntry::getValidFrom)
                .map(v -> getProductComponentGenerationInternal(productCmptId, v))
                .orElse(null);
    }

    @Override
    protected IProductComponentGeneration getProductComponentGenerationInternal(String id, Calendar effectiveDate) {
        ProductCmptTocEntry tocEntry = toc.getProductCmptTocEntry(id);
        if (tocEntry == null) {
            return null;
        }
        return getProductComponentGenerationAtValidFromOrNull(id, tocEntry.findGenerationEntry(effectiveDate));
    }

    @Override
    protected IProductComponentGeneration getNextProductComponentGenerationInternal(
            IProductComponentGeneration generation) {
        String id = generation.getProductComponent().getId();
        ProductCmptTocEntry tocEntry = toc.getProductCmptTocEntry(id);
        Date validFromAsDate = generation.getValidFrom(TimeZone.getDefault());
        Calendar validFromAsCalendar = Calendar.getInstance();
        validFromAsCalendar.setTime(validFromAsDate);
        return getProductComponentGenerationAtValidFromOrNull(id,
                tocEntry.findNextGenerationEntry(validFromAsCalendar));
    }

    @Override
    protected IProductComponentGeneration getPreviousProductComponentGenerationInternal(
            IProductComponentGeneration generation) {
        String id = generation.getProductComponent().getId();
        ProductCmptTocEntry tocEntry = toc.getProductCmptTocEntry(id);
        Date validFromAsDate = generation.getValidFrom(TimeZone.getDefault());
        Calendar validFromAsCalendar = Calendar.getInstance();
        validFromAsCalendar.setTime(validFromAsDate);
        return getProductComponentGenerationAtValidFromOrNull(id,
                tocEntry.findPreviousGenerationEntry(validFromAsCalendar));
    }

    @Override
    protected IProductComponentGeneration getLatestProductComponentGenerationInternal(IProductComponent productCmpt) {
        Objects.requireNonNull(productCmpt, "The parameter productCmpt must not be null.");
        ProductCmptTocEntry tocEntry = toc.getProductCmptTocEntry(productCmpt.getId());
        return getProductComponentGenerationAtValidFromOrNull(productCmpt.getId(),
                tocEntry.findLatestGenerationEntry());
    }

    @Override
    public void getProductComponentGenerations(IProductComponent productCmpt,
            List<IProductComponentGeneration> result) {
        if (productCmpt.getRepository() != this) {
            return;
        }
        ProductCmptTocEntry entry = toc.getProductCmptTocEntry(productCmpt.getId());
        List<GenerationTocEntry> genEntries = entry.getGenerationEntries();
        for (GenerationTocEntry genEntry : genEntries) {
            IProductComponentGeneration gen = getProductComponentGeneration(productCmpt.getId(),
                    genEntry.getValidFrom().toGregorianCalendar(TimeZone.getDefault()));
            result.add(gen);
        }
    }

    @Override
    protected int getNumberOfProductComponentGenerationsInternal(IProductComponent productCmpt) {
        ProductCmptTocEntry tocEntry = toc.getProductCmptTocEntry(productCmpt.getId());
        return tocEntry.getNumberOfGenerationEntries();
    }

    @Override
    protected IProductComponentGeneration getNotCachedProductComponentGeneration(GenerationId generationId) {
        ProductCmptTocEntry tocEntry = toc.getProductCmptTocEntry(generationId.getQName());
        if (tocEntry == null) {
            return null;
        }
        return tocEntry.findGenerationEntry(generationId.getValidFrom()).map(this::createProductCmptGeneration)
                .orElse(null);
    }

    protected abstract IProductComponentGeneration createProductCmptGeneration(GenerationTocEntry generationTocEntry);

    @Override
    public void getAllTables(List<ITable<?>> result) {
        for (TocEntryObject entry : toc.getTableTocEntries()) {
            result.add(getTable(entry.getIpsObjectQualifiedName()));
        }
    }

    @Override
    protected <T extends ITable<?>> T getTableInternal(Class<T> tableClass) {
        TableContentTocEntry tocEntry = toc.getTableTocEntryByClassname(tableClass.getName());
        if (tocEntry == null) {
            return null;
        }
        return tableClass.cast(getTable(tocEntry.getIpsObjectQualifiedName()));
    }

    private ITable<?> getTableInternal(TableContentTocEntry tocEntry) {
        if (tocEntry == null) {
            return null;
        }
        return createTable(tocEntry);
    }

    @Override
    protected ITable<?> getNotCachedTable(String qualifiedTableName) {
        TableContentTocEntry tocEntry = toc.getTableTocEntryByQualifiedTableName(qualifiedTableName);
        return getTableInternal(tocEntry);
    }

    /**
     * Creates the table object for the given toc entry.
     */
    protected abstract ITable<?> createTable(TableContentTocEntry tocEntry);

    @Override
    protected void getAllIpsTestCases(List<IpsTest2> result, IRuntimeRepository runtimeRepository) {
        for (TocEntryObject entry : toc.getTestCaseTocEntries()) {
            result.add(getIpsTestCase(entry.getIpsObjectQualifiedName(), runtimeRepository));
        }
    }

    @Override
    protected void getIpsTestCasesStartingWith(String qNamePrefix,
            List<IpsTest2> result,
            IRuntimeRepository runtimeRepository) {
        for (TocEntryObject entry : toc.getTestCaseTocEntries()) {
            if (entry.getIpsObjectQualifiedName().startsWith(qNamePrefix)) {
                result.add(getIpsTestCase(entry.getIpsObjectQualifiedName(), runtimeRepository));
            }
        }
    }

    /**
     * Creates the test case object for the given toc entry.
     */
    protected abstract IpsTestCaseBase createTestCase(TestCaseTocEntry tocEntry, IRuntimeRepository runtimeRepository);

    /**
     * Initializes the runtime repository by loading the table of contents. This method have to be
     * called from the constructor after all necessary data is set.
     */
    protected void initialize() {
        setTableOfContents(loadTableOfContents());
    }

    /**
     * Returns the class for the given qualified class name.
     *
     * @param className The qualified class name
     * @param cl The classLoader used to load the load.
     *
     * @throws RuntimeException if the class can't be found.
     */
    protected Class<?> getClass(String className, ClassLoader cl) {
        try {
            return cl.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Can't load class " + className, e);
        }
    }

    @Override
    protected void getAllModelTypeImplementationClasses(Set<String> result) {
        Set<ModelTypeTocEntry> entries = toc.getModelTypeTocEntries();
        for (TocEntryObject tocEntryObject : entries) {
            result.add(tocEntryObject.getImplementationClassName());
        }
    }

    /**
     * Creates and returns an {@link IIpsXmlAdapter} instance for the provided class name.
     *
     * @throws Exception can occur while localizing the XML adapter class and creating the instance
     */
    @SuppressWarnings("unchecked")
    protected IIpsXmlAdapter<String, ?> createEnumXmlAdapter(String className, IRuntimeRepository repository)
            throws Exception {
        Class<IIpsXmlAdapter<String, ?>> xmlAdapterClass = (Class<IIpsXmlAdapter<String, ?>>)getClassLoader()
                .loadClass(className);
        Constructor<IIpsXmlAdapter<String, ?>> constructor = xmlAdapterClass.getConstructor(IRuntimeRepository.class);
        return constructor.newInstance(repository);
    }

    @Override
    protected IProductComponent getNotCachedProductComponent(String id) {
        ProductCmptTocEntry tocEntry = toc.getProductCmptTocEntry(id);
        if (tocEntry == null) {
            return null;
        }
        return createProductCmpt(tocEntry);
    }

    @Override
    protected <T> IpsEnum<T> getNotCachedEnumValues(Class<T> clazz) {
        EnumContentTocEntry tocEntries = toc.getEnumContentTocEntry(clazz.getName());
        if (tocEntries == null) {
            return null;
        }
        return createEnumValues(tocEntries, clazz);
    }

    @Override
    protected List<IIpsXmlAdapter<?, ?>> getNotCachedEnumXmlAdapter(IRuntimeRepository repository) {
        List<IIpsXmlAdapter<?, ?>> enumXmlAdapters = new ArrayList<>();
        for (TocEntry tocEntry : toc.getEnumXmlAdapterTocEntries()) {
            try {
                enumXmlAdapters.add(createEnumXmlAdapter(tocEntry.getImplementationClassName(), repository));
                // CSOFF: IllegalCatch
            } catch (Exception e) {
                throw new RuntimeException(
                        "Unable to create an XmlAdapter for the enumeration: " + tocEntry.getImplementationClassName(),
                        e);
            }
            // CSON: IllegalCatch
        }
        return enumXmlAdapters;
    }

    /**
     * IpsTestCases are created new every time and should not be cached because the test data could
     * change for every test creation.
     */
    @Override
    protected synchronized IpsTestCaseBase getIpsTestCaseInternal(String qName, IRuntimeRepository runtimeRepository) {
        TestCaseTocEntry tocEntry = toc.getTestCaseTocEntryByQName(qName);
        if (tocEntry == null) {
            return null;
        }
        return createTestCase(tocEntry, runtimeRepository);
    }

    @Override
    protected <T> T getNotCachedCustomObject(Class<T> type, String id) {
        CustomTocEntryObject<T> tocEntry = toc.getCustomTocEntry(type, id);
        if (tocEntry == null) {
            return null;
        }
        try {
            return createCustomObject(tocEntry);
            // CSOFF: IllegalCatch
        } catch (Exception e) {
            // CSON: IllegalCatch
            throw new RuntimeException(e);
        }
    }

    protected abstract <T> T createCustomObject(CustomTocEntryObject<T> tocEntry);

}
