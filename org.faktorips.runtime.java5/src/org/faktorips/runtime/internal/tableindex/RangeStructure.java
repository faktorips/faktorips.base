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

import java.sql.ResultSet;
import java.util.TreeMap;

/**
 * A {@link SearchStructure} that maps ranges to nested {@link SearchStructure SearchStructures}. A
 * {@link RangeStructure} is configured by a {@link RangeType} to define how the bounds of the
 * contained ranges should be handled.
 * <p>
 * Ranges are set up by putting one or more key-value pairs into this structure. The key is of a
 * comparable data type (of course, as it defines a range). The value is a nested
 * {@link SearchStructure}. The given key defines one of the bounds of a range, which one depends on
 * the {@link RangeType}. In case of {@link RangeType#LOWER_BOUND_EQUAL} the key defines the lower
 * bound of the range (and is included the range). The upper bound is the lower bound of the
 * following/higher range. If there is no following range the range has no upper bound and is
 * infinite.
 * <p>
 * Example: In a {@link RangeType#LOWER_BOUND_EQUAL} {@link RangeStructure} by calling
 * <code>put(10, value1)</code> and <code>put(25, value2)</code>, two ranges are defined: [10..24]
 * and [25..infinity]. Calls to {@link #get(Object)} with the keys 10, 24 and all in between will
 * yield value1 as a result. Calls to {@link #get(Object)} with the keys 25 and higher will yield
 * value2 respectively. The keys 9 and lower, however, will return an empty {@link ResultSet}.
 * 
 * @see RangeType
 */
public class RangeStructure<K extends Comparable<K>, V extends SearchStructure<R> & Mergeable<? super V>, R> extends
        AbstractMapStructure<K, V, R> {

    private final RangeType rangeType;

    /**
     * @param rangeType defines how the bounds of ranges should be handled. Must not be
     *            <code>null</code>.
     * @throws NullPointerException if the {@link RangeType} is <code>null</code>
     */
    RangeStructure(RangeType rangeType) {
        super(new TreeMap<K, V>());
        if (rangeType == null) {
            throw new NullPointerException("RangeType must not be null");
        }
        this.rangeType = rangeType;
    }

    /**
     * Creates an empty {@link RangeStructure}.
     */
    public static <K extends Comparable<K>, V extends SearchStructure<R> & Mergeable<? super V>, R> RangeStructure<K, V, R> create(RangeType keyType) {
        return new RangeStructure<K, V, R>(keyType);
    }

    /**
     * Creates a new {@link RangeStructure} and adds the given key-value pair.
     */
    public static <K extends Comparable<K>, V extends SearchStructure<R> & Mergeable<? super V>, R> RangeStructure<K, V, R> createWith(RangeType keyType,
            K key,
            V value) {
        RangeStructure<K, V, R> structure = new RangeStructure<K, V, R>(keyType);
        structure.put(key, value);
        return structure;
    }

    @Override
    public SearchStructure<R> get(Object key) {
        if (key == null) {
            return createEmptyResult();
        } else {
            @SuppressWarnings("unchecked")
            K kKey = (K)key;
            V result = rangeType.getValue(getMap(), kKey);
            return getValidResult(result);
        }
    }

    @Override
    protected TreeMap<K, V> getMap() {
        return (TreeMap<K, V>)super.getMap();
    }

}