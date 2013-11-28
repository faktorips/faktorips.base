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

import java.util.Map.Entry;
import java.util.TreeMap;

public enum KeyType {

    /**
     * Indicates that the keys are meant to be the lower bound of a range.
     */
    KEY_IS_LOWER_BOUND {
        @Override
        public <K extends Comparable<K>, V> V getValue(TreeMap<K, V> tree, K key) {
            return getLowerValueIfNeccessaryOrNull(tree, key);
        }
    },

    /**
     * Indicates that the keys are meant to be the lower bound of a range including the lower bound.
     */
    KEY_IS_LOWER_BOUND_EQUAL {

        @Override
        public <K extends Comparable<K>, V> V getValue(TreeMap<K, V> tree, K key) {
            Entry<K, V> floorEntry = tree.floorEntry(key);
            return getValueOrNull(floorEntry);
        }
    },

    /**
     * Indicates that the keys are meant to be the upper bound of a range.
     */
    KEY_IS_UPPER_BOUND {

        @Override
        public <K extends Comparable<K>, V> V getValue(TreeMap<K, V> tree, K key) {
            return getHigherValueIfNeccessaryOrNull(tree, key);
        }
    },

    /**
     * Indicates that the keys are meant to be the upper bound of a range including the upper bound.
     */
    KEY_IS_UPPER_BOUND_EQUAL {
        @Override
        public <K extends Comparable<K>, V> V getValue(TreeMap<K, V> tree, K key) {
            Entry<K, V> ceilingEntry = tree.ceilingEntry(key);
            return getValueOrNull(ceilingEntry);
        }
    },
    ;

    /**
     * Retrieves the matching value from the given map using the given key. The strategy used to
     * retrieve the value depends on the type of key. It differs in how the bounds of ranges are
     * processed.
     * 
     * @return the matching value in the given {@link TreeMap} or <code>null</code> if no matching
     *         value could be found.
     */
    public abstract <K extends Comparable<K>, V> V getValue(TreeMap<K, V> tree, K key);

    private static <K extends Comparable<K>, V> V getLowerValueIfNeccessaryOrNull(TreeMap<K, V> tree, K key) {
        Entry<K, V> floorEntry = tree.floorEntry(key);
        if (floorEntry == null) {
            return null;
        }
        return getLowerValueIfNeccessary(tree, key, floorEntry);
    }

    private static <K extends Comparable<K>, V> V getLowerValueIfNeccessary(TreeMap<K, V> tree,
            K key,
            Entry<K, V> floorEntry) {
        if (floorEntry.getKey().compareTo(key) < 0) {
            return floorEntry.getValue();
        } else {
            Entry<K, V> lowerEntry = tree.lowerEntry(floorEntry.getKey());
            return getValueOrNull(lowerEntry);
        }
    }

    private static <K extends Comparable<K>, V> V getHigherValueIfNeccessaryOrNull(TreeMap<K, V> tree, K key) {
        Entry<K, V> ceilingEntry = tree.ceilingEntry(key);
        if (ceilingEntry == null) {
            return null;
        }
        return getHigherValueIfNeccessary(tree, key, ceilingEntry);
    }

    private static <K extends Comparable<K>, V> V getHigherValueIfNeccessary(TreeMap<K, V> tree,
            K key,
            Entry<K, V> ceilingEntry) {
        if (ceilingEntry.getKey().compareTo(key) > 0) {
            return ceilingEntry.getValue();
        } else {
            Entry<K, V> higherEntry = tree.higherEntry(ceilingEntry.getKey());
            return getValueOrNull(higherEntry);
        }
    }

    private static <K, V> V getValueOrNull(Entry<K, V> entry) {
        if (entry != null) {
            return entry.getValue();
        } else {
            return null;
        }
    }
}