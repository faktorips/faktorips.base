/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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

    public static TimeISOStringFormat newInstance() {
        TimeISOStringFormat format = new TimeISOStringFormat();
        format.initFormat();
        return format;
    }

    protected TimeISOStringFormat() {
        // only hide the constructor
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
