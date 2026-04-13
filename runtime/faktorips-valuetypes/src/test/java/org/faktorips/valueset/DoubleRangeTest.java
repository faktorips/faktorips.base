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

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThrows;

import java.util.Set;

import org.junit.Test;

public class DoubleRangeTest {

    @Test
    public void testEmpty() {
        DoubleRange range = DoubleRange.empty();

        assertThat(range.isEmpty(), is(true));
        assertThat(range.isDiscrete(), is(true));
        assertThat(range.containsNull(), is(false));
        assertThat(range.getLowerBound(), is(nullValue()));
        assertThat(range.getUpperBound(), is(nullValue()));
        assertThat(range.getStep(), is(nullValue()));
    }

    @Test
    public void testValueOf_Bounds() {
        DoubleRange range = DoubleRange.valueOf(5.0, 10.0);

        assertThat(range.getLowerBound().doubleValue(), is(5.0));
        assertThat(range.getUpperBound().doubleValue(), is(10.0));
        assertThat(range.containsNull(), is(false));
    }

    @Test
    public void testValueOf_Bounds_ContainsNull() {
        DoubleRange range = DoubleRange.valueOf(5.0, 10.0, true);

        assertThat(range.getLowerBound().doubleValue(), is(5.0));
        assertThat(range.getUpperBound().doubleValue(), is(10.0));
        assertThat(range.containsNull(), is(true));
    }

    @Test
    public void testValueOf_Bounds_Step_ContainsNull() {
        DoubleRange range = DoubleRange.valueOf(5.0, 10.0, 1.0, true);

        assertThat(range.getLowerBound().doubleValue(), is(5.0));
        assertThat(range.getUpperBound().doubleValue(), is(10.0));
        assertThat(range.getStep().doubleValue(), is(1.0));
        assertThat(range.containsNull(), is(true));
    }

    @Test
    public void testContains() {
        DoubleRange range = DoubleRange.valueOf(10.0, 100.0, 10.0, true);

        assertThat(range.contains(null), is(true));
    }

    @Test
    public void testContains_NoLower() {
        DoubleRange range = DoubleRange.valueOf(null, 100.0, 10.0, false);

        assertThat(range.contains(30.0), is(true));
        assertThat(range.contains(100.0), is(true));
        assertThat(range.contains(110.0), is(false));
        assertThat(range.contains(35.0), is(false));
    }

    @Test
    public void testContains_NoUpper() {
        DoubleRange range = DoubleRange.valueOf(10.0, null, 10.0, false);

        assertThat(range.contains(30.0), is(true));
        assertThat(range.contains(10.0), is(true));
        assertThat(range.contains(-10.0), is(false));
        assertThat(range.contains(44.0), is(false));
    }

    @Test
    public void testContains_Empty() {
        DoubleRange range = DoubleRange.empty();
        assertThat(range.contains(null), is(false));
        assertThat(range.contains(1.0), is(false));
    }

    @Test
    public void testGetValues() {
        DoubleRange range = DoubleRange.valueOf(0.0, 100.0, 20.0, false);

        Set<Double> values = range.getValues(false);

        assertThat(range.size(), is(6));
        assertThat(values.contains(0.0), is(true));
        assertThat(values.contains(20.0), is(true));
        assertThat(values.contains(40.0), is(true));
        assertThat(values.contains(60.0), is(true));
        assertThat(values.contains(80.0), is(true));
        assertThat(values.contains(100.0), is(true));
    }

    @Test
    public void testGetValues_WithNull() {
        DoubleRange range = DoubleRange.valueOf(0.0, 100.0, 20.0, true);

        Set<Double> values = range.getValues(false);

        assertThat(range.size(), is(7));
        assertThat(values.contains(0.0), is(true));
        assertThat(values.contains(20.0), is(true));
        assertThat(values.contains(40.0), is(true));
        assertThat(values.contains(60.0), is(true));
        assertThat(values.contains(80.0), is(true));
        assertThat(values.contains(100.0), is(true));
        assertThat(values.contains(null), is(true));
    }

    @SuppressWarnings("unlikely-arg-type")
    @Test
    public void testGetValues_DatatypeMismatch() {
        DoubleRange range = DoubleRange.valueOf(0.0, 100.0, 20.0, false);

        Set<Double> values = range.getValues(false);

        assertThat(values.contains(Integer.valueOf(-10)), is(false));
        assertThat(values.contains(Integer.valueOf(50)), is(false));
        assertThat(values.contains(Integer.valueOf(110)), is(false));
        assertThat(values.contains(Integer.valueOf(120)), is(false));
    }

