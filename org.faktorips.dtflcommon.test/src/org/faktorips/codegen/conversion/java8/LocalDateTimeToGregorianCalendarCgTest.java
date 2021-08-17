/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.codegen.conversion.java8;

import static org.junit.Assert.assertEquals;

import org.faktorips.codegen.conversion.AbstractSingleConversionCgTest;
import org.junit.Before;
import org.junit.Test;

public class LocalDateTimeToGregorianCalendarCgTest extends AbstractSingleConversionCgTest {

    private LocalDateTimeToGregorianCalendarCg converter;

    @Before
    public void setUp() throws Exception {
        converter = new LocalDateTimeToGregorianCalendarCg();
    }

    @Test
    public void testGetConversionCode() {
        assertEquals("f == null ? null : GregorianCalendar.from(f.atZone(ZoneId.systemDefault()))",
                getConversionCode(converter, "f"));
    }
}
