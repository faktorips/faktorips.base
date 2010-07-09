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

package org.faktorips.runtime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.faktorips.runtime.caching.IComputable;
import org.faktorips.runtime.caching.Memoizer;

/**
 * Default cache factory. Uses SoftReferenceCaches for each object type.
 * 
 * @author Jan Ortmann
 */
public class DefaultCacheFactory implements ICacheFactory {

    private Map<Class<?>, Integer> initialSizeMap = new HashMap<Class<?>, Integer>();

    private int defaultInitialSize = 100;

    public DefaultCacheFactory() {
        setInitialSize(IProductComponent.class, 500);
        setInitialSize(IProductComponentGeneration.class, 5000);
        setInitialSize(ITable.class, 100);
    }

    /**
     * Constructor to set the initial capacity of the caches
     * 
     * @param initialCapacityForTablesByQname not used anymore!
     * @param initialCapacityForEnumContentByClassName not used anymore!
     * 
     * @deprecated use the default constructor and set the cache size by calling
     *             {@link #setInitialSize(Class, int)} instead
     */
    @Deprecated
    public DefaultCacheFactory(int initialCapacityForProductCmpts, int initialCapacityForProductCmptGenerations,
            int initialCapacityForTablesByClassname, int initialCapacityForTablesByQname,
            int initialCapacityForEnumContentByClassName) {
        super();
        setInitialSize(IProductComponent.class, initialCapacityForProductCmpts);
        setInitialSize(IProductComponentGeneration.class, initialCapacityForProductCmptGenerations);
        setInitialSize(ITable.class, initialCapacityForTablesByClassname);
    }

    public void setInitialSize(Class<?> typeClass, int size) {
        initialSizeMap.put(typeClass, size);
    }

    /**
     * @param defaultInitialSize The defaultInitialSize to set.
     */
    public void setDefaultInitialSize(int defaultInitialSize) {
        this.defaultInitialSize = defaultInitialSize;
    }

    protected int getInitialSize(Class<?> typeClass) {
        Integer initSize = initialSizeMap.get(typeClass);
        if (initSize == null) {
            initSize = defaultInitialSize;
        }
        return initSize;
    }

    public <K, V> Memoizer<K, V> createCache(IComputable<K, V> computable) {
        return new Memoizer<K, V>(computable);
    }

    public Memoizer<Class<?>, List<?>> createEnumCache(IComputable<Class<?>, List<?>> computable) {
        return createCache(computable);
    }

    public Memoizer<String, IProductComponent> createProductCmptCache(IComputable<String, IProductComponent> computable) {
        return createCache(computable);
    }

    public Memoizer<GenerationId, IProductComponentGeneration> createProductCmptGenerationCache(IComputable<GenerationId, IProductComponentGeneration> computable) {
        return createCache(computable);
    }

    public Memoizer<String, ITable> createTableCache(IComputable<String, ITable> computable) {
        return createCache(computable);
    }

}
