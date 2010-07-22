/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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
