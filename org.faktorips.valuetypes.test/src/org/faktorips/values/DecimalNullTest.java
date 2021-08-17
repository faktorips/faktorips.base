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

import org.junit.Test;

public class DecimalNullTest {

    @Test
    public void testCompareTo() {
        assertEquals(0, Decimal.NULL.compareTo(Decimal.NULL));
        assertEquals(-1, Decimal.NULL.compareTo(Decimal.ZERO));
    }

    @Test(expected = NullPointerException.class)
    public void testCompareTo_ShouldThrowExceptionForNull() {
        Decimal.NULL.compareTo(null);
    }

}
