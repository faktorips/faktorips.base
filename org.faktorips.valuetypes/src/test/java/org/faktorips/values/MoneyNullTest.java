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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MoneyNullTest {

    private static final Money ONE_HUNDRED = Money.euro(100);

    // especially interesting, as Money.NULL has also the value 0;
    private static final Money ZERO = Money.euro(0);

    @Test(expected = NullPointerException.class)
    public void testCompareTo_null() {
        Money.NULL.compareTo(null);
    }

    @Test
    public void testCompareTo_MoneyNull() {
        assertEquals(0, Money.NULL.compareTo(Money.NULL));
        assertEquals(0, Money.NULL.compareTo(new MoneyNull()));
    }

    @Test
    public void testCompareTo_Money() {
        assertEquals(-1, Money.NULL.compareTo(ONE_HUNDRED));
        assertEquals(1, ONE_HUNDRED.compareTo(Money.NULL));
        assertEquals(-1, Money.NULL.compareTo(ZERO));
        assertEquals(1, ZERO.compareTo(Money.NULL));
    }

    @Test
    public void testEquals_null() {
        assertFalse(Money.NULL.equals(null));
    }

    @Test
    public void testEquals_MoneyNull() {
        assertTrue(Money.NULL.equals(Money.NULL));
        assertTrue(Money.NULL.equals(new MoneyNull()));
    }

    @Test
    public void testEquals_Money() {
        assertFalse(Money.NULL.equals(ONE_HUNDRED));
        assertFalse(ONE_HUNDRED.equals(Money.NULL));
        assertFalse(Money.NULL.equals(ZERO));
        assertFalse(ZERO.equals(Money.NULL));
    }

    @Test
    public void testHashCode_multipleMoneyNullInstances() {
        assertEquals(Money.NULL.hashCode(), new MoneyNull().hashCode());
    }

    @Test
    public void testHashCode_otherInstances() {
        assertTrue(ONE_HUNDRED.hashCode() != Money.NULL.hashCode());
    }
}
