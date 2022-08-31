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

import java.text.DecimalFormat;
import java.text.ParsePosition;

import org.eclipse.swt.events.VerifyEvent;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * Base class or number formats.
 * 
 * @author Stefan Widmaier
 */
public abstract class AbstractNumberFormat extends AbstractInputFormat<String> {

    public AbstractNumberFormat() {
        super(IpsStringUtils.EMPTY, IpsPlugin.getDefault().getIpsPreferences().getDatatypeFormattingLocale());
    }

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
