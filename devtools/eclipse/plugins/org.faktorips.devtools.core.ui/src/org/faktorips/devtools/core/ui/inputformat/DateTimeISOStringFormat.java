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

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.values.DateUtil;

/**
 * Format for date and time input. Maps a {@link Locale} specific date/time string to the ISO
 * date/time format (also string) and vice versa.
 */
public class DateTimeISOStringFormat extends AbstractDateFormat<String> {

    protected DateTimeISOStringFormat() {
        // only hide the constructor
    }

    public static DateTimeISOStringFormat newInstance() {
        DateTimeISOStringFormat dateISOStringFormat = new DateTimeISOStringFormat();
        dateISOStringFormat.initFormat();
        return dateISOStringFormat;
    }

    @Override
    protected void initFormat(Locale locale) {
        setDateFormat(IpsPlugin.getDefault().getIpsPreferences().getDateTimeFormat(locale));
        setExampleString(formatDate(new GregorianCalendar(2001, 6, 4, 15, 30, 45).getTime()));
    }

    @Override
    protected String mapDateToObject(Date date) {
        return DateUtil.dateToIsoDateTimeString(date);
    }

    @Override
    protected Date mapObjectToDate(String value) {
        return DateUtil.parseIsoDateTimeStringToDate(value);
    }
}
