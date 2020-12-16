/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

import org.junit.Test;

public class IpsStringUtilsTest {

    @Test
    public void testIsEmpty() {
        assertTrue(IpsStringUtils.isEmpty(null));
        assertTrue(IpsStringUtils.isEmpty(""));
        assertFalse(IpsStringUtils.isEmpty("           "));
        assertFalse(IpsStringUtils.isEmpty("      \n     "));
        assertFalse(IpsStringUtils.isEmpty("a"));
        assertFalse(IpsStringUtils.isEmpty(" a "));
    }

    @Test
    public void testIsBlank() {
        assertTrue(IpsStringUtils.isBlank(null));
        assertTrue(IpsStringUtils.isBlank(""));
        assertTrue(IpsStringUtils.isBlank("           "));
        assertTrue(IpsStringUtils.isBlank("      \n     "));
        assertFalse(IpsStringUtils.isBlank("a"));
        assertFalse(IpsStringUtils.isBlank(" a "));
    }

    @Test
    public void testJoin_Array_Single() {
        assertEquals("a", IpsStringUtils.join(new Object[] { "a" }));
    }

    @Test
    public void testJoin_Array_Mult() {
        assertEquals("a, 3", IpsStringUtils.join(new Object[] { "a", 3 }));
    }

    @Test
    public void testJoin_Array_Null() {
        assertEquals("a, null, 3", IpsStringUtils.join(new Object[] { "a", null, 3 }));
    }

    @Test
    public void testJoin_Array_Empty() {
        assertEquals("", IpsStringUtils.join(new Object[] {}));
    }

    @Test
    public void testJoin_Iterable_Single() {
        assertEquals("a", IpsStringUtils.join(Arrays.asList("a")));
    }

    @Test
    public void testJoin_Iterable_Mult() {
        assertEquals("a, 3", IpsStringUtils.join(Arrays.asList("a", 3)));
    }

    @Test
    public void testJoin_Iterable_Null() {
        assertEquals("a, null, 3", IpsStringUtils.join(Arrays.asList("a", null, 3)));
    }

    @Test
    public void testJoin_Iterable_Empty() {
        assertEquals("", IpsStringUtils.join(Collections::emptyListIterator));
    }

    @Test
    public void testJoin_Array_Separator_Single() {
        assertEquals("a", IpsStringUtils.join(new Object[] { "a" }, "|"));
    }

    @Test
    public void testJoin_Array_Separator_Mult() {
        assertEquals("a|3", IpsStringUtils.join(new Object[] { "a", 3 }, "|"));
    }

    @Test
    public void testJoin_Array_Separator_Null() {
        assertEquals("a|null|3", IpsStringUtils.join(new Object[] { "a", null, 3 }, "|"));
    }

    @Test
    public void testJoin_Array_NullSeparator_Mult() {
        assertEquals("a3", IpsStringUtils.join(new Object[] { "a", 3 }, null));
    }

    @Test
    public void testJoin_Array_Separator_Empty() {
        assertEquals("", IpsStringUtils.join(new Object[] {}, "|"));
    }

    @Test
    public void testJoin_Iterable_Separator_Single() {
        assertEquals("a", IpsStringUtils.join(Arrays.asList("a"), "|"));
    }

    @Test
    public void testJoin_Iterable_Separator_Mult() {
        assertEquals("a|3", IpsStringUtils.join(Arrays.asList("a", 3), "|"));
    }

    @Test
    public void testJoin_Iterable_Separator_Null() {
        assertEquals("a|null|3", IpsStringUtils.join(Arrays.asList("a", null, 3), "|"));
    }

    @Test
    public void testJoin_Iterable_NullSeparator_Mult() {
        assertEquals("a3", IpsStringUtils.join(Arrays.asList("a", 3), (String)null));
    }

    @Test
    public void testJoin_Iterable_Separator_Empty() {
        assertEquals("", IpsStringUtils.join(Collections::emptyListIterator, "|"));
    }

    @Test
    public void testJoin_Iterable_Function_Separator_Single() {
        assertEquals("A", IpsStringUtils.join(Arrays.asList("a"), String::toUpperCase, "|"));
    }

    @Test
    public void testJoin_Iterable_Function_Separator_Mult() {
        assertEquals("A|B", IpsStringUtils.join(Arrays.asList("a", "b"), String::toUpperCase, "|"));
    }

    @Test
    public void testJoin_Iterable_Function_Separator_Null() {
        assertEquals("A|NULL|B",
                IpsStringUtils.join(Arrays.asList("a", null, "b"), o -> Objects.toString(o).toUpperCase(), "|"));
    }

    @Test
    public void testJoin_Iterable_Function_NullSeparator_Mult() {
        assertEquals("AB", IpsStringUtils.join(Arrays.asList("a", "b"), String::toUpperCase, null));
    }

    @Test
    public void testJoin_Iterable_Function_Separator_Empty() {
        assertEquals("", IpsStringUtils.join(Collections::emptyListIterator, $ -> {
            fail("toString function should not be called for empty list");
            return "";
        }, "|"));
    }

    @Test
    public void testJoin_Iterable_Function_Single() {
        assertEquals("A", IpsStringUtils.join(Arrays.asList("a"), String::toUpperCase));
    }

    @Test
    public void testJoin_Iterable_Function_Mult() {
        assertEquals("A, B", IpsStringUtils.join(Arrays.asList("a", "b"), String::toUpperCase));
    }

    public void testJoin_Iterable_Function_Null() {
        assertEquals("A, NULL, B",
                IpsStringUtils.join(Arrays.asList("a", null, "b"), o -> Objects.toString(o).toUpperCase()));
    }

    @Test
    public void testJoin_Iterable_Function_Empty() {
        assertEquals("", IpsStringUtils.join(Collections::emptyListIterator, $ -> {
            fail("toString function should not be called for empty list");
            return "";
        }));
    }

    @Test
    public void testToLowerFirstChar() {
        assertEquals("", IpsStringUtils.toLowerFirstChar(""));
        assertEquals("a", IpsStringUtils.toLowerFirstChar("a"));
        assertEquals("aB", IpsStringUtils.toLowerFirstChar("aB"));
        assertEquals("ab", IpsStringUtils.toLowerFirstChar("ab"));
        assertEquals("a", IpsStringUtils.toLowerFirstChar("A"));
        assertEquals("aB", IpsStringUtils.toLowerFirstChar("AB"));
        assertEquals("ab", IpsStringUtils.toLowerFirstChar("Ab"));
    }
}
