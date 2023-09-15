/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.junit.Test;

public class StringBuilderJoinerTest {

    @Test
    public void testJoin_Array_Empty() throws Exception {
        StringBuilder sb = new StringBuilder("start ");
        StringBuilderJoiner.join(sb, new Object[] {});
        sb.append(" end");

        assertThat(sb.toString(), is("start  end"));
    }

    @Test
    public void testJoin_Array_Single() throws Exception {
        StringBuilder sb = new StringBuilder("start ");
        StringBuilderJoiner.join(sb, new Object[] { "foo" });
        sb.append(" end");

        assertThat(sb.toString(), is("start foo end"));
    }

    @Test
    public void testJoin_Array_Multi() throws Exception {
        StringBuilder sb = new StringBuilder("start ");
        StringBuilderJoiner.join(sb, new Object[] { "foo", "bar" });
        sb.append(" end");

        assertThat(sb.toString(), is("start foo, bar end"));
    }

    @Test
    public void testJoin_Array_Null() throws Exception {
        StringBuilder sb = new StringBuilder("start ");
        StringBuilderJoiner.join(sb, new Object[] { "foo", null, "bar" });
        sb.append(" end");

        assertThat(sb.toString(), is("start foo, null, bar end"));
    }

    @Test
    public void testJoin_Array_Consumer_Empty() throws Exception {
        StringBuilder sb = new StringBuilder("start ");
        StringBuilderJoiner.join(sb, new Object[] {}, o -> sb.append(Objects.toString(o).toUpperCase()));
        sb.append(" end");

        assertThat(sb.toString(), is("start  end"));
    }

    @Test
    public void testJoin_Array_Consumer_Single() throws Exception {
        StringBuilder sb = new StringBuilder("start ");
        StringBuilderJoiner.join(sb, new Object[] { "foo" }, o -> sb.append(Objects.toString(o).toUpperCase()));
        sb.append(" end");

        assertThat(sb.toString(), is("start FOO end"));
    }

    @Test
    public void testJoin_Array_Consumer_Multi() throws Exception {
        StringBuilder sb = new StringBuilder("start ");
        StringBuilderJoiner.join(sb, new Object[] { "foo", "bar" }, o -> sb.append(Objects.toString(o).toUpperCase()));
        sb.append(" end");

        assertThat(sb.toString(), is("start FOO, BAR end"));
    }

    @Test
    public void testJoin_Array_Consumer_Null() throws Exception {
        StringBuilder sb = new StringBuilder("start ");
        StringBuilderJoiner.join(sb, new Object[] { "foo", null, "bar" },
                o -> sb.append(Objects.toString(o).toUpperCase()));
        sb.append(" end");

        assertThat(sb.toString(), is("start FOO, NULL, BAR end"));
    }

    @Test
    public void testJoin_Iterable_Empty() throws Exception {
        StringBuilder sb = new StringBuilder("start ");
        StringBuilderJoiner.join(sb, List.of());
        sb.append(" end");

        assertThat(sb.toString(), is("start  end"));
    }

    @Test
    public void testJoin_Iterable_Single() throws Exception {
        StringBuilder sb = new StringBuilder("start ");
        StringBuilderJoiner.join(sb, List.of("foo"));
        sb.append(" end");

        assertThat(sb.toString(), is("start foo end"));
    }

    @Test
    public void testJoin_Iterable_Multi() throws Exception {
        StringBuilder sb = new StringBuilder("start ");
        StringBuilderJoiner.join(sb, List.of("foo", "bar"));
        sb.append(" end");

        assertThat(sb.toString(), is("start foo, bar end"));
    }

    @Test
    public void testJoin_Iterable_Null() throws Exception {
        StringBuilder sb = new StringBuilder("start ");
        StringBuilderJoiner.join(sb, Arrays.asList("foo", null, "bar"));
        sb.append(" end");

        assertThat(sb.toString(), is("start foo, null, bar end"));
    }

    @Test
    public void testJoin_Iterable_Consumer_Empty() throws Exception {
        StringBuilder sb = new StringBuilder("start ");
        StringBuilderJoiner.join(sb, List.of(), o -> sb.append(Objects.toString(o).toUpperCase()));
        sb.append(" end");

        assertThat(sb.toString(), is("start  end"));
    }

    @Test
    public void testJoin_Iterable_Consumer_Single() throws Exception {
        StringBuilder sb = new StringBuilder("start ");
        StringBuilderJoiner.join(sb, List.of("foo"), o -> sb.append(Objects.toString(o).toUpperCase()));
        sb.append(" end");

        assertThat(sb.toString(), is("start FOO end"));
    }

