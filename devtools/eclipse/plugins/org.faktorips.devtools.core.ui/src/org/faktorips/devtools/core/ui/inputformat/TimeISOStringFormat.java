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

import java.text.DateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.faktorips.values.DateUtil;

/**
 * Format for time input. Maps a {@link Locale} specific time string to the ISO time format (also
 * string) and vice versa.
 */
public class TimeISOStringFormat extends AbstractDateFormat<String> {

    protected TimeISOStringFormat() {
        // only hide the constructor
    }

    public static TimeISOStringFormat newInstance() {
        TimeISOStringFormat format = new TimeISOStringFormat();
        format.initFormat();
        return format;
    }

    @Override
    protected void initFormat(Locale locale) {
        setDateFormat(DateFormat.getTimeInstance(DateFormat.MEDIUM, locale));
        setExampleString(formatDate(new GregorianCalendar(2001, 6, 4, 15, 30, 45).getTime()));
    }

    @Override
    protected String mapDateToObject(Date date) {
        return DateUtil.dateToIsoTimeString(date);
    }

    @Override
    protected Date mapObjectToDate(String value) {
        return DateUtil.parseIsoTimeStringToDate(value);
    }
}