    @Test
    public void testCheckIfStepFitsIntoBounds() throws Exception {
        DoubleRange doubleRange = DoubleRange.valueOf(0.0, 5.0, 0.1, false);

        doubleRange.checkIfStepFitsIntoBounds();
    }

    @Test
    public void testValueOf_WithOpenBounds() {
        DoubleRange range = DoubleRange.valueOf("5.0", "10.0", null, false, true, false);

        assertThat(range.isLowerBoundOpen(), is(true));
        assertThat(range.isUpperBoundOpen(), is(false));
        assertThat(range.contains(5.0), is(false));
        assertThat(range.contains(5.01), is(true));
        assertThat(range.contains(10.0), is(true));
    }

    @Test
    public void testValueOf_WithBothOpenBounds() {
        DoubleRange range = DoubleRange.valueOf("5.0", "10.0", null, false, true, true);

        assertThat(range.contains(5.0), is(false));
        assertThat(range.contains(5.01), is(true));
        assertThat(range.contains(9.99), is(true));
        assertThat(range.contains(10.0), is(false));
    }

    @Test
    public void testGetValues_WithLowerOpenBoundAndStep() {
        DoubleRange range = DoubleRange.valueOf("0.0", "10.0", "2.0", false, true, false);

        Set<Double> values = range.getValues(false);

        assertThat(values.size(), is(5));
        assertThat(values.contains(0.0), is(false));
        assertThat(values.contains(2.0), is(true));
        assertThat(values.contains(10.0), is(true));
    }

    @Test
    public void testGetValues_WithBothOpenBoundsAndStep() {
        DoubleRange range = DoubleRange.valueOf("0.0", "10.0", "2.0", false, true, true);

        Set<Double> values = range.getValues(false);

        assertThat(values.size(), is(4));
        assertThat(values.contains(0.0), is(false));
        assertThat(values.contains(2.0), is(true));
        assertThat(values.contains(8.0), is(true));
        assertThat(values.contains(10.0), is(false));
    }

    @Test
    public void testSize_WithBothOpenBoundsAndStep() {
        DoubleRange range = DoubleRange.valueOf("0.0", "10.0", "2.0", false, true, true);

        assertThat(range.size(), is(4));
    }

    @Test
    public void testSize_WithOpenBoundsAndStepNotFittingClosedBounds() {
        DoubleRange range = DoubleRange.valueOf("0.0", "10.0", "3.0", false, true, false);

        assertThat(range.size(), is(3));
    }

    @Test
    public void testValueOf_StepMismatch() {
        assertThrows(IllegalArgumentException.class,
                () -> DoubleRange.valueOf(0.0, 10.0, 3.0, false));
    }

    @Test
    public void testContains_WithUpperOpenBound() {
        DoubleRange range = DoubleRange.valueOf("5.0", "10.0", null, false, false, true);

        assertThat(range.contains(4.99), is(false));
        assertThat(range.contains(5.0), is(true));
        assertThat(range.contains(9.99), is(true));
        assertThat(range.contains(10.0), is(false));
    }

    @Test
    public void testContains_WithLowerOpenBound() {
        DoubleRange range = DoubleRange.valueOf("5.0", "10.0", null, false, true, false);

        assertThat(range.contains(4.99), is(false));
        assertThat(range.contains(5.0), is(false));
        assertThat(range.contains(5.01), is(true));
        assertThat(range.contains(10.0), is(true));
    }

    @Test
    public void testSize_WithLowerOpenBound() {
        DoubleRange range = DoubleRange.valueOf("0.0", "10.0", "2.0", false, true, false);

        assertThat(range.size(), is(5));
    }

    @Test
    public void testSize_WithUpperOpenBound() {
        DoubleRange range = DoubleRange.valueOf("0.0", "10.0", "2.0", false, false, true);

        assertThat(range.size(), is(5));
    }

    @Test
    public void testSerializable_WithOpenBounds() throws Exception {
        TestUtil.testSerializable(DoubleRange.valueOf("5.0", "10.0", null, false, true, true));
    }

    @Test
    public void testSerializable() throws Exception {
        TestUtil.testSerializable(DoubleRange.valueOf(5.0, 10.0));
    }

}
