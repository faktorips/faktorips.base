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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.faktorips.values.Money;
import org.junit.Test;

public class UnrestrictedValueSetTest {
  
    @Test
    public void testIsUnrestricted_WithoutNull_includeNull() {
        UnrestrictedValueSet<String> emptyWithoutNull = new UnrestrictedValueSet<String>(false);

        assertThat(emptyWithoutNull.isUnrestricted(false), is(false));
    }

    @Test
    public void testIsUnrestricted_WithoutNull_excludeNull() {
        UnrestrictedValueSet<String> emptyWithoutNull = new UnrestrictedValueSet<String>(false);

        assertThat(emptyWithoutNull.isUnrestricted(true), is(true));
    }

    @Test
    public void testIsUnrestricted_WithNull_includeNull() {
        UnrestrictedValueSet<String> emptyWithNull = new UnrestrictedValueSet<String>(true);

        assertThat(emptyWithNull.isUnrestricted(false), is(true));
    }

    @Test
    public void testIsUnrestricted_WithNull_excludeNull() {
        UnrestrictedValueSet<String> emptyWithNull = new UnrestrictedValueSet<String>(false);

        assertThat(emptyWithNull.isUnrestricted(true), is(true));
    }

    @Test
    public void testContains() throws Exception {
        assertThat(new UnrestrictedValueSet<Integer>(false).contains(null), is(false));
        assertThat(new UnrestrictedValueSet<Integer>(true).contains(null), is(true));
        assertThat(new UnrestrictedValueSet<Money>(false).contains(Money.NULL), is(false));
        assertThat(new UnrestrictedValueSet<Money>(true).contains(Money.NULL), is(true));
    }

    @Test
    public void testContainsNull() throws Exception {
        assertThat(new UnrestrictedValueSet<Integer>(false).containsNull(), is(false));
        assertThat(new UnrestrictedValueSet<Integer>(true).containsNull(), is(true));
        assertThat(new UnrestrictedValueSet<Money>(false).containsNull(), is(false));
        assertThat(new UnrestrictedValueSet<Money>(true).containsNull(), is(true));
    }

    @Test(expected = IllegalStateException.class)
    public void testGetValues() throws Exception {
        new UnrestrictedValueSet<Integer>(false).getValues(true);
    }

    @Test
    public void testIsDiscrete() throws Exception {
        assertThat(new UnrestrictedValueSet<Integer>(false).isDiscrete(), is(false));
        assertThat(new UnrestrictedValueSet<TestEnum>(false).isDiscrete(), is(false));
    }

    @Test
    public void testIsEmpty() throws Exception {
        assertThat(new UnrestrictedValueSet<Integer>(false).isEmpty(), is(false));
        assertThat(new UnrestrictedValueSet<TestEnum>(false).isEmpty(), is(false));
    }

    @Test
    public void testIsRange() throws Exception {
        assertThat(new UnrestrictedValueSet<Integer>(false).isRange(), is(false));
        assertThat(new UnrestrictedValueSet<TestEnum>(false).isRange(), is(false));
    }

    @Test
    public void testSize() throws Exception {
        assertThat(new UnrestrictedValueSet<Integer>(false).size(), is(Integer.MAX_VALUE));
        // yes, even when there are only 3 actual values...
        assertThat(new UnrestrictedValueSet<TestEnum>(false).size(), is(Integer.MAX_VALUE));
    }

    @Test
    public void testToString() throws Exception {
        assertThat(new UnrestrictedValueSet<Integer>(false).toString(), is("UnrestrictedValueSet"));
        assertThat(new UnrestrictedValueSet<Integer>(true).toString(), is("UnrestrictedValueSet"));
    }

    private enum TestEnum {
        TEST_A,
        TEST_B,
        TEST_C
    }

}
