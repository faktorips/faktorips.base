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

package org.faktorips.runtime.internal.tableindex;

import java.util.HashMap;

/**
 * An implementation of {@link AbstractMapStructure} mapping keys to nested {@link SearchStructure
 * SearchStructures} much like a map. This structure is useful for values that can be identified
 * exactly by their. The generic types are described in {@link AbstractMapStructure}
 * 
 * @see AbstractMapStructure
 */
public class KeyStructure<K, V extends SearchStructure<R> & Mergeable<? super V>, R> extends
        AbstractMapStructure<K, V, R> {

    KeyStructure() {
        super(new HashMap<K, V>());
    }

    /**
     * Creates a new empty {@link KeyStructure}.
     */
    public static <K, V extends SearchStructure<R> & Mergeable<? super V>, R> KeyStructure<K, V, R> create() {
        return new KeyStructure<K, V, R>();
    }

    /**
     * Creates a new {@link KeyStructure} and put the given key value pair.
     */
    public static <K, V extends SearchStructure<R> & Mergeable<? super V>, R> KeyStructure<K, V, R> createWith(K key,
            V value) {
        KeyStructure<K, V, R> structure = new KeyStructure<K, V, R>();
        structure.put(key, value);
        return structure;
    }

    @Override
    public SearchStructure<R> get(Object key) {
        if (key == null) {
            return createEmptyResult();
        } else {
            V result = getMap().get(key);
            return getValidResult(result);
        }
    }
}