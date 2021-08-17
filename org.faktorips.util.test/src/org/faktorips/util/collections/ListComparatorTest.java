/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.util.collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.junit.Test;

public class ListComparatorTest {

    @Test
    public void testCompare_BothEmpty() throws Exception {
        List<Object> list1 = Arrays.asList();
        List<Object> list2 = Arrays.asList();

        assertThat(compareWithListComparator(list1, list2), is(0));
        assertThat(compareWithListComparator(list2, list1), is(0));
    }

    @Test
    public void testCompare_OneEmpty() throws Exception {
        List<String> list1 = Arrays.asList();
        List<String> list2 = Arrays.asList("abc");

        assertThat(compareWithListComparator(list1, list2), is(-1));
        assertThat(compareWithListComparator(list2, list1), is(1));
    }

    @Test
    public void testCompare_Equal() throws Exception {
        List<String> list1 = Arrays.asList("abc", "xyz");
        List<String> list2 = Arrays.asList("abc", "xyz");

        assertThat(compareWithListComparator(list1, list2), is(0));
        assertThat(compareWithListComparator(list2, list1), is(0));
    }

    @Test
    public void testCompare_EqualInFirstElements() throws Exception {
        List<String> list1 = Arrays.asList("abc", "xyz");
        List<String> list2 = Arrays.asList("abc", "xyz", "");

        assertThat(compareWithListComparator(list1, list2), is(-1));
        assertThat(compareWithListComparator(list2, list1), is(1));
    }

    @Test
    public void testCompare_EqualInLastElements() throws Exception {
        List<String> list1 = Arrays.asList("zzz", "xyz");
        List<String> list2 = Arrays.asList("abc", "xyz", "123");

        assertThat(compareWithListComparator(list1, list2), is(-1));
        assertThat(compareWithListComparator(list2, list1), is(1));
    }

    @Test
    public void testCompare_ContainsNull() throws Exception {
        List<String> list1 = Arrays.asList("abc", null);
        List<String> list2 = Arrays.asList("abc", "xyz", "");

        assertThat(compareWithListComparator(list1, list2), is(-1));
        assertThat(compareWithListComparator(list2, list1), is(1));
    }

    @Test
    public void testCompare_EqualContainsNull() throws Exception {
        List<String> list1 = Arrays.asList((String)null);
        List<String> list2 = Arrays.asList((String)null);

        assertThat(compareWithListComparator(list1, list2), is(0));
        assertThat(compareWithListComparator(list2, list1), is(0));
    }

    @Test
    public void testCompare_EqualInteger() throws Exception {
        List<Integer> list1 = Arrays.asList(13, 2, 3);
        List<Integer> list2 = Arrays.asList(13, 2, 3);

        assertThat(compareWithListComparator(list1, list2), is(0));
        assertThat(compareWithListComparator(list2, list1), is(0));
    }

    @Test
    public void testCompare_IntegerCompare() throws Exception {
        List<Integer> list1 = Arrays.asList(13, 2, 3);
        List<Integer> list2 = Arrays.asList(13, 12, 3);

        assertThat(compareWithListComparator(list1, list2), is(-1));
        assertThat(compareWithListComparator(list2, list1), is(1));
    }

    @Test
    public void testCompare_CustomComparator() throws Exception {
        List<Integer> list1 = Arrays.asList(13, 2, 3);
        List<Integer> list2 = Arrays.asList(13, 12, 3);

        Comparator<Integer> customComparator = Comparator.comparing(i -> i.toString());
        assertThat(ListComparator.listComparator(customComparator).compare(list1, list2), is(1));
        assertThat(ListComparator.listComparator(customComparator).compare(list2, list1), is(-1));
    }

    private static <T> int compareWithListComparator(List<T> l1, List<T> l2) {
        return ListComparator.listComparator(ListComparatorTest.<T> naturalComparator()).compare(l1, l2);
    }

    private static <T> Comparator<T> naturalComparator() {
        return new NaturalComparator<>();
    }

    private static class NaturalComparator<T> implements Comparator<T> {

        @SuppressWarnings({ "unchecked", "null" })
        @Override
        public int compare(T o1, T o2) {
            if (o1 == o2) {
                return 0;
            }
            if (o1 == null && o2 != null) {
                return -1;
            }
            if (o2 == null && o1 != null) {
                return 1;
            }
            return ((Comparable<T>)o1).compareTo(o2);
        }

    }

}
