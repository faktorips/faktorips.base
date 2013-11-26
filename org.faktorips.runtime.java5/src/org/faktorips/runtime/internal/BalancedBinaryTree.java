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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import org.faktorips.runtime.internal.ReadOnlyBinaryRangeTree.KeyType;
import org.faktorips.runtime.internal.indexstructure.TwoColumnKey;

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
public class BalancedBinaryTree<K extends Comparable<K>, V> implements Serializable {

    private static final long serialVersionUID = -5127537049885131034L;

    // the root node of the tree. The variable is protected to be able to test the created tree
    protected Node<K> root;

    // the values that are associated with the ranges
    private ArrayList<V> values;

    /**
     * Constructor for a range tree. Keys of the provided map must be of type Comparable and really
     * be comparable (the compareTo()-method must return a defined value of -1, 0 or 1 on any
     * combination of two keys in the map).
     * 
     * @param map The keys and values from which the tree is built.
     */
    public BalancedBinaryTree(Map<K, V> map) {
        if (map == null) {
            throw new NullPointerException();
        }
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

    private void buildValuesArray(ArrayList<K> keys, Map<K, V> map) {

        values = new ArrayList<V>();
        for (K key : keys) {
            values.add(map.get(key));
        }
    }

    private void buildTree(Map<K, V> map) {

        if (map.isEmpty()) {
            return;
        }

        ArrayList<K> keys = new ArrayList<K>(map.keySet());
        Collections.sort(keys);
        buildValuesArray(keys, map);

        final int keySize = keys.size();
        int middlePos = keySize / 2;
        int[] visited = createInitialVisitedArray(keySize);
        root = new Node<K>(keys.get(middlePos), middlePos);
        visited[middlePos] = 1;
        int widthCount = isEven(keySize) ? keySize - middlePos : keySize - (1 + middlePos);
        buildChildNodes(middlePos, widthCount, keys, visited, root);
    }

    private void buildChildNodes(int middlePos, int widthCount, ArrayList<K> keys, int[] visited, Node<K> parent) {

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
     * <p>
     * In case of keyType is {@link KeyType#KEY_IS_TWO_COLUMN_KEY} the object key must be of type
     * {@link TwoColumnKey}. To ask for a single value, just set lower and upper bound to the same
     * value.
     */
    public V getValue(K key, KeyType keyType) {
        NodeVisitor<K> visitor;
        visitor = new NodeVisitor<K>(keyType);
        visitor.start(root, key);
        int foundIndex = visitor.getFoundIndex();
        return foundIndex != -1 ? values.get(foundIndex) : null;
    }

    /**
     * Visits the tree of nodes starting from the startNode provided to the start(Node) method. On
     * every node it checks if the key associated with this visitor is smaller or greater than the
     * key that is associated with the node. According to the result is saves the index for the
     * value that is associated with the tree node.
     * 
     */
    private static class NodeVisitor<K extends Comparable<K>> implements Serializable {

        private static final long serialVersionUID = 8409704039187989276L;
        private K key;
        private Node<K> keyForSmallestMax = null;
        private Node<K> keyForGreatestMin = null;
        private Node<K> keyForEqual = null;
        private final KeyType keyType;

        private NodeVisitor(KeyType keyType) {
            this.keyType = keyType;
        }

        /**
         * Searches the tree under startNode for an entry that matches the given key.
         * 
         * @param startNode The root of the (sub-)tree in which to perform the search.
         * @param key The key the visited nodes are compared to.
         */
        public void start(Node<K> startNode, K key) {
            this.key = key;
            visit(startNode);
        }

        private boolean isSmallestMaxAvailable() {
            return keyForSmallestMax != null;
        }

        private boolean isGreatestMinAvailable() {
            return keyForGreatestMin != null;
        }

        private boolean isEqualAvailable() {
            return keyForEqual != null;
        }

        private void visit(Node<K> node) {
            int comparationResult = key.compareTo(node.key);
            if (comparationResult > 0) {
                keyForGreatestMin = node;
                if (node.right != null) {
                    visit(node.right);
                }
                return;
            }
            if (comparationResult < 0) {
                keyForSmallestMax = node;
                if (node.left != null) {
                    visit(node.left);
                }
                return;
            }
            keyForEqual = node;
        }

        /**
         * Returns the value index that is stored in the found node.
         * 
         * @return The found index or -1 if no match was found for the given key.
         */
        public <IK extends Comparable<IK>> int getFoundIndex() {
            switch (keyType) {
                case KEY_IS_UPPER_BOUND:
                    if (isSmallestMaxAvailable()) {
                        return keyForSmallestMax.fValueIndex;
                    } else {
                        return -1;
                    }
                case KEY_IS_UPPER_BOUND_EQUAL:
                    if (isEqualAvailable()) {
                        return keyForEqual.fValueIndex;
                    }
                    if (isSmallestMaxAvailable()) {
                        return keyForSmallestMax.fValueIndex;
                    } else {
                        return -1;
                    }
                case KEY_IS_LOWER_BOUND:
                    if (isGreatestMinAvailable()) {
                        return keyForGreatestMin.fValueIndex;
                    } else {
                        return -1;
                    }
                case KEY_IS_LOWER_BOUND_EQUAL:
                    if (isEqualAvailable()) {
                        return keyForEqual.fValueIndex;
                    }
                    if (isGreatestMinAvailable()) {
                        return keyForGreatestMin.fValueIndex;
                    }
                    return -1;
                case KEY_IS_TWO_COLUMN_KEY:
                    if (isEqualAvailable()) {
                        return keyForEqual.fValueIndex;
                    } else if (isGreatestMinAvailable()) {
                        @SuppressWarnings("unchecked")
                        TwoColumnKey<IK> twoColumnKey = (TwoColumnKey<IK>)key;
                        @SuppressWarnings("unchecked")
                        TwoColumnKey<IK> foundKey = (TwoColumnKey<IK>)keyForGreatestMin.key;
                        if (twoColumnKey.getLowerBound().compareTo(foundKey.getLowerBound()) >= 0
                                && twoColumnKey.getUpperBound().compareTo(foundKey.getUpperBound()) <= 0) {
                            return keyForGreatestMin.fValueIndex;
                        }
                    }
                    return -1;
                default:
                    throw new RuntimeException("Encountered unexpected tree type: " + keyType);
            }
        }

    }

    /**
     * Instances of this class represent a node within the tree. A node can be asked for the value
     * it holds.
     * 
     * @author Peter Erzberger
     */
    public static class Node<K extends Comparable<K>> implements Serializable {

        private static final long serialVersionUID = -3023843176585381905L;
        protected K key;
        protected Node<K> left;
        protected Node<K> right;
        private final int fValueIndex;

        private Node(K key, int valueIndex) {
            this.key = key;
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

}
