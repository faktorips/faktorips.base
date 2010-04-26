/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.datatype.classtypes;

import java.util.GregorianCalendar;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.faktorips.datatype.ValueClassDatatype;
import org.faktorips.values.DateUtil;

/**
 *
 */
public class GregorianCalendarDatatype extends ValueClassDatatype {

    private boolean timeInfoIncluded;

    public GregorianCalendarDatatype(String name, boolean timeInfoIncluded) {
        super(GregorianCalendar.class, name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMutable() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue(String value) {
        if (timeInfoIncluded) {
            throw new RuntimeException("Not implemented yet");
        }
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        if (!DateUtil.isIsoDate(value)) {
            throw new IllegalArgumentException("Date value must have the format YYYY-MM-DD");
        }
        try {
            StringTokenizer tokenizer = new StringTokenizer(value, "-");
            int year = Integer.parseInt(tokenizer.nextToken());
            int month = Integer.parseInt(tokenizer.nextToken());
            int date = Integer.parseInt(tokenizer.nextToken());
            return new GregorianCalendar(year, month - 1, date);
        } catch (Exception e) {
            throw new IllegalArgumentException("Can't parse " + value + " to a date!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isParsable(String value) {
        return StringUtils.isEmpty(value) || DateUtil.isIsoDate(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String valueToString(Object value) {
        if (timeInfoIncluded) {
            throw new RuntimeException("Not implemented yet");
        }
        GregorianCalendar calendar = (GregorianCalendar)value;
        if (calendar == null) {
            return "";
        }
        int month = calendar.get(GregorianCalendar.MONTH) + 1;
        int date = calendar.get(GregorianCalendar.DATE);
        return calendar.get(GregorianCalendar.YEAR) + "-" + (month < 10 ? "0" + month : "" + month) + "-"
                + (date < 10 ? "0" + date : "" + date);

    }

    /**
     * {@inheritDoc}
     */
    public boolean supportsCompare() {
        return true;
    }
}
