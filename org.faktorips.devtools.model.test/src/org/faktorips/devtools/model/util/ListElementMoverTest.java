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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class ListElementMoverTest {
    List<String> content;
    ListElementMover<String> mover;

    @Before
    public void setUp() {
        content = new ArrayList<>();
        content.add("a");
        content.add("b");
        content.add("c");
        content.add("d");
        content.add("e");
        content.add("f");
        mover = new ListElementMover<>(content);
    }

    @Test
    public void testMoveToIndexIntArray_Below() throws Exception {
        mover.moveToIndex(new int[] { 1, 3 }, 4, true);

        assertThat(content.get(0), is("a"));
        assertThat(content.get(1), is("c"));
        assertThat(content.get(2), is("e"));
        assertThat(content.get(3), is("b"));
        assertThat(content.get(4), is("d"));
        assertThat(content.get(5), is("f"));
    }

    @Test
    public void testMoveToIndexIntArray_Above() throws Exception {
        mover.moveToIndex(new int[] { 1, 3 }, 4, false);

        assertThat(content.get(0), is("a"));
        assertThat(content.get(1), is("c"));
        assertThat(content.get(2), is("b"));
        assertThat(content.get(3), is("d"));
        assertThat(content.get(4), is("e"));
        assertThat(content.get(5), is("f"));
    }

}
