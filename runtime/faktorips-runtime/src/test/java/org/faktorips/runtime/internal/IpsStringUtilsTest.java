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

import static org.faktorips.runtime.internal.IpsStringUtils.equalsNullAsEmpty;
import static org.faktorips.runtime.internal.IpsStringUtils.isBlank;
import static org.faktorips.runtime.internal.IpsStringUtils.isEmpty;
import static org.faktorips.runtime.internal.IpsStringUtils.isNotBlank;
import static org.faktorips.runtime.internal.IpsStringUtils.isNotEmpty;
import static org.faktorips.runtime.internal.IpsStringUtils.join;
import static org.faktorips.runtime.internal.IpsStringUtils.toLowerFirstChar;
import static org.faktorips.runtime.internal.IpsStringUtils.trimEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.junit.Test;

public class IpsStringUtilsTest {

    @Test
    public void testIsEmpty() {
        assertTrue(isEmpty(null));
        assertTrue(isEmpty(""));
        assertFalse(isEmpty("           "));
        assertFalse(isEmpty("      \n     "));
        assertFalse(isEmpty("a"));
        assertFalse(isEmpty(" a "));
    }

    @Test
    public void testIsNotEmpty() {
        assertFalse(isNotEmpty(null));
        assertFalse(isNotEmpty(""));
        assertTrue(isNotEmpty("           "));
        assertTrue(isNotEmpty("      \n     "));
        assertTrue(isNotEmpty("a"));
        assertTrue(isNotEmpty(" a "));
    }

    @Test
    public void testIsBlank() {
        assertTrue(isBlank(null));
        assertTrue(isBlank(""));
        assertTrue(isBlank("           "));
        assertTrue(isBlank("      \n     "));
        assertFalse(isBlank("a"));
        assertFalse(isBlank(" a "));
    }

    @Test
    public void testIsNotBlank() {
        assertFalse(isNotBlank(null));
        assertFalse(isNotBlank(""));
        assertFalse(isNotBlank("           "));
        assertFalse(isNotBlank("      \n     "));
        assertTrue(isNotBlank("a"));
        assertTrue(isNotBlank(" a "));
    }

    @Test
    public void testJoin_Array_Single() {
        assertEquals("a", join(new Object[] { "a" }));
    }

    @Test
    public void testJoin_Array_Mult() {
        assertEquals("a, 3", join(new Object[] { "a", 3 }));
    }

    @Test
    public void testJoin_Array_Null() {
        assertEquals("a, null, 3", join(new Object[] { "a", null, 3 }));
    }

    @Test
    public void testJoin_Array_Empty() {
        assertEquals("", join(new Object[] {}));
    }

    @Test
    public void testJoin_Iterable_Single() {
        assertEquals("a", join(List.of("a")));
    }

    @Test
    public void testJoin_Iterable_Mult() {
        assertEquals("a, 3", join(List.of("a", 3)));
    }

    @Test
    public void testJoin_Iterable_Null() {
        assertEquals("a, null, 3", join(Arrays.asList("a", null, 3)));
    }

    @Test
    public void testJoin_Iterable_Empty() {
        assertEquals("", join(Collections::emptyListIterator));
    }

    @Test
    public void testJoin_Array_Separator_Single() {
        assertEquals("a", join(new Object[] { "a" }, "|"));
    }

    @Test
    public void testJoin_Array_Separator_Mult() {
        assertEquals("a|3", join(new Object[] { "a", 3 }, "|"));
    }

    @Test
    public void testJoin_Array_Separator_Null() {
        assertEquals("a|null|3", join(new Object[] { "a", null, 3 }, "|"));
    }

    @Test
    public void testJoin_Array_NullSeparator_Mult() {
        assertEquals("a3", join(new Object[] { "a", 3 }, null));
    }

    @Test
    public void testJoin_Array_Separator_Empty() {
        assertEquals("", join(new Object[] {}, "|"));
    }

    @Test
    public void testJoin_Iterable_Separator_Single() {
        assertEquals("a", join(List.of("a"), "|"));
    }

    @Test
    public void testJoin_Iterable_Separator_Mult() {
        assertEquals("a|3", join(List.of("a", 3), "|"));
    }

    @Test
    public void testJoin_Iterable_Separator_Null() {
        assertEquals("a|null|3", join(Arrays.asList("a", null, 3), "|"));
    }

