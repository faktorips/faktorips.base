package org.faktorips.valueset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class StringLengthValueSetTest {

    @Test
    public void testDefaultConstructor() {
        StringLengthValueSet sl = new StringLengthValueSet();

        assertEquals(null, sl.getMaximumLength());
        assertEquals(true, sl.containsNull());
    }

    @Test
    public void testContains() {
        StringLengthValueSet sl = new StringLengthValueSet(10, true);

        assertTrue(sl.contains("within"));
        assertFalse(sl.contains("tooLongForLimitOf10"));
    }

    @Test
    public void testContainsNull() {
        StringLengthValueSet sl1 = new StringLengthValueSet(10, true);
        StringLengthValueSet sl2 = new StringLengthValueSet(10, false);

        assertTrue(sl1.containsNull());
        assertTrue(sl1.contains(null));
        assertFalse(sl2.containsNull());
        assertFalse(sl2.contains(null));
    }

    @Test
    public void testIsEmpty() {
        StringLengthValueSet sl1 = new StringLengthValueSet(0, false);
        StringLengthValueSet sl2 = new StringLengthValueSet(10, false);
        StringLengthValueSet sl3 = new StringLengthValueSet(0, true);

        assertTrue(sl1.isEmpty());
        assertFalse(sl2.isEmpty());
        assertFalse(sl3.isEmpty());
    }

}
