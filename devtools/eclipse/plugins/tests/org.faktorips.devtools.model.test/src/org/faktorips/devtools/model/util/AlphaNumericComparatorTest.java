/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AlphaNumericComparatorTest {

    @InjectMocks
    private AlphaNumericComparator comparator;

    @Test
    public void testCompare_EqualString() throws Exception {
        assertEquals(0, comparator.compare("a", "a"));
    }

    @Test
    public void testCompare_SimpleString() throws Exception {
        assertTrue(comparator.compare("a", "b") < 0);
        assertTrue(comparator.compare("b", "a") > 0);
    }

    @Test
    public void testCompare_EqualNum() throws Exception {
        assertEquals(0, comparator.compare("1234", "1234"));
    }

    @Test
    public void testCompare_SimpleNum() throws Exception {
        assertTrue(comparator.compare("3", "1234") < 0);
        assertTrue(comparator.compare("1234", "3") > 0);
    }

    @Test
    public void testCompare_EqualAlphaNum() throws Exception {
        assertEquals(0, comparator.compare("1.1.abc", "1.1.abc"));
    }

    @Test
    public void testCompare_AlphaNum1() throws Exception {
        assertTrue(comparator.compare("1.3.xxx", "1.10.xxx") < 0);
        assertTrue(comparator.compare("1.10.xxx", "1.3.xxx") > 0);
    }

    @Test
    public void testCompare_AlphaNum2() throws Exception {
        assertTrue(comparator.compare("1.3.aaa", "1.3.xxx") < 0);
        assertTrue(comparator.compare("1.3.xxx", "1.3.aaa") > 0);
    }

    @Test
    public void testCompare_AlphaNum3() throws Exception {
        assertTrue(comparator.compare("a3", "aa3") < 0);
        assertTrue(comparator.compare("aa3", "a3") > 0);
    }

    @Test
    public void testCompare_AlphaNum4() throws Exception {
        assertTrue(comparator.compare("a+3", "a3") < 0);
        assertTrue(comparator.compare("a3", "a+3") > 0);
    }

    @Test
    public void testCompare_AlphaNum5() throws Exception {
        assertTrue(comparator.compare("a 3", "aa3") < 0);
        assertTrue(comparator.compare("aa3", "a 3") > 0);
    }

    @Test
    public void testCompare_AlphaNum6() throws Exception {
        assertTrue(comparator.compare("1.12.xxx", "1.4.xxx") > 0);
        assertTrue(comparator.compare("1.4.xxx", "1.12.xxx") < 0);
    }

    @Test
    public void testCompare_AlphaNum7() throws Exception {
        assertTrue(comparator.compare("abcasdfasf sadfasdf asdf asd 12340", "abcasdfasf sadfasdf asdf asd 1234") > 0);
        assertTrue(comparator.compare("abcasdfasf sadfasdf asdf asd 1234", "abcasdfasf sadfasdf asdf asd 12340") < 0);
    }

    @Test
    public void testCompare_LongAlphaNum() throws Exception {
        assertTrue(comparator.compare("12017100002", "12017100001") > 0);
        assertTrue(comparator.compare("A9223372036854775807B", "A9223372036854775808B") < 0);
    }

    @Test
    public void testCompare_LeadingZeroNum1() throws Exception {
        assertTrue(comparator.compare("a01a", "a1b") < 0);
        assertTrue(comparator.compare("a1b", "a01a") > 0);
        assertTrue(comparator.compare("a01c", "a1b") > 0);
        assertTrue(comparator.compare("a1b", "a01c") < 0);
    }

    @Test
    public void testCompare_LeadingZeroNum2() throws Exception {
        assertTrue(comparator.compare("a01", "a1") < 0);
        assertTrue(comparator.compare("a1", "a01") > 0);
        assertTrue(comparator.compare("a1xxx0001", "a001xxx1") < 0);
        assertTrue(comparator.compare("a001xxx1", "a1xxx0001") > 0);
    }

}
