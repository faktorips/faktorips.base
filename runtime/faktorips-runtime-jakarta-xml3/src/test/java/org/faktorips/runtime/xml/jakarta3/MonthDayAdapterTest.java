/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - www.faktorzehn.de
 * 
 * All Rights Reserved - Alle Rechte vorbehalten.
 *******************************************************************************/

package org.faktorips.runtime.xml.jakarta3;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

import java.time.MonthDay;
import java.time.format.DateTimeParseException;

import org.junit.Test;

public class MonthDayAdapterTest {

    @Test
    public void testUnmarshal_NullString() {
        MonthDayAdapter adapter = new MonthDayAdapter();
        assertThat(adapter.unmarshal(null), is(nullValue()));
    }

    @Test
    public void testUnmarshal_EmptyString() {
        MonthDayAdapter adapter = new MonthDayAdapter();
        assertThat(adapter.unmarshal(""), is(nullValue()));
    }

    @Test
    public void testUnmarshal_BlankString() {
        MonthDayAdapter adapter = new MonthDayAdapter();
        assertThat(adapter.unmarshal("    "), is(nullValue()));
    }

    @Test
    public void testUnmarshal_UnknownFormat() {
        assertThrows(DateTimeParseException.class, () -> {
            MonthDayAdapter adapter = new MonthDayAdapter();

            assertThat(adapter.unmarshal("foo"), is(nullValue()));
        });
    }

    @Test
    public void testUnmarshal_KnownFormat() {
        MonthDayAdapter adapter = new MonthDayAdapter();
        assertThat(adapter.unmarshal("--08-31"), is(MonthDay.of(8, 31)));
    }

    @Test
    public void testMarshal_NullValue() {
        MonthDayAdapter adapter = new MonthDayAdapter();
        assertThat(adapter.marshal(null), is(nullValue()));
    }

    @Test
    public void testMarshal() {
        MonthDayAdapter adapter = new MonthDayAdapter();
        assertThat(adapter.marshal(MonthDay.of(8, 31)), is("--08-31"));
    }

}
