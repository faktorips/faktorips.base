/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.valueset;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

public class RegexValueSetTest {

    @Test
    public void testContains() {
        var valueSet = new RegexValueSet("^[a-zA-Z0-9\\-]{1,255}$");

        // Test invalid values
        assertThat(valueSet.contains(null), is(false));
        assertThat(valueSet.contains(""), is(false));
        assertThat(valueSet.contains(" "), is(false));
        assertThat(valueSet.contains("/a/b/c/.."), is(false));
        assertThat(valueSet.contains("12345,67"), is(false));
        assertThat(valueSet.contains("12.345,67"), is(false));
        assertThat(valueSet.contains("12345\n67"), is(false));
        assertThat(valueSet.contains("12345 67"), is(false));
        assertThat(valueSet.contains("  1234567  "), is(false));
        assertThat(valueSet.contains("a".repeat(256)), is(false));

        // Test valid values
        assertThat(valueSet.contains("12345678"), is(true));
        assertThat(valueSet.contains("-1234"), is(true));
        assertThat(valueSet.contains("ABCDEFGHIJKLMNO"), is(true));
        assertThat(valueSet.contains("8a8fe06d-513f-4f42-8770-d28fb895bd99"), is(true));
    }

    @Test
    public void testIsSubsetOf() {
        RegexValueSet valueSet1 = new RegexValueSet("\\d+");
        RegexValueSet valueSet2 = new RegexValueSet("\\d+");
        RegexValueSet valueSet3 = new RegexValueSet("[a-z]+");
        UnrestrictedValueSet<String> unrestrictedSet = new UnrestrictedValueSet<>();

        assertThat(valueSet1.isSubsetOf(unrestrictedSet), is(true));
        assertThat(valueSet1.isSubsetOf(valueSet2), is(true));
        assertThat(valueSet1.isSubsetOf(valueSet3), is(false));
    }

    @Test
    public void testProperties() {
        RegexValueSet valueSet = new RegexValueSet(".*");

        assertThat(valueSet.isDiscrete(), is(false));
        assertThat(valueSet.containsNull(), is(false));
        assertThat(valueSet.isEmpty(), is(false));
        assertThat(valueSet.isRange(), is(false));
        assertThat(valueSet.isUnrestricted(true), is(false));
        assertThat(valueSet.isUnrestricted(false), is(false));
        assertThat(valueSet.size(), is(Integer.MAX_VALUE));
    }

    @Test
    public void testGetDatatype() {
        RegexValueSet valueSet = new RegexValueSet(".*");

        assertThat(valueSet.getDatatype().isPresent(), is(true));
        assertThat(valueSet.getDatatype().get(), is(equalTo(String.class)));
    }

    @Test
    public void testRegexValueSet_NullPattern() {
        assertThrows(IllegalArgumentException.class,
                () -> new RegexValueSet(null));
    }

    @Test
    public void testRegexValueSet_EmptyPattern() {
        assertThrows(IllegalArgumentException.class,
                () -> new RegexValueSet(""));
    }

    @Test
    public void testRegexValueSet_BlankPattern() {
        assertThrows(IllegalArgumentException.class,
                () -> new RegexValueSet("  "));
    }

    @Test
    public void testRegexValueSet_InvalidPattern() {
        assertThrows(IllegalArgumentException.class,
                () -> new RegexValueSet("}{"));
    }
}