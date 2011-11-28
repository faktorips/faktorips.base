/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class SubListElementMoverTest {

    @Test
    public void testMove_StandardMoveUp() {
        List<Integer> list = Arrays.asList(1, 2, 3);
        List<Integer> subList = Arrays.asList(1, 2, 3);
        int[] indices = new int[] { 2, 1 };

        SubListElementMover<Integer> mover = new SubListElementMover<Integer>(list, subList);
        assertArrayEquals(new int[] { 1, 0 }, mover.move(indices, true));
        assertEquals(Integer.valueOf(2), list.get(0));
        assertEquals(Integer.valueOf(3), list.get(1));
        assertEquals(Integer.valueOf(1), list.get(2));
    }

    @Test
    public void testMove_StandardMoveDown() {
        List<Integer> list = Arrays.asList(1, 2, 3);
        List<Integer> subList = Arrays.asList(1, 2, 3);
        int[] indices = new int[] { 0, 1 };

        SubListElementMover<Integer> mover = new SubListElementMover<Integer>(list, subList);
        assertArrayEquals(new int[] { 1, 2 }, mover.move(indices, false));
        assertEquals(Integer.valueOf(3), list.get(0));
        assertEquals(Integer.valueOf(1), list.get(1));
        assertEquals(Integer.valueOf(2), list.get(2));
    }

    @Test
    public void testMove_DoNotMoveIfEmptyIndexArrayGiven() {
        List<Integer> list = Arrays.asList(1, 2, 3);
        List<Integer> subList = Arrays.asList(1, 2, 3);
        int[] indices = new int[0];

        SubListElementMover<Integer> mover = new SubListElementMover<Integer>(list, subList);
        assertArrayEquals(new int[0], mover.move(indices, true));
        assertEquals(Integer.valueOf(1), list.get(0));
        assertEquals(Integer.valueOf(2), list.get(1));
        assertEquals(Integer.valueOf(3), list.get(2));
    }

    @Test
    public void testMove_DoNotMoveUpIfFirstSubListElementIsPartOfOperation() {
        List<Integer> list = Arrays.asList(1, 2, 3, 4);
        List<Integer> subList = Arrays.asList(2, 3, 4);
        int[] indices = new int[] { 0, 1 };

        SubListElementMover<Integer> mover = new SubListElementMover<Integer>(list, subList);
        assertArrayEquals(new int[] { 0, 1 }, mover.move(indices, true));
        assertEquals(Integer.valueOf(1), list.get(0));
        assertEquals(Integer.valueOf(2), list.get(1));
        assertEquals(Integer.valueOf(3), list.get(2));
        assertEquals(Integer.valueOf(4), list.get(3));
    }

    @Test
    public void testMove_DoNotMoveDownIfLastSubListElementIsPartOfOperation() {
        List<Integer> list = Arrays.asList(1, 2, 3, 4);
        List<Integer> subList = Arrays.asList(1, 2, 3);
        int[] indices = new int[] { 1, 2 };

        SubListElementMover<Integer> mover = new SubListElementMover<Integer>(list, subList);
        assertArrayEquals(new int[] { 1, 2 }, mover.move(indices, false));
        assertEquals(Integer.valueOf(1), list.get(0));
        assertEquals(Integer.valueOf(2), list.get(1));
        assertEquals(Integer.valueOf(3), list.get(2));
        assertEquals(Integer.valueOf(4), list.get(3));
    }

    /**
     * <strong>Scenario:</strong><br>
     * An element is moved up, but in-between the logically affected elements, an element that does
     * not belong to the sub list is located.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * The move operation should skip the in-between element, so that only elements belonging to the
     * sub list are swapped with each other, and return the new indices within the sub list.
     */
    @Test
    public void testMove_MoveUpElementInDistributedSubList() {
        List<Integer> list = Arrays.asList(1, 2, 3);
        List<Integer> subList = Arrays.asList(1, 3);
        int[] indices = new int[] { 1 };

        SubListElementMover<Integer> mover = new SubListElementMover<Integer>(list, subList);
        assertArrayEquals(new int[] { 0 }, mover.move(indices, true));
        assertEquals(Integer.valueOf(3), list.get(0));
        assertEquals(Integer.valueOf(2), list.get(1));
        assertEquals(Integer.valueOf(1), list.get(2));
    }

    /**
     * <strong>Scenario:</strong><br>
     * An element is moved down, but in-between the logically affected elements, an element that
     * does not belong to the sub list is located.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * The move operation should skip the in-between element, so that only elements belonging to the
     * sub list are swapped with each other, and return the new indices within the sub list.
     */
    @Test
    public void testMove_MoveDownElementInDistributedSubList() {
        List<Integer> list = Arrays.asList(1, 2, 3);
        List<Integer> subList = Arrays.asList(1, 3);
        int[] indices = new int[] { 0 };

        SubListElementMover<Integer> mover = new SubListElementMover<Integer>(list, subList);
        assertArrayEquals(new int[] { 1 }, mover.move(indices, false));
        assertEquals(Integer.valueOf(3), list.get(0));
        assertEquals(Integer.valueOf(2), list.get(1));
        assertEquals(Integer.valueOf(1), list.get(2));
    }

}
