/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class IntegerUtilsTest {

    @Test
    public void testCompare() {
        assertThat(Integer.compare(0, 0), is(0));
        assertThat(Integer.compare(10, 10), is(0));
        assertThat(Integer.compare(-10, -10), is(0));

        assertThat(Integer.compare(1, 0), is(1));
        assertThat(Integer.compare(0, 1), is(-1));
        assertThat(Integer.compare(10, 11), is(-1));
        assertThat(Integer.compare(-10, -11), is(1));
    }

}
