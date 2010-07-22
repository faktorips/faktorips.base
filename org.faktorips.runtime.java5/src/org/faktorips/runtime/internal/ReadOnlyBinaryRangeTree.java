/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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
import java.util.Arrays;
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
public class ReadOnlyBinaryRangeTree implements Serializable {

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

    // the root node of the tree. The variable is protected to be able to test the created tree
    protected Node root;

    // the values that are associated with the ranges
    private Object[] values;

    // the type that specifies how the result of the visitor has to be interpreted
    private final KeyType keyType;

    /**
     * Constructor for a range tree. Keys of the provided map must be of type Comparable and really
     * be comparable (the compareTo()-method must return a defined value of -1, 0 or 1 on any
     * combination of two keys in the map).
     * 
     * @param map The keys and values from which the tree is built.
     * @param keyType see class description on range types.
     */
    public ReadOnlyBinaryRangeTree(Map map, KeyType keyType) {
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

    private void buildValuesArray(Comparable[] keys, Map map) {

        values = new Object[keys.length];
        for (int i = 0; i < keys.length; i++) {
            values[i] = map.get(keys[i]);
        }
    }

    private void buildTree(Map map) {

        if (map.isEmpty()) {
            return;
        }

        Comparable[] keys = new Comparable[map.size()];
        map.keySet().toArray(keys);
        Arrays.sort(keys);
        buildValuesArray(keys, map);

        int middlePos = keys.length / 2;
        int[] visited = createInitialVisitedArray(keys.length);
        root = new Node(keys[middlePos], middlePos);
        visited[middlePos] = 1;
        int widthCount = isEven(keys.length) ? keys.length - middlePos : keys.length - (1 + middlePos);
        buildChildNodes(middlePos, widthCount, keys, visited, root);
    }

    private void buildChildNodes(int middlePos, int widthCount, Comparable[] keys, int[] visited, Node parent) {

        if (parent == null) {
            return;
        }
        int leftPos = isEven(widthCount) ? middlePos - (widthCount / 2) : middlePos - (widthCount + 1) / 2;
        int rightPos = isEven(widthCount) ? middlePos + (widthCount / 2) : middlePos + (widthCount + 1) / 2;
        int newWidthCount = widthCount / 2;

        if (rightPos > keys.length - 1) {
            rightPos--;
        }

        if (leftPos < 0) {
            leftPos++;
        }

        Node leftNode = null;
        Node rightNode = null;

        if (visited[leftPos] == 0) {
            leftNode = parent.newLeft(keys[leftPos], leftPos);
            visited[leftPos] = 1;
        }

        if (visited[rightPos] == 0) {
            rightNode = parent.newRight(keys[rightPos], rightPos);
            visited[rightPos] = 1;
        }
        buildChildNodes(leftPos, newWidthCount, keys, visited, leftNode);
        buildChildNodes(rightPos, newWidthCount, keys, visited, rightNode);
    }

    /**
     * Returns the value that is associated with the range the provided key lies in. If the key is
     * out of range null will be returned. This method is thread safe.
     */
    public Object getValue(Comparable key) {
        NodeVisitor visitor;
        if (keyType == KEY_IS_TWO_COLUMN_KEY) {
            visitor = new TwoColumnNodeVisitor();
        } else {
            visitor = new OneColumnNodeVisitor(keyType);
        }
        visitor.start(root, key);
        int foundIndex = visitor.getFoundIndex();
        return foundIndex != -1 ? values[foundIndex] : null;
    }

    /**
     * Interface for node-visitors to find the value index for the given key, if any.
     * 
     * @author Thorsten WÃ¤rtel
     */
    private static interface NodeVisitor {
        /**
         * Searches the tree under startNode for an entry that matches the given key.
         * 
         * @param startNode The root of the (sub-)tree in which to perform the search.
         * @param key The key the visited nodes are compared to.
         */
        void start(Node startNode, Comparable key);

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
    private static class OneColumnNodeVisitor implements NodeVisitor, Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = 8409704039187989276L;
        private Comparable key;
        private int keyForSmallestMax = -1;
        private int keyForGreatestMin = -1;
        private int keyForEqual = -1;
        private final KeyType keyType;

        private OneColumnNodeVisitor(KeyType keyType) {
            this.keyType = keyType;
        }

        public void start(Node startNode, Comparable key) {
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

        private void visit(Node node) {
            int comparationResult = key.compareTo(node.key);
            if (comparationResult > 0) {
                keyForGreatestMin = node.fValueIndex;
                if (node.right != null) {
                    visit(node.right);
                }
                return;
            }
            if (comparationResult < 0) {
                keyForSmallestMax = node.fValueIndex;
                if (node.left != null) {
                    visit(node.left);
                }
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
    private static class TwoColumnNodeVisitor implements NodeVisitor, Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = 42L;
        private int foundIndex = -1;
        private Comparable key;

        public void start(Node startNode, Comparable key) {
            this.key = key;
            visit(startNode);
        }

        private void visit(Node node) {
            final TwoColumnKey nodeKey = (TwoColumnKey)node.key;
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
    public static class Node implements Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = -3023843176585381905L;
        protected Comparable key;
        protected Node left;
        protected Node right;
        private final int fValueIndex;

        private Node(Comparable key, int valueIndex) {
            this.key = key;
            fValueIndex = valueIndex;
        }

        /**
         * Creates a left child node on this node that keeps the provided value.
         */
        private Node newLeft(Comparable key, int valueIndex) {
            left = new Node(key, valueIndex);
            return left;
        }

        /**
         * Creates a right hand side child node on this node that keeps the provided value.
         */
        private Node newRight(Comparable key, int valueIndex) {
            right = new Node(key, valueIndex);
            return right;
        }
    }

    public static class TwoColumnKey implements Comparable, Serializable {
        /**
         * 
         */
        private static final long serialVersionUID = 42L;
        private final Comparable lowerBound;
        private final Comparable upperBound;

        /**
         * @param lowerBound The lowerBound of this TwoColumnKey.
         * @param upperBound The upperBound of this TwoColumnKey.
         */
        public TwoColumnKey(Comparable lowerBound, Comparable upperBound) {
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

        public int compareTo(Object o) {
            final TwoColumnKey other = (TwoColumnKey)o;
            return lowerBound.compareTo(other.lowerBound);
        }

        @Override
        public boolean equals(Object obj) {
            final TwoColumnKey other = (TwoColumnKey)obj;
            return lowerBound.equals(other.lowerBound) && upperBound.equals(other.upperBound);
        }

        @Override
        public int hashCode() {
            return lowerBound.hashCode() + upperBound.hashCode();
        }

        /**
         * @return Returns the lowerBound.
         */
        public Comparable getLowerBound() {
            return lowerBound;
        }

        /**
         * @return Returns the upperBound.
         */
        public Comparable getUpperBound() {
            return upperBound;
        }
    }
}
