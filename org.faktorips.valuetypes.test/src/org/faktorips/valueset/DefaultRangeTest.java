package org.faktorips.valueset;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class DefaultRangeTest {

    @Test
    public void testIsDiscrete() {
        TestRange rangeWithStep = new TestRange(0, 10, 1);

        assertTrue(rangeWithStep.isDiscrete());
    }

    @Test
    public void testIsDiscrete_NoStep() {
        TestRange rangeWithoutStep = new TestRange(0, 10, null);

        assertFalse(rangeWithoutStep.isDiscrete());
    }

    @Test
    public void testIsDiscrete_EmptyLegacy() {
        TestRange emptyLegacyRangeWithStep = new TestRange(10, 0, 1);

        assertTrue(emptyLegacyRangeWithStep.isDiscrete());
    }

    @Test
    public void testIsDiscrete_EmptyLegacyNoStep() {
        TestRange emptytLegacyRangeWithoutStep = new TestRange(10, 0, null);

        assertTrue(emptytLegacyRangeWithoutStep.isDiscrete());
    }

    @Test
    public void testIsDiscrete_Empty() {
        TestRange emptytRange = new TestRange();

        assertTrue(emptytRange.isDiscrete());
    }

    @Test
    public void testIsDiscrete_Size1() {
        TestRange rangeWithStep = new TestRange(0, 0, 1);

        assertTrue(rangeWithStep.isDiscrete());
    }

    @Test
    public void testIsDiscrete_Size1NoStep() {
        TestRange rangeWithoutStep = new TestRange(0, 0, null);

        assertTrue(rangeWithoutStep.isDiscrete());
    }

    @Test
    public void testIsDiscrete_NoBoundsAndNoStep() {
        TestRange rangeWithoutStep = new TestRange(null, null, null, true);

        assertFalse(rangeWithoutStep.isDiscrete());
    }

    @Test
    public void testIsDiscrete_NoBounds() {
        TestRange rangeWithoutStep = new TestRange(null, null, 10, true);

        assertTrue(rangeWithoutStep.isDiscrete());
    }

    @Test
    public void testEquals_LegacyAndNewEmpty() {
        TestRange emptytRange = new TestRange();
        TestRange emptyLegacyRangeWithStep = new TestRange(10, 0, 1);

        assertEquals(emptyLegacyRangeWithStep, emptytRange);
    }

    @Test
    public void testHashCode_LegacyAndNewEmpty() {
        TestRange emptytRange = new TestRange();
        TestRange emptyLegacyRangeWithStep = new TestRange(10, 0, 1);

        assertEquals(emptyLegacyRangeWithStep.hashCode(), emptytRange.hashCode());
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
