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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.inputformat.IInputFormat;
import org.faktorips.devtools.model.internal.valueset.EnumValueSet;
import org.faktorips.devtools.model.valueset.IEnumValueSet;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.devtools.model.valueset.IValueSetOwner;
import org.faktorips.devtools.model.valueset.ValueSetType;

/**
 * Class to parse and format an {@link IEnumValueSet}.
 * 
 */
public class EnumValueSetFormat extends AbstractValueSetFormat {

    public EnumValueSetFormat(IValueSetOwner valueSetOwner, IpsUIPlugin uiPlugin) {
        super(valueSetOwner, uiPlugin);
    }

    @Override
    protected String getNullPresentationInValueSet() {
        return IpsPlugin.getDefault().getIpsPreferences().getNullPresentation();
    }

    @Override
    public String formatInternal(IValueSet valueSet) {
        if (valueSet.isEnum()) {
            return formatEnumValueSet((IEnumValueSet)valueSet);
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
        List<String> formattedValues = Arrays.stream(values).map(inputFormat::format).collect(Collectors.toList());

        return StringUtils.join(formattedValues, EnumValueSet.ENUM_VALUESET_SEPARATOR_WITH_WHITESPACE);
    }

    @Override
    protected IValueSet parseInternal(String stringToBeparsed) {
        if (EnumValueSet.ENUM_VALUESET_EMPTY.equals(stringToBeparsed)) {
            return createNewEnumValueSet(new ArrayList<String>());
        }
        String[] split = stringToBeparsed.split(Pattern.quote(EnumValueSet.ENUM_VALUESET_SEPARATOR));
        List<String> parsedValues = parseValues(split);
        if (!isEqualContent(parsedValues)) {
            return createNewEnumValueSet(parsedValues);
        }
        return getValueSet();
    }

    private EnumValueSet createNewEnumValueSet(List<String> values) {
        return new EnumValueSet(getValueSetOwner(), values, getNextPartId());
    }

    private boolean isEqualContent(List<String> parsedValues) {
        return getValueSet().isEnum() && getValuesAsList().equals(parsedValues);
    }

    private List<String> getValuesAsList() {
        return ((IEnumValueSet)getValueSet()).getValuesAsList();
    }

    private List<String> parseValues(String[] split) {
        List<String> parseValues = new ArrayList<>();
        for (String text : split) {
            String parsedValue = parseValue(text);
            if (isValidValue(text, parsedValue)) {
                parseValues.add(parsedValue);
            }
        }
        return parseValues;
    }

    /**
     * A value is valid if the parsed value is not <code>null</code> or if the text (raw, not yet
     * parsed value) is the null presentation.
     * <p>
     * It is necessary to check whether the raw input text is the null presentation because the
     * parser also returns <code>null</code> if the value is invalid. For example: the
     * IntegerInputFormat parses the String "abc" to <code>null</code>. But this is no valid value.
     */
    private boolean isValidValue(String text, String parsedValue) {
        return parsedValue != null || getNullPresentationInValueSet().equals(text.trim());
    }

    @Override
    public boolean isResponsibleFor(String resultingText) {
        return isOnlyAllowedValueSetType(ValueSetType.ENUM)
                || (isAllowedValueSetType(ValueSetType.ENUM) && textLooksLikeEnum(resultingText));
    }

    /**
     * If there is text, we assume it is an enum value set. If there is no text it is only
     * considered as enum value set, if the input format can parse the empty string to a legal
     * value. Considering {@link #getNullPresentationInValueSet()} we always use the configured null
     * presentation. Hence only a datatype like String, where an empty String is a legal value,
     * returns <code>true</code>. For other datatypes we assume that no entry should be parsed to
     * <em>unrestricted</em>, hence it is no enum value set.
     * 
     */
    private boolean textLooksLikeEnum(String text) {
        String parsedValue = getInputFormat().parse(text);
        return !text.isEmpty() || parsedValue != null;
    }

}
