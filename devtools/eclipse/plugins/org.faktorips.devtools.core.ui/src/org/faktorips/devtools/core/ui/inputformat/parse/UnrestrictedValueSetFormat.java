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

import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.model.internal.valueset.UnrestrictedValueSet;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.devtools.model.valueset.IValueSetOwner;
import org.faktorips.devtools.model.valueset.Messages;
import org.faktorips.devtools.model.valueset.ValueSetType;
import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * Class to parse and format an {@link UnrestrictedValueSet}.
 * 
 */
public class UnrestrictedValueSetFormat extends AbstractValueSetFormat {

    public UnrestrictedValueSetFormat(IValueSetOwner valueSetOwner, IpsUIPlugin uiPlugin) {
        super(valueSetOwner, uiPlugin);
    }

    @Override
    protected String getNullPresentationInValueSet() {
        return IpsStringUtils.EMPTY;
    }

    @Override
    protected IValueSet parseInternal(String stringToBeparsed) {
        final IValueSet valueSet = getValueSet();
        if (valueSet.isUnrestricted()) {
            return valueSet;
        } else {
            return new UnrestrictedValueSet(getValueSetOwner(), getNextPartId());
        }
    }

    @Override
    public String formatInternal(IValueSet value) {
        return value.toShortString();
    }

    @Override
    public boolean isResponsibleFor(String stringToBeParsed) {
        return ((isUnrestrictedText(stringToBeParsed)) && isUnrestrictedAllowed())
                || isOnlyAllowedValueSetType(ValueSetType.UNRESTRICTED);
    }

    private boolean isUnrestrictedText(String stringToBeParsed) {
        return Messages.ValueSetFormat_unrestricted.equals(stringToBeParsed)
                || Messages.ValueSet_unrestrictedWithoutNull.equals(stringToBeParsed);
    }

    private boolean isUnrestrictedAllowed() {
        return isAllowedValueSetType(ValueSetType.UNRESTRICTED);
    }
}
