/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search.matcher;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class WildcardMatcherTest {

    @Test
    public void testIsMatchingWithoutWildcard() {
        assertTrue(new WildcardMatcher("abc").isMatching("abc"));
        assertTrue(new WildcardMatcher("abc").isMatching("abcDef"));

        assertFalse(new WildcardMatcher("abc").isMatching("ab"));
        assertFalse(new WildcardMatcher("abc").isMatching("def"));
    }

    @Test
    public void testIsMatchingWithAsterisk() {

        assertTrue(new WildcardMatcher("a*c").isMatching("abc"));
        assertTrue(new WildcardMatcher("a*c").isMatching("abasdfasdfc"));
        assertTrue(new WildcardMatcher("a*c").isMatching("ac"));
        assertTrue(new WildcardMatcher("*").isMatching("acasdfasdfce"));

        assertFalse(new WildcardMatcher("a*c").isMatching("abcb"));
    }

    @Test
    public void testIsMatchingWithQuestion() {

        assertTrue(new WildcardMatcher("a?c").isMatching("abc"));
        assertTrue(new WildcardMatcher("?").isMatching("a"));

        assertFalse(new WildcardMatcher("?").isMatching("ab"));
        assertFalse(new WildcardMatcher("a?c").isMatching("ac"));
        assertFalse(new WildcardMatcher("a?c").isMatching("abcb"));
        assertFalse(new WildcardMatcher("a?c").isMatching("abasdfasdfc"));
    }

    @Test
    public void testIsMatchingWithInvalidSearchString() {

        assertFalse(new WildcardMatcher("a c").isMatching("abc"));

        // Error in Regexp
        assertFalse(new WildcardMatcher("a*{c").isMatching("abc"));
    }

    @Test
    public void testCaseInsensitive() {

        assertTrue(new WildcardMatcher("Abc").isMatching("abc"));

        assertTrue(new WildcardMatcher("A*C").isMatching("abc"));
    }
}
