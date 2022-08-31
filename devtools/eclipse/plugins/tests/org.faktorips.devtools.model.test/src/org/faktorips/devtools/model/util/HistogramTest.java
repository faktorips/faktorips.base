/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.util;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.function.Function;

import org.faktorips.devtools.model.internal.util.Histogram;
import org.faktorips.devtools.model.internal.util.Histogram.BestValue;
import org.faktorips.values.Decimal;
import org.junit.Test;

public class HistogramTest {

    private static final class Element {
        private final String value;

        public Element(String value) {
            super();
            this.value = value;
        }
    }

    private static final Function<Element, String> VALUE_FUNCTION = element -> element.value;

    private static final List<Element> EMPTY = Collections.emptyList();

    @Test
    public void testDistribution_NoValues() {
        Histogram<String, Element> histogram = new Histogram<>(VALUE_FUNCTION, EMPTY);
        assertThat(histogram.getDistribution().isEmpty(), is(true));
    }

    @Test
    public void testDistribution_SingleValue() {
        Element a = element("A");
        Histogram<String, Element> histogram = new Histogram<>(VALUE_FUNCTION, a);
        assertThat(histogram.getDistribution().size(), is(1));
        assertThat(histogram.getDistribution().get("A"), hasItems(a));
        assertThat(histogram.getDistribution().get("B").isEmpty(), is(true));
    }

    @Test
    public void testGetDistribution_SameValue() {
        Element a1 = element("A");
        Element a2 = element("A");
        Element a3 = element("A");
        List<Element> elements = List.of(a1, a2, a3);
        Histogram<String, Element> histogram = new Histogram<>(VALUE_FUNCTION, elements);
        assertThat(histogram.getDistribution().size(), is(3));
        assertThat(histogram.getDistribution().get("A"), hasItems(a1, a2, a3));
        assertThat(histogram.getDistribution().get("B").isEmpty(), is(true));
    }

    @Test
    public void testGetDistribution_NullValue() {
        Element null1 = element(null);
        Element null2 = element(null);
        Element a = element("A");
        List<Element> elements = List.of(null1, null2, a);
        Histogram<String, Element> histogram = new Histogram<>(VALUE_FUNCTION, elements);
        assertThat(histogram.getDistribution().size(), is(3));
        assertThat(histogram.getDistribution().get(null), hasItems(null1, null2));
        assertThat(histogram.getDistribution().get("A"), hasItems(a));
    }

    @Test
    public void testGetDistribution() {
        Element a1 = element("A");
        Element a3 = element("A");
        Element a2 = element("A");
        Element b1 = element("B");
        Element b2 = element("B");
        Element c = element("C");
        List<Element> elements = List.of(a1, a2, a3, b1, b2, c);

        Histogram<String, Element> histogram = new Histogram<>(VALUE_FUNCTION, elements);
        assertThat(histogram.getDistribution().size(), is(6));
        assertThat(histogram.getDistribution().get("A"), hasItems(a1, a2, a3));
        assertThat(histogram.getDistribution().get("B"), hasItems(b1, b2));
        assertThat(histogram.getDistribution().get("C"), hasItems(c));

        assertThat(histogram.getDistribution().get("D").isEmpty(), is(true));
    }

    @Test
    public void testGetAbsoluteDistribution_NoValues() {
        Histogram<String, Element> histogram = new Histogram<>(VALUE_FUNCTION, EMPTY);
        assertThat(histogram.getAbsoluteDistribution().isEmpty(), is(true));
    }

    @Test
    public void testGetAbsoluteDistribution_SingleValue() {
        Histogram<String, Element> histogram = new Histogram<>(VALUE_FUNCTION, element("A"));
        assertThat(histogram.getAbsoluteDistribution().size(), is(1));
        assertThat(histogram.getAbsoluteDistribution().get("A"), is(1));
        assertThat(histogram.getAbsoluteDistribution().get("B"), is(nullValue()));
    }

    @Test
    public void testGetAbsoluteDistribution_SameValue() {
        List<Element> elements = List.of(element("A"), element("A"), element("A"));
        Histogram<String, Element> histogram = new Histogram<>(VALUE_FUNCTION, elements);
        assertThat(histogram.getAbsoluteDistribution().size(), is(1));
        assertThat(histogram.getAbsoluteDistribution().get("A"), is(3));
        assertThat(histogram.getAbsoluteDistribution().get("B"), is(nullValue()));
    }

