package org.faktorips.valueset;

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
    public void testEquals_LegacyAndNewEmpty() {
        TestRange emptytRange = new TestRange();
        TestRange emptyLegacyRangeWithStep = new TestRange(10, 0, 1);

        assertEquals(emptyLegacyRangeWithStep, emptytRange);
    }

    @Test
    public void testhashCode_LegacyAndNewEmpty() {
        TestRange emptytRange = new TestRange();
        TestRange emptyLegacyRangeWithStep = new TestRange(10, 0, 1);

        assertEquals(emptyLegacyRangeWithStep.hashCode(), emptytRange.hashCode());
    }

    private class TestRange extends DefaultRange<Integer> {

        private static final long serialVersionUID = 3385179851895588865L;

        public TestRange() {
            super();
        }

        public TestRange(Integer lower, Integer upper, Integer step) {
            super(lower, upper, step, false);
        }

        @Override
        protected boolean checkIfValueCompliesToStepIncrement(Integer value, Integer bound) {
            return true;
        }
    }
}
