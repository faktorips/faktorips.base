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

package org.faktorips.runtime.internal.tableindex;

import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.junit.matchers.JUnitMatchers.hasItems;
import static org.mockito.Matchers.anyString;

import org.hamcrest.Matcher;
import org.junit.Test;

public class RangeStructureTest {
    private RangeStructure<Integer, ResultStructure<String>, String> structure;

    @Test(expected = NullPointerException.class)
    public void testGet_null() {
        structure.get(null);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructor() {
        createStructure(null);
    }

    @Test
    public void testGet_KeyTypeLowerBoundEqual() {
        createStructure(RangeType.LOWER_BOUND_EQUAL);
        setForKeyWayOutOfLowerBound(isEmpty());
        setForKeyLessThanSmallestLowerBound(isEmpty());
        setForKeyExactSmallestLowerBound(hasItem("A"));
        setForKeyGreaterThanSmallestLowerBound(hasItem("A"));
        setForKeyInRange1(hasItem("A"));
        setForKeyInRange2(hasItem("B"));
        setForKeyLessThanGreatestUpperBound(hasItem("B"));
        setForKeyExactGreatestUpperBound(hasItem("C"));
        setForKeyGreaterThanGreatestUpperBound(hasItem("C"));
        setForKeyWayOutOfUpperBound(hasItem("C"));
    }

    @SuppressWarnings("deprecation")
    /**
     * Tests the {@link RangeStructure} with {@link RangeType#LOWER_BOUND}. May be deleted when
     * {@link RangeType#LOWER_BOUND} is removed.
     */
    @Test
    public void testGet_KeyTypeLowerBound() {
        createStructure(RangeType.LOWER_BOUND);
        setForKeyWayOutOfLowerBound(isEmpty());
        setForKeyLessThanSmallestLowerBound(isEmpty());
        setForKeyExactSmallestLowerBound(isEmpty());
        setForKeyGreaterThanSmallestLowerBound(hasItem("A"));
        setForKeyInRange1(hasItem("A"));
        setForKeyInRange2(hasItem("B"));
        setForKeyLessThanGreatestUpperBound(hasItem("B"));
        setForKeyExactGreatestUpperBound(hasItem("B"));
        setForKeyGreaterThanGreatestUpperBound(hasItem("C"));
        setForKeyWayOutOfUpperBound(hasItem("C"));
    }

    @Test
    public void testGet_KeyTypeUpperBoundEqual() {
        createStructure(RangeType.UPPER_BOUND_EQUAL);
        setForKeyWayOutOfLowerBound(hasItem("A"));
        setForKeyLessThanSmallestLowerBound(hasItem("A"));
        setForKeyExactSmallestLowerBound(hasItem("A"));
        setForKeyGreaterThanSmallestLowerBound(hasItem("B"));
        setForKeyInRange1(hasItem("B"));
        setForKeyInRange2(hasItem("C"));
        setForKeyLessThanGreatestUpperBound(hasItem("C"));
        setForKeyExactGreatestUpperBound(hasItem("C"));
        setForKeyGreaterThanGreatestUpperBound(isEmpty());
        setForKeyWayOutOfUpperBound(isEmpty());
    }

    @SuppressWarnings("deprecation")
    /**
     * Tests the {@link RangeStructure} with {@link RangeType#UPPER_BOUND}. May be deleted when
     * {@link RangeType#UPPER_BOUND} is removed.
     */
    @Test
    public void testGet_KeyTypeUpperBound() {
        createStructure(RangeType.UPPER_BOUND);
        setForKeyWayOutOfLowerBound(hasItem("A"));
        setForKeyLessThanSmallestLowerBound(hasItem("A"));
        setForKeyExactSmallestLowerBound(hasItem("B"));
        setForKeyGreaterThanSmallestLowerBound(hasItem("B"));
        setForKeyInRange1(hasItem("B"));
        setForKeyInRange2(hasItem("C"));
        setForKeyLessThanGreatestUpperBound(hasItem("C"));
        setForKeyExactGreatestUpperBound(isEmpty());
        setForKeyGreaterThanGreatestUpperBound(isEmpty());
        setForKeyWayOutOfUpperBound(isEmpty());
    }

    private void createStructure(RangeType keyType) {
        structure = RangeStructure.create(keyType);
        structure.put(-5, new ResultStructure<String>("A"));
        structure.put(2, new ResultStructure<String>("B"));
        structure.put(10, new ResultStructure<String>("C"));
    }

    public void setForKeyWayOutOfLowerBound(Matcher<Iterable<String>> matcher) {
        assertThat(structure.get(-100).get(), matcher);
    }

    public void setForKeyLessThanSmallestLowerBound(Matcher<Iterable<String>> matcher) {
        assertThat(structure.get(-6).get(), matcher);
    }

    public void setForKeyExactSmallestLowerBound(Matcher<Iterable<String>> matcher) {
        assertThat(structure.get(-5).get(), matcher);
    }

    public void setForKeyGreaterThanSmallestLowerBound(Matcher<Iterable<String>> matcher) {
        assertThat(structure.get(-4).get(), matcher);
    }

    public void setForKeyInRange1(Matcher<Iterable<String>> matcher) {
        assertThat(structure.get(0).get(), matcher);
    }

    public void setForKeyInRange2(Matcher<Iterable<String>> matcher) {
        assertThat(structure.get(5).get(), matcher);
    }

    public void setForKeyLessThanGreatestUpperBound(Matcher<Iterable<String>> matcher) {
        assertThat(structure.get(9).get(), matcher);
    }

    public void setForKeyExactGreatestUpperBound(Matcher<Iterable<String>> matcher) {
        assertThat(structure.get(10).get(), matcher);
    }

    public void setForKeyGreaterThanGreatestUpperBound(Matcher<Iterable<String>> matcher) {
        assertThat(structure.get(11).get(), matcher);
    }

    public void setForKeyWayOutOfUpperBound(Matcher<Iterable<String>> matcher) {
        assertThat(structure.get(100).get(), matcher);
    }

    private Matcher<Iterable<String>> isEmpty() {
        return not(hasItems(anyString()));
    }
}
