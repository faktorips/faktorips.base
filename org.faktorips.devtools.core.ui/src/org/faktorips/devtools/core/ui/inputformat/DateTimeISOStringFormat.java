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

    public static DateTimeISOStringFormat newInstance() {
        DateTimeISOStringFormat dateISOStringFormat = new DateTimeISOStringFormat();
        dateISOStringFormat.initFormat();
        return dateISOStringFormat;
    }

    protected DateTimeISOStringFormat() {
        // only hide the constructor
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
