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

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;

public class DefaultRangeTest {

    @Test
    public void testIsDiscrete() {
        TestRange rangeWithStep = new TestRange(0, 10, 1);

        assertThat(rangeWithStep.isDiscrete(), is(true));
    }

    @Test
    public void testIsDiscrete_NoStep() {
        TestRange rangeWithoutStep = new TestRange(0, 10, null);

        assertThat(rangeWithoutStep.isDiscrete(), is(false));
    }

    @Test
    public void testIsDiscrete_EmptyLegacy() {
        TestRange emptyLegacyRangeWithStep = new TestRange(10, 0, 1);

        assertThat(emptyLegacyRangeWithStep.isDiscrete(), is(true));
    }

    @Test
    public void testIsDiscrete_EmptyLegacyNoStep() {
        TestRange emptytLegacyRangeWithoutStep = new TestRange(10, 0, null);

        assertThat(emptytLegacyRangeWithoutStep.isDiscrete(), is(true));
    }

    @Test
    public void testIsDiscrete_Empty() {
        TestRange emptytRange = new TestRange();

        assertThat(emptytRange.isDiscrete(), is(true));
    }

    @Test
    public void testIsDiscrete_Size1() {
        TestRange rangeWithStep = new TestRange(0, 0, 1);

        assertThat(rangeWithStep.isDiscrete(), is(true));
    }

    @Test
    public void testIsDiscrete_Size1NoStep() {
        TestRange rangeWithoutStep = new TestRange(0, 0, null);

        assertThat(rangeWithoutStep.isDiscrete(), is(true));
    }

    @Test
    public void testIsDiscrete_NoBoundsAndNoStep() {
        TestRange rangeWithoutStep = new TestRange(null, null, null, true);

        assertThat(rangeWithoutStep.isDiscrete(), is(false));
    }

    @Test
    public void testIsDiscrete_NoBounds() {
        TestRange rangeWithStep = new TestRange(null, null, 10, true);

        assertThat(rangeWithStep.isDiscrete(), is(true));
    }

    @Test
    public void testEquals_LegacyAndNewEmpty() {
        TestRange emptytRange = new TestRange();
        TestRange emptyLegacyRangeWithStep = new TestRange(10, 0, 1);

        assertThat(emptytRange, is(emptyLegacyRangeWithStep));
    }

    @Test
    public void testHashCode_LegacyAndNewEmpty() {
        TestRange emptytRange = new TestRange();
        TestRange emptyLegacyRangeWithStep = new TestRange(10, 0, 1);

        assertThat(emptytRange.hashCode(), is(emptyLegacyRangeWithStep.hashCode()));
    }

    @Test
    public void testIsUnrestricted_EmptyRange_includesNull() {
        TestRange emptyRangeWithoutNull = new TestRange();

        assertThat(emptyRangeWithoutNull.isUnrestricted(false), is(false));
    }

    @Test
    public void testIsUnrestricted_EmptyRange_excludesNull() {
        TestRange emptyRangeWithoutNull = new TestRange();

        assertThat(emptyRangeWithoutNull.isUnrestricted(true), is(false));
    }

    @Test
    public void testIsUnrestricted_RangeWithNull_includesNull() {
        TestRange emptyWithNull = new TestRange(null, null, null, true);

        assertThat(emptyWithNull.isUnrestricted(false), is(true));
    }

    @Test
    public void testIsUnrestricted_RangeWithNull_excludesNull() {
        TestRange emptyWithNull = new TestRange(null, null, null, true);

        assertThat(emptyWithNull.isUnrestricted(true), is(true));
    }

    @Test
    public void testIsUnrestricted_RangeWithLower_includesNull() {
        TestRange range = new TestRange(Integer.valueOf(1), null, null, true);

        assertThat(range.isUnrestricted(false), is(false));
    }

    @Test
    public void testIsUnrestricted_RangeWithLower_excludesNull() {
        TestRange range = new TestRange(Integer.valueOf(1), null, null, true);

        assertThat(range.isUnrestricted(true), is(false));
    }

    @Test
    public void testIsUnrestricted_RangeWithUpper_excludesNull() {
        TestRange range = new TestRange(null, Integer.valueOf(10), null, true);

        assertThat(range.isUnrestricted(true), is(false));
    }

    @Test
    public void testIsUnrestricted_RangeWithUpper_includesNull() {
        TestRange range = new TestRange(null, Integer.valueOf(10), null, true);

        assertThat(range.isUnrestricted(false), is(false));
    }

    @Test
    public void testIsUnrestricted_RangeWithStep_includesNull() {
        TestRange range = new TestRange(null, null, Integer.valueOf(10), true);

        assertThat(range.isUnrestricted(false), is(false));
    }

    @Test
    public void testIsUnrestricted_RangeWithStep_excludesNull() {
        TestRange range = new TestRange(null, null, Integer.valueOf(10), true);

        assertThat(range.isUnrestricted(true), is(false));
    }

    @Test
    public void testGetValues_UpperIsLower() {
        assertThat(new TestRange(5, 5, null).getValues(true), hasItems(Integer.valueOf(5)));
        assertThat(new TestRange(5, 5, null, true).getValues(true), hasItems(Integer.valueOf(5)));
        assertThat(new TestRange(5, 5, null, true).getValues(false), hasItems((Integer)null, Integer.valueOf(5)));
    }

    @Test(expected = RuntimeException.class)
    public void testGetValues_NoStep() {
        new TestRange(1, 5, null).getValues(true);
    }

    private class TestRange extends DefaultRange<Integer> {

        private static final long serialVersionUID = 3385179851895588865L;

        public TestRange() {
            super();
        }

        public TestRange(Integer lower, Integer upper, Integer step) {
            super(lower, upper, step, false);
        }

        public TestRange(Integer lower, Integer upper, Integer step, boolean containsNull) {
            super(lower, upper, step, containsNull);
        }

        @Override
        protected boolean checkIfValueCompliesToStepIncrement(Integer value, Integer bound) {
            return true;
        }

        @Override
        protected Integer getNullValue() {
            return null;
        }
    }
}
