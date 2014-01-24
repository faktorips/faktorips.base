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
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.inputformat.IInputFormat;

/**
 * Class to parse and format an {@link IEnumValueSet}.
 * 
 */
public class EnumValueSetParser extends ValueSetParser {

    public EnumValueSetParser(IValueSetOwner valueSetOwner, IpsUIPlugin uiPlugin) {
        super(valueSetOwner, uiPlugin);
    }

    public String formatEnumValueSet(IEnumValueSet enumValueSet) {
        StringBuffer buffer = new StringBuffer();
        String[] values = enumValueSet.getValues();
        if (values.length == 0) {
            return EnumValueSet.ENUM_VALUESET_EMPTY;
        }
        IInputFormat<String> inputFormat = getInputFormat();
        for (String id : values) {
            String formatedEnumText = inputFormat.format(id);
            buffer.append(formatedEnumText);
            buffer.append(" " + EnumValueSet.ENUM_VALUESET_SEPARATOR + " "); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (buffer.length() > 3) {
            // Remove the separator after the last value (" | ")
            buffer.delete(buffer.length() - 3, buffer.length());
        }
        return buffer.toString();
    }

    @Override
    public IValueSet parseValueSet(String stringTobeParsed) {
        if (EnumValueSet.ENUM_VALUESET_EMPTY.equals(stringTobeParsed)) {
            return getEmptyEnumSet();
        }
        String[] split = stringTobeParsed.split("\\" + EnumValueSet.ENUM_VALUESET_SEPARATOR); //$NON-NLS-1$
        List<String> parsedValues = parseValues(split);
        if (!isEqualContent(parsedValues)) {
            EnumValueSet enumValueSet = createNewEnumValueSet(parsedValues);
            return enumValueSet;
        }
        return getValueSet();
    }

    private EnumValueSet createNewEnumValueSet(List<String> values) {
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

    public IValueSet getEmptyEnumSet() {
        IValueSet valueSet = getValueSet();
        if (valueSet.isEnum() && ((IEnumValueSet)valueSet).getValuesAsList().isEmpty()) {
            return valueSet;
        } else {
            return createNewEnumValueSet(new ArrayList<String>());
        }
    }

    @Override
    public boolean isResponsibleFor(String stringTobeParsed) {
        return isAllowedValueSetType(ValueSetType.ENUM);
    }
}
