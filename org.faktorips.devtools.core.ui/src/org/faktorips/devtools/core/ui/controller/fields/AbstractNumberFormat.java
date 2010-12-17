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

import org.eclipse.swt.events.VerifyEvent;

/**
 * Base class or number formats.
 * 
 * @author Stefan Widmaier
 */
public abstract class AbstractNumberFormat extends AbstractInputFormat {

    @Override
    protected Object parseInternal(String stringToBeParsed) {
        ParsePosition position = new ParsePosition(0);
        Object value = getNumberFormat().parse(stringToBeParsed, position);
        if (position.getIndex() == stringToBeParsed.length()) {
            return value.toString();
        } else {
            return null;
        }
    }

    @Override
    protected void verifyInternal(VerifyEvent e, String resultingText) {
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

    protected abstract NumberFormat getNumberFormat();

    protected abstract String getExampleString();

}
