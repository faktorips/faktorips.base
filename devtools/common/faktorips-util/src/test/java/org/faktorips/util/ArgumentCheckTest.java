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

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

public class ArgumentCheckTest {

    @Test
    public void testIsSubclassOf() {
        ArgumentCheck.isSubclassOf(String.class, String.class);
        ArgumentCheck.isSubclassOf(Double.class, Number.class);
        assertThrows(IllegalArgumentException.class,
                () -> ArgumentCheck.isSubclassOf(String.class, Number.class));
    }

    @Test
    public void testIsInstanceOf() {
        ArgumentCheck.isInstanceOf("123", String.class);
        ArgumentCheck.isInstanceOf(Double.valueOf(1234), Number.class);
        assertThrows(IllegalArgumentException.class, () -> ArgumentCheck.isInstanceOf(this, String.class));
    }

    @Test
    public void testIsNullArray() {
        String[] ids = new String[3];
        assertThrows(RuntimeException.class, () -> ArgumentCheck.notNull(ids));

        ids[0] = "";
        ids[1] = "";
        ids[2] = "";

        // expected to pass
        ArgumentCheck.notNull(ids);
    }

    @Test
    public void testIsNullArrayContext() {
        String[] ids = new String[3];
        assertThrows(RuntimeException.class, () -> ArgumentCheck.notNull(ids, this));

        ids[0] = "";
        ids[1] = "";
        ids[2] = "";

        // expected to pass
        ArgumentCheck.notNull(ids, this);
    }

    @Test
    public void testAtLeast_SizeTooSmallWithEmptyCollection() {
        assertThrows(IllegalArgumentException.class, () -> ArgumentCheck.atLeast(Collections.emptyList(), 1));
    }

    @Test
    public void testAtLeast_SizeTooSmallWithNonEmptyCollection() {
        assertThrows(IllegalArgumentException.class, () -> ArgumentCheck.atLeast(Arrays.asList("A", "B"), 3));
    }

    @Test
    public void testAtLeast_NullCollection() {
        assertThrows(NullPointerException.class, () -> ArgumentCheck.atLeast(null, 0));
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
