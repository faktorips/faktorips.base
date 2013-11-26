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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A Map which maps keys to sets of values.
 * 
 */

public class MultiMap<K, V> {

    private Map<K, Set<V>> internalHashMap = new HashMap<K, Set<V>>();

    private final Set<V> emptySet = new HashSet<V>();

    /**
     * Associates the specified value with the specified key in this map. If the map previously
     * contained a mapping for the key, the set will be extended by the specified value.
     * 
     * @param key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     */
    protected void put(K key, V value) {
        Set<V> newSet;
        if (internalHashMap.containsKey(key)) {
            newSet = internalHashMap.get(key);
        } else {
            newSet = new HashSet<V>();
            internalHashMap.put(key, newSet);
        }
        newSet.add(value);
    }

    /**
     * Returns the according set of a key of the internalHashMap. If there is no set for a key, an
     * empty set will be returned.
     * 
     * @param key the key whose associated set of values will be returned
     * @return set of values to which the specified key is mapped or an empty Set if the map
     *         contains no mapping for the key
     */
    protected Set<V> get(K key) {
        Set<V> set = checkAndGetRightSet(key);
        return Collections.unmodifiableSet(set);
    }

    private Set<V> checkAndGetRightSet(K key) {
        if (internalHashMap.containsKey(key)) {
            return internalHashMap.get(key);
        }
        return emptySet;
    }

    public Map<K, Set<V>> getInternalMap() {
        return internalHashMap;
    }
}
