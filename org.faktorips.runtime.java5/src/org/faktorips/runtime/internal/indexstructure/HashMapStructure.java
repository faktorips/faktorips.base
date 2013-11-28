/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.runtime.internal.indexstructure;

import java.util.HashMap;

/**
 * An implementation of {@link AbstractMapStructure} using a {@link HashMap} as underlying data
 * structure. This structure is useful for every value that could be identified exactly by its key.
 * The generic types are described in {@link AbstractMapStructure}
 * 
 * @see AbstractMapStructure
 */
public class HashMapStructure<K, V extends Structure<R> & Mergeable<? super V>, R> extends
        AbstractMapStructure<K, V, R> {

    HashMapStructure() {
        super(new HashMap<K, V>());
    }

    /**
     * Creates a new empty {@link HashMapStructure}.
     */
    public static <K, V extends Structure<R> & Mergeable<? super V>, R> HashMapStructure<K, V, R> create() {
        return new HashMapStructure<K, V, R>();
    }

    /**
     * Creates a new {@link HashMapStructure} and put the given key value pair.
     */
    public static <K, V extends Structure<R> & Mergeable<? super V>, R> HashMapStructure<K, V, R> createWith(K key,
            V value) {
        HashMapStructure<K, V, R> structure = new HashMapStructure<K, V, R>();
        structure.put(key, value);
        return structure;
    }

    @Override
    public Structure<R> get(Object key) {
        V result = getMap().get(key);
        return getValidResult(result);
    }

}