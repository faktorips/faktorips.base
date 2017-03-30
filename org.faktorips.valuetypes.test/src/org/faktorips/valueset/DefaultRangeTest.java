package org.faktorips.valueset;

import org.junit.Assert;
import org.junit.Test;

public class DefaultRangeTest {

    @Test
    public void testIsDiscrete() {
        TestRange rangeWithStep = new TestRange(0, 10, 1);
        Assert.assertTrue(rangeWithStep.isDiscrete());

        TestRange rangeWithoutStep = new TestRange(0, 10, null);
        Assert.assertFalse(rangeWithoutStep.isDiscrete());
    }

    @Test
    public void testIsDiscrete_empty() {
        TestRange emptyRangeWithStep = new TestRange(10, 0, 1);
        Assert.assertTrue(emptyRangeWithStep.isDiscrete());

        TestRange emptytRangeWithoutStep = new TestRange(10, 0, null);
        Assert.assertTrue(emptytRangeWithoutStep.isDiscrete());
    }

    @Test
    public void testIsDiscrete_size1() {
        TestRange rangeWithStep = new TestRange(0, 0, 1);
        Assert.assertTrue(rangeWithStep.isDiscrete());

        TestRange rangeWithoutStep = new TestRange(0, 0, null);
        Assert.assertTrue(rangeWithoutStep.isDiscrete());
    }

    private class TestRange extends DefaultRange<Integer> {

        /**
         * Comment for <code>serialVersionUID</code>
         */
        private static final long serialVersionUID = 3385179851895588865L;

        public TestRange(Integer lower, Integer upper, Integer step) {
            super(lower, upper, step, false);
        }

        @Override
        protected boolean checkIfValueCompliesToStepIncrement(Integer value, Integer bound) {
            return true;
        }
    }
}
