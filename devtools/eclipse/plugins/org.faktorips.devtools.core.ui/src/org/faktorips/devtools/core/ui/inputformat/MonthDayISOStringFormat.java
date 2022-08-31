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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.eclipse.swt.events.VerifyEvent;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * Format for time input. Maps a {@link Locale} specific time string to the ISO time format (also
 * string) and vice versa.
 */
public class MonthDayISOStringFormat extends AbstractInputFormat<String> {

    private SimpleDateFormat localFormat = null;
    private final SimpleDateFormat isoMonthDayFormat = new SimpleDateFormat("--MM-dd"); //$NON-NLS-1$

    protected MonthDayISOStringFormat(String defaultNullString, Locale locale) {
        super(defaultNullString, locale);
        initFormat(locale);
    }

    public static MonthDayISOStringFormat newInstance() {
        return new MonthDayISOStringFormat(IpsStringUtils.EMPTY, IpsPlugin.getDefault()
                .getIpsPreferences().getDatatypeFormattingLocale());
    }

    @Override
    protected String parseInternal(String stringToBeParsed) {
        try {
            Date date = localFormat.parse(stringToBeParsed);
            return isoMonthDayFormat.format(date);
        } catch (IllegalArgumentException | ParseException e) {
            return stringToBeParsed;
        }
    }

    @Override
    protected String formatInternal(String stringToBeFormatted) {
        try {
            Date date = isoMonthDayFormat.parse(stringToBeFormatted);
            return localFormat.format(date);
        } catch (ParseException e) {
            return stringToBeFormatted;
        }
    }

    @Override
    protected void verifyInternal(VerifyEvent e, String resultingText) {
        // nothing to do
    }

    @Override
    protected void initFormat(Locale locale) {
        String pattern = ((SimpleDateFormat)SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT, locale))
                .toPattern();
        String monthDayPattern = pattern.replaceAll("[yY]*", IpsStringUtils.EMPTY); //$NON-NLS-1$
        localFormat = new SimpleDateFormat(monthDayPattern, locale);
    }
}
