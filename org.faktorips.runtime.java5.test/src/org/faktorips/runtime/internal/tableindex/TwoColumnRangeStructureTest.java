/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal.tableindex;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.hasItem;

import java.util.GregorianCalendar;

import org.junit.Before;
import org.junit.Test;

public class TwoColumnRangeStructureTest {

    private TwoColumnRangeStructure<Integer, ResultStructure<String>, String> structure;

    @Before
    public void setUp() {
        structure = TwoColumnRangeStructure.create();
        structure.put(0, 8, new ResultStructure<String>("A"));
        structure.put(12, 20, new ResultStructure<String>("B"));
    }

    @Test
    public void testGet_MiddleOfRange() {
        assertEquals(1, structure.get(5).get().size());
        assertThat(structure.get(5).get(), hasItem("A"));
    }

    @Test
    public void testGet_lowerBoundOfRange() {
        assertEquals(1, structure.get(0).get().size());
        assertThat(structure.get(0).get(), hasItem("A"));
    }

    @Test
    public void testGet_upperBoundOfRange() {
        assertEquals(1, structure.get(8).get().size());
        assertThat(structure.get(8).get(), hasItem("A"));
    }

    @Test
    public void testGet_NextValue() {
        assertEquals(1, structure.get(12).get().size());
        assertThat(structure.get(12).get(), hasItem("B"));
    }

    @Test
    public void testGet_Invalid() {
        assertEquals(0, structure.get(10).get().size());
    }

    @Test
    public void testGet_ValueOutOfRange() {
        assertEquals(0, structure.get(100).get().size());
    }

    @Test
    public void testGet_ValueOutOfRangeLowerBound() {
        assertEquals(0, structure.get(-123).get().size());
    }

    @Test()
    public void testGet_Null() {
        assertEquals(0, structure.get(null).get().size());
    }

    /**
     * FIPS-2595. This tests checks the correctness of the comparable generics. GregorianCalendar
     * extends Comparable<Calendar> hence we need <em>K extends Comparable<? super K</em> instead of
     * simply <em>K extends Comparable<K></em>
     */
    public void testInstantiateSuperComparable() {
        TwoColumnRangeStructure<GregorianCalendar, ResultStructure<Object>, Object> structure = TwoColumnRangeStructure
                .create();

        // The assert is not really necessary, the real test is that the statement above compiles.
        assertTrue(structure.get().isEmpty());
    }
}
