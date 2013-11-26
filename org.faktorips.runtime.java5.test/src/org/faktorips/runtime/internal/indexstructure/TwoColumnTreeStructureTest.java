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

package org.faktorips.runtime.internal.indexstructure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;

import org.junit.Before;
import org.junit.Test;

public class TwoColumnTreeStructureTest {

    private TwoColumnTreeStructure<Integer, ResultStructure<String>, String> structure;

    @Before
    public void setUp() {
        structure = TwoColumnTreeStructure.create();
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

    @Test(expected = NullPointerException.class)
    public void testGet_Null() {
        structure.get(null);
    }

}
