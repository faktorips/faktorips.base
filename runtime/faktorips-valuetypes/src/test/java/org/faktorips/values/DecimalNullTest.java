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

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DecimalNullTest {

    @Test
    public void testCompareTo_ShouldThrowException() {
        assertThrows(UnsupportedOperationException.class, () -> {
            Decimal.NULL.compareTo(Decimal.NULL);
        });
    }

}
