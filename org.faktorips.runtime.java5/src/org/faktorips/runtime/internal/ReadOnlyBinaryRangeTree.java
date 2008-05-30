/***************************************************************************************************
 *  * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.  *  * Alle Rechte vorbehalten.  *  *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,  * Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der  * Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community)  * genutzt werden, die Bestandteil der Auslieferung ist und auch
 * unter  *   http://www.faktorips.org/legal/cl-v01.html  * eingesehen werden kann.  *  *
 * Mitwirkende:  *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de  *  
 **************************************************************************************************/

package org.faktorips.runtime.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
 * is not included in it. That means the range is open at least at one side. The two-column tree
 * expects the keys of the given map to be instances of the static innner class TwoColumnKey. The
 * upper and lower bound of TwoColumnKeys are always inclusive, which means there are no different
 * tree types for a two-column tree. There is no check whether the keys of the map given to the
 * two-column tree constructor are really instances of TwoColumnKey, instead a ClassCastException
 * may occur in getValue() in a two-column tree that was initialized with non-TwoColumnKey keys.
 * When created the tree can be asked to return a value for the range the provided key lies in by
 * means of the getValue(Comparable key) method. The returned values are the values of the map upon
 * which the tree is created. Request to the getValue() method of this class are thread safe.
 * 
 * @author Peter Erzberger
 */
public class ReadOnlyBinaryRangeTree<K extends Comparable<? super K>,V> implements Serializable {

    private static final long serialVersionUID = -5127537049885131034L;

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
     * Indicates that the keys are meant to be the lower bound of a range including the lower
     * bound.
     */
    public static final KeyType KEY_IS_LOWER_BOUND_EQUAL = KeyType.KEY_IS_LOWER_BOUND_EQUAL;

    /**
     * Indicates that the keys are meant to be the upper bound of a range.
     */
    public static final KeyType KEY_IS_UPPER_BOUND = KeyType.KEY_IS_UPPER_BOUND;

    /**
     * Indicates that the keys are meant to be the upper bound of a range including the upper
     * bound.
     */
    public static final KeyType KEY_IS_UPPER_BOUND_EQUAL = KeyType.KEY_IS_UPPER_BOUND_EQUAL;

    /**
     * Indicates that the keys represent Instances of the inner class TwoColumnKey.
     */
    public static final KeyType KEY_IS_TWO_COLUMN_KEY = KeyType.KEY_IS_TWO_COLUMN_KEY;

    // the root node of the tree. The variable is protected to be able to test the created tree
    protected Node<K> root;

    // the values that are associated with the ranges
    private List<Object> values;

    // the type that specifies how the result of the visitor has to be interpreted
    private KeyType keyType;

    /**
     * Constructor for a range tree. Keys of the provided map must be of type Comparable and really
     * be comparable (the compareTo()-method must return a defined value of -1, 0 or 1 on any
     * combination of two keys in the map).
     * 
     * @param map The keys and values from which the tree is built.
     * @param keyType see class description on range types.
     */
    public ReadOnlyBinaryRangeTree(Map<K,V> map, KeyType keyType) {
        if (map == null) {
            throw new NullPointerException();
        }
        this.keyType = keyType;
        buildTree(map);
    }

    private boolean isEven(int value) {
        return value % 2 == 0;
    }

    private int[] createInitialVisitedArray(int size) {
        int[] visited = new int[size];
        for (int i = 0; i < visited.length; i++) {
            visited[i] = 0;
        }
        return visited;
    }

    private void buildValuesArray(List<K> keys, Map<K,V> map) {

        values = new ArrayList<Object>(keys.size());
        for (K key : keys) {
            values.add(map.get(key));
        }
    }

    private void buildTree(Map<K,V> map) {

        if (map.isEmpty()) {
            return;
        }

        List<K> keys = new ArrayList<K>(map.keySet());
        Collections.sort(keys);
        buildValuesArray(keys, map);

        int middlePos = keys.size() / 2;
        int[] visited = createInitialVisitedArray(keys.size());
        root = new Node<K>(keys.get(middlePos), middlePos);
        visited[middlePos] = 1;
        int widthCount = isEven(keys.size()) ? keys.size() - middlePos : keys.size() - (1 + middlePos);
        buildChildNodes(middlePos, widthCount, keys, visited, root);
    }

