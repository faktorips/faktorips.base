/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - www.faktorzehn.de
 * 
 * All Rights Reserved - Alle Rechte vorbehalten.
 *******************************************************************************/

package org.faktorips.runtime.jaxb;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import org.junit.Test;

public class LocalDateTimeAdapterTest {

    @Test
    public void testUnmarshal_NullString() {
        LocalDateTimeAdapter adapter = new LocalDateTimeAdapter();
        assertThat(adapter.unmarshal(null), is(nullValue()));
    }

    @Test
    public void testUnmarshal_EmptyString() {
        LocalDateTimeAdapter adapter = new LocalDateTimeAdapter();
        assertThat(adapter.unmarshal(""), is(nullValue()));
    }

    @Test
    public void testUnmarshal_BlankString() {
        LocalDateTimeAdapter adapter = new LocalDateTimeAdapter();
        assertThat(adapter.unmarshal("    "), is(nullValue()));
    }

    @Test
    public void testUnmarshal_UnknownFormat() {
        assertThrows(DateTimeParseException.class, () -> {
            LocalDateTimeAdapter adapter = new LocalDateTimeAdapter();

            assertThat(adapter.unmarshal("foo"), is(nullValue()));
        });
    }

    @Test
    public void testUnmarshal_KnownFormat() {
        LocalDateTimeAdapter adapter = new LocalDateTimeAdapter();
        assertThat(adapter.unmarshal("2018-08-31T13:45"), is(LocalDateTime.of(2018, 8, 31, 13, 45)));
        assertThat(adapter.unmarshal("2018-08-31T13:45:01"), is(LocalDateTime.of(2018, 8, 31, 13, 45, 1)));
        assertThat(adapter.unmarshal("0018-08-31T09:00:30"), is(LocalDateTime.of(18, 8, 31, 9, 0, 30)));
        assertThat(adapter.unmarshal("2022-04-21T12:41:01:123"), is(LocalDateTime.of(2022, 4, 21, 12, 41, 1)));
    }

    @Test
    public void testMarshal_NullValue() {
        LocalDateTimeAdapter adapter = new LocalDateTimeAdapter();
        assertThat(adapter.marshal(null), is(nullValue()));
    }

    @Test
    public void testMarshal() {
        LocalDateTimeAdapter adapter = new LocalDateTimeAdapter();
        assertThat(adapter.marshal(LocalDateTime.of(2018, 8, 31, 13, 30)), is("2018-08-31T13:30"));
        assertThat(adapter.marshal(LocalDateTime.of(18, 8, 31, 13, 30, 1)), is("0018-08-31T13:30:01"));
    }

}
