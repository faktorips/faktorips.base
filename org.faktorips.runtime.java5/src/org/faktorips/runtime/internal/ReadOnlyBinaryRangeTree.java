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

import java.io.Serializable;
import java.util.Map;

/**
 * A ReadOnlyBinaryRangeTree provides a specific interpretation of the keys within a java.util.Map
 * upon which the tree is created. The keys in the map are expected to implement the
 * java.lang.Comparable interface. This implies an order among the keys in the map. There are two
 * different types of ranges this tree can support: one-column (unbound) ranges and two-column
 * (bound) ranges. The one-column tree interprets two consecutive keys as the bounds of a range. The
 * tree when created can be configured with 4 different kinds of range types. That means that a key
 * can be considered as the lower bound, lower or equal bound, upper bound, upper or equal bound of
 * a range. The following key in the order of keys is considered the other end of the range while it
 * is not included in it. That means the range is open at least at one side.
 * <p>
 * The two-column tree expects the keys of the given map to be instances of the static innner class
 * TwoColumnKey. The upper and lower bound of TwoColumnKeys are always inclusive, which means there
 * are no different tree types for a two-column tree. There is no check whether the keys of the map
 * given to the two-column tree constructor are really instances of TwoColumnKey, instead a
 * ClassCastException may occur in getValue() in a two-column tree that was initialized with
 * non-TwoColumnKey keys. When created the tree can be asked to return a value for the range the
 * provided key lies in by means of the getValue(Comparable key) method. The returned values are the
 * values of the map upon which the tree is created. Request to the getValue() method of this class
 * are thread safe.
 * 
 * @author Peter Erzberger
 */
public class ReadOnlyBinaryRangeTree<K extends Comparable<K>, V> implements Serializable {

    private static final long serialVersionUID = -5127537049885131034L;

    @SuppressWarnings("hiding")
    // We use the same name for enum and fields here to easy generate code with field access
    // and using the enum in not generated code
    public enum KeyType {

        /**
         * Indicates that the keys are meant to be the lower bound of a range.
         */
        KEY_IS_LOWER_BOUND,

        /**
         * Indicates that the keys are meant to be the lower bound of a range including the lower
         * bound.
         */
        KEY_IS_LOWER_BOUND_EQUAL,

        /**
         * Indicates that the keys are meant to be the upper bound of a range.
         */
        KEY_IS_UPPER_BOUND,

        /**
         * Indicates that the keys are meant to be the upper bound of a range including the upper
         * bound.
         */
        KEY_IS_UPPER_BOUND_EQUAL,

        /**
         * Indicates that the keys represent Instances of the inner class TwoColumnKey.
         */
        KEY_IS_TWO_COLUMN_KEY
    }

    /**
     * Indicates that the keys are meant to be the lower bound of a range.
     */
    public static final KeyType KEY_IS_LOWER_BOUND = KeyType.KEY_IS_LOWER_BOUND;

    /**
     * Indicates that the keys are meant to be the lower bound of a range including the lower bound.
     */
    public static final KeyType KEY_IS_LOWER_BOUND_EQUAL = KeyType.KEY_IS_LOWER_BOUND_EQUAL;

    /**
     * Indicates that the keys are meant to be the upper bound of a range.
     */
    public static final KeyType KEY_IS_UPPER_BOUND = KeyType.KEY_IS_UPPER_BOUND;

    /**
     * Indicates that the keys are meant to be the upper bound of a range including the upper bound.
     */
    public static final KeyType KEY_IS_UPPER_BOUND_EQUAL = KeyType.KEY_IS_UPPER_BOUND_EQUAL;

    /**
     * Indicates that the keys represent Instances of the inner class TwoColumnKey.
     */
    public static final KeyType KEY_IS_TWO_COLUMN_KEY = KeyType.KEY_IS_TWO_COLUMN_KEY;

    private final InternalTree<K, V> internalTree;

    /**
     * Constructor for a range tree. Keys of the provided map must be of type Comparable and really
     * be comparable (the compareTo()-method must return a defined value of -1, 0 or 1 on any
     * combination of two keys in the map).
     * 
     * @param map The keys and values from which the tree is built.
     * @param keyType see class description on range types.
     */
    public ReadOnlyBinaryRangeTree(Map<? extends Comparable<?>, V> map, KeyType keyType) {
        if (map == null) {
            throw new NullPointerException();
        }
        if (keyType == KeyType.KEY_IS_TWO_COLUMN_KEY) {
            @SuppressWarnings("unchecked")
            Map<TwoColumnKey<K>, V> castedMap = (Map<TwoColumnKey<K>, V>)map;
            internalTree = new TwoColumnTree<K, V>(castedMap);
        } else {
            @SuppressWarnings("unchecked")
            Map<K, V> castedMap = (Map<K, V>)map;
            internalTree = new OneColumnTree<K, V>(castedMap, keyType);
        }
    }

    /**
     * Returns the value that is associated with the range the provided key lies in. If the key is
     * out of range null will be returned. This method is thread safe.
     */
    public V getValue(K key) {
        return internalTree.getValue(key);
    }

    private static interface InternalTree<K, V> {

        public V getValue(K key);

    }

    private static class OneColumnTree<K extends Comparable<K>, V> implements InternalTree<K, V> {

        private final BalancedBinaryTree<K, V> internalTree;

        private final KeyType keyType;

        public OneColumnTree(Map<K, V> map, KeyType keyType) {
            this.keyType = keyType;
            internalTree = new BalancedBinaryTree<K, V>(map);
        }

        public V getValue(K key) {
            return internalTree.getValue(key, keyType);
        }

    }

    private static class TwoColumnTree<K extends Comparable<K>, V> implements InternalTree<K, V> {

        private final BalancedBinaryTree<TwoColumnKey<K>, V> internalTree;

        public TwoColumnTree(Map<TwoColumnKey<K>, V> map) {
            internalTree = new BalancedBinaryTree<TwoColumnKey<K>, V>(map);
        }

        public V getValue(K key) {
            return internalTree.getValue(new TwoColumnKey<K>(key, key), KeyType.KEY_IS_TWO_COLUMN_KEY);
        }

    }

    public static class TwoColumnKey<K extends Comparable<K>> implements Comparable<TwoColumnKey<K>>, Serializable {

        private static final long serialVersionUID = 42L;
        private final K lowerBound;
        private final K upperBound;

        /**
         * @param lowerBound The lowerBound of this TwoColumnKey.
         * @param upperBound The upperBound of this TwoColumnKey.
         */
        public TwoColumnKey(K lowerBound, K upperBound) {
            super();
            if (lowerBound == null) {
                throw new NullPointerException();
            }
            if (upperBound == null) {
                throw new NullPointerException();
            }
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
        }

        public int compareTo(TwoColumnKey<K> other) {
            return lowerBound.compareTo(other.lowerBound);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            TwoColumnKey<?> other = (TwoColumnKey<?>)obj;
            if (lowerBound == null) {
                if (other.lowerBound != null) {
                    return false;
                }
            } else if (!lowerBound.equals(other.lowerBound)) {
                return false;
            }
            if (upperBound == null) {
                if (other.upperBound != null) {
                    return false;
                }
            } else if (!upperBound.equals(other.upperBound)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((lowerBound == null) ? 0 : lowerBound.hashCode());
            result = prime * result + ((upperBound == null) ? 0 : upperBound.hashCode());
            return result;
        }

        /**
         * @return Returns the lowerBound.
         */
        public K getLowerBound() {
            return lowerBound;
        }

        /**
         * @return Returns the upperBound.
         */
        public K getUpperBound() {
            return upperBound;
        }
    }
}
