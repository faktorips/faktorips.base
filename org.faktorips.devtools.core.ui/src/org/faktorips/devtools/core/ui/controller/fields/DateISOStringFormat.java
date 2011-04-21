/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.controller.fields;

import java.util.Date;

import org.faktorips.values.DateUtil;

/**
 * Format for Date input. Maps a locale specific date string to the ISO date format (also string)
 * and vice versa.
 * <p>
 * If you need a field that maps a locale-specific string to a gregorian calendar use a
 * {@link FormattingTextField} with {@link GregorianCalendarFormat} instead.
 * 
 * @see GregorianCalendarFormat
 * 
 * @author Stefan Widmaier
 */
public class DateISOStringFormat extends AbstractDateFormat<String> {

    public static DateISOStringFormat newInstance() {
        DateISOStringFormat dateISOStringFormat = new DateISOStringFormat();
        dateISOStringFormat.initFormat();
        return dateISOStringFormat;
    }

    protected DateISOStringFormat() {
        // only hide the constructor
    }

    @Override
    protected String mapDateToObject(Date date) {
        return DateUtil.dateToIsoDateString(date);
    }

    @Override
    protected Date mapObjectToDate(String value) {
        return DateUtil.parseIsoDateStringToDate(value);
    }
}
