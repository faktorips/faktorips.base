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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ObjectPropertyTest {

    @Test
    public void testHashCode() {
        ObjectProperty op1 = new ObjectProperty(Integer.valueOf(1), "toString");
        ObjectProperty op2 = new ObjectProperty(Integer.valueOf(1), "toString");
        assertEquals(op1.hashCode(), op2.hashCode());

        ObjectProperty op3 = new ObjectProperty(Integer.valueOf(2), "toString");
        assertFalse(op1.hashCode() == op3.hashCode());
    }

    @Test
    public void testEqualsObject() {
        ObjectProperty op1 = new ObjectProperty(Integer.valueOf(1), "toString");
        ObjectProperty op2 = new ObjectProperty(Integer.valueOf(1), "toString");
        assertEquals(op1, op2);

        ObjectProperty op3 = new ObjectProperty(Integer.valueOf(2), "toString");
        assertTrue(!op1.equals(op3));
    }

}
