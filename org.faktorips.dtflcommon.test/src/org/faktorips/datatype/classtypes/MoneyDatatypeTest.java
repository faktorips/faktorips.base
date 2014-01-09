/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.datatype.classtypes;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

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

}
