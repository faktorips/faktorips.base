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
import java.util.Arrays;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import org.apache.commons.lang.StringUtils;
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
public class EnumValueSetFormat extends AbstractValueSetFormat {

    public EnumValueSetFormat(IValueSetOwner valueSetOwner, IpsUIPlugin uiPlugin) {
        super(valueSetOwner, uiPlugin);
    }

    @Override
    public String formatInternal(IValueSet value) {
        if (value instanceof IEnumValueSet) {
            return formatEnumValueSet((IEnumValueSet)value);
        } else {
            return StringUtils.EMPTY;
        }
    }

    private String formatEnumValueSet(IEnumValueSet enumValueSet) {
        String[] values = enumValueSet.getValues();
        if (values.length == 0) {
            return EnumValueSet.ENUM_VALUESET_EMPTY;
        }

        final IInputFormat<String> inputFormat = getInputFormat();
        List<String> formattedValues = Lists.transform(Arrays.asList(values), new Function<String, String>() {
            @Override
            public String apply(String value) {
                return inputFormat.format(value);
            }
        });

        return StringUtils.join(formattedValues, " " + EnumValueSet.ENUM_VALUESET_SEPARATOR + " "); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    protected IValueSet parseInternal(String stringToBeparsed) {
        if (EnumValueSet.ENUM_VALUESET_EMPTY.equals(stringToBeparsed)) {
            return createNewEnumValueSet(new ArrayList<String>());
        }
        String[] split = stringToBeparsed.split("\\" + EnumValueSet.ENUM_VALUESET_SEPARATOR); //$NON-NLS-1$
        List<String> parsedValues = parseValues(split);
        if (!isEqualContent(parsedValues)) {
            EnumValueSet enumValueSet = createNewEnumValueSet(parsedValues);
            return enumValueSet;
        }
        return getValueSet();
    }

    private EnumValueSet createNewEnumValueSet(List<String> values) {
        EnumValueSet valueSet = new EnumValueSet(getValueSetOwner(), values, getNextPartId());
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
        for (String value : split) {
            String parsedValue = parseValue(value);
            parseValues.add(parsedValue);
        }
        return parseValues;
    }

    @Override
    public boolean isResponsibleFor(String resultingText) {
        return isAllowedValueSetType(ValueSetType.ENUM);
    }

}
