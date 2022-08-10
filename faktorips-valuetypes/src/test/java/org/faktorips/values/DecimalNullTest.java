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

import org.junit.Test;

public class DecimalNullTest {

    @Test(expected = UnsupportedOperationException.class)
    public void testCompareTo_ShouldThrowException() {
        Decimal.NULL.compareTo(Decimal.NULL);
    }

}
