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
 * The MultiMap manages existing and new Keys and their according Set of Values.
 * 
 */

public class MultiMap<K, V> {

    private Map<K, Set<V>> internalHashMap = new HashMap<K, Set<V>>();

    public Set<V> get(K key) {
        Set<V> set = internalHashMap.get(key);
        if (set == null) {
            return new HashSet<V>();
        }
        return set;
    }

    public void put(K indexKey, V row) {
        Set<V> setInMap;
        if (internalHashMap.containsKey(indexKey)) {
            setInMap = internalHashMap.get(indexKey);
        } else {
            setInMap = new HashSet<V>();
            internalHashMap.put(indexKey, setInMap);
        }
        setInMap.add(row);
    }

    public Map<K, Set<V>> getInternalMap() {
        return internalHashMap;
    }
}
