/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItems;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

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

    private static final Function<Element, String> VALUE_FUNCTION = new Function<Element, String>() {
        @Override
        public String apply(Element s) {
            return s.value;
        }
    };

    private static final List<Element> EMPTY = Collections.emptyList();

    @Test
    public void testGetAbsoluteDistribution_NoValues() {
        Histogram<String, Element> histogram = new Histogram<String, Element>(VALUE_FUNCTION, EMPTY);
        assertThat(histogram.getAbsoluteDistribution().isEmpty(), is(true));
    }

    @Test
    public void testGetAbsoluteDistribution_SingleValue() {
        Histogram<String, Element> histogram = new Histogram<String, Element>(VALUE_FUNCTION, element("A"));
        assertThat(histogram.getAbsoluteDistribution().size(), is(1));
        assertThat(histogram.getAbsoluteDistribution().get("A"), is(1));
        assertThat(histogram.getAbsoluteDistribution().get("B"), is(nullValue()));
    }

    @Test
    public void testGetAbsoluteDistribution_SameValue() {
        List<Element> elements = Lists.newArrayList(element("A"), element("A"), element("A"));
        Histogram<String, Element> histogram = new Histogram<String, Element>(VALUE_FUNCTION, elements);
        assertThat(histogram.getAbsoluteDistribution().size(), is(1));
        assertThat(histogram.getAbsoluteDistribution().get("A"), is(3));
        assertThat(histogram.getAbsoluteDistribution().get("B"), is(nullValue()));
    }

    @Test
    public void testGetAbsoluteDistribution_EqualCount() {
        Histogram<String, Element> histogram = new Histogram<String, Element>(VALUE_FUNCTION, element("A"),
                element("B"));
        assertThat(histogram.getAbsoluteDistribution().size(), is(2));
        assertThat(histogram.getAbsoluteDistribution().get("A"), is(1));
        assertThat(histogram.getAbsoluteDistribution().get("B"), is(1));
    }

    @Test
    public void testGetAbsoluteDistribution() {
        List<Element> elements = Lists.newArrayList();
        elements.add(element("A"));
        elements.add(element("A"));
        elements.add(element("A"));
        elements.add(element("B"));
        elements.add(element("B"));
        elements.add(element("C"));

        Histogram<String, Element> histogram = new Histogram<String, Element>(VALUE_FUNCTION, elements);
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
        Histogram<String, Element> histogram = new Histogram<String, Element>(VALUE_FUNCTION, EMPTY);
        assertThat(histogram.getRelativeDistribution().isEmpty(), is(true));
    }

    @Test
    public void testGetRelativeDistribution_SingleValue() {
        Histogram<String, Element> histogram = new Histogram<String, Element>(VALUE_FUNCTION, element("A"));
        assertThat(histogram.getRelativeDistribution().size(), is(1));
        assertThat(histogram.getRelativeDistribution().get("A"), is(Decimal.valueOf(1)));
        assertThat(histogram.getRelativeDistribution().get("B"), is(nullValue()));
    }

    @Test
    public void testGetRelativeDistribution_SameValue() {
        List<Element> elements = Lists.newArrayList(element("A"), element("A"), element("A"));
        Histogram<String, Element> histogram = new Histogram<String, Element>(VALUE_FUNCTION, elements);

        assertThat(histogram.getRelativeDistribution().size(), is(1));
        assertThat(histogram.getRelativeDistribution().get("A"), is(Decimal.valueOf(1)));
        assertThat(histogram.getRelativeDistribution().get("B"), is(nullValue()));
    }

    @Test
    public void testGetRelativeDistribution_EqualValues() {
        Histogram<String, Element> histogram = new Histogram<String, Element>(VALUE_FUNCTION, element("A"),
                element("B"));
        assertThat(histogram.getRelativeDistribution().size(), is(2));
        assertThat(histogram.getRelativeDistribution().get("A"), is(Decimal.valueOf(0.5)));
        assertThat(histogram.getRelativeDistribution().get("B"), is(Decimal.valueOf(0.5)));
    }

    @Test
    public void testGetRelativeDistribution() {
        List<Element> elements = Lists.newArrayList();
        elements.add(element("A"));
        elements.add(element("A"));
        elements.add(element("A"));
        elements.add(element("B"));
        elements.add(element("B"));
        elements.add(element("C"));

        Histogram<String, Element> histogram = new Histogram<String, Element>(VALUE_FUNCTION, elements);

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
        Comparator<String> stringLengthComparator = new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return IntegerUtils.compare(o1.length(), o2.length());
            }
        };

        List<Element> elements = Lists.newArrayList();
        elements.add(element("3"));
        elements.add(element("2"));
        elements.add(element("1"));
        elements.add(element("11"));
        elements.add(element("22"));
        elements.add(element("333"));

        Histogram<String, Element> histogram = new Histogram<String, Element>(VALUE_FUNCTION, stringLengthComparator,
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

        List<Element> elements = Lists.newArrayList(a1, a2, b);

        Histogram<String, Element> histogram = new Histogram<String, Element>(VALUE_FUNCTION, elements);
        assertThat(histogram.getElements("A").size(), is(2));
        assertThat(histogram.getElements("A"), hasItems(a1, a2));
        assertThat(histogram.getElements("B").size(), is(1));
        assertThat(histogram.getElements("B"), hasItems(b));

        assertThat(histogram.getElements(null).size(), is(0));
        assertThat(histogram.getElements("").size(), is(0));
        assertThat(histogram.getElements("C").size(), is(0));
    }

    @Test
    public void testIsEmtpy() {
        assertThat(new Histogram<String, Element>(VALUE_FUNCTION, EMPTY).isEmtpy(), is(true));
        assertThat(new Histogram<String, Element>(VALUE_FUNCTION, element("A")).isEmtpy(), is(false));

    }

    private static Element element(String s) {
        return new Element(s);
    }

}