    private void buildChildNodes(int middlePos, int widthCount, List<K> keys, int[] visited, Node<K> parent) {

        if (parent == null) {
            return;
        }
        int leftPos = isEven(widthCount) ? middlePos - (widthCount / 2) : middlePos - (widthCount + 1) / 2;
        int rightPos = isEven(widthCount) ? middlePos + (widthCount / 2) : middlePos + (widthCount + 1) / 2;
        int newWidthCount = widthCount / 2;

        if (rightPos > keys.size() - 1) {
            rightPos--;
        }

        if (leftPos < 0) {
            leftPos++;
        }

        Node<K> leftNode = null;
        Node<K> rightNode = null;

        if (visited[leftPos] == 0) {
            leftNode = parent.newLeft(keys.get(leftPos), leftPos);
            visited[leftPos] = 1;
        }

        if (visited[rightPos] == 0) {
            rightNode = parent.newRight(keys.get(rightPos), rightPos);
            visited[rightPos] = 1;
        }
        buildChildNodes(leftPos, newWidthCount, keys, visited, leftNode);
        buildChildNodes(rightPos, newWidthCount, keys, visited, rightNode);
    }

    /**
     * Returns the value that is associated with the range the provided key lies in. If the key is
     * out of range null will be returned. This method is thread safe.
     */
    public Object getValue(K key) {
        NodeVisitor<K> visitor;
        if (keyType == KeyType.KEY_IS_TWO_COLUMN_KEY) {
            visitor = new TwoColumnNodeVisitor<K>();
        } else {
            visitor = new OneColumnNodeVisitor<K>(keyType);
        }
        visitor.start(root, key);
        int foundIndex = visitor.getFoundIndex();
        return foundIndex != -1 ? values.get(foundIndex) : null;
    }

    /**
     * Interface for node-visitors to find the value index for the given key, if any.
     * 
     * @author Thorsten WÃ¤rtel
     */
    private static interface NodeVisitor<K> {
        /**
         * Searches the tree under startNode for an entry that matches the given key.
         * 
         * @param startNode The root of the (sub-)tree in which to perform the search.
         * @param key The key the visited nodes are compared to.
         */
        void start(Node<K> startNode, K key);

        /**
         * Returns the value index that is stored in the found node.
         * 
         * @return The found index or -1 if no match was found for the given key.
         */
        int getFoundIndex();
    }

    /**
     * Visits the tree of nodes starting from the startNode provided to the start(Node) method. On
     * every node it checks if the key associated with this visitor is smaller or greater than the
     * key that is associated with the node. According to the result is saves the index for the
     * value that is associated with the tree node.
     * 
     * @author Peter Erzberger
     */
    private static class OneColumnNodeVisitor<K extends Comparable<? super K>> implements NodeVisitor<K>, Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = 8409704039187989276L;
        private K key;
        private int keyForSmallestMax = -1;
        private int keyForGreatestMin = -1;
        private int keyForEqual = -1;
        private KeyType keyType;

        private OneColumnNodeVisitor(KeyType keyType) {
            this.keyType = keyType;
        }

        public void start(Node<K> startNode, K key) {
            this.key = key;
            visit(startNode);
        }

        private boolean isSmallestMaxAvailable() {
            return keyForSmallestMax != -1;
        }

        private boolean isGreatestMinAvailable() {
            return keyForGreatestMin != -1;
        }

        private boolean isEqualAvailable() {
            return keyForEqual != -1;
        }

        private void visit(Node<K> node) {
            int comparationResult = key.compareTo(node.key);
            if (comparationResult > 0) {
                keyForGreatestMin = node.fValueIndex;
                if (node.right != null)
                    visit(node.right);
                return;
            }
            if (comparationResult < 0) {
                keyForSmallestMax = node.fValueIndex;
                if (node.left != null)
                    visit(node.left);
                return;
            }
            keyForEqual = node.fValueIndex;
        }