    @Test
    public void testJoin_Iterable_NullSeparator_Mult() {
        assertEquals("a3", join(List.of("a", 3), (String)null));
    }

    @Test
    public void testJoin_Iterable_Separator_Empty() {
        assertEquals("", join(Collections::emptyListIterator, "|"));
    }

    @Test
    public void testJoin_Iterable_Function_Separator_Single() {
        assertEquals("A", join(List.of("a"), String::toUpperCase, "|"));
    }

    @Test
    public void testJoin_Iterable_Function_Separator_Mult() {
        assertEquals("A|B", join(List.of("a", "b"), String::toUpperCase, "|"));
    }

    @Test
    public void testJoin_Iterable_Function_Separator_Null() {
        assertEquals("A|NULL|B",
                join(Arrays.asList("a", null, "b"), o -> Objects.toString(o).toUpperCase(), "|"));
    }

    @Test
    public void testJoin_Iterable_Function_NullSeparator_Mult() {
        assertEquals("AB", join(List.of("a", "b"), String::toUpperCase, null));
    }

    @Test
    public void testJoin_Iterable_Function_Separator_Empty() {
        assertEquals("", join(Collections::emptyListIterator, $ -> {
            fail("toString function should not be called for empty list");
            return "";
        }, "|"));
    }

    @Test
    public void testJoin_Iterable_Function_Single() {
        assertEquals("A", join(List.of("a"), String::toUpperCase));
    }

    @Test
    public void testJoin_Iterable_Function_Mult() {
        assertEquals("A, B", join(List.of("a", "b"), String::toUpperCase));
    }

    public void testJoin_Iterable_Function_Null() {
        assertEquals("A, NULL, B",
                join(List.of("a", null, "b"), o -> Objects.toString(o).toUpperCase()));
    }

    @Test
    public void testJoin_Iterable_Function_Empty() {
        assertEquals("", join(Collections::emptyListIterator, $ -> {
            fail("toString function should not be called for empty list");
            return "";
        }));
    }

    @Test
    public void testToLowerFirstChar() {
        assertEquals(null, toLowerFirstChar(null));
        assertEquals("", toLowerFirstChar(""));
        assertEquals("a", toLowerFirstChar("a"));
        assertEquals("aB", toLowerFirstChar("aB"));
        assertEquals("ab", toLowerFirstChar("ab"));
        assertEquals("a", toLowerFirstChar("A"));
        assertEquals("aB", toLowerFirstChar("AB"));
        assertEquals("ab", toLowerFirstChar("Ab"));
    }

    @Test
    public void testTrimEquals() {
        assertTrue(trimEquals(null, null));
        assertTrue(trimEquals("", ""));
        assertTrue(trimEquals("", " "));
        assertTrue(trimEquals(" ", ""));
        assertTrue(trimEquals(" a ", " a "));
        assertTrue(trimEquals("a", " a "));
        assertTrue(trimEquals(" a ", "a"));
        assertTrue(trimEquals(" a", "a "));
        assertTrue(trimEquals("\ta", " a"));
        assertTrue(trimEquals("\ta", "a"));

        assertFalse(trimEquals("", null));
        assertFalse(trimEquals(null, ""));
        assertFalse(trimEquals("a  b", "a b"));
    }

    @Test
    public void testEqualsNullAsEmpty() {
        assertTrue(equalsNullAsEmpty(null, null));
        assertTrue(equalsNullAsEmpty("", null));
        assertTrue(equalsNullAsEmpty(null, ""));
        assertTrue(equalsNullAsEmpty("", ""));
        assertTrue(equalsNullAsEmpty(" ", " "));
        assertTrue(equalsNullAsEmpty("a", "a"));

        assertFalse(equalsNullAsEmpty("", " "));
        assertFalse(equalsNullAsEmpty(" ", ""));
        assertFalse(equalsNullAsEmpty(null, " "));
        assertFalse(equalsNullAsEmpty(" ", null));
        assertFalse(equalsNullAsEmpty("", "a"));
        assertFalse(equalsNullAsEmpty("a", ""));
        assertFalse(equalsNullAsEmpty(null, "a"));
        assertFalse(equalsNullAsEmpty("a", null));
    }
}
