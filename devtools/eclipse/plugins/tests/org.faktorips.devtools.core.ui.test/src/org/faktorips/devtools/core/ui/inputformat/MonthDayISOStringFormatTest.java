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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MonthDayISOStringFormatTest {

    private MonthDayISOStringFormat monthDayISOStringFormat;
    private static final String DEFAULT_ISO_STRING = "--01-31";
    private static final String GERMAN_DATE = "31.01.";
    private static final String ENGLISH_DATE = "1/31/";
    private static final String NOT_A_DATE = "NO_DATE";

    @BeforeEach
    public void setUp() throws Exception {
        monthDayISOStringFormat = new MonthDayISOStringFormat("defaultNullString", Locale.GERMANY);
        monthDayISOStringFormat.initFormat();
    }

    @Test
    public void testFormatIsoToGermanDate() {
        assertThat(GERMAN_DATE, is(monthDayISOStringFormat.formatInternal(DEFAULT_ISO_STRING)));
    }

    @Test
    public void testParseGermanDateToIso() {
        assertThat(DEFAULT_ISO_STRING, is(monthDayISOStringFormat.parseInternal(GERMAN_DATE)));
    }

    @Test
    public void testFormatIsoToEnglishDate() {
        monthDayISOStringFormat.initFormat(Locale.ENGLISH);
        assertThat(ENGLISH_DATE, is(monthDayISOStringFormat.formatInternal(DEFAULT_ISO_STRING)));
    }

    @Test
    public void testParseEnglishDateToIso() {
        monthDayISOStringFormat.initFormat(Locale.ENGLISH);
        assertThat(DEFAULT_ISO_STRING, is(monthDayISOStringFormat.parseInternal(ENGLISH_DATE)));
    }

    @Test
    public void testParseNotADate() {
        assertThat(NOT_A_DATE, is(monthDayISOStringFormat.parseInternal(NOT_A_DATE)));
    }

    @Test
    public void testFormatNotADate() {
        assertThat(NOT_A_DATE, is(monthDayISOStringFormat.formatInternal(NOT_A_DATE)));
    }
}
