/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.util;

import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

public class ArgumentCheckTest {

    @Test
    public void testIsSubclassOf() {
        ArgumentCheck.isSubclassOf(String.class, String.class);
        ArgumentCheck.isSubclassOf(Double.class, Number.class);
        try {
            ArgumentCheck.isSubclassOf(String.class, Number.class);
            fail();
        } catch (IllegalArgumentException e) {
            // an exception is excepted to be thrown
        }
    }

    @Test
    public void testIsInstanceOf() {
        ArgumentCheck.isInstanceOf("123", String.class);
        ArgumentCheck.isInstanceOf(Double.valueOf(1234), Number.class);
        try {
            ArgumentCheck.isInstanceOf(this, String.class);
            fail();
        } catch (IllegalArgumentException e) {
            // an exception is excepted to be thrown
        }
    }

    @Test
    public void testIsNullArray() {
        String[] ids = new String[3];
        try {
            ArgumentCheck.notNull(ids);
            fail();
        } catch (RuntimeException e) {
            // an exception is excepted to be thrown
        }

        ids[0] = "";
        ids[1] = "";
        ids[2] = "";

        // expected to pass
        ArgumentCheck.notNull(ids);
    }

    @Test
    public void testIsNullArrayContext() {
        String[] ids = new String[3];
        try {
            ArgumentCheck.notNull(ids, this);
            fail();
        } catch (RuntimeException e) {
            // an exception is excepted to be thrown
        }

        ids[0] = "";
        ids[1] = "";
        ids[2] = "";

        // expected to pass
        ArgumentCheck.notNull(ids, this);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAtLeast_SizeTooSmallWithEmptyCollection() {
        ArgumentCheck.atLeast(Collections.emptyList(), 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAtLeast_SizeTooSmallWithNonEmptyCollection() {
        ArgumentCheck.atLeast(Arrays.asList("A", "B"), 3);
    }

    @Test(expected = NullPointerException.class)
    public void testAtLeast_NullCollection() {
        ArgumentCheck.atLeast(null, 0);
    }

    @Test
    public void testAtLeast() {
        // No asserts needed, exception is thrown if check fails
        ArgumentCheck.atLeast(Collections.emptyList(), 0);
        ArgumentCheck.atLeast(Arrays.asList("A"), 1);
        ArgumentCheck.atLeast(Arrays.asList("A", "B"), 1);
        ArgumentCheck.atLeast(Arrays.asList("A", "B"), 2);
    }

}
