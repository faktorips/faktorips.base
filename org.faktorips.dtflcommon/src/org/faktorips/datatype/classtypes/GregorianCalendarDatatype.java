/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.datatype.classtypes;

import java.util.GregorianCalendar;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.faktorips.datatype.ValueClassDatatype;

/**
 *
 */
public class GregorianCalendarDatatype extends ValueClassDatatype {
    
    private boolean timeInfoIncluded;

    public GregorianCalendarDatatype(String name, boolean timeInfoIncluded) {
        super(GregorianCalendar.class, name);
    }

    /** 
     * Overridden method.
     * @see org.faktorips.datatype.ValueDatatype#getValue(java.lang.String)
     */
    public Object getValue(String value) {
        if (timeInfoIncluded) {
            throw new RuntimeException("Not implemented yet");
        }
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        try {
            StringTokenizer tokenizer = new StringTokenizer(value, "-");
            int year = Integer.parseInt(tokenizer.nextToken());
            int month = Integer.parseInt(tokenizer.nextToken());
            int date = Integer.parseInt(tokenizer.nextToken());
            return new GregorianCalendar(year, month-1, date);
        } catch (Exception e) {
            throw new IllegalArgumentException("Can't parse " + value + " to a date!");
        }
    }

    /**
     * Overridden method.
     * @see org.faktorips.datatype.ValueDatatype#valueToString(java.lang.Object)
     */
	public String valueToString(Object value) {
        if (timeInfoIncluded) {
            throw new RuntimeException("Not implemented yet");
        }
		GregorianCalendar calendar = (GregorianCalendar)value;
        if (calendar==null) {
            return "";
        }
        int month = calendar.get(GregorianCalendar.MONTH) + 1;
        int date = calendar.get(GregorianCalendar.DATE);
        return calendar.get(GregorianCalendar.YEAR)
        	+ "-" + (month<10?"0"+month:""+month)
        	+ "-" + (date<10?"0"+date:""+date);
		
	}

    /**
     * {@inheritDoc}
     */
    public boolean supportsCompare() {
        return true;
    }
}
