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

/**
 * Format for integer number input.
 * 
 * @author Stefan Widmaier
 */
public class IntegerFormat extends AbstractInputFormat {

    private NumberFormat numberFormat;

    /**
     * String that is an example of a valid input string.
     */
    private String exampleString;

    @Override
    protected void initFormat(Locale locale) {
        numberFormat = NumberFormat.getIntegerInstance(locale);
        numberFormat.setGroupingUsed(true);
        numberFormat.setParseIntegerOnly(true);
        exampleString = numberFormat.format(-100000000);
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
            e.doit = isParsable(numberFormat, resultingText);
        } else {
            e.doit = containsAllowedCharactersOnly(exampleString, resultingText);
        }
    }

}
