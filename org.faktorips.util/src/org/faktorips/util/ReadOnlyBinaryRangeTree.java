package org.faktorips.util;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;


/**
 * A ReadOnlyBinaryRangeTree provides a specific interpretation of the keys within a java.util.Map upon which the tree
 * is created. The keys in the map are expected to implement the java.lang.Comparable interface. This implies an
 * order among the keys in the map. The tree interprets two consecutive keys as the bounds of a range. The tree
 * when created can be configured with 4 different kinds of range types. That means that a key can be considered as the
 * lower bound, lower or equal bound, upper bound, upper or equal bound of a range. The following key in the order of keys
 * is considered the other end of the range while it is not included in it. That means the range is open at least at
 * one side.    
 * When created the tree can be asked to return a value for the range the provided key lies in by means of the 
 * getValue(Comparable key) method. The returned values are the values of the map upon which the tree is created.
 * Request to the getValue() method of this class are thread safe.
 * 
 * @author Peter Erzberger
 */
public class ReadOnlyBinaryRangeTree implements Serializable{

    private static final long serialVersionUID = -5127537049885131034L;
    
    /**
     * Indicates that the keys are meant to be the lower bound of a range.
     */
    public static final int KEY_IS_LOWER_BOUND = 0;
    
    /**
     * Indicates that the keys are meant to be the lower bound of a range including the lower bound.
     */
    public static final int KEY_IS_LOWER_BOUND_EQUAL = 1;
    
    /**
     * Indicates that the keys are meant to be the upper bound of a range.
     */
    public static final int KEY_IS_UPPER_BOUND = 2;

    /**
     * Indicates that the keys are meant to be the upper bound of a range including the upper bound.
     */
    public static final int KEY_IS_UPPER_BOUND_EQUAL = 3;
    
    // the root node of the tree. The variable is protected to be able to test the created tree
    protected Node root;
    
    // the values that are associated with the ranges
    private Object[] values;
    
    // the type that specifies how the result of the visitor has to be interpreted
    private int keyType;
    
    public ReadOnlyBinaryRangeTree(Map map, int keyType){
        ArgumentCheck.notNull(map);
        ArgumentCheck.isTrue(keyType >= 0 || keyType <= 3, "The tree type parameter doesn't one of the expected values.");
        this.keyType = keyType;
        buildTree(map);
    }
    
    private boolean isEven(int value){
        return value % 2 == 0;
    }
    
    private int[] createInitialVisitedArray(int size){
        int[] visited = new int[size];
        for (int i = 0; i < visited.length; i++) {
            visited[i] = 0;
        }
        return visited;
    }
    
    private void buildValuesArray(Comparable[] keys, Map map){
        
        values = new Object[keys.length];
        for (int i = 0; i < keys.length; i++) {
            values[i] = map.get(keys[i]);
        }
    }
    
    private void buildTree(Map map){
        
        if(map.isEmpty()){
            return;
        }
        
        Comparable[] keys = new Comparable[map.size()];
        map.keySet().toArray(keys);
        Arrays.sort(keys);
        buildValuesArray(keys, map);
        
        int middlePos = isEven(keys.length) ? (keys.length - 1)/2 : keys.length/2;
        int[] visited = createInitialVisitedArray(keys.length);
        root = new Node(keys[middlePos], middlePos);
        visited[middlePos] = 1;
        int widthCount = keys.length - (1 + middlePos);
        buildChildNodes(middlePos, widthCount , keys, visited, root);
    }
    
