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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashMap;

import org.junit.Test;

public class BalancedBinaryTreeTest {

    @Test
    public void testBuildTreeWith1Node() {
        BalancedBinaryTree<Integer, Integer> tree = createTreeWidthIntegerValues(1, 1, 1);

        assertNode(1, tree.root);

        assertNull(tree.root.left);
        assertNull(tree.root.left);
    }

    @Test
    public void testBuildTreeWith2Nodes() {
        BalancedBinaryTree<Integer, Integer> tree = createTreeWidthIntegerValues(2, 0, 1);

        assertNode(1, tree.root);
        assertNode(0, tree.root.left);
        assertNull(tree.root.right);
    }

    @Test
    public void testBuildTreeWith3Nodes() {
        BalancedBinaryTree<Integer, Integer> tree = createTreeWidthIntegerValues(3, 1, 1);

        assertNode(2, tree.root);
        assertNode(1, tree.root.left);
        assertNode(3, tree.root.right);

        assertNull(tree.root.left.left);
        assertNull(tree.root.left.right);

        assertNull(tree.root.right.left);
        assertNull(tree.root.right.right);
    }

    @Test
    public void testBuildTreeWith4Nodes() {
        BalancedBinaryTree<Integer, Integer> tree = createTreeWidthIntegerValues(4, 0, 1);

        assertNode(2, tree.root);
        assertNode(1, tree.root.left);
        assertNode(3, tree.root.right);
        assertNode(0, tree.root.left.left);
    }

    @Test
    public void testBuildTreeWidth8Nodes() {
        BalancedBinaryTree<Integer, Integer> tree = createTreeWidthIntegerValues(8, 0, 1);

        assertNode(4, tree.root);
        assertNode(2, tree.root.left);
        assertNode(6, tree.root.right);
        assertNode(1, tree.root.left.left);
        assertNode(3, tree.root.left.right);
        assertNode(5, tree.root.right.left);
        assertNode(7, tree.root.right.right);
        assertNode(0, tree.root.left.left.left);

        // 0
        assertNull(tree.root.left.left.left.left);
        assertNull(tree.root.left.left.left.right);
        // 1
        assertNull(tree.root.left.left.right);
        // 3
        assertNull(tree.root.left.right.left);
        assertNull(tree.root.left.right.right);
        // 5
        assertNull(tree.root.right.left.left);
        assertNull(tree.root.right.left.right);
        // 7
        assertNull(tree.root.right.right.left);
        assertNull(tree.root.right.right.right);
    }

    @Test
    public void testBuildTreeWidth5Nodes() {
        BalancedBinaryTree<Integer, Integer> tree = createTreeWidthIntegerValues(5, 1, 1);

        assertNode(3, tree.root);
        assertNode(2, tree.root.left);
        assertNode(4, tree.root.right);
        assertNode(1, tree.root.left.left);
        assertNode(5, tree.root.right.right);

        // 1
        assertNull(tree.root.left.left.left);
        assertNull(tree.root.left.left.right);

        // 2
        assertNull(tree.root.left.right);
        // 4
        assertNull(tree.root.right.left);

        // 5
        assertNull(tree.root.right.right.left);
        assertNull(tree.root.right.right.right);
    }

    @Test
    public void testBuildTreeWidth7Nodes() {
        BalancedBinaryTree<Integer, Integer> tree = createTreeWidthIntegerValues(7, 1, 1);

        assertNode(4, tree.root);
        assertNode(2, tree.root.left);
        assertNode(6, tree.root.right);
        assertNode(1, tree.root.left.left);
        assertNode(3, tree.root.left.right);
        assertNode(7, tree.root.right.right);
        assertNode(5, tree.root.right.left);

        // 1
        assertNull(tree.root.left.left.left);
        assertNull(tree.root.left.left.right);

        // 3
        assertNull(tree.root.left.right.left);
        assertNull(tree.root.left.right.right);

        // 5
        assertNull(tree.root.right.left.left);
        assertNull(tree.root.right.left.right);

        // 7
        assertNull(tree.root.right.right.left);
        assertNull(tree.root.right.right.right);
    }

    private <K extends Comparable<K>> void assertNode(K expected, BalancedBinaryTree.Node<K> node) {
        assertEquals(expected, node.key);
    }

    public static BalancedBinaryTree<Integer, Integer> createTreeWidthIntegerValues(int nodeCount,
            int startPos,
            int rangeWidth) {

        HashMap<Integer, Integer> map = new HashMap<Integer, Integer>(nodeCount);
        for (int i = 0; i < nodeCount; i++) {
            Integer value = new Integer(startPos + rangeWidth * i);
            map.put(value, value);
        }
        return new BalancedBinaryTree<Integer, Integer>(map);
    }

}
