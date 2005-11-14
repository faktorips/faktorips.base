package org.faktorips.util;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

/**
 * 
 * @author Peter Erzberger
 */
public class ReadOnlyBinaryRangeTreeTest extends TestCase {

    public void testBuildTreeWith1Node(){
        TestReadOnlyBinaryRangTree tree = TestReadOnlyBinaryRangTree.createTreeWidthIntegerValues(1, 1);
        
        assertNode(1,tree.root);
        
        assertNull(tree.root.left);
        assertNull(tree.root.left);
    }

    public void testBuildTreeWith2Nodes(){
        TestReadOnlyBinaryRangTree tree = TestReadOnlyBinaryRangTree.createTreeWidthIntegerValues(2, 1);
        
        assertNode(1,tree.root);
        assertNode(2,tree.root.right);
//        assertNull(tree.root.left);
//        assertNull(tree.root.left);
    }

    public void testBuildTreeWith3Nodes(){
        TestReadOnlyBinaryRangTree tree = TestReadOnlyBinaryRangTree.createTreeWidthIntegerValues(3, 1);
        
        assertNode(2,tree.root);
        assertNode(1,tree.root.left);
        assertNode(3,tree.root.right);
        
        assertNull(tree.root.left.left);
        assertNull(tree.root.left.right);
        
        assertNull(tree.root.right.left);
        assertNull(tree.root.right.right);
    }
    
    public void testBuildTreeWith4Nodes(){
        TestReadOnlyBinaryRangTree tree = TestReadOnlyBinaryRangTree.createTreeWidthIntegerValues(4, 1);
        
        assertNode(2,tree.root);
        assertNode(1,tree.root.left);
        assertNode(3,tree.root.right);
        assertNode(4,tree.root.right.right);
    }
    
    public void testBuildTreeWidth8Nodes(){
        TestReadOnlyBinaryRangTree tree = TestReadOnlyBinaryRangTree.createTreeWidthIntegerValues(8, 1);
        
        assertNode(4,tree.root);
        assertNode(2,tree.root.left);
        assertNode(6,tree.root.right);
        assertNode(1,tree.root.left.left);
        assertNode(3,tree.root.left.right);
        assertNode(5,tree.root.right.left);
        assertNode(7,tree.root.right.right);
        assertNode(8,tree.root.right.right.right);
        
        //1
        assertNull(tree.root.left.left.left);
        assertNull(tree.root.left.left.right);
        //3
        assertNull(tree.root.left.right.left);
        assertNull(tree.root.left.right.right);
        //5
        assertNull(tree.root.right.left.left);
        assertNull(tree.root.right.left.right);
        //7
        assertNull(tree.root.right.right.left);
        //8
        assertNull(tree.root.right.right.right.left);
        assertNull(tree.root.right.right.right.right);
    }
    
    public void testBuildTreeWidth5Nodes(){
        TestReadOnlyBinaryRangTree tree = TestReadOnlyBinaryRangTree.createTreeWidthIntegerValues(5, 1);
        
        assertNode(3,tree.root);
        assertNode(2,tree.root.left);
        assertNode(4,tree.root.right);
        assertNode(1, tree.root.left.left);
        assertNode(5, tree.root.right.right);
        
        //1
        assertNull(tree.root.left.left.left);
        assertNull(tree.root.left.left.right);
        
        //2
        assertNull(tree.root.left.right);
        //4
        assertNull(tree.root.right.left);
        
        //5
        assertNull(tree.root.right.right.left);
        assertNull(tree.root.right.right.right);
    }
    
    public void testBuildTreeWidth7Nodes(){
        TestReadOnlyBinaryRangTree tree = TestReadOnlyBinaryRangTree.createTreeWidthIntegerValues(7, 1);
        
        assertNode(4,tree.root);
        assertNode(2,tree.root.left);
        assertNode(6,tree.root.right);
        assertNode(1, tree.root.left.left);
        assertNode(3, tree.root.left.right);
        assertNode(7, tree.root.right.right);
        assertNode(5, tree.root.right.left);
        
        //1
        assertNull(tree.root.left.left.left);
        assertNull(tree.root.left.left.right);
        
        //3
        assertNull(tree.root.left.right.left);
        assertNull(tree.root.left.right.right);
        
        //5
        assertNull(tree.root.right.left.left);
        assertNull(tree.root.right.left.right);
        
        //7
        assertNull(tree.root.right.right.left);
        assertNull(tree.root.right.right.right);
    }
    
