package org.faktorips.datatype.joda;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MonthDayDatatypeTest {

    @Test
    public void testIsParsable() {
        MonthDayDatatype d = new MonthDayDatatype();

        assertTrue(d.isParsable(null));
        assertTrue(d.isParsable(""));

        assertFalse(d.isParsable("bla"));

        assertFalse(d.isParsable("15.04."));
        assertFalse(d.isParsable("04/15"));

        assertTrue(d.isParsable("--04-15"));
    }

}