        public int getFoundIndex() {
            switch (keyType) {
                case KEY_IS_UPPER_BOUND:
                    return keyForSmallestMax;
                case KEY_IS_UPPER_BOUND_EQUAL:
                    if (isEqualAvailable()) {
                        return keyForEqual;
                    }
                    if (isSmallestMaxAvailable()) {
                        return keyForSmallestMax;
                    }
                    return -1;
                case KEY_IS_LOWER_BOUND:
                    return keyForGreatestMin;
                case KEY_IS_LOWER_BOUND_EQUAL:
                    if (isEqualAvailable()) {
                        return keyForEqual;
                    }
                    if (isGreatestMinAvailable()) {
                        return keyForGreatestMin;
                    }
                    return -1;
                default:
                    throw new RuntimeException("Encountered unexpected tree type: " + keyType);
            }
        }
    }

    /**
     * Visits the tree nodex starting from the startNode given to the start() method. This visitor
     * assumes that the keys of the visited nodes are instances of TwoColumnKey, i.e. the given tree
     * represents a set of two-column ranges.
     * 
     * @author Thorsten WÃ¤rtel
     */
    private static class TwoColumnNodeVisitor<K extends Comparable<? super K>> implements NodeVisitor<K>, Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = 42L;
        private int foundIndex = -1;
        private K key;

        public void start(Node<K> startNode, K key) {
            this.key = key;
            visit(startNode);
        }

        @SuppressWarnings("unchecked")
        private void visit(Node<K> node) {
            final TwoColumnKey<K> nodeKey = (TwoColumnKey<K>)node.key;
            if (key.compareTo(nodeKey.lowerBound) < 0) {
                if (node.left != null) {
                    visit(node.left);
                }
            } else {
                if (key.compareTo(nodeKey.upperBound) > 0) {
                    if (node.right != null) {
                        visit(node.right);
                    }
                } else {
                    foundIndex = node.fValueIndex;
                }
            }
        }

        public int getFoundIndex() {
            return foundIndex;
        }

    }

    /**
     * Instances of this class represent a node within the tree. A node can be asked for the value
     * it holds.
     * 
     * @author Peter Erzberger
     */
    public static class Node<K> implements Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = -3023843176585381905L;
        protected K key;
        protected Node<K> left;
        protected Node<K> right;
        private int fValueIndex;

        private Node(K k, int valueIndex) {
            this.key = k;
            fValueIndex = valueIndex;
        }

        /**
         * Creates a left child node on this node that keeps the provided value.
         */
        private Node<K> newLeft(K key, int valueIndex) {
            left = new Node<K>(key, valueIndex);
            return left;
        }

        /**
         * Creates a right hand side child node on this node that keeps the provided value.
         */
        private Node<K> newRight(K key, int valueIndex) {
            right = new Node<K>(key, valueIndex);
            return right;
        }
    }

    public static class TwoColumnKey<K extends Comparable<? super K>> implements Comparable<TwoColumnKey<K>>, Serializable {
        /**
         * 
         */
        private static final long serialVersionUID = 42L;
        private final K lowerBound;
        private final K upperBound;
        
        /**
         * @param lowerBound The lowerBound of this TwoColumnKey.
         * @param upperBound The upperBound of this TwoColumnKey.
         */
        public TwoColumnKey(K lowerBound, K upperBound) {
            super();
            if (lowerBound==null) {
                throw new NullPointerException();
            }
            if (upperBound==null) {
                throw new NullPointerException();
            }
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
        }

        public int compareTo(TwoColumnKey<K> other) {
            return lowerBound.compareTo(other.lowerBound);
        }
        
        public boolean equals(TwoColumnKey<K> other) {
            return lowerBound.equals(other.lowerBound) && upperBound.equals(other.upperBound);
        }

        public int hashCode() {
            return lowerBound.hashCode() + upperBound.hashCode();
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
