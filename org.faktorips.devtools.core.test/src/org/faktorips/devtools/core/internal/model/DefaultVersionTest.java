/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class DefaultVersionTest {

    @Test
    public void testCompareTo_equalString() throws Exception {
        DefaultVersion version1 = new DefaultVersion("a");
        DefaultVersion version2 = new DefaultVersion("a");

        assertEquals(0, version1.compareTo(version2));
    }

    @Test
    public void testCompareTo_lesserString() throws Exception {
        DefaultVersion version1 = new DefaultVersion("a");
        DefaultVersion version2 = new DefaultVersion("b");

        assertTrue(version1.compareTo(version2) < 0);
    }

    @Test
    public void testCompareTo_greaterString() throws Exception {
        DefaultVersion version1 = new DefaultVersion("b");
        DefaultVersion version2 = new DefaultVersion("a");

        assertTrue(version1.compareTo(version2) > 0);
    }

    @Test
    public void testCompareTo_equalNum() throws Exception {
        DefaultVersion version1 = new DefaultVersion("1234");
        DefaultVersion version2 = new DefaultVersion("1234");

        assertEquals(0, version1.compareTo(version2));
    }

    @Test
    public void testCompareTo_lesserNum() throws Exception {
        DefaultVersion version1 = new DefaultVersion("3");
        DefaultVersion version2 = new DefaultVersion("1234");

        assertTrue(version1.compareTo(version2) < 0);
    }

    @Test
    public void testCompareTo_greaterNum() throws Exception {
        DefaultVersion version1 = new DefaultVersion("1234");
        DefaultVersion version2 = new DefaultVersion("2");

        assertTrue(version1.compareTo(version2) > 0);
    }

    @Test
    public void testCompareTo_equalAlphaNum() throws Exception {
        DefaultVersion version1 = new DefaultVersion("1.1.abc");
        DefaultVersion version2 = new DefaultVersion("1.1.abc");

        assertEquals(0, version1.compareTo(version2));
    }

    @Test
    public void testCompareTo_lesserAlphaNum1() throws Exception {
        DefaultVersion version1 = new DefaultVersion("1.3.xxx");
        DefaultVersion version2 = new DefaultVersion("1.10.xxx");

        assertTrue(version1.compareTo(version2) < 0);
    }

    @Test
    public void testCompareTo_lesserAlphaNum2() throws Exception {
        DefaultVersion version1 = new DefaultVersion("1.3.aaa");
        DefaultVersion version2 = new DefaultVersion("1.3.xxx");

        assertTrue(version1.compareTo(version2) < 0);
    }

    @Test
    public void testCompareTo_lesserAlphaNum3() throws Exception {
        DefaultVersion version1 = new DefaultVersion("a3");
        DefaultVersion version2 = new DefaultVersion("aa3");

        assertTrue(version1.compareTo(version2) < 0);
    }

    @Test
    public void testCompareTo_greaterAlphaNum3() throws Exception {
        DefaultVersion version1 = new DefaultVersion("aa3");
        DefaultVersion version2 = new DefaultVersion("a3");

        assertTrue(version1.compareTo(version2) > 0);
    }

    @Test
    public void testCompareTo_lesserAlphaNum5() throws Exception {
        DefaultVersion version1 = new DefaultVersion("a+3");
        DefaultVersion version2 = new DefaultVersion("a3");

        assertTrue(version1.compareTo(version2) < 0);
    }

    @Test
    public void testCompareTo_greaterAlphaNum5() throws Exception {
        DefaultVersion version1 = new DefaultVersion("a3");
        DefaultVersion version2 = new DefaultVersion("a+3");

        assertTrue(version1.compareTo(version2) > 0);
    }

    @Test
    public void testCompareTo_lesserAlphaNum4() throws Exception {
        DefaultVersion version1 = new DefaultVersion("a 3");
        DefaultVersion version2 = new DefaultVersion("aa3");

        assertTrue(version1.compareTo(version2) < 0);
    }

    @Test
    public void testCompareTo_greaterAlphaNum1() throws Exception {
        DefaultVersion version1 = new DefaultVersion("1.12.xxx");
        DefaultVersion version2 = new DefaultVersion("1.4.xxx");

        assertTrue(version1.compareTo(version2) > 0);
    }

    @Test
    public void testCompareTo_greaterAlphaNum2() throws Exception {
        DefaultVersion version1 = new DefaultVersion("1.4.xxx");
        DefaultVersion version2 = new DefaultVersion("1.4.aaa");

        assertTrue(version1.compareTo(version2) > 0);
    }

}