    @Test
    public void testGetAbsoluteDistribution_NullValue() {
        List<Element> elements = List.of(element(null), element(null), element("A"));
        Histogram<String, Element> histogram = new Histogram<>(VALUE_FUNCTION, elements);
        assertThat(histogram.getAbsoluteDistribution().size(), is(2));
        assertThat(histogram.getAbsoluteDistribution().get(null), is(2));
        assertThat(histogram.getAbsoluteDistribution().get("A"), is(1));

        assertThat(histogram.getAbsoluteDistribution().firstKey(), is((String)null));
        assertThat(histogram.getAbsoluteDistribution().lastKey(), is("A"));
    }

    @Test
    public void testGetAbsoluteDistribution_EqualCount() {
        List<Element> elements = List.of(element("A"), element("B"));
        Histogram<String, Element> histogram = new Histogram<>(VALUE_FUNCTION, elements);
        assertThat(histogram.getAbsoluteDistribution().size(), is(2));
        assertThat(histogram.getAbsoluteDistribution().get("A"), is(1));
        assertThat(histogram.getAbsoluteDistribution().get("B"), is(1));
    }

    @Test
    public void testGetAbsoluteDistribution() {
        List<Element> elements = List.of(
                element("A"),
                element("A"),
                element("A"),
                element("B"),
                element("B"),
                element("C"));

        Histogram<String, Element> histogram = new Histogram<>(VALUE_FUNCTION, elements);
        assertThat(histogram.getAbsoluteDistribution().size(), is(3));
        assertThat(histogram.getAbsoluteDistribution().get("A"), is(3));
        assertThat(histogram.getAbsoluteDistribution().get("B"), is(2));
        assertThat(histogram.getAbsoluteDistribution().get("C"), is(1));

        assertThat(histogram.getAbsoluteDistribution().firstKey(), is("A"));
        assertThat(histogram.getAbsoluteDistribution().lastKey(), is("C"));

        assertThat(histogram.getAbsoluteDistribution().get("D"), is(nullValue()));
    }

    @Test
    public void testGetRelativeDistribution_NoValues() {
        Histogram<String, Element> histogram = new Histogram<>(VALUE_FUNCTION, EMPTY);
        assertThat(histogram.getRelativeDistribution().isEmpty(), is(true));
    }

    @Test
    public void testGetRelativeDistribution_SingleValue() {
        Histogram<String, Element> histogram = new Histogram<>(VALUE_FUNCTION, element("A"));
        assertThat(histogram.getRelativeDistribution().size(), is(1));
        assertThat(histogram.getRelativeDistribution().get("A"), is(Decimal.valueOf(1)));
        assertThat(histogram.getRelativeDistribution().get("B"), is(nullValue()));
    }

    @Test
    public void testGetRelativeDistribution_SameValue() {
        List<Element> elements = List.of(element("A"), element("A"), element("A"));
        Histogram<String, Element> histogram = new Histogram<>(VALUE_FUNCTION, elements);

        assertThat(histogram.getRelativeDistribution().size(), is(1));
        assertThat(histogram.getRelativeDistribution().get("A"), is(Decimal.valueOf(1)));
        assertThat(histogram.getRelativeDistribution().get("B"), is(nullValue()));
    }

    @Test
    public void testGetRelativeDistribution_NullValue() {
        List<Element> elements = List.of(element(null), element(null), element("A"));
        Histogram<String, Element> histogram = new Histogram<>(VALUE_FUNCTION, elements);

        assertThat(histogram.getRelativeDistribution().size(), is(2));
        assertThat(histogram.getRelativeDistribution().get(null), is(Decimal.valueOf(67, 2)));
        assertThat(histogram.getRelativeDistribution().get("A"), is(Decimal.valueOf(33, 2)));

        assertThat(histogram.getRelativeDistribution().firstKey(), is((String)null));
        assertThat(histogram.getRelativeDistribution().lastKey(), is("A"));
    }

    @Test
    public void testGetRelativeDistribution_EqualValues() {
        List<Element> elements = List.of(element("A"), element("B"));
        Histogram<String, Element> histogram = new Histogram<>(VALUE_FUNCTION, elements);
        assertThat(histogram.getRelativeDistribution().size(), is(2));
        assertThat(histogram.getRelativeDistribution().get("A"), is(Decimal.valueOf(0.5)));
        assertThat(histogram.getRelativeDistribution().get("B"), is(Decimal.valueOf(0.5)));
    }

    @Test
    public void testGetRelativeDistribution() {
        List<Element> elements = List.of(
                element("A"),
                element("A"),
                element("A"),
                element("B"),
                element("B"),
                element("C"));

        Histogram<String, Element> histogram = new Histogram<>(VALUE_FUNCTION, elements);

        assertThat(histogram.getRelativeDistribution().size(), is(3));
        assertThat(histogram.getRelativeDistribution().get("A"), is(Decimal.valueOf(5, 1)));
        assertThat(histogram.getRelativeDistribution().get("B"), is(Decimal.valueOf(33, 2)));
        assertThat(histogram.getRelativeDistribution().get("C"), is(Decimal.valueOf(17, 2)));

        assertThat(histogram.getRelativeDistribution().firstKey(), is("A"));
        assertThat(histogram.getRelativeDistribution().lastKey(), is("C"));

        assertThat(histogram.getRelativeDistribution().get("D"), is(nullValue()));
    }

