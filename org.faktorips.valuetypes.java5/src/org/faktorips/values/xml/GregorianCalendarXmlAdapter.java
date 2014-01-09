/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
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
 * This adapter can be used, if you are only interested in the date portion of an
 * {@link GregorianCalendar}.
 */
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
