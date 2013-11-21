/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The MultiMap manages a Map, which maps keys to a Set of values.
 * 
 */

public class MultiMap<K, V> {

    private Map<K, Set<V>> internalHashMap = new HashMap<K, Set<V>>();

    protected Set<V> get(K key) {
        getOrCreateSetFor(key);
        return internalHashMap.get(key);
    }

    protected void put(K key, V value) {
        Set<V> setInMap = getOrCreateSetFor(key);
        setInMap = internalHashMap.get(key);
        setInMap.add(value);
    }

    private Set<V> getOrCreateSetFor(K key) {
        Set<V> set = internalHashMap.get(key);
        if (set != null) {
            return set;
        }
        Set<V> newSet = new HashSet<V>();
        return internalHashMap.put(key, newSet);
    }

    public Map<K, Set<V>> getInternalMap() {
        return internalHashMap;
    }
}
