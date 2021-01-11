/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.inputformat.parse;

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.core.internal.model.valueset.UnrestrictedValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSetOwner;
import org.faktorips.devtools.core.model.valueset.Messages;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

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
        return StringUtils.EMPTY;
    }

    @Override
    protected IValueSet parseInternal(String stringToBeparsed) {
        final IValueSet valueSet = getValueSet();
        if (valueSet.isUnrestricted()) {
            return valueSet;
        } else {
            UnrestrictedValueSet newValueSet = new UnrestrictedValueSet(getValueSetOwner(), getNextPartId());
            return newValueSet;
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
