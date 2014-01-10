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

package org.faktorips.valueset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class DoubleRangeTest {

    @Test
    public void testConstructor() {
        DoubleRange range = new DoubleRange(5.0, 10.0);
        assertEquals(range.getLowerBound().doubleValue(), 5.0, 0.0);
        assertEquals(range.getUpperBound().doubleValue(), 10.0, 0.0);
        assertFalse(range.containsNull());
    }

    @Test
    public void testConstructor2() {
        DoubleRange range = new DoubleRange(5.0, 10.0, true);
        assertEquals(range.getLowerBound().doubleValue(), 5.0, 0.0);
        assertEquals(range.getUpperBound().doubleValue(), 10.0, 0.0);
        assertTrue(range.containsNull());
    }

    @Test
    public void testSerializable() throws Exception {
        TestUtil.testSerializable(new DoubleRange(5.0, 10.0));
    }

}
