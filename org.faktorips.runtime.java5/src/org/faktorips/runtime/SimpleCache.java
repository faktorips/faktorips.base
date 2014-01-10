/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime;

import java.util.concurrent.ConcurrentHashMap;

import org.faktorips.runtime.caching.IComputable;

/**
 * Simple cache that just uses a HashMap to cache objects and never releases them. This Cache is
 * thread safe but not very high-performance.
 * 
 * @author Jan Ortmann
 */
public class SimpleCache implements IComputable<Object, Object> {

    private final ConcurrentHashMap<Object, Object> objects;
    private final IComputable<Object, Object> computable;

    public SimpleCache(IComputable<Object, Object> computable) {
        this(computable, 16);
    }

    /**
     * 
     */
    public SimpleCache(IComputable<Object, Object> computable, int initialCapacity) {
        this.computable = computable;
        objects = new ConcurrentHashMap<Object, Object>(initialCapacity);
    }

    public Object compute(Object key) throws InterruptedException {
        Object result = objects.get(key);
        if (result != null) {
            return result;
        }
        synchronized (this) {
            result = objects.get(key);
            if (result != null) {
                return result;
            }
            result = computable.compute(key);
            if (result != null) {
                objects.putIfAbsent(key, result);
                return result;
            }
            return null;
        }
    }

    public Class<? super Object> getValueClass() {
        return computable.getValueClass();
    }

}
