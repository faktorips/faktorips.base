/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.values;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class DecimalsTest {

    @Test
    public void testIsNull() {
        assertTrue(Decimals.isNull(null));
        assertTrue(Decimals.isNull(Decimal.NULL));
        assertFalse(Decimals.isNull(Decimal.ZERO));
    }

    @Test
    public void testLessThanIncludingNull() {
        assertFalse(Decimals.lessThanIncludingNull(null, null));
        assertFalse(Decimals.lessThanIncludingNull(null, Decimal.NULL));
        assertFalse(Decimals.lessThanIncludingNull(Decimal.NULL, null));
        assertFalse(Decimals.lessThanIncludingNull(Decimal.NULL, Decimal.NULL));
        assertFalse(Decimals.lessThanIncludingNull(Decimal.NULL, Decimal.ZERO));

        assertTrue(Decimals.lessThanIncludingNull(Decimal.ZERO, Decimal.NULL));
        assertTrue(Decimals.lessThanIncludingNull(Decimal.valueOf(-1), Decimal.ZERO));
        assertTrue(Decimals.lessThanIncludingNull(Decimal.ZERO, Decimal.valueOf(1)));

        assertFalse(Decimals.lessThanIncludingNull(Decimal.ZERO, Decimal.ZERO));
        assertFalse(Decimals.lessThanIncludingNull(Decimal.valueOf(1), Decimal.ZERO));

    }

    @Test
    public void testOrNull() {
        assertThat(Decimals.orNull(null), is(equalTo(Decimal.NULL)));
        assertThat(Decimals.orNull(Decimal.NULL), is(equalTo(Decimal.NULL)));
        assertThat(Decimals.orNull(Decimal.ZERO), is(equalTo(Decimal.ZERO)));
    }

    @Test
    public void testIsAnyNull() {
        assertThat(Decimals.isAnyNull(), is(equalTo(false)));
        assertThat(Decimals.isAnyNull((Decimal)null), is(equalTo(true)));
        assertThat(Decimals.isAnyNull(Decimal.NULL), is(equalTo(true)));
        assertThat(Decimals.isAnyNull(null, Decimal.ZERO), is(equalTo(true)));
        assertThat(Decimals.isAnyNull(Decimal.ZERO, Decimal.NULL), is(equalTo(true)));
        assertThat(Decimals.isAnyNull(Decimal.ZERO, Decimal.valueOf(500), null), is(equalTo(true)));
        assertThat(Decimals.isAnyNull(Decimal.ZERO), is(equalTo(false)));
        assertThat(Decimals.isAnyNull(Decimal.ZERO, Decimal.valueOf(500)), is(equalTo(false)));
    }

    @Test
    public void testMinIgnoreNull() {
        assertThat(Decimals.minIgnoreNull(), is(equalTo(Decimal.NULL)));
        assertThat(Decimals.minIgnoreNull((Decimal)null), is(equalTo(Decimal.NULL)));
        assertThat(Decimals.minIgnoreNull(Decimal.NULL), is(equalTo(Decimal.NULL)));
        assertThat(Decimals.minIgnoreNull(Decimal.valueOf(100)), is(equalTo(Decimal.valueOf(100))));
        assertThat(Decimals.minIgnoreNull(Decimal.valueOf(100), null), is(equalTo(Decimal.valueOf(100))));
        assertThat(Decimals.minIgnoreNull(Decimal.NULL, Decimal.valueOf(100)), is(equalTo(Decimal.valueOf(100))));
        assertThat(Decimals.minIgnoreNull(Decimal.valueOf(100), null, Decimal.valueOf(200)),
                is(equalTo(Decimal.valueOf(100))));
        assertThat(Decimals.minIgnoreNull(Decimal.valueOf(250), Decimal.valueOf(100)),
                is(equalTo(Decimal.valueOf(100))));
    }

    @Test
    public void testMaxIgnoreNull() {
        assertThat(Decimals.maxIgnoreNull(), is(equalTo(Decimal.NULL)));
        assertThat(Decimals.maxIgnoreNull((Decimal)null), is(equalTo(Decimal.NULL)));
        assertThat(Decimals.maxIgnoreNull(Decimal.NULL), is(equalTo(Decimal.NULL)));
        assertThat(Decimals.maxIgnoreNull(Decimal.valueOf(100)), is(equalTo(Decimal.valueOf(100))));
        assertThat(Decimals.maxIgnoreNull(Decimal.valueOf(100), null), is(equalTo(Decimal.valueOf(100))));
        assertThat(Decimals.maxIgnoreNull(Decimal.NULL, Decimal.valueOf(100)), is(equalTo(Decimal.valueOf(100))));
        assertThat(Decimals.maxIgnoreNull(Decimal.valueOf(100), null, Decimal.valueOf(200)),
                is(equalTo(Decimal.valueOf(200))));
        assertThat(Decimals.maxIgnoreNull(Decimal.valueOf(250), Decimal.valueOf(100)),
                is(equalTo(Decimal.valueOf(250))));
    }

}
