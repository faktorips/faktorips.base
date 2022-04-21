/*******************************************************
 * Copyright (c) Faktor Zehn GmbH - www.faktorzehn.de
 *
 * All Rights Reserved - Alle Rechte vorbehalten.
 *******************************************************/

package org.faktorips.runtime.jaxb;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

import java.time.DateTimeException;
import java.time.Month;

import org.junit.Test;

public class MonthAdapterTest {

    private final MonthAdapter adapter = new MonthAdapter();

    @Test
    public void testMarshal() {
        assertThat(adapter.marshal(Month.MAY), is(5));
    }

    @Test
    public void testUnmarshal() {
        assertThat(adapter.unmarshal(1), is(Month.JANUARY));
    }

    @Test
    public void testUnmarshalWrongString() {
        assertThrows(DateTimeException.class, () -> {
            adapter.unmarshal(13);
        });
    }
}
