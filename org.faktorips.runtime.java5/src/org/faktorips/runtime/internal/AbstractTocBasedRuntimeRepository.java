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
import org.faktorips.runtime.IEnumValue;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.ITable;
import org.faktorips.runtime.test.IpsTest2;
import org.faktorips.runtime.test.IpsTestCaseBase;

/**
 * Abstract base implementation of runtime repository that uses a table of contents to
 * lazily load the product data. 
 * 
 * @author Jan Ortmann 
 */
public abstract class AbstractTocBasedRuntimeRepository extends AbstractRuntimeRepository {
   
    protected AbstractReadonlyTableOfContents toc;
    
    private ICacheFactory cacheFactory;
    
    private ICache<IProductComponent> productCmptCache;
    private ICache<IProductComponentGeneration> productCmptGenerationCache;
    private ICache<ITable> tableCacheByQName;
    private ICache<ITable> tableCacheByClass;
    @SuppressWarnings("unchecked")
    private ICache<List> enumValuesCacheByClass;
    private List<XmlAdapter<?, IEnumValue>> enumXmlAdapters;
    
    public AbstractTocBasedRuntimeRepository(String name, ICacheFactory cacheFactory) {
        super(name);
        this.cacheFactory = cacheFactory;
        initCaches();
    }
    
    protected abstract AbstractReadonlyTableOfContents loadTableOfContents();
    
    @SuppressWarnings("unchecked")
    private void initCaches() {
        productCmptCache = (ICache<IProductComponent>)cacheFactory.createCache(ICacheFactory.Type.PRODUCT_CMPT_CHACHE);
        productCmptGenerationCache  = (ICache<IProductComponentGeneration>)cacheFactory.createCache(ICacheFactory.Type.PRODUCT_CMPT_GENERATION_CHACHE);
        tableCacheByQName = (ICache<ITable>)cacheFactory.createCache(ICacheFactory.Type.TABLE_BY_QUALIFIED_NAME_CACHE);
        tableCacheByClass = (ICache<ITable>)cacheFactory.createCache(ICacheFactory.Type.TABLE_BY_CLASSNAME_CACHE);
        enumValuesCacheByClass = (ICache<List>)cacheFactory.createCache(ICacheFactory.Type.ENUM_CONTENT_BY_CLASS);
        enumXmlAdapters = new LinkedList<XmlAdapter<?, IEnumValue>>();
    }
    
    /**
     * {@inheritDoc}
     */
    protected synchronized IProductComponent getProductComponentInternal(String id) {
        Object obj = productCmptCache.getObject(id);
        if (obj!=null) {
            return (IProductComponent)obj;
        }
        TocEntryObject tocEntry = toc.getProductCmptTocEntry(id);
        if (tocEntry==null) {
            return null;
        }
        IProductComponent pc = createProductCmpt(tocEntry);
        if (pc!=null) {
            productCmptCache.put(id, pc);
        }
        return pc;
    }
    
    protected synchronized IProductComponent getProductComponentInternal(TocEntryObject tocEntry) {
        if (tocEntry==null) {
            return null;
        }
        Object obj = productCmptCache.getObject(tocEntry.getIpsObjectId());
        if (obj!=null) {
            return (IProductComponent)obj;
        }
        IProductComponent pc = createProductCmpt(tocEntry);
        if (pc!=null) {
            productCmptCache.put(pc.getId(), pc);
        }
        return pc;
    }

    @SuppressWarnings("unchecked")
    protected <T extends IEnumValue> List<T> getEnumValuesInternal(Class<T> clazz){
        List<T> enumValues = enumValuesCacheByClass.getObject(clazz);
        if (enumValues != null) {
            return enumValues;
        }
        Set<TocEntryObject> tocEntries = toc.getEnumContentTocEntries();
        if (tocEntries==null) {
            return null;
        }
        TocEntryObject currentTocEntry = null;
        for (TocEntryObject tocEntryObject : tocEntries) {
            if(tocEntryObject.getImplementationClassName().equals(clazz.getName())){
                currentTocEntry = tocEntryObject;
            }
        }
        if(currentTocEntry == null){
            return null;
        }
        enumValues = createEnumValues(currentTocEntry, clazz);
        if (enumValues!=null) {
            enumValuesCacheByClass.put(clazz, enumValues);
        }
        return enumValues;
    }

    protected abstract <T extends IEnumValue> List<T> createEnumValues(TocEntryObject tocEntry, Class<T> clazz);    

    /**
     * {@inheritDoc}
     */
    public final IProductComponent getProductComponentInternal(String kindId, String versionId) {
        TocEntryObject entry = toc.getProductCmptTocEntry(kindId, versionId);
        if (entry==null) {
            return null;
        }
        return getProductComponent(entry.getIpsObjectId());
    }

