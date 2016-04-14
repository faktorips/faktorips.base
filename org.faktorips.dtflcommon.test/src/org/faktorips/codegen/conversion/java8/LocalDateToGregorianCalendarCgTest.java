package org.faktorips.codegen.conversion.java8;

import static org.junit.Assert.assertEquals;

import org.faktorips.codegen.conversion.AbstractSingleConversionCgTest;
import org.junit.Before;
import org.junit.Test;

public class LocalDateToGregorianCalendarCgTest extends AbstractSingleConversionCgTest {

    private LocalDateToGregorianCalendarCg converter;

    @Before
    public void setUp() throws Exception {
        converter = new LocalDateToGregorianCalendarCg();
    }

    @Test
    public void testGetConversionCode() {
        assertEquals("f == null ? null : GregorianCalendar.from(f.atStartOfDay(ZoneId.systemDefault()))",
                getConversionCode(converter, "f"));
    }

}