    @Test
    public void testJoin_Iterable_Consumer_Multi() throws Exception {
        StringBuilder sb = new StringBuilder("start ");
        StringBuilderJoiner.join(sb, List.of("foo", "bar"), o -> sb.append(Objects.toString(o).toUpperCase()));
        sb.append(" end");

        assertThat(sb.toString(), is("start FOO, BAR end"));
    }

    @Test
    public void testJoin_Iterable_Consumer_Null() throws Exception {
        StringBuilder sb = new StringBuilder("start ");
        StringBuilderJoiner.join(sb, Arrays.asList("foo", null, "bar"),
                o -> sb.append(Objects.toString(o).toUpperCase()));
        sb.append(" end");

        assertThat(sb.toString(), is("start FOO, NULL, BAR end"));
    }

    @Test
    public void testJoin_Iterable_Separator_Consumer_Empty() throws Exception {
        StringBuilder sb = new StringBuilder("start ");
        StringBuilderJoiner.join(sb, List.of(), "|", o -> sb.append(Objects.toString(o).toUpperCase()));
        sb.append(" end");

        assertThat(sb.toString(), is("start  end"));
    }

    @Test
    public void testJoin_Iterable_Separator_Consumer_Single() throws Exception {
        StringBuilder sb = new StringBuilder("start ");
        StringBuilderJoiner.join(sb, List.of("foo"), "|", o -> sb.append(Objects.toString(o).toUpperCase()));
        sb.append(" end");

        assertThat(sb.toString(), is("start FOO end"));
    }

    @Test
    public void testJoin_Iterable_Separator_Consumer_Multi() throws Exception {
        StringBuilder sb = new StringBuilder("start ");
        StringBuilderJoiner.join(sb, List.of("foo", "bar"), "|",
                o -> sb.append(Objects.toString(o).toUpperCase()));
        sb.append(" end");

        assertThat(sb.toString(), is("start FOO|BAR end"));
    }

    @Test
    public void testJoin_Iterable_NullSeparator_Consumer_Multi() throws Exception {
        StringBuilder sb = new StringBuilder("start ");
        StringBuilderJoiner.join(sb, List.of("foo", "bar"), null,
                o -> sb.append(Objects.toString(o).toUpperCase()));
        sb.append(" end");

        assertThat(sb.toString(), is("start FOOBAR end"));
    }

    @Test
    public void testJoin_Iterable_Separator_Consumer_Null() throws Exception {
        StringBuilder sb = new StringBuilder("start ");
        StringBuilderJoiner.join(sb, Arrays.asList("foo", null, "bar"), "|",
                o -> sb.append(Objects.toString(o).toUpperCase()));
        sb.append(" end");

        assertThat(sb.toString(), is("start FOO|NULL|BAR end"));
    }

    @Test
    public void testJoin_Iterable_Separator_Empty() throws Exception {
        StringBuilder sb = new StringBuilder("start ");
        StringBuilderJoiner.join(sb, List.of(), "|");
        sb.append(" end");

        assertThat(sb.toString(), is("start  end"));
    }

    @Test
    public void testJoin_Iterable_Separator_Single() throws Exception {
        StringBuilder sb = new StringBuilder("start ");
        StringBuilderJoiner.join(sb, List.of("foo"), "|");
        sb.append(" end");

        assertThat(sb.toString(), is("start foo end"));
    }

    @Test
    public void testJoin_Iterable_Separator_Multi() throws Exception {
        StringBuilder sb = new StringBuilder("start ");
        StringBuilderJoiner.join(sb, List.of("foo", "bar"), "|");
        sb.append(" end");

        assertThat(sb.toString(), is("start foo|bar end"));
    }

    @Test
    public void testJoin_Iterable_NullSeparator_Multi() throws Exception {
        StringBuilder sb = new StringBuilder("start ");
        StringBuilderJoiner.join(sb, List.of("foo", "bar"), (String)null);
        sb.append(" end");

        assertThat(sb.toString(), is("start foobar end"));
    }

    @Test
    public void testJoin_Iterable_Separator_Null() throws Exception {
        StringBuilder sb = new StringBuilder("start ");
        StringBuilderJoiner.join(sb, Arrays.asList("foo", null, "bar"), "|");
        sb.append(" end");

        assertThat(sb.toString(), is("start foo|null|bar end"));
    }
}
