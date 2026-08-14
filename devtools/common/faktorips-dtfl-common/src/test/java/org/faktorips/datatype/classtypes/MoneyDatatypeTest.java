/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.datatype.classtypes;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 *
 * @author Thorsten Guenther
 */
public class MoneyDatatypeTest {

    @Test
    public void testDivisibleWithoutRemainder() {
        MoneyDatatype datatype = new MoneyDatatype();
        assertTrue(datatype.divisibleWithoutRemainder("10 EUR", "2 EUR")); //$NON-NLS-1$ //$NON-NLS-2$
        assertFalse(datatype.divisibleWithoutRemainder("10 EUR", "3 EUR")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void testCompare() {
        MoneyDatatype datatype = new MoneyDatatype();
        assertThat(datatype.compare(null, null), is(0));
        assertThat(datatype.compare(null, ""), is(0));
        assertThat(datatype.compare(null, "10 EUR"), is(-1));
        assertThat(datatype.compare("10 EUR", null), is(1));
        assertThat(datatype.compare("10 EUR", "20 EUR"), is(-1));
        assertThat(datatype.compare("30 EUR", "20 EUR"), is(1));
    }

}
