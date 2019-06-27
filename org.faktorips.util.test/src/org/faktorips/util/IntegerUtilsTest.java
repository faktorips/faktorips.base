/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.faktorips.util.IntegerUtils;
import org.junit.Test;

public class IntegerUtilsTest {

    @Test
    public void testCompare() {
        assertThat(IntegerUtils.compare(0, 0), is(0));
        assertThat(IntegerUtils.compare(10, 10), is(0));
        assertThat(IntegerUtils.compare(-10, -10), is(0));

        assertThat(IntegerUtils.compare(1, 0), is(1));
        assertThat(IntegerUtils.compare(0, 1), is(-1));
        assertThat(IntegerUtils.compare(10, 11), is(-1));
        assertThat(IntegerUtils.compare(-10, -11), is(1));

    }

}
