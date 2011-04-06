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

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.faktorips.runtime.GenerationId;
import org.faktorips.runtime.ICacheFactory;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.ITable;
import org.faktorips.runtime.caching.AbstractComputable;
import org.faktorips.runtime.caching.IComputable;

/**
 * This abstract runtime repository handles the caching for already loaded instances. The caching
 * have to be thread safe for every cache instance that we do not load an object twice. That means,
 * the regular way we load an object is to have a look in the cache if it is already there if not we
 * call the getNotCached... method.
 * <p>
 * To be performant and thread safe we use the double checking ideom as it is discribed by doug lea.
 * Since we use Java 5 there is a threadsafe way to do so:<br>
 * First of all we have a not synchronized look in the cache. As we assume that the cache itself is
 * implemented with a {@link ConcurrentHashMap} this lookup would see the real state of the map. If
 * there is no object yet we have to enter a synchronized block. This block is synchronized for
 * every cache instance so we could handle calls for different objects at same time. In this
 * synchonized block it is important to check again if there is an cached object now because another
 * thread may created one meantime (double-check). Again this only works for
 * {@link ConcurrentHashMap}. If there still is no object cached we create a new one. As long we are
 * in the synchronized block, every other thread is blocked. After the new object is created we put
 * it in the cache and the {@link ConcurrentHashMap} ensures that it is stored completely
 * initialized before another thread would get access.
 * <p>
 * There is still potentially more performance by synchronizing for different keys instead of
 * blocking for every cache instance. This would be more complicated and should be well-considered.
 * 
 * @author dirmeier
 */
public abstract class AbstractCachingRuntimeRepository extends AbstractRuntimeRepository {

    private ICacheFactory cacheFactory;
    private volatile IComputable<String, IProductComponent> productCmptCache;
    private volatile IComputable<GenerationId, IProductComponentGeneration> productCmptGenerationCache;
    private volatile IComputable<String, ITable> tableCacheByQName;
    private volatile IComputable<Class<?>, List<?>> enumValuesCacheByClass;
    private List<XmlAdapter<?, ?>> enumXmlAdapters;

    public AbstractCachingRuntimeRepository(String name, ICacheFactory cacheFactory, ClassLoader cl) {
        super(name);
        this.cacheFactory = cacheFactory;
        initCaches(cl);
    }

    protected void initCaches(ClassLoader cl) {
        try {
            @SuppressWarnings("unchecked")
            Class<IProductComponent> productCmptClass = (Class<IProductComponent>)cl.loadClass(IProductComponent.class
                    .getName());
            IComputable<String, IProductComponent> productCmptComputer = new AbstractComputable<String, IProductComponent>(
                    productCmptClass) {
                public IProductComponent compute(String key) throws InterruptedException {
                    return getNotCachedProductComponent(key);
                }
            };
            productCmptCache = cacheFactory.createProductCmptCache(productCmptComputer);

            @SuppressWarnings("unchecked")
            Class<IProductComponentGeneration> productCmptGenClass = (Class<IProductComponentGeneration>)cl
                    .loadClass(IProductComponentGeneration.class.getName());
            IComputable<GenerationId, IProductComponentGeneration> productCmptGenComputer = new AbstractComputable<GenerationId, IProductComponentGeneration>(
                    productCmptGenClass) {

                public IProductComponentGeneration compute(GenerationId key) throws InterruptedException {
                    return getNotCachedProductComponentGeneration(key);
                }

            };
            productCmptGenerationCache = cacheFactory.createProductCmptGenerationCache(productCmptGenComputer);

            @SuppressWarnings("unchecked")
            Class<ITable> tableClass = (Class<ITable>)cl.loadClass(ITable.class.getName());
            IComputable<String, ITable> tableComputer = new AbstractComputable<String, ITable>(tableClass) {

                public ITable compute(String key) throws InterruptedException {
                    return getNotCachedTable(key);
                }

            };
            tableCacheByQName = cacheFactory.createTableCache(tableComputer);

            IComputable<Class<?>, List<?>> enumValueComputer = new AbstractComputable<Class<?>, List<?>>(List.class) {

                public List<?> compute(Class<?> key) throws InterruptedException {
                    return getNotCachedEnumValues(key);
                }
            };
            enumValuesCacheByClass = cacheFactory.createEnumCache(enumValueComputer);

            enumXmlAdapters = new CopyOnWriteArrayList<XmlAdapter<?, ?>>();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected IProductComponent getProductComponentInternal(String id) {
        try {
            return productCmptCache.compute(id);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract IProductComponent getNotCachedProductComponent(String id);

    protected IProductComponentGeneration getProductComponentGenerationInternal(String id, DateTime validFrom) {
        GenerationId generationId = new GenerationId(id, validFrom);
        try {
            return productCmptGenerationCache.compute(generationId);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract IProductComponentGeneration getNotCachedProductComponentGeneration(GenerationId generationId);

    @Override
    protected <T> List<T> getEnumValuesInternal(Class<T> clazz) {
        try {
            @SuppressWarnings("unchecked")
            List<T> result = (List<T>)enumValuesCacheByClass.compute(clazz);
            return result;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract <T> List<T> getNotCachedEnumValues(Class<T> clazz);

    @Override
    protected ITable getTableInternal(String qualifiedTableName) {
        try {
            return tableCacheByQName.compute(qualifiedTableName);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract ITable getNotCachedTable(String qualifiedTableName);

    @Override
    protected List<XmlAdapter<?, ?>> getAllInternalEnumXmlAdapters(IRuntimeRepository repository) {
        if (!enumXmlAdapters.isEmpty()) {
            return enumXmlAdapters;
        }
        enumXmlAdapters = getNotCachedEnumXmlAdapter(repository);
        return enumXmlAdapters;
    }

    protected abstract List<XmlAdapter<?, ?>> getNotCachedEnumXmlAdapter(IRuntimeRepository repository);

}
