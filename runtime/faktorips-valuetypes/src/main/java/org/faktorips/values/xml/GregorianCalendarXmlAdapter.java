/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.values.xml;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Maps a {@link GregorianCalendar} to an ISO date.
 * <p>
 * This adapter can be used, if you are only interested in the date portion of a
 * {@link GregorianCalendar}.
 * 
 * @deprecated for removal since 23.6. Use
 *                 {@code org.faktorips.runtime.jaxb.GregorianCalendarAdapter} instead.
 */
@Deprecated
public class GregorianCalendarXmlAdapter extends XmlAdapter<String, GregorianCalendar> {

    private DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public String marshal(GregorianCalendar v) throws Exception {
        return df.format(v.getTime());
    }

    @Override
    public GregorianCalendar unmarshal(String v) throws Exception {
        GregorianCalendar cal = new GregorianCalendar();
        Date date = df.parse(v);
        cal.setTime(date);

        return cal;
    }

}