    private void buildChildNodes(int middlePos, int widthCount, Comparable[] keys, int[] visited, Node parent){

        if(parent == null){
            return;
        }
        int leftPos = isEven(widthCount) ? middlePos - (widthCount / 2) : middlePos - (widthCount + 1) / 2;
        int rightPos = isEven(widthCount) ? middlePos + (widthCount / 2) : middlePos + (widthCount + 1) / 2;
        int newWidthCount = isEven(widthCount) ? widthCount / 2 : (widthCount + 1) / 2;
        
        if(rightPos > keys.length - 1){
            rightPos--;
        }
        
        if(leftPos < 0){
            leftPos++;
        }
        
        Node leftNode = null;
        Node rightNode = null;
        
        if(visited[leftPos] == 0){
            leftNode = parent.newLeft(keys[leftPos], leftPos);
            visited[leftPos] = 1;
        }
        
        if(visited[rightPos] == 0){
            rightNode = parent.newRight(keys[rightPos], rightPos);
            visited[rightPos] = 1;    
        }
        buildChildNodes(leftPos, newWidthCount, keys, visited, leftNode);
        buildChildNodes(rightPos, newWidthCount, keys, visited, rightNode);
    }
    
    /**
     * Returns the value that is associated with the range the provided key lies in. If the key is out of range null
     * will be returned. This method is thread safe.
     */
    public Object getValue(Comparable key){
        NodeVisitor visitor = new NodeVisitor(key);
        visitor.start(root);
        switch(keyType){
            case KEY_IS_UPPER_BOUND:
                return visitor.isSmallestMaxAvailable() ? values[visitor.keyForSmallestMax] : null;
            case KEY_IS_UPPER_BOUND_EQUAL:
                if(visitor.isEqualAvailable()){
                    return values[visitor.keyForEqual];
                }
                if(visitor.isSmallestMaxAvailable()){
                    return values[visitor.keyForSmallestMax];
                }
                return null;
            case KEY_IS_LOWER_BOUND:
                return visitor.isGreatestMinAvailable() ? values[visitor.keyForGreatestMin] : null;
            case KEY_IS_LOWER_BOUND_EQUAL:
                if(visitor.isEqualAvailable()){
                    return values[visitor.keyForEqual];
                }
                if(visitor.isGreatestMinAvailable()){
                    return values[visitor.keyForGreatestMin];
                }
                return null;
            default:
                throw new RuntimeException("Encountered unexpected tree type: " + keyType);
        }
    }
    
    /**
     * Visits the tree of nodes starting from the startNode provided to the start(Node) method. On every
     * node it checks if the key associated with this visitor is smaller or greater than the key that is
     * associated with the node. According to the result is saves the index for the value that is associated
     * with the tree node. 
     * 
     * @author Peter Erzberger
     */
    private static class NodeVisitor implements Serializable{
    
        /**
         * 
         */
        private static final long serialVersionUID = 8409704039187989276L;
        private Comparable key;
        private int keyForSmallestMax = -1;
        private int keyForGreatestMin = -1;
        private int keyForEqual = -1;
        
        private NodeVisitor(Comparable key){
            this.key = key;
        }
        
        private void start(Node startNode){
        
            visit(startNode);
        }
        
        private boolean isSmallestMaxAvailable(){
            return keyForSmallestMax != -1;
        }
        
        private boolean isGreatestMinAvailable(){
            return keyForGreatestMin != -1;
        }
        
        private boolean isEqualAvailable(){
            return keyForEqual != -1;
        }
        
        private void visit(Node node){
            int comparationResult = key.compareTo(node.key);
            switch(comparationResult){
                case 1: 
                    keyForGreatestMin = node.fValueIndex;
                    if(node.right != null) visit(node.right);
                    return;
                case -1:
                    keyForSmallestMax = node.fValueIndex;
                	if(node.left != null) visit(node.left);
                	return;
                case 0:
                    keyForEqual = node.fValueIndex;
                    return;
                default:
                    throw new RuntimeException("An unexpected value of: " + comparationResult + " was returned " +
                    		"by the compareTo method of the following type: " + key.getClass());
                    
            }
        }
    }
    
    /**
     * Instances of this class represent a node within the tree. A node can be asked for the value it holds.
     * 
     * @author Peter Erzberger
     */
    public static class Node implements Serializable{
        
        /**
         * 
         */
        private static final long serialVersionUID = -3023843176585381905L;
        protected Comparable key;
        protected Node left;
        protected Node right;
        private int fValueIndex;
        
        private Node(Comparable key, int valueIndex){
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
}
