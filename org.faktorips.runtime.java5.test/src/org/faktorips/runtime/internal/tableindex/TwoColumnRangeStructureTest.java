/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal.tableindex;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;

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

}
