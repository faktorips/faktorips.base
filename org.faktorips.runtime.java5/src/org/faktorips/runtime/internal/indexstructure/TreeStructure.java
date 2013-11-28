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

import java.util.TreeMap;

public class TreeStructure<K extends Comparable<K>, V extends Structure<R> & Mergeable<? super V>, R> extends
        AbstractMapStructure<K, V, R> {

    private final KeyType keyType;

    /**
     * @param keyType the key type (access-function) to be used when accessing ranges. Must not be
     *            <code>null</code>.
     * @throws NullPointerException if the {@link KeyType} is <code>null</code>
     */
    public TreeStructure(KeyType keyType) {
        super(new TreeMap<K, V>());
        if (keyType == null) {
            throw new NullPointerException("KeyType must not be null");
        }
        this.keyType = keyType;
    }

    public static <K extends Comparable<K>, V extends Structure<R> & Mergeable<? super V>, R> TreeStructure<K, V, R> create(KeyType keyType) {
        return new TreeStructure<K, V, R>(keyType);
    }

    @Override
    protected TreeMap<K, V> getMap() {
        return (TreeMap<K, V>)super.getMap();
    }

    /**
     * Creates a new {@link TreeStructure} and put the given key value pair.
     */
    public static <K extends Comparable<K>, V extends Structure<R> & Mergeable<? super V>, R> TreeStructure<K, V, R> createWith(KeyType keyType,
            K key,
            V value) {
        TreeStructure<K, V, R> structure = new TreeStructure<K, V, R>(keyType);
        structure.put(key, value);
        return structure;
    }

    @Override
    public Structure<R> get(Object key) {
        @SuppressWarnings("unchecked")
        K kKey = (K)key;
        V result = keyType.getValue(getMap(), kKey);
        return getValidResult(result);
    }

}