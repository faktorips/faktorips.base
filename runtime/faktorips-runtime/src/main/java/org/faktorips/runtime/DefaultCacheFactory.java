/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime;

import java.util.HashMap;
import java.util.Map;

import org.faktorips.runtime.caching.IComputable;
import org.faktorips.runtime.caching.Memoizer;
import org.faktorips.runtime.internal.AbstractCacheFactory;

/**
 * Default cache factory. Uses SoftReferenceCaches for each object type.
 * 
 * @author Jan Ortmann
 */
public class DefaultCacheFactory extends AbstractCacheFactory {

    private Map<Class<?>, Integer> initialSizeMap = new HashMap<>();

    private int defaultInitialSize = 100;

    private float laodFactor = 0.75f;

    private int concurrencyLevel = 16;

    public DefaultCacheFactory(ClassLoader cl) {
        this(cl, 500, 5000, 100, 100, 100);
    }

    /**
     * Constructor to set the initial capacity of the caches
     * 
     * @param initialCapacityForTablesByQname not used anymore!
     * @param initialCapacityForEnumContentByClassName not used anymore!
     * 
     * @deprecated Use the default constructor and set the cache size by calling
     *                 {@link #setInitialSize(Class, int)} instead
     */
    @Deprecated
    public DefaultCacheFactory(ClassLoader cl, int initialCapacityForProductCmpts,
            int initialCapacityForProductCmptGenerations, int initialCapacityForTablesByClassname,
            int initialCapacityForTablesByQname, int initialCapacityForEnumContentByClassName) {
        super();
        try {
            setInitialSize(cl.loadClass(IProductComponent.class.getName()), initialCapacityForProductCmpts);
            setInitialSize(cl.loadClass(IProductComponentGeneration.class.getName()),
                    initialCapacityForProductCmptGenerations);
            setInitialSize(cl.loadClass(ITable.class.getName()), initialCapacityForTablesByClassname);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
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
        return initialSizeMap.getOrDefault(typeClass, defaultInitialSize);
    }

    @Override
    public <K, V> Memoizer<K, V> createCache(IComputable<K, V> computable) {
        Integer initSize = initialSizeMap.getOrDefault(computable.getValueClass(), defaultInitialSize);
        return new Memoizer<>(computable, initSize, laodFactor, concurrencyLevel);
    }

    public void setConcurrencyLevel(int concurrencyLevel) {
        this.concurrencyLevel = concurrencyLevel;
    }

    public int getConcurrencyLevel() {
        return concurrencyLevel;
    }

}
