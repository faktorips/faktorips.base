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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Objects;

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
        assertTrue(ObjectUtil.isNull(TestEnumWithNullSupport.NULL));
        assertFalse(ObjectUtil.isNull(TestEnumWithNullSupport.A));
    }

    @Test
    public void testEqualsObject() {
        assertTrue(Objects.equals(null, null));

        Object o1 = new Object();
        assertTrue(Objects.equals(o1, o1));
        assertFalse(Objects.equals(null, o1));
        assertFalse(Objects.equals(o1, null));

        Object o2 = new Object();
        assertFalse(Objects.equals(o1, o2));
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
        ObjectUtil.checkInstanceOf(Long.valueOf(12), Number.class);
    }

    @Test
    public void testDefaultIfNull() {
        assertNull(ObjectUtil.defaultIfNull(null, null));
        Object o = new Object();
        assertSame(o, ObjectUtil.defaultIfNull(null, o));
        assertSame(o, ObjectUtil.defaultIfNull(o, ""));
        assertSame(o, ObjectUtil.defaultIfNull(o, null));
    }

    private static class TestEnumWithNullSupport implements NullObjectSupport {

        public static final TestEnumWithNullSupport A = new TestEnumWithNullSupport();

        public static final TestEnumWithNullSupport NULL = new TestEnumWithNullSupport();

        @Override
        public boolean isNull() {
            return this == NULL;
        }

        @Override
        public boolean isNotNull() {
            return !isNull();
        }
    }

}
