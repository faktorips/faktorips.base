/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal.tableindex;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.GregorianCalendar;
import java.util.Map;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

public class TwoColumnRangeStructureTest {

    private TwoColumnRangeStructure<Integer, ResultStructure<String>, String> structure;

    private TwoColumnRangeStructure<Integer, ResultStructure<String>, String> structure2;

    @Before
    public void setUp() {
        structure = TwoColumnRangeStructure.create();
        structure.put(0, 8, new ResultStructure<>("A"));
        structure.put(12, 20, new ResultStructure<>("B"));
        structure.put(120, 200, false, false, new ResultStructure<>("exclusiveExclusive"));
        structure.put(220, 300, false, true, new ResultStructure<>("exclusiveInclusive"));
        structure.put(320, 400, true, false, new ResultStructure<>("inclusiveExclusive"));
        structure2 = TwoColumnRangeStructure.create();
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
    public void testGet_lowerBoundOfRangeExclusive() {
        assertEquals(0, structure.get(119).get().size());
        assertEquals(0, structure.get(120).get().size());
        assertEquals(1, structure.get(121).get().size());
        assertThat(structure.get(121).get(), hasItem("exclusiveExclusive"));

        assertEquals(0, structure.get(220).get().size());
        assertEquals(1, structure.get(221).get().size());
        assertThat(structure.get(221).get(), hasItem("exclusiveInclusive"));

        assertEquals(0, structure.get(319).get().size());
        assertEquals(1, structure.get(320).get().size());
        assertThat(structure.get(320).get(), hasItem("inclusiveExclusive"));
    }

    @Test
    public void testGet_upperBoundOfRange() {
        assertEquals(1, structure.get(8).get().size());
        assertThat(structure.get(8).get(), hasItem("A"));
    }

    @Test
    public void testGet_upperBoundOfRangeExclusive() {
        assertEquals(0, structure.get(200).get().size());
        assertEquals(0, structure.get(201).get().size());
        assertEquals(1, structure.get(199).get().size());
        assertThat(structure.get(199).get(), hasItem("exclusiveExclusive"));

        assertEquals(0, structure.get(301).get().size());
        assertEquals(1, structure.get(300).get().size());
        assertThat(structure.get(300).get(), hasItem("exclusiveInclusive"));

        assertEquals(0, structure.get(401).get().size());
        assertEquals(0, structure.get(400).get().size());
        assertEquals(1, structure.get(399).get().size());
        assertThat(structure.get(399).get(), hasItem("inclusiveExclusive"));
    }

    @Test
    public void testPutOverlappingMap() {
        structure.getMap().clear();
        structure.put(500, 600, new ResultStructure<>("A"));
        // [500 - 600] : A
        assertThat(structure.getMap().keySet(), hasRange(new TwoColumnRange<>(500, 600)));
        assertEquals(1, structure.getMap().size());

        structure.put(400, 550, new ResultStructure<>("B"));
        // [400 - 500[ : B
        // [500 - 550] : A,B
        // ]550 - 600] : A
        assertThat(structure.getMap().keySet(), hasRange(new TwoColumnRange<>(400, 500, true, false)));
        assertThat(structure.getMap().keySet(), hasRange(new TwoColumnRange<>(500, 550, true, true)));
        assertThat(structure.getMap().keySet(), hasRange(new TwoColumnRange<>(550, 600, false, true)));
        assertEquals(3, structure.getMap().size());

        structure.put(550, 700, new ResultStructure<>("C"));
        // [400 - 500[ : B
        // [500 - 550[ : A,B
        // [550 - 550] : A,B,C
        // ]550 - 600] : A,C
        // ]600 - 700] : C
        assertThat(structure.getMap().keySet(), hasRange(new TwoColumnRange<>(400, 500, true, false)));
        assertThat(structure.getMap().keySet(), hasRange(new TwoColumnRange<>(500, 550, true, false)));
        assertThat(structure.getMap().keySet(), hasRange(new TwoColumnRange<>(550, 550, true, true)));
        assertThat(structure.getMap().keySet(), hasRange(new TwoColumnRange<>(550, 600, false, true)));
        assertThat(structure.getMap().keySet(), hasRange(new TwoColumnRange<>(600, 700, false, true)));
        assertEquals(5, structure.getMap().size());

        structure.put(500, 700, new ResultStructure<>("D"));
        // [400 - 500[ : B
        // [500 - 550[ : A,B,D
        // [550 - 550] : A,B,C,D
        // ]550 - 600] : A,C,D
        // ]600 - 700] : C,D
        assertThat(structure.getMap().keySet(), hasRange(new TwoColumnRange<>(400, 500, true, false)));
        assertThat(structure.getMap().keySet(), hasRange(new TwoColumnRange<>(500, 550, true, false)));
        assertThat(structure.getMap().keySet(), hasRange(new TwoColumnRange<>(550, 550, true, true)));
        assertThat(structure.getMap().keySet(), hasRange(new TwoColumnRange<>(550, 600, false, true)));
        assertThat(structure.getMap().keySet(), hasRange(new TwoColumnRange<>(600, 700, false, true)));
        assertEquals(5, structure.getMap().size());

        structure.put(300, 900, new ResultStructure<>("E"));
        // resulting ranges:
        // [300 - 400[ : E
        // [400 - 500[ : B,E
        // [500 - 550[ : A,B,D,E
        // [550 - 550] : A,B,C,D,E
        // ]550 - 600] : A,C,D,E
        // ]600 - 700] : C,D,E
        // ]700 - 900] : E
        assertThat(structure.getMap().keySet(), hasRange(new TwoColumnRange<>(300, 400, true, false)));
        assertThat(structure.getMap().keySet(), hasRange(new TwoColumnRange<>(400, 500, true, false)));
        assertThat(structure.getMap().keySet(), hasRange(new TwoColumnRange<>(500, 550, true, false)));
        assertThat(structure.getMap().keySet(), hasRange(new TwoColumnRange<>(550, 550, true, true)));
        assertThat(structure.getMap().keySet(), hasRange(new TwoColumnRange<>(550, 600, false, true)));
        assertThat(structure.getMap().keySet(), hasRange(new TwoColumnRange<>(600, 700, false, true)));
        assertThat(structure.getMap().keySet(), hasRange(new TwoColumnRange<>(700, 900, false, true)));
        assertEquals(7, structure.getMap().size());

        structure.put(750, 800, new ResultStructure<>("F"));
        // resulting ranges:
        // [300 - 400[ : E
        // [400 - 500[ : B,E
        // [500 - 550[ : A,B,D,E
        // [550 - 550] : A,B,C,D,E
        // ]550 - 600] : A,C,D,E
        // ]600 - 700] : C,D,E
        // ]700 - 750[ : E
        // [750 - 800] : E,F
        // ]800 - 900] : E
        assertThat(structure.getMap().keySet(), hasRange(new TwoColumnRange<>(300, 400, true, false)));
        assertThat(structure.getMap().keySet(), hasRange(new TwoColumnRange<>(400, 500, true, false)));
        assertThat(structure.getMap().keySet(), hasRange(new TwoColumnRange<>(500, 550, true, false)));
        assertThat(structure.getMap().keySet(), hasRange(new TwoColumnRange<>(550, 550, true, true)));
        assertThat(structure.getMap().keySet(), hasRange(new TwoColumnRange<>(550, 600, false, true)));
        assertThat(structure.getMap().keySet(), hasRange(new TwoColumnRange<>(600, 700, false, true)));
        assertThat(structure.getMap().keySet(), hasRange(new TwoColumnRange<>(700, 750, false, false)));
        assertThat(structure.getMap().keySet(), hasRange(new TwoColumnRange<>(750, 800, true, true)));
        assertThat(structure.getMap().keySet(), hasRange(new TwoColumnRange<>(800, 900, false, true)));
        assertEquals(9, structure.getMap().size());

        structure.put(null, null, new ResultStructure<>("X"));
        // resulting ranges:
        // ]null - 300[ : X
        // [300 - 400[ : E,X
        // [400 - 500[ : B,E,X
        // [500 - 550[ : A,B,D,E,X
        // [550 - 550] : A,B,C,D,E,X
        // ]550 - 600] : A,C,D,E,X
        // ]600 - 700] : C,D,E,X
        // ]700 - 750[ : E,X
        // [750 - 800] : E,F,X
        // ]800 - 900] : E,X
        // ]900 - null[ : X
        assertThat(structure.getMap().keySet(), hasRange(new TwoColumnRange<>(null, 300, false, false)));
        assertThat(structure.getMap().keySet(), hasRange(new TwoColumnRange<>(300, 400, true, false)));
        assertThat(structure.getMap().keySet(), hasRange(new TwoColumnRange<>(400, 500, true, false)));
        assertThat(structure.getMap().keySet(), hasRange(new TwoColumnRange<>(500, 550, true, false)));
        assertThat(structure.getMap().keySet(), hasRange(new TwoColumnRange<>(550, 550, true, true)));
        assertThat(structure.getMap().keySet(), hasRange(new TwoColumnRange<>(550, 600, false, true)));
        assertThat(structure.getMap().keySet(), hasRange(new TwoColumnRange<>(600, 700, false, true)));
        assertThat(structure.getMap().keySet(), hasRange(new TwoColumnRange<>(700, 750, false, false)));
        assertThat(structure.getMap().keySet(), hasRange(new TwoColumnRange<>(750, 800, true, true)));
        assertThat(structure.getMap().keySet(), hasRange(new TwoColumnRange<>(800, 900, false, true)));
        assertThat(structure.getMap().keySet(), hasRange(new TwoColumnRange<>(900, null, false, false)));
        assertEquals(11, structure.getMap().size());
    }

    @Test
    public void testPutKeyValue() {
        structure.getMap().clear();
        structure.put(new TwoColumnRange<>(500, 600), new ResultStructure<>("A"));
        // [500 - 600] : A
        assertThat(structure.getMap().keySet(), hasRange(new TwoColumnRange<>(500, 600)));
        assertEquals(1, structure.getMap().size());

        structure.put(new TwoColumnRange<>(400, 550), new ResultStructure<>("B"));
        // [400 - 500[ : B
        // [500 - 550] : A,B
        // ]550 - 600] : A
        assertThat(structure.getMap().keySet(), hasRange(new TwoColumnRange<>(400, 500, true, false)));
        assertThat(structure.getMap().keySet(), hasRange(new TwoColumnRange<>(500, 550, true, true)));
        assertThat(structure.getMap().keySet(), hasRange(new TwoColumnRange<>(550, 600, false, true)));
        assertEquals(3, structure.getMap().size());
    }

    @Test
    public void testPutKKBooleanBooleanV_normal() throws Exception {
        structure.put(1000, 8000, new ResultStructure<>("A"));

        assertEquals(1, structure.get(1000).get().size());
        assertThat(structure.get(1000).get(), hasItem("A"));
        assertEquals(1, structure.get(8000).get().size());
        assertThat(structure.get(8000).get(), hasItem("A"));
    }

    public void initOverlappingMap() {
        structure.getMap().clear();
        structure.put(500, 600, new ResultStructure<>("A"));
        structure.put(400, 550, new ResultStructure<>("B"));
        structure.put(550, 700, new ResultStructure<>("C"));
        structure.put(500, 700, new ResultStructure<>("D"));
        structure.put(300, 900, new ResultStructure<>("E"));
        structure.put(750, 800, new ResultStructure<>("F"));
        // resulting ranges:
        // [300 - 400[ : E
        // [400 - 500[ : B,E
        // [500 - 550[ : A,B,D,E
        // [550 - 550] : A,B,C,D,E
        // ]550 - 600] : A,C,D,E
        // ]600 - 700] : C,D,E
        // ]700 - 750[ : E
        // [750 - 800] : E,F
        // ]800 - 900] : E
    }

    @Test
    public void testGet_overlapping_belowValues() {
        initOverlappingMap();

        assertEquals(0, structure.get(1299).get().size());
    }

    @Test
    public void testGet_overlapping_1() {
        initOverlappingMap();

        assertEquals(1, structure.get(300).get().size());
        assertThat(structure.get(300).get(), hasItem("E"));
    }

    @Test
    public void testGet_overlapping_2() {
        initOverlappingMap();

        assertEquals(2, structure.get(400).get().size());
        assertThat(structure.get(400).get(), hasItem("B"));
        assertThat(structure.get(400).get(), hasItem("E"));
    }

    @Test
    public void testGet_overlapping_3() {
        initOverlappingMap();

        assertEquals(4, structure.get(500).get().size());
        assertThat(structure.get(500).get(), hasItem("A"));
        assertThat(structure.get(500).get(), hasItem("B"));
        assertThat(structure.get(500).get(), hasItem("D"));
        assertThat(structure.get(500).get(), hasItem("E"));
    }

    @Test
    public void testGet_overlapping_4() {
        initOverlappingMap();

        assertEquals(5, structure.get(550).get().size());
        assertThat(structure.get(550).get(), hasItem("A"));
        assertThat(structure.get(550).get(), hasItem("B"));
        assertThat(structure.get(550).get(), hasItem("C"));
        assertThat(structure.get(550).get(), hasItem("D"));
        assertThat(structure.get(550).get(), hasItem("E"));
    }

    @Test
    public void testGet_overlapping_5() {
        initOverlappingMap();

        assertEquals(4, structure.get(600).get().size());
        assertThat(structure.get(600).get(), hasItem("A"));
        assertThat(structure.get(600).get(), hasItem("C"));
        assertThat(structure.get(600).get(), hasItem("D"));
        assertThat(structure.get(600).get(), hasItem("E"));
    }

    @Test
    public void testGet_overlapping_6() {
        initOverlappingMap();

        assertEquals(3, structure.get(700).get().size());
        assertThat(structure.get(700).get(), hasItem("C"));
        assertThat(structure.get(700).get(), hasItem("D"));
        assertThat(structure.get(700).get(), hasItem("E"));
    }

    @Test
    public void testGet_overlapping_7() {
        initOverlappingMap();

        assertEquals(1, structure.get(749).get().size());
        assertThat(structure.get(749).get(), hasItem("E"));
    }

    @Test
    public void testGet_overlapping_8() {
        initOverlappingMap();

        assertEquals(2, structure.get(750).get().size());
        assertThat(structure.get(750).get(), hasItem("E"));
        assertThat(structure.get(750).get(), hasItem("F"));
    }

    @Test
    public void testGet_overlapping_9() {
        initOverlappingMap();

        assertEquals(2, structure.get(800).get().size());
        assertThat(structure.get(800).get(), hasItem("E"));
        assertThat(structure.get(800).get(), hasItem("F"));
    }

    @Test
    public void testGet_overlapping_10() {
        initOverlappingMap();

        assertEquals(1, structure.get(900).get().size());
        assertThat(structure.get(900).get(), hasItem("E"));
    }

    @Test
    public void testGet_overlapping_negativeInf() {
        initOverlappingMap();
        structure.put(null, 500, new ResultStructure<>("X"));

        assertEquals(1, structure.get(0).get().size());
        assertThat(structure.get(0).get(), hasItem("X"));
    }

    @Test
    public void testGet_overlapping_positiveInf() {
        initOverlappingMap();
        structure.put(500, null, new ResultStructure<>("X"));

        assertEquals(1, structure.get(1000).get().size());
        assertThat(structure.get(1000).get(), hasItem("X"));
    }

    @Test
    public void testGet_overlapping_infInf() {
        initOverlappingMap();
        structure.put(null, null, new ResultStructure<>("X"));

        assertEquals(1, structure.get(0).get().size());
        assertThat(structure.get(0).get(), hasItem("X"));
        assertEquals(1, structure.get(1000).get().size());
        assertThat(structure.get(1000).get(), hasItem("X"));
    }

    @Test
    public void testGet_overlapping_overLastValue() {
        initOverlappingMap();

        assertEquals(0, structure.get(1801).get().size());
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
     * extends {@code Comparable<Calendar>} hence we need {@code K extends Comparable<? super
     * K>} instead of simply {@code K extends Comparable<K>}.
     */
    public void testInstantiateSuperComparable() {
        TwoColumnRangeStructure<GregorianCalendar, ResultStructure<Object>, Object> structure = TwoColumnRangeStructure
                .create();

        // The assert is not really necessary, the real test is that the statement above compiles.
        assertTrue(structure.get().isEmpty());
    }

    @Test
    public void testCopy_DeepCopyForMapContent() {
        structure = new TwoColumnRangeStructure<>();
        structure.put(new TwoColumnRange<>(1, 4), new ResultStructure<>("OneToFour"));

        TwoColumnRangeStructure<Integer, ResultStructure<String>, String> copiedStructure = structure.copy();

        assertEquals(copiedStructure.getMap(), structure.getMap());
        assertNotSame(copiedStructure, structure);
        assertEquals(copiedStructure.get(1), structure.get(1));
        assertNotSame(copiedStructure.get(1), structure.get(1));
    }

    @Test
    public void testCopy() {
        structure = new TwoColumnRangeStructure<>();
        TwoColumnRange<Integer> firstInput = new TwoColumnRange<>(1, 4);
        TwoColumnRange<Integer> secondInput = new TwoColumnRange<>(5, 6);
        structure.put(firstInput, new ResultStructure<>("OneToFour"));
        structure.put(secondInput, new ResultStructure<>("FiveToSix"));

        TwoColumnRangeStructure<Integer, ResultStructure<String>, String> copiedStructure = structure.copy();

        assertEquals(copiedStructure.getMap(), structure.getMap());
        assertNotSame(copiedStructure, structure);
    }

    @Test
    public void testMerge() throws Exception {
        structure.getMap().clear();
        structure.put(0, 100, new ResultStructure<>("A"));
        structure2.put(50, 150, new ResultStructure<>("B"));

        structure.merge(structure2);

        Map<TwoColumnRange<Integer>, ResultStructure<String>> resultMap = structure.getMap();
        assertEquals(3, resultMap.size());
        assertThat(resultMap.keySet(), hasRange(new TwoColumnRange<>(0, 50, true, false)));
        assertThat(resultMap.keySet(), hasRange(new TwoColumnRange<>(50, 100)));
    }

    /**
     * hasItem with {@link TwoColumnRange} would only check the lower bound because the equals
     * method in {@link TwoColumnRange} is designed like that. Using
     * {@link #hasRange(TwoColumnRange)} we introduce an own matcher that checks lower and upper
     * bounds.
     */
    public static <K extends Comparable<? super K>> Matcher<Iterable<? super TwoColumnRange<K>>> hasRange(
            TwoColumnRange<K> element) {
        TwoColumnRangeMatcher<K> elementMatcher = new TwoColumnRangeMatcher<>(element);
        return hasItem(elementMatcher);
    }

    private static class TwoColumnRangeMatcher<K extends Comparable<? super K>> extends BaseMatcher<TwoColumnRange<K>> {

        private final TwoColumnRange<K> range;

        public TwoColumnRangeMatcher(TwoColumnRange<K> range) {
            this.range = range;
        }

        @Override
        public void describeTo(Description description) {
            // no description
        }

        @Override
        public boolean matches(Object obj) {
            @SuppressWarnings("unchecked")
            TwoColumnRange<K> item = (TwoColumnRange<K>)obj;
            return range.compareTo(item) == 0 && range.compareToUpperBound(item) == 0;
        }
    }

}
