/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - www.faktorzehn.de
 * 
 * All Rights Reserved - Alle Rechte vorbehalten.
 *******************************************************************************/

package org.faktorips.runtime.xml.javax;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;

import org.junit.Test;

public class LocalTimeAdapterTest {

    @Test
    public void testUnmarshal_NullString() {
        LocalTimeAdapter adapter = new LocalTimeAdapter();
        assertThat(adapter.unmarshal(null), is(nullValue()));
    }

    @Test
    public void testUnmarshal_EmptyString() {
        LocalTimeAdapter adapter = new LocalTimeAdapter();
        assertThat(adapter.unmarshal(""), is(nullValue()));
    }

    @Test
    public void testUnmarshal_BlankString() {
        LocalTimeAdapter adapter = new LocalTimeAdapter();
        assertThat(adapter.unmarshal("    "), is(nullValue()));
    }

    @Test
    public void testUnmarshal_UnknownFormat() {
        assertThrows(DateTimeParseException.class, () -> {
            LocalTimeAdapter adapter = new LocalTimeAdapter();

            assertThat(adapter.unmarshal("foo"), is(nullValue()));
        });
    }

    @Test
    public void testUnmarshal_KnownFormat() {
        LocalTimeAdapter adapter = new LocalTimeAdapter();
        assertThat(adapter.unmarshal("13:45"), is(LocalTime.of(13, 45)));
        assertThat(adapter.unmarshal("13:45:01"), is(LocalTime.of(13, 45, 1)));
        assertThat(adapter.unmarshal("09:00:30"), is(LocalTime.of(9, 0, 30)));
        assertThat(adapter.unmarshal("12:41:01:123"), is(LocalTime.of(12, 41, 1)));
    }

    @Test
    public void testMarshal_NullValue() {
        LocalTimeAdapter adapter = new LocalTimeAdapter();
        assertThat(adapter.marshal(null), is(nullValue()));
    }

    @Test
    public void testMarshal() {
        LocalTimeAdapter adapter = new LocalTimeAdapter();
        assertThat(adapter.marshal(LocalTime.of(13, 30)), is("13:30"));
        assertThat(adapter.marshal(LocalTime.of(13, 30, 1)), is("13:30:01"));
    }

}
