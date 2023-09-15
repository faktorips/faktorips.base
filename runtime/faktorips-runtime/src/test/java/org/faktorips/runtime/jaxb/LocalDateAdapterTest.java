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

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import org.junit.Test;

@SuppressWarnings("deprecation")
public class LocalDateAdapterTest {

    @Test
    public void testUnmarshal_NullString() {
        LocalDateAdapter adapter = new LocalDateAdapter();
        assertThat(adapter.unmarshal(null), is(nullValue()));
    }

    @Test
    public void testUnmarshal_EmptyString() {
        LocalDateAdapter adapter = new LocalDateAdapter();
        assertThat(adapter.unmarshal(""), is(nullValue()));
    }

    @Test
    public void testUnmarshal_BlankString() {
        LocalDateAdapter adapter = new LocalDateAdapter();
        assertThat(adapter.unmarshal("    "), is(nullValue()));
    }

    @Test
    public void testUnmarshal_UnknownFormat() {
        assertThrows(DateTimeParseException.class, () -> {
            LocalDateAdapter adapter = new LocalDateAdapter();

            assertThat(adapter.unmarshal("foo"), is(nullValue()));
        });
    }

    @Test
    public void testUnmarshal_KnownFormat() {
        LocalDateAdapter adapter = new LocalDateAdapter();
        assertThat(adapter.unmarshal("2018-08-31"), is(LocalDate.of(2018, 8, 31)));
        assertThat(adapter.unmarshal("0018-08-31"), is(LocalDate.of(18, 8, 31)));
    }

    @Test
    public void testMarshal_NullValue() {
        LocalDateAdapter adapter = new LocalDateAdapter();
        assertThat(adapter.marshal(null), is(nullValue()));
    }

    @Test
    public void testMarshal() {
        LocalDateAdapter adapter = new LocalDateAdapter();
        assertThat(adapter.marshal(LocalDate.of(2018, 8, 31)), is("2018-08-31"));
        assertThat(adapter.marshal(LocalDate.of(18, 8, 31)), is("0018-08-31"));
    }

}