    public void testLowerBoundEqualGetValue(){
        TestReadOnlyBinaryRangTree tree = 
            TestReadOnlyBinaryRangTree.createTreeWidthIntegerValues(
            10, 10, ReadOnlyBinaryRangeTree.KEY_IS_LOWER_BOUND_EQUAL);
        
        Integer lowerBound = (Integer)tree.getValue(new Integer(5));
        assertNull(lowerBound);

        lowerBound = (Integer)tree.getValue(new Integer(110));
        assertEquals(new Integer(100), lowerBound);

        lowerBound = (Integer)tree.getValue(new Integer(10));
        assertEquals(new Integer(10), lowerBound);

        lowerBound = (Integer)tree.getValue(new Integer(19));
        assertEquals(new Integer(10), lowerBound);
        
        lowerBound = (Integer)tree.getValue(new Integer(100));
        assertEquals(new Integer(100), lowerBound);
        
        lowerBound = (Integer)tree.getValue(new Integer(35));
        assertEquals(new Integer(30), lowerBound);

        lowerBound = (Integer)tree.getValue(new Integer(69));
        assertEquals(new Integer(60), lowerBound);        
    }
    
    public void testLowerBoundGetValue(){
        TestReadOnlyBinaryRangTree tree = 
            TestReadOnlyBinaryRangTree.createTreeWidthIntegerValues(
            10, 10, ReadOnlyBinaryRangeTree.KEY_IS_LOWER_BOUND);
        
        Integer lowerBound = (Integer)tree.getValue(new Integer(10));
        assertNull(lowerBound);
        
        lowerBound = (Integer)tree.getValue(new Integer(11));
        assertEquals(new Integer(10), lowerBound);

    }
    
    public void testUpperBoundEqualGetValue(){
        TestReadOnlyBinaryRangTree tree = 
            TestReadOnlyBinaryRangTree.createTreeWidthIntegerValues(
            10, 10, ReadOnlyBinaryRangeTree.KEY_IS_UPPER_BOUND_EQUAL);
        
        Integer upperBound = (Integer)tree.getValue(new Integer(5));
        assertEquals(new Integer(10), upperBound);
        
        upperBound = (Integer)tree.getValue(new Integer(110));
        assertNull(upperBound);
        
        upperBound = (Integer)tree.getValue(new Integer(100));
        assertEquals(new Integer(100), upperBound);

        upperBound = (Integer)tree.getValue(new Integer(10));
        assertEquals(new Integer(10), upperBound);

        upperBound = (Integer)tree.getValue(new Integer(26));
        assertEquals(new Integer(30), upperBound);

        upperBound = (Integer)tree.getValue(new Integer(76));
        assertEquals(new Integer(80), upperBound);

        upperBound = (Integer)tree.getValue(new Integer(30));
        assertEquals(new Integer(30), upperBound);

        upperBound = (Integer)tree.getValue(new Integer(80));
        assertEquals(new Integer(80), upperBound);
    }
    
    public void testUpperBoundGetValue(){
        TestReadOnlyBinaryRangTree tree = 
            TestReadOnlyBinaryRangTree.createTreeWidthIntegerValues(
            10, 10, ReadOnlyBinaryRangeTree.KEY_IS_UPPER_BOUND);
        
        Integer upperBound = (Integer)tree.getValue(new Integer(10));
        assertEquals(new Integer(20), upperBound);
        
        upperBound = (Integer)tree.getValue(new Integer(9));
        assertEquals(new Integer(10), upperBound);
    }
    
    private void assertNode(int expected, ReadOnlyBinaryRangeTree.Node node){
        assertEquals(expected, ((Integer)node.key).intValue());
    }
    
    private static class TestReadOnlyBinaryRangTree extends ReadOnlyBinaryRangeTree{

        /**
         * @param values
         */
        public TestReadOnlyBinaryRangTree(Map map, int treeType) {
            super(map, treeType);
        }
        
        private static TestReadOnlyBinaryRangTree createTreeWidthIntegerValues(int nodeCount, int interval){
            return createTreeWidthIntegerValues(nodeCount, interval, ReadOnlyBinaryRangeTree.KEY_IS_LOWER_BOUND_EQUAL);
        }
        
        private static TestReadOnlyBinaryRangTree createTreeWidthIntegerValues(int nodeCount, int interval, int treeType){
            HashMap map = new HashMap(nodeCount);
            for (int i = 0; i < nodeCount; i++) {
                Integer value = new Integer(interval * (i + 1));
                map.put(value, value);
            }
            return new TestReadOnlyBinaryRangTree(map, treeType);
        }
    }
}
