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

import org.faktorips.devtools.core.ui.controller.fields.FormattingTextField;

/**
 * Format for Date input. Maps a locale-specific string to a gregorian calendar and vice versa.
 * <p>
 * If you need a field that maps a locale specific date string to the ISO date format (also string)
 * use a {@link FormattingTextField} with {@link DateISOStringFormat} instead.
 * 
 * @see DateISOStringFormat
 * 
 * @author Stefan Widmaier
 */
public class GregorianCalendarFormat extends AbstractDateFormat<GregorianCalendar> {

    private GregorianCalendarFormat() {
        // only hide the constructor
    }

    public static GregorianCalendarFormat newInstance() {
        GregorianCalendarFormat format = new GregorianCalendarFormat();
        format.initFormat();
        return format;
    }

    @Override
    protected GregorianCalendar mapDateToObject(Date date) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        return calendar;
    }

    @Override
    protected Date mapObjectToDate(GregorianCalendar value) {
        return value.getTime();
    }

}
