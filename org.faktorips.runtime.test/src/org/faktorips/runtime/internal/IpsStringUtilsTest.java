/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
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
    public void testJoinSingle() {
        assertEquals("a", IpsStringUtils.join(new Object[] { "a" }, ", "));
    }

    @Test
    public void testJoinMult() {
        assertEquals("a, 3", IpsStringUtils.join(new Object[] { "a", 3 }, ", "));
    }

    @Test
    public void testJoinEmpty() {
        assertEquals("", IpsStringUtils.join(new Object[] {}, ", "));
    }
}
