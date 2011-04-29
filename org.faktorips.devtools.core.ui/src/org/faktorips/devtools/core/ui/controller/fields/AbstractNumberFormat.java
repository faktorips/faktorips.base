/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

import java.text.DecimalFormat;
import java.text.ParsePosition;

import org.eclipse.swt.events.VerifyEvent;

/**
 * Base class or number formats.
 * 
 * @author Stefan Widmaier
 */
public abstract class AbstractNumberFormat extends AbstractInputFormat<String> {

    @Override
    protected String parseInternal(String stringToBeParsed) {
        if (stringToBeParsed.isEmpty()) {
            // this is important to show null representation when the text field is empty
            return stringToBeParsed;
        }
        ParsePosition position = new ParsePosition(0);
        Object value = getNumberFormat().parse(stringToBeParsed, position);
        if (position.getIndex() == stringToBeParsed.length() && value != null) {
            return value.toString();
        } else {
            return stringToBeParsed;
        }
    }

    @Override
    protected void verifyInternal(VerifyEvent e, String resultingText) {
        // FIPS-453 do not allow inserting group separator
        if (e.text.equals("" + getNumberFormat().getDecimalFormatSymbols().getGroupingSeparator())) { //$NON-NLS-1$
            e.doit = false;
            return;
        }

        if (resultingText.length() > 2) {
            /*
             * Allow valid numbers as well as numbers with a grouping separator at the end (though
             * not after the decimal separator) so they can be entered in order.
             */
            e.doit = isParsable(getNumberFormat(), resultingText) || isParsable(getNumberFormat(), resultingText + "0"); //$NON-NLS-1$
        } else {
            e.doit = containsAllowedCharactersOnly(getExampleString(), resultingText);
        }
    }

    public abstract DecimalFormat getNumberFormat();

    protected abstract String getExampleString();

}
