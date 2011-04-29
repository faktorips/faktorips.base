/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
