/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.inputformat.parse;

import org.apache.commons.lang.StringUtils;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.model.internal.valueset.StringLengthValueSet;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.devtools.model.valueset.IValueSetOwner;

/**
 * Formats and parses the contents of a {@link StringLengthValueSet}
 * 
 */
public class StringLengthValueSetFormat extends AbstractValueSetFormat {

    private static final String UNLIMITED = Messages.StringLengthValueSetFormat_Unlimited;
    private static final String STRINGLENGTH_INDICATOR = Messages.StringLengthValueSetFormat_ParseIndicator;
    private static final String REGEX_NON_NUMERIC = "[^0-9]"; //$NON-NLS-1$

    public StringLengthValueSetFormat(IValueSetOwner valueSetOwner, IpsUIPlugin uiPlugin) {
        super(valueSetOwner, uiPlugin);
    }

    @Override
    protected String getNullPresentationInValueSet() {
        return UNLIMITED;
    }

    @Override
    public boolean isResponsibleFor(String stringToBeParsed) {
        return stringToBeParsed.startsWith(STRINGLENGTH_INDICATOR);
    }

    @Override
    protected IValueSet parseInternal(String stringToBeparsed) {
        if (stringToBeparsed.isEmpty()) {
            return new StringLengthValueSet(getValueSetOwner(), getNextPartId(), null, true);
        }
        return parseNonEmpty(stringToBeparsed);
    }

    private IValueSet parseNonEmpty(String stringToBeParsed) {
        boolean containsNull = stringToBeParsed.endsWith(getNullSuffix());
        String max = stringToBeParsed.contains(UNLIMITED) ? null
                : stringToBeParsed.replaceAll(REGEX_NON_NUMERIC, StringUtils.EMPTY);
        return new StringLengthValueSet(getValueSetOwner(), getNextPartId(), max, containsNull);
    }

    private String getNullSuffix() {
        return NLS.bind(Messages.RangeValueSetFormat_includingNull,
                IpsPlugin.getDefault().getIpsPreferences().getNullPresentation());
    }

    @Override
    public String formatInternal(IValueSet value) {
        if (value.isStringLength()) {
            return value.toShortString();
        }
        return StringUtils.EMPTY;
    }

}
