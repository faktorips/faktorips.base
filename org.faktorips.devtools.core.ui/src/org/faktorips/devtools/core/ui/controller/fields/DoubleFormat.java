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

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Locale;

import org.eclipse.swt.events.VerifyEvent;

public class DoubleFormat extends Format {

    private NumberFormat numberFormat;

    @Override
    protected void initFormat(Locale locale) {
        numberFormat = DecimalFormat.getNumberInstance(locale);
        numberFormat.setGroupingUsed(false);
        numberFormat.setParseIntegerOnly(false);
        numberFormat.setMaximumFractionDigits(3);
        numberFormat.setRoundingMode(RoundingMode.HALF_UP);
    }

    @Override
    protected Object parseInternal(String stringToBeParsed) {
        ParsePosition position = new ParsePosition(0);
        Object value = numberFormat.parse(stringToBeParsed, position);
        if (value != null && position.getIndex() == stringToBeParsed.length()) {
            // System.out.println("String \"" + stringToBeParsed + "\" was parsed to value \"" +
            // value.toString() + "\"");
            return value.toString();
        } else {
            // System.out.println("String \"" + stringToBeParsed +
            // "\" could not be parsed to a value. Returning null");
            return null;
        }
    }

    @Override
    protected String formatInternal(Object value) {
        Double d = new Double((String)value);
        // System.out.print("Value \"" + d + "\" is being parsed...");
        String stringToBeDisplayed = numberFormat.format(d);
        // System.out.println("Value \"" + stringToBeDisplayed + "\" will be displayed");
        return stringToBeDisplayed;
    }

    @Override
    protected void verifyInternal(VerifyEvent e, String resultingText) {
        if (resultingText.length() > 2) {
            e.doit = isParsable(resultingText);
        } else {
            e.doit = isParsableRegEx(resultingText);
            // Fuer alle Datentypen Locale spezifisch machen! Wie?
        }
        // System.out.println("Value \"" + resultingText + "\" is " + (e.doit ? "ok" : "invalid"));
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
        boolean isParsable = position.getIndex() == resultingText.length();
        return isParsable;
    }

    private boolean isParsableRegEx(String resultingText) {
        return resultingText.matches("-?[0-9]*[\\.,]?[0-9]*"); //$NON-NLS-1$
    }

}