    /**
     * {@inheritDoc}
     */
    public void getAllProductComponents(String kindId, List<IProductComponent> result) {
        for (TocEntryObject entry : toc.getProductCmptTocEntries(kindId)) {
            result.add(getProductComponent(entry.getIpsObjectId()));
        }
    }

    protected abstract IProductComponent createProductCmpt(TocEntryObject tocEntry);

    /**
     * {@inheritDoc}
     */
    protected IProductComponentGeneration getProductComponentGenerationInternal(String id, Calendar effectiveDate) {
        TocEntryObject tocEntry = toc.getProductCmptTocEntry(id);
        if (tocEntry==null) {
            return null;
        }
        TocEntryGeneration tocEntryGeneration = tocEntry.getGenerationEntry(effectiveDate);
        if (tocEntryGeneration==null) {
            return null;
        }
        return getProductComponentGeneration(id, tocEntryGeneration);
    }

    private IProductComponentGeneration getProductComponentGeneration(String id, TocEntryGeneration tocEntryGeneration){
        GenerationId generationId = new GenerationId(id, tocEntryGeneration.getValidFrom());
        synchronized (this) {
            Object obj = productCmptGenerationCache.getObject(generationId);
            if (obj!=null) {
                return (IProductComponentGeneration)obj;
            }
            IProductComponentGeneration pcGen = createProductCmptGeneration(tocEntryGeneration);
            if (pcGen!=null) {
                productCmptGenerationCache.put(generationId, pcGen);
            }
            return pcGen;
        }
    }
    
    protected abstract IProductComponentGeneration createProductCmptGeneration(TocEntryGeneration tocEntryGeneration);
    
