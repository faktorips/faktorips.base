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

import org.faktorips.devtools.core.ui.controller.fields.FormattingTextField;
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

    protected DateISOStringFormat() {
        // only hide the constructor
    }

    public static DateISOStringFormat newInstance() {
        DateISOStringFormat dateISOStringFormat = new DateISOStringFormat();
        dateISOStringFormat.initFormat();
        return dateISOStringFormat;
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
