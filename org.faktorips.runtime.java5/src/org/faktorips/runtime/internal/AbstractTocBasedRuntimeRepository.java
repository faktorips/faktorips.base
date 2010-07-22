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

package org.faktorips.runtime.internal;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.faktorips.runtime.GenerationId;
import org.faktorips.runtime.ICacheFactory;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.ITable;
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

/**
 * Abstract base implementation of runtime repository that uses a table of contents to lazily load
 * the product data. This implementation also manages the caches.
 * 
 * @author Jan Ortmann
 */
public abstract class AbstractTocBasedRuntimeRepository extends AbstractCachingRuntimeRepository {

    private volatile IReadonlyTableOfContents toc;

    public AbstractTocBasedRuntimeRepository(String name, ICacheFactory cacheFactory) {
        super(name, cacheFactory);
    }

    protected abstract IReadonlyTableOfContents loadTableOfContents();

    /**
     * @param toc The toc to set.
     */
    private void setTableOfContents(IReadonlyTableOfContents toc) {
        this.toc = toc;
    }

    /**
     * @return Returns the toc.
     */
    protected IReadonlyTableOfContents getTableOfContents() {
        return toc;
    }

    protected abstract <T> List<T> createEnumValues(EnumContentTocEntry tocEntry, Class<T> clazz);

