/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.codegen.conversion.joda;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.faktorips.codegen.conversion.AbstractSingleConversionCgTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LocalDateToGregorianCalendarCgTest extends AbstractSingleConversionCgTest {

    private LocalDateToGregorianCalendarCg converter;

    @BeforeEach
    public void setUp() throws Exception {
        converter = new LocalDateToGregorianCalendarCg();
    }

    @Test
    public void testGetConversionCode() throws Exception {
        assertEquals("JodaUtil.toGregorianCalendar(calender)", getConversionCode(converter, "calender")); //$NON-NLS-1$ //$NON-NLS-2$
    }
}
