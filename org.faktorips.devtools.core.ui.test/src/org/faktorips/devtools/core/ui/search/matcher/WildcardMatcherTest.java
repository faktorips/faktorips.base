/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search.matcher;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.faktorips.devtools.core.ui.search.matcher.WildcardMatcher;
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
