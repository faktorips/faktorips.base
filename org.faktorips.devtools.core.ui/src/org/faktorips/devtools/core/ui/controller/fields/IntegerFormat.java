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

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Locale;

import org.eclipse.swt.events.VerifyEvent;

public class IntegerFormat extends Format {

    private NumberFormat numberFormat;

    @Override
    protected void initFormat(Locale locale) {
        numberFormat = NumberFormat.getIntegerInstance(locale);
        numberFormat.setGroupingUsed(false);
        numberFormat.setParseIntegerOnly(true);
    }

    @Override
    protected Object parseInternal(String stringToBeParsed) {
        ParsePosition position = new ParsePosition(0);
        Object value = numberFormat.parse(stringToBeParsed, position);
        if (position.getIndex() == stringToBeParsed.length()) {
            return value.toString();
        } else {
            return null;
        }
    }

    @Override
    protected String formatInternal(Object value) {
        Integer integer = new Integer((String)value);
        return numberFormat.format(integer);
    }

    @Override
    protected void verifyInternal(VerifyEvent e, String resultingText) {
        if (resultingText.length() > 2) {
            e.doit = isParsable(resultingText);
        } else {
            e.doit = isParsableRegEx(resultingText);
            // Fuer alle Datentypen Locale spezifisch machen! Wie?
        }
    }

    /**
     * Returns <code>true</code> if the entire String was parsed to a number, else
     * <code>false</code>.
     * 
     * @param resultingText the string to be parsed
     * @return <code>true</code> if the entire String could be parsed to a number
     */
    private boolean isParsable(String resultingText) {
        ParsePosition position = new ParsePosition(0);
        numberFormat.parse(resultingText, position);
        // System.out.println("ResultinText.length()=" + resultingText.length() + ", position=" +
        // position.getIndex());
        return position.getIndex() == resultingText.length();
    }

    private boolean isParsableRegEx(String resultingText) {
        return resultingText.matches("-?[0-9]*"); //$NON-NLS-1$
    }

}
