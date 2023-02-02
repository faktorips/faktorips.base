/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.xml.jakarta3;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Maps a {@link GregorianCalendar} to an ISO date.
 * <p>
 * This adapter can be used, if you are only interested in the date portion of a
 * {@link GregorianCalendar}.
 */
public class GregorianCalendarXmlAdapter extends XmlAdapter<String, GregorianCalendar> {

    // can't move this to an interface, as it can't have instance fields and DateFormat is not
    // thread-safe
    private final DateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public GregorianCalendar unmarshal(String v) {
        try {
            GregorianCalendar cal = new GregorianCalendar();
            Date date = isoDateFormat.parse(v);
            cal.setTime(date);
            return cal;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String marshal(GregorianCalendar v) {
        return isoDateFormat.format(v.getTime());
    }

}
