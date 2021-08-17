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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;

import org.junit.Test;

public class StringLengthValueSetTest {

    @Test
    public void testDefaultConstructor() {
        StringLengthValueSet sl = new StringLengthValueSet();

        assertThat(sl.getMaximumLength(), is(nullValue()));
        assertThat(sl.containsNull(), is(true));
    }

    @Test
    public void testContains() {
        StringLengthValueSet sl = new StringLengthValueSet(10, true);

        assertThat(sl.contains("within"), is(true));
        assertThat(sl.contains("tooLongForLimitOf10"), is(false));
    }

    @Test
    public void testContainsNull() {
        StringLengthValueSet sl1 = new StringLengthValueSet(10, true);
        StringLengthValueSet sl2 = new StringLengthValueSet(10, false);

        assertThat(sl1.containsNull(), is(true));
        assertThat(sl1.contains(null), is(true));
        assertThat(sl2.containsNull(), is(false));
        assertThat(sl2.contains(null), is(false));
    }

    @Test
    public void testIsEmpty() {
        StringLengthValueSet sl1 = new StringLengthValueSet(0, false);
        StringLengthValueSet sl2 = new StringLengthValueSet(10, false);
        StringLengthValueSet sl3 = new StringLengthValueSet(0, true);
        StringLengthValueSet sl4 = new StringLengthValueSet(null, true);
        StringLengthValueSet sl5 = new StringLengthValueSet(null, false);
        assertThat(sl1.isEmpty(), is(true));
        assertThat(sl2.isEmpty(), is(false));
        assertThat(sl3.isEmpty(), is(false));
        assertThat(sl4.isEmpty(), is(false));
        assertThat(sl5.isEmpty(), is(false));
    }

    @Test
    public void testIsDiscrete_empty() {
        StringLengthValueSet sl1 = new StringLengthValueSet(0, false);

        assertThat(sl1.isDiscrete(), is(true));
    }

    @Test
    public void testIsDiscrete_notEmpty() {
        StringLengthValueSet sl1 = new StringLengthValueSet(0, true);
        StringLengthValueSet sl2 = new StringLengthValueSet(null, true);
        StringLengthValueSet sl3 = new StringLengthValueSet(null, false);

        assertThat(sl1.isDiscrete(), is(false));
        assertThat(sl2.isDiscrete(), is(false));
        assertThat(sl3.isDiscrete(), is(false));
    }

    @Test
    public void testGetValues_empty() {
        StringLengthValueSet sl1 = new StringLengthValueSet(0, false);

        assertThat(sl1.getValues(false).isEmpty(), is(true));
    }

    @Test(expected = IllegalStateException.class)
    public void testGetValues_notEmpty() {
        StringLengthValueSet sl1 = new StringLengthValueSet(null, false);

        sl1.getValues(false).isEmpty();
    }

    @Test
    public void testIsUnrestricted_WithoutNull_includesNull() {
        StringLengthValueSet emptyWithoutNull = new StringLengthValueSet(null, false);

        assertThat(emptyWithoutNull.isUnrestricted(false), is(false));
    }

    @Test
    public void testIsUnrestricted_WithoutNull_excludesNull() {
        StringLengthValueSet emptyWithoutNull = new StringLengthValueSet(null, false);

        assertThat(emptyWithoutNull.isUnrestricted(true), is(true));
    }

    @Test
    public void testIsUnrestricted_WithNull_includesNull() {
        StringLengthValueSet emptyWithNull = new StringLengthValueSet(null, true);

        assertThat(emptyWithNull.isUnrestricted(false), is(true));
    }

    @Test
    public void testIsUnrestricted_WithNull_excludesNull() {
        StringLengthValueSet emptyWithNull = new StringLengthValueSet(null, false);

        assertThat(emptyWithNull.isUnrestricted(true), is(true));
    }

    @Test
    public void testIsUnrestricted_WithMaxLen_includesNull() {
        StringLengthValueSet withMaxLen = new StringLengthValueSet(Integer.valueOf(1), true);

        assertThat(withMaxLen.isUnrestricted(false), is(false));
    }

    @Test
    public void testIsUnrestricted_WithMaxLen_excludesNull() {
        StringLengthValueSet withMaxLen = new StringLengthValueSet(Integer.valueOf(1), true);

        assertThat(withMaxLen.isUnrestricted(true), is(false));
    }

}
