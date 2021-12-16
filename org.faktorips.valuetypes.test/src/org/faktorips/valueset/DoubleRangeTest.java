/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.valueset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class DoubleRangeTest {

    @Test
    public void testEmpty() {
        DoubleRange range = DoubleRange.empty();

        assertTrue(range.isEmpty());
        assertTrue(range.isDiscrete());
        assertFalse(range.containsNull());
        assertNull(range.getLowerBound());
        assertNull(range.getUpperBound());
        assertNull(range.getStep());
    }

    @Test
    public void testValueOf_Bounds() {
        DoubleRange range = DoubleRange.valueOf(5.0, 10.0);

        assertEquals(range.getLowerBound().doubleValue(), 5.0, 0.0);
        assertEquals(range.getUpperBound().doubleValue(), 10.0, 0.0);
        assertFalse(range.containsNull());
    }

    @Test
    public void testValueOf_Bounds_ContainsNull() {
        DoubleRange range = DoubleRange.valueOf(5.0, 10.0, true);

        assertEquals(range.getLowerBound().doubleValue(), 5.0, 0.0);
        assertEquals(range.getUpperBound().doubleValue(), 10.0, 0.0);
        assertTrue(range.containsNull());
    }

    @Test
    public void testValueOf_Bounds_Step_ContainsNull() {
        DoubleRange range = DoubleRange.valueOf(5.0, 10.0, 1.0, true);

        assertEquals(range.getLowerBound().doubleValue(), 5.0, 0.0);
        assertEquals(range.getUpperBound().doubleValue(), 10.0, 0.0);
        assertEquals(range.getStep().doubleValue(), 1.0, 0.0);
        assertTrue(range.containsNull());
    }

    @Test
    public void testContains() {
        DoubleRange range = DoubleRange.valueOf(10.0, 100.0, 10.0, true);

        assertTrue(range.contains(null));
    }

    @Test
    public void testContains_NoLower() {
        DoubleRange range = DoubleRange.valueOf(null, 100.0, 10.0, false);

        assertTrue(range.contains(30.0));
        assertTrue(range.contains(100.0));
        assertFalse(range.contains(110.0));
        assertFalse(range.contains(35.0));
    }

    @Test
    public void testContains_NoUpper() {
        DoubleRange range = DoubleRange.valueOf(10.0, null, 10.0, false);

        assertTrue(range.contains(30.0));
        assertTrue(range.contains(10.0));
        assertFalse(range.contains(-10.0));
        assertFalse(range.contains(44.0));
    }

    @Test
    public void testGetValues() {
        DoubleRange range = DoubleRange.valueOf(0.0, 100.0, 20.0, false);

        Set<Double> values = range.getValues(false);

        assertEquals(6, range.size());
        assertTrue(values.contains(0.0));
        assertTrue(values.contains(20.0));
        assertTrue(values.contains(40.0));
        assertTrue(values.contains(60.0));
        assertTrue(values.contains(80.0));
        assertTrue(values.contains(100.0));
    }

    @Test
    public void testGetValues_WithNull() {
        DoubleRange range = DoubleRange.valueOf(0.0, 100.0, 20.0, true);

        Set<Double> values = range.getValues(false);

        assertEquals(7, range.size());
        assertTrue(values.contains(0.0));
        assertTrue(values.contains(20.0));
        assertTrue(values.contains(40.0));
        assertTrue(values.contains(60.0));
        assertTrue(values.contains(80.0));
        assertTrue(values.contains(100.0));
        assertTrue(values.contains(null));
    }

    @SuppressWarnings("unlikely-arg-type")
    @Test
    public void testGetValues_DatatypeMismatch() {
        DoubleRange range = DoubleRange.valueOf(0.0, 100.0, 20.0, false);

        Set<Double> values = range.getValues(false);

        assertFalse(values.contains(Integer.valueOf(-10)));
        assertFalse(values.contains(Integer.valueOf(50)));
        assertFalse(values.contains(Integer.valueOf(110)));
        assertFalse(values.contains(Integer.valueOf(120)));
    }

    @Test
    public void testCheckIfStepFitsIntoBounds() throws Exception {
        DoubleRange doubleRange = DoubleRange.valueOf(0.0, 5.0, 0.1, false);

        doubleRange.checkIfStepFitsIntoBounds();
    }

    @Test
    public void testSerializable() throws Exception {
        TestUtil.testSerializable(DoubleRange.valueOf(5.0, 10.0));
    }

}
