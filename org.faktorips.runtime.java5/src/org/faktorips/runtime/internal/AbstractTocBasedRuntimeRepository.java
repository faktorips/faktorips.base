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

import java.lang.reflect.Constructor;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.faktorips.runtime.GenerationId;
import org.faktorips.runtime.ICache;
import org.faktorips.runtime.ICacheFactory;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.ITable;
import org.faktorips.runtime.internal.toc.GenerationTocEntry;
import org.faktorips.runtime.internal.toc.IEnumContentTocEntry;
import org.faktorips.runtime.internal.toc.IProductCmptTocEntry;
import org.faktorips.runtime.internal.toc.IReadonlyTableOfContents;
import org.faktorips.runtime.internal.toc.ITableContentTocEntry;
import org.faktorips.runtime.internal.toc.ITestCaseTocEntry;
import org.faktorips.runtime.internal.toc.ITocEntry;
import org.faktorips.runtime.internal.toc.ITocEntryObject;
import org.faktorips.runtime.internal.toc.ModelTypeTocEntry;
import org.faktorips.runtime.test.IpsTest2;
import org.faktorips.runtime.test.IpsTestCaseBase;

/**
 * Abstract base implementation of runtime repository that uses a table of contents to lazily load
 * the product data.
 * 
 * @author Jan Ortmann
 */
public abstract class AbstractTocBasedRuntimeRepository extends AbstractRuntimeRepository {

    protected IReadonlyTableOfContents toc;

    private ICacheFactory cacheFactory;

    private ICache<IProductComponent> productCmptCache;
    private ICache<IProductComponentGeneration> productCmptGenerationCache;
    private ICache<ITable> tableCacheByQName;
    private ICache<ITable> tableCacheByClass;
    @SuppressWarnings("unchecked")
    private ICache<List> enumValuesCacheByClass;
    private List<XmlAdapter<?, ?>> enumXmlAdapters;

    public AbstractTocBasedRuntimeRepository(String name, ICacheFactory cacheFactory) {
        super(name);
        this.cacheFactory = cacheFactory;
        initCaches();
    }

    protected abstract IReadonlyTableOfContents loadTableOfContents();

