/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal.tableindex;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

public class UniqueResultStructureTest {

    private final Integer resultValue = Integer.valueOf(123);

    @Test
    public void testCopy() {
        UniqueResultStructure<Integer> structure = new UniqueResultStructure<>(resultValue);
        UniqueResultStructure<Integer> copyStructure = structure.copy();

        assertEquals(copyStructure, structure);
        assertEquals(copyStructure.getUnique(), structure.getUnique());
        assertSame(copyStructure, structure);
    }
}
