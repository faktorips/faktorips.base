/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.Test;

public class CardinalityRangeTest {

    @Test
    public void testIsEmpty() {
        assertTrue(new CardinalityRange(0, 0, 0).isEmpty());
        assertTrue(CardinalityRange.EXCLUDED.isEmpty());
        assertFalse(new CardinalityRange(0, 1, 0).isEmpty());
    }

}
