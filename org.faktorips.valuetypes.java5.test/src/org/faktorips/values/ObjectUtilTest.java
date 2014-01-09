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

package org.faktorips.values;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * 
 * @author Jan Ortmann
 */
public class ObjectUtilTest {

    @Test
    public void testIsNull() {
        assertTrue(ObjectUtil.isNull(null));
        assertTrue(ObjectUtil.isNull(Money.NULL));
        assertFalse(ObjectUtil.isNull(Money.euro(42, 0)));
    }

    @Test
    public void testEqualsObject() {
        assertTrue(ObjectUtil.equals(null, null));

        Object o1 = new Object();
        assertTrue(ObjectUtil.equals(o1, o1));
        assertFalse(ObjectUtil.equals(null, o1));
        assertFalse(ObjectUtil.equals(o1, null));

        Object o2 = new Object();
        assertFalse(ObjectUtil.equals(o1, o2));
    }

    @Test(expected = ClassCastException.class)
    public void testCheckInstanceOf_fail() throws Exception {
        ObjectUtil.checkInstanceOf("", Integer.class);
    }

    @Test
    public void testCheckInstanceOf_failOnNull() throws Exception {
        ObjectUtil.checkInstanceOf(null, Integer.class);
    }

    @Test
    public void testCheckInstanceOf() throws Exception {
        ObjectUtil.checkInstanceOf(12, Number.class);
        ObjectUtil.checkInstanceOf(12, Integer.class);
        ObjectUtil.checkInstanceOf(new Long(12), Number.class);
    }

}
