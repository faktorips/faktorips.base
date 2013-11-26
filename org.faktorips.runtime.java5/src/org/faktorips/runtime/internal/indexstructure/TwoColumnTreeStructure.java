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

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class TwoColumnTreeStructure<K extends Comparable<K>, V extends Structure<R> & Mergeable<? super V>, R> extends
        AbstractMapStructure<TwoColumnKey<K>, V, R> {

    public TwoColumnTreeStructure() {
        super(new TreeMap<TwoColumnKey<K>, V>());
    }

    public static <K extends Comparable<K>, V extends Structure<R> & Mergeable<? super V>, R> TwoColumnTreeStructure<K, V, R> create() {
        return new TwoColumnTreeStructure<K, V, R>();
    }

    public void put(K lower, K upper, V value) {
        super.put(new TwoColumnKey<K>(lower, upper), value);
    }

    @Override
    protected TreeMap<TwoColumnKey<K>, V> getMap() {
        return (TreeMap<TwoColumnKey<K>, V>)super.getMap();
    }

    @Override
    public Structure<R> get(Object key) {
        TwoColumnKey<K> twoColumnKey = createTwoColumnKey(key);
        V result = getMatchingResult(twoColumnKey);
        return getValidResult(result);
    }

    private TwoColumnKey<K> createTwoColumnKey(Object key) {
        @SuppressWarnings("unchecked")
        K kKey = (K)key;
        TwoColumnKey<K> twoColumnKey = new TwoColumnKey<K>(kKey, kKey);
        return twoColumnKey;
    }

    /**
     * Returns value mapped by the given TwoColumnKey/range or <code>null</code> no matching value
     * can be found.
     * <p>
     * A simple get on the tree map might not yield the correct result (if any at all). This is due
     * to the fact that {@link TwoColumnKey}'s hashCode() considers only the lowerBound. Thus you
     * will only find a value requesting the same lower bound (when calling the
     * {@link Map#get(Object)} method directly). Mostly though, this is not the case.
     * {@link #get(Object)} is called for values <em>within</em> such a range.
     * <p>
     * This method uses the {@link TreeMap#floorEntry(Object)} method to retrieve the entry holding
     * the value for the next lower key. For example: the map contains key (10-20); Asking for
     * (15-15) will find the entry for (10-20) as it is the one with the next lower bound. Asking
     * for (15-25) will yield <code>null</code> however as the requested range does no fully overlap
     * with the range defined by the {@link TreeMap}.
     */
    private V getMatchingResult(TwoColumnKey<K> twoColumnKey) {
        Entry<TwoColumnKey<K>, V> floorEntry = getMap().floorEntry(twoColumnKey);
        if (isMatchingEntry(twoColumnKey, floorEntry)) {
            return floorEntry.getValue();
        } else {
            return null;
        }
    }

    private boolean isMatchingEntry(TwoColumnKey<K> twoColumnKey, Entry<TwoColumnKey<K>, V> floorEntry) {
        return floorEntry != null && twoColumnKey.isSubRangeOf(floorEntry.getKey());
    }

    private Structure<R> getValidResult(V result) {
        if (result == null) {
            return ResultStructure.create();
        } else {
            return result;
        }
    }

}