    @Test
    public void testHistogram_CustomComparator() {
        Comparator<String> stringLengthComparator = Comparator.comparing(String::length);

        List<Element> elements = List.of(
                element("3"),
                element("2"),
                element("1"),
                element("11"),
                element("22"),
                element("333"));

        Histogram<String, Element> histogram = new Histogram<>(VALUE_FUNCTION, stringLengthComparator,
                elements);
        SortedMap<String, Integer> absoluteDistribution = histogram.getAbsoluteDistribution();
        assertThat(absoluteDistribution.size(), is(3));

        // Values with the same length have to occur in the distribution
        assertThat(absoluteDistribution.get("A"), is(3));
        assertThat(absoluteDistribution.get("3"), is(3));
        assertThat(absoluteDistribution.get("2"), is(3));
        assertThat(absoluteDistribution.get("1"), is(3));

        assertThat(absoluteDistribution.get("AA"), is(2));
        assertThat(absoluteDistribution.get("11"), is(2));
        assertThat(absoluteDistribution.get("22"), is(2));

        assertThat(absoluteDistribution.get("AAA"), is(1));
        assertThat(absoluteDistribution.get("333"), is(1));

        // Values with other lengths must not occur
        assertThat(absoluteDistribution.get(""), is(nullValue()));
        assertThat(absoluteDistribution.get("1234"), is(nullValue()));

    }

    @Test
    public void testGetElements() {
        Element a1 = new Element("A");
        Element a2 = new Element("A");
        Element b = new Element("B");

        List<Element> elements = List.of(a1, a2, b);

        Histogram<String, Element> histogram = new Histogram<>(VALUE_FUNCTION, elements);
        assertThat(histogram.getElements("A").size(), is(2));
        assertThat(histogram.getElements("A"), hasItems(a1, a2));
        assertThat(histogram.getElements("B").size(), is(1));
        assertThat(histogram.getElements("B"), hasItems(b));

        assertThat(histogram.getElements(null).size(), is(0));
        assertThat(histogram.getElements("").size(), is(0));
        assertThat(histogram.getElements("C").size(), is(0));
    }

    @Test
    public void testIsEmpty() {
        assertThat(new Histogram<>(VALUE_FUNCTION, EMPTY).isEmpty(), is(true));
        assertThat(new Histogram<>(VALUE_FUNCTION, element("A")).isEmpty(), is(false));
    }

    @Test
    public void testGetBestValue() {
        Element a1 = new Element("A");
        Element a2 = new Element("A");
        Element b = new Element("B");

        List<Element> elements = List.of(a1, a2, b);
        Histogram<String, Element> histogram = new Histogram<>(VALUE_FUNCTION, elements);

        BestValue<String> bestValue = histogram.getBestValue(Decimal.valueOf(0.1));
        assertThat(bestValue.isPresent(), is(true));
        assertThat(bestValue.getValue(), is("A"));
    }

    @Test
    public void testGetBestValue_missingValue() {
        Element a1 = new Element("A");
        Element a2 = new Element("A");
        Element b = new Element("B");

        List<Element> elements = List.of(a1, a2, b);
        Histogram<String, Element> histogram = new Histogram<>(VALUE_FUNCTION, elements);

        BestValue<String> bestValue = histogram.getBestValue(Decimal.valueOf(0.99));
        assertThat(bestValue.isPresent(), is(false));
    }

    private static Element element(String s) {
        return new Element(s);
    }

    @Test
    public void testBestValueIsPresent() {
        BestValue<?> valueOptional = new BestValue<String>();
        assertFalse(valueOptional.isPresent());
    }

    @Test
    public void testBestValueIsPresent_null() {
        BestValue<?> valueOptional = new BestValue<String>(null, Decimal.ZERO);
        assertTrue(valueOptional.isPresent());
    }

    @Test
    public void testBestValueIsPresent_validValue() {
        BestValue<?> valueOptional = new BestValue<>("test", Decimal.ZERO);
        assertTrue(valueOptional.isPresent());
    }

    @Test
    public void testBestValueGetValue_null() {
        BestValue<?> valueOptional = new BestValue<String>(null, Decimal.ZERO);
        assertNull(valueOptional.getValue());
    }

    @Test
    public void testBestValueGetValue_validValue() {
        BestValue<?> valueOptional = new BestValue<>("test", Decimal.ZERO);
        assertEquals("test", valueOptional.getValue());
    }

}
