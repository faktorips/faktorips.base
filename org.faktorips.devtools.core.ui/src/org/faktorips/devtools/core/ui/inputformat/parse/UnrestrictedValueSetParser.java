/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.inputformat.parse;

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
public class UnrestrictedValueSetParser extends ValueSetParser {

    public UnrestrictedValueSetParser(IValueSetOwner valueSetOwner, IpsUIPlugin uiPlugin) {
        super(valueSetOwner, uiPlugin);
    }

    public String formatUnrestrictedValueSet() {
        return org.faktorips.devtools.core.model.valueset.Messages.ValueSetFormat_unrestricted;
    }

    @Override
    public IValueSet parseValueSet(String stringTobeParsed) {
        final IValueSet valueSet = getValueSet();
        if (valueSet.isUnrestricted()) {
            return valueSet;
        } else {
            UnrestrictedValueSet newValueSet = new UnrestrictedValueSet(getValueSetOwner(),
                    getNextPartIdOfValueSetOwner());
            return newValueSet;
        }
    }

    @Override
    public boolean isResponsibleFor(String stringTobeParsed) {
        return Messages.ValueSetFormat_unrestricted.equals(stringTobeParsed) && isUnrestrictedAllowed();
    }

    public boolean isUnrestrictedAllowed() {
        return isAllowedValueSetType(ValueSetType.UNRESTRICTED);
    }
}