    /**
     * {@inheritDoc}
     */
    public void getAllProductComponents(List<IProductComponent> result) {
        for (TocEntryObject entry : toc.getProductCmptTocEntries()) {
            result.add(getProductComponent(entry.getIpsObjectId()));
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void getProductComponentGenerations(IProductComponent productCmpt, List<IProductComponentGeneration> result) {
        if (productCmpt.getRepository()!=this) {
            return;
        }
        TocEntryObject entry = toc.getProductCmptTocEntry(productCmpt.getId());
        List<TocEntryGeneration> genEntries = entry.getGenerationEntries();
        for (TocEntryGeneration genEntry : genEntries) {
            IProductComponentGeneration gen = getProductComponentGeneration(productCmpt.getId(), genEntry.getValidFrom().toGregorianCalendar(TimeZone.getDefault()));
            result.add(gen);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void getAllProductComponentIds(List<String> result) {
        List<TocEntryObject> entries = toc.getProductCmptTocEntries();
        for (TocEntryObject entry : entries) {
            result.add(entry.getIpsObjectId());
        }
    }

    /**
     * {@inheritDoc}
     */
    public synchronized ITable getTableInternal(Class<?> tableClass) {
        Object obj = tableCacheByClass.getObject(tableClass);
        if (obj != null) {
            return (ITable) obj;
        }
        String tableClassName = tableClass.getName();
        TocEntryObject tocEntry = toc.getTableTocEntryByClassname(tableClassName);
        return getTableInternal(tocEntry);
    }
    
    /**
     * {@inheritDoc}
     */
    public synchronized ITable getTableInternal(String qualifiedTableName) {
        Object obj = tableCacheByQName.getObject(qualifiedTableName);
        if (obj != null) {
            return (ITable) obj;
        }
        TocEntryObject tocEntry = toc.getTableTocEntryByQualifiedTableName(qualifiedTableName);
        return getTableInternal(tocEntry);
    }
    
    private ITable getTableInternal(TocEntryObject tocEntry) {
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
    protected abstract ITable createTable(TocEntryObject tocEntry);
    
    /**
     * {@inheritDoc}
     */
    protected void getAllIpsTestCases(List<IpsTest2> result, IRuntimeRepository runtimeRepository) {
        for (TocEntryObject entry : toc.getTestCaseTocEntries()) {
            result.add(getIpsTestCase(entry.getIpsObjectQualifiedName(), runtimeRepository));
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void getIpsTestCasesStartingWith(String qNamePrefix, List<IpsTest2> result, IRuntimeRepository runtimeRepository) {
        for (TocEntryObject entry : toc.getTestCaseTocEntries()) {
            if (entry.getIpsObjectQualifiedName().startsWith(qNamePrefix)){
                result.add(getIpsTestCase(entry.getIpsObjectQualifiedName(), runtimeRepository));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public synchronized IpsTestCaseBase getIpsTestCaseInternal(String qName, IRuntimeRepository runtimeRepository) {
        TocEntryObject tocEntry = toc.getTestCaseTocEntryByQName(qName);
        if (tocEntry == null) {
            return null;
        }
        return createTestCase(tocEntry, runtimeRepository);
    }
    
    /**
     * Creates the test case object for the given toc entry.
     */
    protected abstract IpsTestCaseBase createTestCase(TocEntryObject tocEntry, IRuntimeRepository runtimeRepository);
    
    /**
     * {@inheritDoc}
     */
    public void reload() {
        this.initCaches();
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
    protected IProductComponentGeneration getNextProductComponentGenerationInternal(IProductComponentGeneration generation) {
        String id = generation.getProductComponent().getId();
        TocEntryObject tocEntry = toc.getProductCmptTocEntry(id);
        TimeZone timeZone = TimeZone.getDefault();
        Date validFromAsDate = generation.getValidFrom(timeZone);
        Calendar validFromAsCalendar = Calendar.getInstance();
        validFromAsCalendar.setTime(validFromAsDate);
        TocEntryGeneration tocEntryGeneration = tocEntry.getNextGenerationEntry(validFromAsCalendar);
        if(tocEntryGeneration == null){
            return null;
        }
        return getProductComponentGeneration(id, tocEntryGeneration);
    }

    /**
     * {@inheritDoc}
     */
    protected int getNumberOfProductComponentGenerationsInternal(IProductComponent productCmpt) {
        TocEntryObject tocEntry = toc.getProductCmptTocEntry(productCmpt.getId());
        return tocEntry.getNumberOfGenerationEntries();
    }

    /**
     * {@inheritDoc}
     */
    protected IProductComponentGeneration getPreviousProductComponentGenerationInternal(IProductComponentGeneration generation) {
        String id = generation.getProductComponent().getId();
        TocEntryObject tocEntry = toc.getProductCmptTocEntry(id);
        TimeZone timeZone = TimeZone.getDefault();
        Date validFromAsDate = generation.getValidFrom(timeZone);
        Calendar validFromAsCalendar = Calendar.getInstance();
        validFromAsCalendar.setTime(validFromAsDate);
        TocEntryGeneration tocEntryGeneration = tocEntry.getPreviousGenerationEntry(validFromAsCalendar);
        if(tocEntryGeneration == null){
            return null;
        }
        return getProductComponentGeneration(id, tocEntryGeneration);
    }
    
    /**
     * {@inheritDoc}
     */
    public IProductComponentGeneration getLatestProductComponentGeneration(IProductComponent productCmpt) {
        if(productCmpt == null){
            throw new NullPointerException("The parameter productCmpt must not be null.");
        }
        TocEntryObject tocEntry = toc.getProductCmptTocEntry(productCmpt.getId());
        TocEntryGeneration entryGeneration = tocEntry.getLatestGenerationEntry();
        return getProductComponentGeneration(productCmpt.getId(), entryGeneration);
    }

	@Override
	protected void getAllModelTypeImplementationClasses(Set<String> result) {
		Set<TocEntryObject> entries = toc.getModelTypeTocEntries();
		for (TocEntryObject tocEntryObject : entries) {
			result.add(tocEntryObject.getImplementationClassName());
		}
	}

	/**
	 * {@inheritDoc}
	 */
    protected List<XmlAdapter<?, IEnumValue>> getAllInternalEnumXmlAdapters(IRuntimeRepository repository){
        if(!enumXmlAdapters.isEmpty()){
            return enumXmlAdapters;
        }
        for (TocEntry tocEntry : toc.getEnumXmlAdapterTocEntries()) {
            try {
            	enumXmlAdapters.add(createEnumXmlAdapter(tocEntry.getImplementationClassName(), repository));
            } catch (Exception e) {
                throw new RuntimeException("Unable to create an XmlAdapter for the enumeration: " + tocEntry.getImplementationClassName(), e);
            }
        }            
        return enumXmlAdapters;
    }
	
    /**
     * Creates and returns an {@link XmlAdapter} instance for the provided class name. 
     * @throws Exception can occur while localizing the xml adapter class and creating the instance
     */
    @SuppressWarnings("unchecked")
    protected XmlAdapter<String, IEnumValue> createEnumXmlAdapter(String className, IRuntimeRepository repository) throws Exception {
        Class<XmlAdapter<String, IEnumValue>> xmlAdapterClass = (Class<XmlAdapter<String, IEnumValue>>)getClassLoader()
                .loadClass(className);
        Constructor<XmlAdapter<String, IEnumValue>> constructor = xmlAdapterClass
                .getConstructor(IRuntimeRepository.class);
        XmlAdapter<String, IEnumValue> instance = constructor.newInstance(repository);
        return instance;
    }
}