    private void initCaches() {
        productCmptCache = cacheFactory.createProductCmptCache();
        productCmptGenerationCache = cacheFactory.createProductCmptGenerationCache();
        tableCacheByQName = cacheFactory.createTableCache();
        tableCacheByClass = cacheFactory.createTableCache();
        enumValuesCacheByClass = cacheFactory.createCache(List.class);
        enumXmlAdapters = new LinkedList<XmlAdapter<?, ?>>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected synchronized IProductComponent getProductComponentInternal(String id) {
        Object obj = productCmptCache.getObject(id);
        if (obj != null) {
            return (IProductComponent)obj;
        }
        IProductCmptTocEntry tocEntry = toc.getProductCmptTocEntry(id);
        if (tocEntry == null) {
            return null;
        }
        IProductComponent pc = createProductCmpt(tocEntry);
        if (pc != null) {
            productCmptCache.put(id, pc);
        }
        return pc;
    }

    protected synchronized IProductComponent getProductComponentInternal(IProductCmptTocEntry tocEntry) {
        if (tocEntry == null) {
            return null;
        }
        Object obj = productCmptCache.getObject(tocEntry.getIpsObjectId());
        if (obj != null) {
            return (IProductComponent)obj;
        }
        IProductComponent pc = createProductCmpt(tocEntry);
        if (pc != null) {
            productCmptCache.put(pc.getId(), pc);
        }
        return pc;
    }

    @Override
    protected <T> List<T> getEnumValuesInternal(Class<T> clazz) {
        @SuppressWarnings("unchecked")
        List<T> enumValues = enumValuesCacheByClass.getObject(clazz);
        if (enumValues != null) {
            return enumValues;
        }
        IEnumContentTocEntry tocEntries = toc.getEnumContentTocEntry(clazz.getName());
        if (tocEntries == null) {
            return null;
        }
        enumValues = createEnumValues(tocEntries, clazz);
        if (enumValues != null) {
            enumValuesCacheByClass.put(clazz, enumValues);
        }
        return enumValues;
    }

    protected abstract <T> List<T> createEnumValues(IEnumContentTocEntry tocEntry, Class<T> clazz);

    /**
     * {@inheritDoc}
     */
    @Override
    public final IProductComponent getProductComponentInternal(String kindId, String versionId) {
        IProductCmptTocEntry entry = toc.getProductCmptTocEntry(kindId, versionId);
        if (entry == null) {
            return null;
        }
        return getProductComponent(entry.getIpsObjectId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void getAllProductComponents(String kindId, List<IProductComponent> result) {
        for (IProductCmptTocEntry entry : toc.getProductCmptTocEntries(kindId)) {
            result.add(getProductComponent(entry.getIpsObjectId()));
        }
    }

    protected abstract IProductComponent createProductCmpt(IProductCmptTocEntry tocEntry);

    /**
     * {@inheritDoc}
     */
    @Override
    protected IProductComponentGeneration getProductComponentGenerationInternal(String id, Calendar effectiveDate) {
        IProductCmptTocEntry tocEntry = toc.getProductCmptTocEntry(id);
        if (tocEntry == null) {
            return null;
        }
        GenerationTocEntry generationTocEntry = tocEntry.getGenerationEntry(effectiveDate);
        if (generationTocEntry == null) {
            return null;
        }
        return getProductComponentGeneration(id, generationTocEntry);
    }

    private IProductComponentGeneration getProductComponentGeneration(String id, GenerationTocEntry generationTocEntry) {
        GenerationId generationId = new GenerationId(id, generationTocEntry.getValidFrom());
        synchronized (this) {
            Object obj = productCmptGenerationCache.getObject(generationId);
            if (obj != null) {
                return (IProductComponentGeneration)obj;
            }
            IProductComponentGeneration pcGen = createProductCmptGeneration(generationTocEntry);
            if (pcGen != null) {
                productCmptGenerationCache.put(generationId, pcGen);
            }
            return pcGen;
        }
    }

    protected abstract IProductComponentGeneration createProductCmptGeneration(GenerationTocEntry generationTocEntry);

    @Override
    public void getAllProductComponents(List<IProductComponent> result) {
        for (ITocEntryObject entry : toc.getProductCmptTocEntries()) {
            result.add(getProductComponent(entry.getIpsObjectId()));
        }
    }

    @Override
    public void getAllTables(List<ITable> result) {
        for (ITocEntryObject entry : toc.getTableTocEntries()) {
            result.add(getTable(entry.getIpsObjectQualifiedName()));
        }
    }

    @Override
    public void getProductComponentGenerations(IProductComponent productCmpt, List<IProductComponentGeneration> result) {
        if (productCmpt.getRepository() != this) {
            return;
        }
        IProductCmptTocEntry entry = toc.getProductCmptTocEntry(productCmpt.getId());
        List<GenerationTocEntry> genEntries = entry.getGenerationEntries();
        for (GenerationTocEntry genEntry : genEntries) {
            IProductComponentGeneration gen = getProductComponentGeneration(productCmpt.getId(), genEntry
                    .getValidFrom().toGregorianCalendar(TimeZone.getDefault()));
            result.add(gen);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void getAllProductComponentIds(List<String> result) {
        List<IProductCmptTocEntry> entries = toc.getProductCmptTocEntries();
        for (ITocEntryObject entry : entries) {
            result.add(entry.getIpsObjectId());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized ITable getTableInternal(Class<?> tableClass) {
        ITable obj = tableCacheByClass.getObject(tableClass);
        if (obj != null) {
            return obj;
        }
        String tableClassName = tableClass.getName();
        ITableContentTocEntry tocEntry = toc.getTableTocEntryByClassname(tableClassName);
        return getTableInternal(tocEntry);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized ITable getTableInternal(String qualifiedTableName) {
        ITable obj = tableCacheByQName.getObject(qualifiedTableName);
        if (obj != null) {
            return obj;
        }
        ITableContentTocEntry tocEntry = toc.getTableTocEntryByQualifiedTableName(qualifiedTableName);
        return getTableInternal(tocEntry);
    }

    private ITable getTableInternal(ITableContentTocEntry tocEntry) {
        if (tocEntry == null) {
            return null;
        }
        ITable table = createTable(tocEntry);
        if (table != null) {
            tableCacheByClass.put(table.getClass(), table);
            tableCacheByQName.put(tocEntry.getIpsObjectId(), table);
        }
        return table;
    }

    /**
     * Creates the table object for the given toc entry.
     */
    protected abstract ITable createTable(ITableContentTocEntry tocEntry);

    /**
     * {@inheritDoc}
     */
    @Override
    protected void getAllIpsTestCases(List<IpsTest2> result, IRuntimeRepository runtimeRepository) {
        for (ITocEntryObject entry : toc.getTestCaseTocEntries()) {
            result.add(getIpsTestCase(entry.getIpsObjectQualifiedName(), runtimeRepository));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void getIpsTestCasesStartingWith(String qNamePrefix,
            List<IpsTest2> result,
            IRuntimeRepository runtimeRepository) {
        for (ITocEntryObject entry : toc.getTestCaseTocEntries()) {
            if (entry.getIpsObjectQualifiedName().startsWith(qNamePrefix)) {
                result.add(getIpsTestCase(entry.getIpsObjectQualifiedName(), runtimeRepository));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized IpsTestCaseBase getIpsTestCaseInternal(String qName, IRuntimeRepository runtimeRepository) {
        ITestCaseTocEntry tocEntry = toc.getTestCaseTocEntryByQName(qName);
        if (tocEntry == null) {
            return null;
        }
        return createTestCase(tocEntry, runtimeRepository);
    }

    /**
     * Creates the test case object for the given toc entry.
     */
    protected abstract IpsTestCaseBase createTestCase(ITestCaseTocEntry tocEntry, IRuntimeRepository runtimeRepository);

    /**
     * {@inheritDoc}
     */
    public void reload() {
        initCaches();
        toc = loadTableOfContents();
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

    /**
     * {@inheritDoc}
     */
    @Override
    protected IProductComponentGeneration getNextProductComponentGenerationInternal(IProductComponentGeneration generation) {
        String id = generation.getProductComponent().getId();
        IProductCmptTocEntry tocEntry = toc.getProductCmptTocEntry(id);
        TimeZone timeZone = TimeZone.getDefault();
        Date validFromAsDate = generation.getValidFrom(timeZone);
        Calendar validFromAsCalendar = Calendar.getInstance();
        validFromAsCalendar.setTime(validFromAsDate);
        GenerationTocEntry generationTocEntry = tocEntry.getNextGenerationEntry(validFromAsCalendar);
        if (generationTocEntry == null) {
            return null;
        }
        return getProductComponentGeneration(id, generationTocEntry);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getNumberOfProductComponentGenerationsInternal(IProductComponent productCmpt) {
        IProductCmptTocEntry tocEntry = toc.getProductCmptTocEntry(productCmpt.getId());
        return tocEntry.getNumberOfGenerationEntries();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected IProductComponentGeneration getPreviousProductComponentGenerationInternal(IProductComponentGeneration generation) {
        String id = generation.getProductComponent().getId();
        IProductCmptTocEntry tocEntry = toc.getProductCmptTocEntry(id);
        TimeZone timeZone = TimeZone.getDefault();
        Date validFromAsDate = generation.getValidFrom(timeZone);
        Calendar validFromAsCalendar = Calendar.getInstance();
        validFromAsCalendar.setTime(validFromAsDate);
        GenerationTocEntry generationTocEntry = tocEntry.getPreviousGenerationEntry(validFromAsCalendar);
        if (generationTocEntry == null) {
            return null;
        }
        return getProductComponentGeneration(id, generationTocEntry);
    }

    /**
     * {@inheritDoc}
     */
    public IProductComponentGeneration getLatestProductComponentGeneration(IProductComponent productCmpt) {
        if (productCmpt == null) {
            throw new NullPointerException("The parameter productCmpt must not be null.");
        }
        IProductCmptTocEntry tocEntry = toc.getProductCmptTocEntry(productCmpt.getId());
        GenerationTocEntry entryGeneration = tocEntry.getLatestGenerationEntry();
        return getProductComponentGeneration(productCmpt.getId(), entryGeneration);
    }

    @Override
    protected void getAllModelTypeImplementationClasses(Set<String> result) {
        Set<ModelTypeTocEntry> entries = toc.getModelTypeTocEntries();
        for (ITocEntryObject tocEntryObject : entries) {
            result.add(tocEntryObject.getImplementationClassName());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<XmlAdapter<?, ?>> getAllInternalEnumXmlAdapters(IRuntimeRepository repository) {
        if (!enumXmlAdapters.isEmpty()) {
            return enumXmlAdapters;
        }
        for (ITocEntry tocEntry : toc.getEnumXmlAdapterTocEntries()) {
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

}