    /**
     * {@inheritDoc}
     */
    @Override
    public final IProductComponent getProductComponentInternal(String kindId, String versionId) {
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void getAllProductComponentIds(List<String> result) {
        List<ProductCmptTocEntry> entries = toc.getProductCmptTocEntries();
        for (TocEntryObject entry : entries) {
            result.add(entry.getIpsObjectId());
        }
    }

    protected abstract IProductComponent createProductCmpt(ProductCmptTocEntry tocEntry);

    /**
     * {@inheritDoc}
     */
    @Override
    protected IProductComponentGeneration getProductComponentGenerationInternal(String id, Calendar effectiveDate) {
        ProductCmptTocEntry tocEntry = toc.getProductCmptTocEntry(id);
        if (tocEntry == null) {
            return null;
        }
        GenerationTocEntry generationTocEntry = tocEntry.getGenerationEntry(effectiveDate);
        if (generationTocEntry == null) {
            return null;
        }
        return getProductComponentGenerationInternal(id, generationTocEntry.getValidFrom());
    }

    @Override
    protected IProductComponentGeneration getNextProductComponentGenerationInternal(IProductComponentGeneration generation) {
        String id = generation.getProductComponent().getId();
        ProductCmptTocEntry tocEntry = toc.getProductCmptTocEntry(id);
        Date validFromAsDate = generation.getValidFrom(TimeZone.getDefault());
        Calendar validFromAsCalendar = Calendar.getInstance();
        validFromAsCalendar.setTime(validFromAsDate);
        GenerationTocEntry generationTocEntry = tocEntry.getNextGenerationEntry(validFromAsCalendar);
        if (generationTocEntry == null) {
            return null;
        }
        return getProductComponentGenerationInternal(id, generationTocEntry.getValidFrom());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected IProductComponentGeneration getPreviousProductComponentGenerationInternal(IProductComponentGeneration generation) {
        String id = generation.getProductComponent().getId();
        ProductCmptTocEntry tocEntry = toc.getProductCmptTocEntry(id);
        Date validFromAsDate = generation.getValidFrom(TimeZone.getDefault());
        Calendar validFromAsCalendar = Calendar.getInstance();
        validFromAsCalendar.setTime(validFromAsDate);
        GenerationTocEntry generationTocEntry = tocEntry.getPreviousGenerationEntry(validFromAsCalendar);
        if (generationTocEntry == null) {
            return null;
        }
        return getProductComponentGenerationInternal(id, generationTocEntry.getValidFrom());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IProductComponentGeneration getLatestProductComponentGenerationInternal(IProductComponent productCmpt) {
        if (productCmpt == null) {
            throw new NullPointerException("The parameter productCmpt must not be null.");
        }
        ProductCmptTocEntry tocEntry = toc.getProductCmptTocEntry(productCmpt.getId());
        GenerationTocEntry entryGeneration = tocEntry.getLatestGenerationEntry();
        return getProductComponentGenerationInternal(productCmpt.getId(), entryGeneration.getValidFrom());
    }

    @Override
    public void getProductComponentGenerations(IProductComponent productCmpt, List<IProductComponentGeneration> result) {
        if (productCmpt.getRepository() != this) {
            return;
        }
        ProductCmptTocEntry entry = toc.getProductCmptTocEntry(productCmpt.getId());
        List<GenerationTocEntry> genEntries = entry.getGenerationEntries();
        for (GenerationTocEntry genEntry : genEntries) {
            IProductComponentGeneration gen = getProductComponentGeneration(productCmpt.getId(), genEntry
                    .getValidFrom().toGregorianCalendar(TimeZone.getDefault()));
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
        GenerationTocEntry generationTocEntry = tocEntry.getGenerationEntry(generationId.getValidFrom());
        if (generationTocEntry == null) {
            return null;
        }
        return createProductCmptGeneration(generationTocEntry);
    }

    protected abstract IProductComponentGeneration createProductCmptGeneration(GenerationTocEntry generationTocEntry);

    @Override
    public void getAllTables(List<ITable> result) {
        for (TocEntryObject entry : toc.getTableTocEntries()) {
            result.add(getTable(entry.getIpsObjectQualifiedName()));
        }
    }

    @Override
    protected ITable getTableInternal(Class<?> tableClass) {
        TableContentTocEntry tocEntry = toc.getTableTocEntryByClassname(tableClass.getName());
        if (tocEntry == null) {
            return null;
        }
        return getTable(tocEntry.getIpsObjectQualifiedName());
    }

    private ITable getTableInternal(TableContentTocEntry tocEntry) {
        if (tocEntry == null) {
            return null;
        }
        ITable table = createTable(tocEntry);
        return table;
    }

    @Override
    protected ITable getNotCachedTable(String qualifiedTableName) {
        TableContentTocEntry tocEntry = toc.getTableTocEntryByQualifiedTableName(qualifiedTableName);
        return getTableInternal(tocEntry);
    }

    /**
     * Creates the table object for the given toc entry.
     */
    protected abstract ITable createTable(TableContentTocEntry tocEntry);

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
    protected synchronized void initialize() {
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
            Class<?> clazz = cl.loadClass(className);
            return clazz;
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
     * Creates and returns an {@link XmlAdapter} instance for the provided class name.
     * 
     * @throws Exception can occur while localizing the xml adapter class and creating the instance
     */
    @SuppressWarnings("unchecked")
    protected XmlAdapter<String, ?> createEnumXmlAdapter(String className, IRuntimeRepository repository)
            throws Exception {
        Class<XmlAdapter<String, ?>> xmlAdapterClass = (Class<XmlAdapter<String, ?>>)getClassLoader().loadClass(
                className);
        Constructor<XmlAdapter<String, ?>> constructor = xmlAdapterClass.getConstructor(IRuntimeRepository.class);
        XmlAdapter<String, ?> instance = constructor.newInstance(repository);
        return instance;
    }

    @Override
    protected IProductComponent getNotCachedProductComponent(String id) {
        ProductCmptTocEntry tocEntry = toc.getProductCmptTocEntry(id);
        if (tocEntry == null) {
            return null;
        }
        IProductComponent pc = createProductCmpt(tocEntry);
        return pc;
    }

    @Override
    protected <T> List<T> getNotCachedEnumValues(Class<T> clazz) {
        List<T> enumValues;
        EnumContentTocEntry tocEntries = toc.getEnumContentTocEntry(clazz.getName());
        if (tocEntries == null) {
            return null;
        }
        enumValues = createEnumValues(tocEntries, clazz);
        return enumValues;
    }

    @Override
    protected List<XmlAdapter<?, ?>> getNotCachedEnumXmlAdapter(IRuntimeRepository repository) {
        List<XmlAdapter<?, ?>> enumXmlAdapters = new ArrayList<XmlAdapter<?, ?>>();
        for (TocEntry tocEntry : toc.getEnumXmlAdapterTocEntries()) {
            try {
                enumXmlAdapters.add(createEnumXmlAdapter(tocEntry.getImplementationClassName(), repository));
            } catch (Exception e) {
                throw new RuntimeException("Unable to create an XmlAdapter for the enumeration: "
                        + tocEntry.getImplementationClassName(), e);
            }
        }
        return enumXmlAdapters;
    }

    /**
     * IpsTestCases are created new every time and should not be cached because the test data could
     * change for every test creation.
     */
    @Override
    public synchronized IpsTestCaseBase getIpsTestCaseInternal(String qName, IRuntimeRepository runtimeRepository) {
        TestCaseTocEntry tocEntry = toc.getTestCaseTocEntryByQName(qName);
        if (tocEntry == null) {
            return null;
        }
        return createTestCase(tocEntry, runtimeRepository);
    }

}
