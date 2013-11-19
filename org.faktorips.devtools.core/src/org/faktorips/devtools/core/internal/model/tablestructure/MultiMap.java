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

package org.faktorips.devtools.core.internal.model.tablestructure;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.faktorips.devtools.core.model.tablestructure.IIndex;

public class MultiMap<K, V> {
    private HashMap<K, Set<V>> internalHashMap = new HashMap<K, Set<V>>();

    public Set<V> getSet(IIndex uniqueKey) {
        Set<V> set = internalHashMap.get(uniqueKey);
        if (set == null) {
            return new HashSet<V>();
        }
        return set;
    }

    public void addInternal(K indexKey, V row) {
        Set<V> newSet = new HashSet<V>();
        if (internalHashMap.containsKey(indexKey)) {
            Set<V> oldSet = internalHashMap.get(indexKey);
            newSet = oldSet;
            internalHashMap.remove(indexKey);
        }
        newSet.add(row);
        internalHashMap.put(indexKey, newSet);
    }

    public HashMap<K, Set<V>> getInternalHashMap() {
        return internalHashMap;
    }
}
