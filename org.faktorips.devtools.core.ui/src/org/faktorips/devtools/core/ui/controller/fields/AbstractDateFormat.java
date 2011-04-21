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

import java.text.DateFormat;
import java.text.ParsePosition;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.eclipse.swt.events.VerifyEvent;
import org.faktorips.devtools.core.IpsPlugin;

/**
 * Base class for data type format date/gregorian calendar.
 * 
 * @author Stefan Widmaier
 */
public abstract class AbstractDateFormat<T> extends AbstractInputFormat<T> {

    /**
     * {@link AbstractDateFormat} used internally by this {@link AbstractInputFormat} to validate
     * given date strings.
     */
    protected DateFormat dateFormat;

    /**
     * String that is an example of a valid input string.
     */
    private String exampleString;

    protected AbstractDateFormat() {
        super();
    }

    @Override
    protected void initFormat(Locale locale) {
        dateFormat = IpsPlugin.getDefault().getIpsPreferences().getDateFormat(locale);
        exampleString = formatDate(new GregorianCalendar(2001, 6, 4).getTime());
    }

    @Override
    protected T parseInternal(String stringToBeparsed) {
        Date date = parseToDate(stringToBeparsed);
        if (date != null) {
            return mapDateToObject(date);
        } else {
            return null;
        }
    }

    /**
     * Maps a {@link Date} to another object.
     * <p>
     * When {@link #parseInternal(String)} is called this method is used to convert the parsed
     * {@link Date} object to another specific object as expected by the model.
     * 
     * @param date the date to be converted to another object
     * @return an object representing the given date
     */
    protected abstract T mapDateToObject(Date date);

    /**
     * Accepts a {@link GregorianCalendar} as well as an ISO date String as value.
     */
    @Override
    protected String formatInternal(T value) {
        if (value != null) {
            return formatDate(mapObjectToDate(value));
        } else {
            return null;
        }
    }

    /**
     * Maps a value object (as provided by the model) to a {@link Date} object.
     * 
     * @param value a non-<code>null</code> object
     * @return the Date represented by the given value object
     */
    protected abstract Date mapObjectToDate(T value);

    /**
     * Converts a {@link Date} object to a string ad it is displayed in a
     * {@link FormattingTextField} configured with an {@link AbstractDateFormat}.
     * 
     * @param date the date to be represented as string
     * @return the string that represents the given date, or <code>null</code> if the given
     *         {@link Date} is <code>null</code>.
     */
    public String formatDate(Date date) {
        if (date != null) {
            String formattedString = getDateFormat().format(date);
            return formattedString;
        } else {
            return null;
        }
    }

    /**
     * Converts a string (in date format) to a {@link Date} object.
     * 
     * @param dateFormatString a string that can be parsed by this format
     * @return a {@link Date} object or <code>null</code> if the given string is <code>null</code>
     *         or cannot be parsed.
     */
    public Date parseToDate(String dateFormatString) {
        ParsePosition pos = new ParsePosition(0);
        Date date = getDateFormat().parse(dateFormatString, pos);

        if (date != null && pos.getIndex() == dateFormatString.length()) {
            return date;
        } else {
            return null;
        }
    }

    @Override
    protected void verifyInternal(VerifyEvent e, String resultingText) {
        // if (resultingText.length() > 9) {
        // e.doit = isParsable(dateFormat, resultingText);
        // } else {
        e.doit = containsAllowedCharactersOnly(exampleString, resultingText);
        // }
    }

    protected DateFormat getDateFormat() {
        return dateFormat;
    }

}