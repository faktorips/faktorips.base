/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.inputformat;

import java.util.Locale;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MonthDayISOStringFormatTest {

    private MonthDayISOStringFormat monthDayISOStringFormat;
    private static final String DEFAULT_ISO_STRING = "--01-31";
    private static final String GERMAN_DATE = "31.01.";
    private static final String ENGLISH_DATE = "1/31/";
    private static final String NOT_A_DATE = "NO_DATE";

    @Before
    public void setUp() throws Exception {
        monthDayISOStringFormat = new MonthDayISOStringFormat("defaultNullString", Locale.GERMANY);
        monthDayISOStringFormat.initFormat();
    }

    @Test
    public void testFormatIsoToGermanDate() {
        Assert.assertEquals(GERMAN_DATE, monthDayISOStringFormat.formatInternal(DEFAULT_ISO_STRING));
    }

    @Test
    public void testParseGermanDateToIso() {
        Assert.assertEquals(DEFAULT_ISO_STRING, monthDayISOStringFormat.parseInternal(GERMAN_DATE));
    }

    @Test
    public void testFormatIsoToEnglishDate() {
        monthDayISOStringFormat.initFormat(Locale.ENGLISH);
        Assert.assertEquals(ENGLISH_DATE, monthDayISOStringFormat.formatInternal(DEFAULT_ISO_STRING));
    }

    @Test
    public void testParseEnglishDateToIso() {
        monthDayISOStringFormat.initFormat(Locale.ENGLISH);
        Assert.assertEquals(DEFAULT_ISO_STRING, monthDayISOStringFormat.parseInternal(ENGLISH_DATE));
    }

    @Test
    public void testParseNotADate() {
        Assert.assertEquals(NOT_A_DATE, monthDayISOStringFormat.parseInternal(NOT_A_DATE));
    }

    @Test
    public void testFormatNotADate() {
        Assert.assertEquals(NOT_A_DATE, monthDayISOStringFormat.formatInternal(NOT_A_DATE));
    }
}
