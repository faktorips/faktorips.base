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

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.core.internal.model.valueset.EnumValueSet;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSetOwner;
import org.faktorips.devtools.core.ui.inputformat.IInputFormat;

public class EnumValueSetParser extends ValueSetParser {

    public EnumValueSetParser(IValueSetOwner valueSetOwner, IInputFormat<String> inputFormat) {
        super(valueSetOwner, inputFormat);
    }

    @Override
    public IValueSet parseValueSet(String stringTobeParsed) {
        String[] split = stringTobeParsed.split("\\" + EnumValueSet.ENUM_VALUESET_SEPARATOR); //$NON-NLS-1$
        List<String> parsedValues = parseValues(split);
        if (!isEqualContent(parsedValues)) {
            EnumValueSet enumValueSet = createNewEnumValueSet(parsedValues);
            return enumValueSet;
        }
        return getValueSet();
    }

    public EnumValueSet createNewEnumValueSet(List<String> values) {
        EnumValueSet valueSet = new EnumValueSet(getValueSetOwner(), values, getNextPartIdOfValueSetOwner());
        return valueSet;
    }

    private boolean isEqualContent(List<String> parsedValues) {
        return getValueSet() instanceof IEnumValueSet && getValuesAsList().equals(parsedValues);
    }

    private List<String> getValuesAsList() {
        return ((IEnumValueSet)getValueSet()).getValuesAsList();
    }

    private List<String> parseValues(String[] split) {
        List<String> parseValues = new ArrayList<String>();
        IInputFormat<String> inputFormat = getInputFormat();
        for (String value : split) {
            parseValues.add(inputFormat.parse(value.trim()));
        }
        return parseValues;
    }
}
