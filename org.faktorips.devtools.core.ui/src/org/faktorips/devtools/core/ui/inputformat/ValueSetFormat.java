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

package org.faktorips.devtools.core.ui.inputformat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.events.VerifyEvent;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.valueset.EnumValueSet;
import org.faktorips.devtools.core.internal.model.valueset.RangeValueSet;
import org.faktorips.devtools.core.internal.model.valueset.UnrestrictedValueSet;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IRangeValueSet;
import org.faktorips.devtools.core.model.valueset.IUnrestrictedValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSetOwner;
import org.faktorips.devtools.core.model.valueset.Messages;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

public class ValueSetFormat extends AbstractInputFormat<IValueSet> {

    private final IValueSetOwner valueSetOwner;

    private final IpsUIPlugin uiPlugin;

    private IInputFormat<String> cachedIinputFormat;

    private ValueDatatype cachedValueDatatype;

    public ValueSetFormat(IValueSetOwner valueSetOwner, IpsUIPlugin uiPlugin) {
        this.valueSetOwner = valueSetOwner;
        this.uiPlugin = uiPlugin;
    }

    public static ValueSetFormat newInstance(IValueSetOwner valueSetOwner) {
        ValueSetFormat format = new ValueSetFormat(valueSetOwner, IpsUIPlugin.getDefault());
        format.initFormat();
        return format;
    }

    @Override
    protected IValueSet parseInternal(String stringToBeParsed) {
        if (stringToBeParsed.isEmpty()) {
            return getParsedEmptyString();
        } else if (Messages.ValueSetFormat_unrestricted.equals(stringToBeParsed) && isUnrestrictedAllowed()) {
            return getUnrestrictedValueSet();
        } else if (EnumValueSet.ENUM_VALUESET_EMPTY.equals(stringToBeParsed) && isEnumValueSetAllowed()) {
            return getEmptyEnumSet();
        } else if ((isRange(stringToBeParsed) && isRangeValueSetAllowed()) || isOnlyRangeAllowed()) {
            return parseRangeValue(stringToBeParsed);
        } else if (isEnumValueSetAllowed()) {
            return parseEnumValue(stringToBeParsed);
        }
        return getValueSet();
    }

    private IValueSet getParsedEmptyString() {
        if (isUnrestrictedAllowed()) {
            return getUnrestrictedValueSet();
        } else if (isOnlyRangeAllowed()) {
            return getUnlimitedRangeSet();
        } else {
            return getEmptyEnumSet();
        }
    }

    protected IValueSet parseRangeValue(String stringToBeParsed) {
        List<String> parsedValues = getParsedStringOfRangeValueSet(stringToBeParsed);
        if (parsedValues.size() == 2 || parsedValues.size() == 3) {
            if (!isEqualContentRange(parsedValues)) {
                return createNewRangeValueSet(parsedValues);
            }
        }
        return getValueSet();
    }

    private boolean isRange(String stringToBeParsed) {
        return stringToBeParsed.startsWith(IRangeValueSet.RANGE_VALUESET_START)
                && stringToBeParsed.endsWith(IRangeValueSet.RANGE_VALUESET_END);
    }

    private List<String> getParsedStringOfRangeValueSet(String stringToBeParsed) {
        String stringWithoutBrackets = stringToBeParsed.replaceAll("(^\\[?)|(\\]?$)", ""); //$NON-NLS-1$ //$NON-NLS-2$
        String[] splitPonits = stringWithoutBrackets.split("(\\s*\\.\\.+\\s*)", 2); //$NON-NLS-1$
        if (splitPonits.length == 2) {
            String[] splitSeperator = splitPonits[1].split("(\\s*/\\s*)", 2); //$NON-NLS-1$
            String[] split = concatenateSplittedArrays(splitPonits, splitSeperator);
            return parseValues(split);
        }
        return Arrays.asList(splitPonits[0]);
    }

    private String[] concatenateSplittedArrays(String[] splitPonits, String[] splitSeperator) {
        String[] split = new String[1 + splitSeperator.length];
        System.arraycopy(splitPonits, 0, split, 0, 1);
        System.arraycopy(splitSeperator, 0, split, 1, splitSeperator.length);
        return split;
    }

    private IValueSet createNewRangeValueSet(List<String> parsedValues) {
        if (parsedValues.size() == 0) {
            return new RangeValueSet(valueSetOwner, getNextPartId(valueSetOwner), null, null, null);
        }
        if (parsedValues.size() == 2) {
            return new RangeValueSet(valueSetOwner, getNextPartId(valueSetOwner), parsedValues.get(0),
                    parsedValues.get(1), null);
        }
        return new RangeValueSet(valueSetOwner, getNextPartId(valueSetOwner), parsedValues.get(0), parsedValues.get(1),
                parsedValues.get(2));
    }

    private boolean isEqualContentRange(List<String> parsedValues) {
        if (getValueSet() instanceof IRangeValueSet) {
            IRangeValueSet range = (IRangeValueSet)getValueSet();
            if (ObjectUtils.equals(range.getLowerBound(), parsedValues.get(0))
                    && ObjectUtils.equals(range.getUpperBound(), parsedValues.get(1))
                    && ((parsedValues.size() == 3 && ObjectUtils.equals(range.getStep(), parsedValues.get(2)) || (parsedValues
                            .size() == 2 && StringUtils.isEmpty(range.getStep()))))) {
                return true;
            }
        }
        return false;
    }

    private IValueSet parseEnumValue(String stringToBeParsed) {
        String[] split = stringToBeParsed.split("\\" + EnumValueSet.ENUM_VALUESET_SEPARATOR); //$NON-NLS-1$
        List<String> parsedValues = parseValues(split);
        if (!isEqualContent(parsedValues)) {
            EnumValueSet enumValueSet = createNewEnumValueSet(parsedValues);
            return enumValueSet;
        }
        return getValueSet();
    }

    private boolean isUnrestrictedAllowed() {
        return isAllowedValueSetType(ValueSetType.UNRESTRICTED);
    }

    private boolean isEnumValueSetAllowed() {
        return isAllowedValueSetType(ValueSetType.ENUM);
    }

    private boolean isRangeValueSetAllowed() {
        return isAllowedValueSetType(ValueSetType.RANGE);
    }

    private boolean isOnlyRangeAllowed() {
        try {
            List<ValueSetType> allowedValueSetTypes = this.valueSetOwner.getAllowedValueSetTypes(this.valueSetOwner
                    .getIpsProject());
            return allowedValueSetTypes.size() == 1 && allowedValueSetTypes.get(0).equals(ValueSetType.RANGE);
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    private boolean isAllowedValueSetType(ValueSetType valueSetType) {
        try {
            List<ValueSetType> allowedValueSetTypes = this.valueSetOwner.getAllowedValueSetTypes(this.valueSetOwner
                    .getIpsProject());
            return allowedValueSetTypes.contains(valueSetType);
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    private IValueSet getUnrestrictedValueSet() {
        final IValueSet valueSet = getValueSet();
        if (valueSet.isUnrestricted()) {
            return valueSet;
        } else {
            UnrestrictedValueSet newValueSet = new UnrestrictedValueSet(valueSetOwner, getNextPartId(valueSetOwner));
            return newValueSet;
        }
    }

    private IValueSet getEmptyEnumSet() {
        IValueSet valueSet = getValueSet();
        if (valueSet.isEnum() && ((IEnumValueSet)valueSet).getValuesAsList().isEmpty()) {
            return valueSet;
        } else {
            return createNewEnumValueSet(new ArrayList<String>());
        }
    }

    private IValueSet getUnlimitedRangeSet() {
        IValueSet valueSet = getValueSet();
        if (valueSet.isRange() && isUnlimitedRange((IRangeValueSet)valueSet)) {
            return valueSet;
        } else {
            return createNewRangeValueSet(new ArrayList<String>());
        }
    }

    private boolean isUnlimitedRange(IRangeValueSet valueSet) {
        return valueSet.getLowerBound() == null && valueSet.getUpperBound() == null;
    }

    private List<String> parseValues(String[] split) {
        List<String> parseValues = new ArrayList<String>();
        IInputFormat<String> inputFormat = getInputFormat();
        for (String value : split) {
            parseValues.add(inputFormat.parse(value.trim()));
        }
        return parseValues;
    }

    protected IInputFormat<String> getInputFormat() {
        ValueDatatype valueDatatype = getValueDatatype();
        if (cachedIinputFormat == null || valueDatatype != cachedValueDatatype) {
            cachedIinputFormat = uiPlugin.getInputFormat(valueDatatype, valueSetOwner.getIpsProject());
            cachedValueDatatype = valueDatatype;
        }

        return cachedIinputFormat;
    }

    private boolean isEqualContent(List<String> parsedValues) {
        return getValueSet() instanceof IEnumValueSet && getValuesAsList().equals(parsedValues);
    }

    private List<String> getValuesAsList() {
        return ((IEnumValueSet)getValueSet()).getValuesAsList();
    }

    private IValueSet getValueSet() {
        return this.valueSetOwner.getValueSet();
    }

    private EnumValueSet createNewEnumValueSet(List<String> values) {
        EnumValueSet valueSet = new EnumValueSet(valueSetOwner, values, getNextPartId(valueSetOwner));
        return valueSet;
    }

    private String getNextPartId(IIpsObjectPartContainer parent) {
        return parent.getIpsModel().getNextPartId(parent);
    }

    @Override
    protected String formatInternal(IValueSet valueSet) {
        if (valueSet instanceof IEnumValueSet) {
            return formatEnumValueSet((IEnumValueSet)valueSet);
        } else if (valueSet instanceof IRangeValueSet) {
            return formatRangeValueSet((IRangeValueSet)valueSet);
        } else if (valueSet instanceof IUnrestrictedValueSet) {
            return formatUnrestrictedValueSet();
        }
        return StringUtils.EMPTY;
    }

    private String formatEnumValueSet(IEnumValueSet enumValueSet) {
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

    private String formatRangeValueSet(IRangeValueSet valueSet) {
        String lowerBound = valueSet.getLowerBound();
        String upperBound = valueSet.getUpperBound();
        String step = valueSet.getStep();
        StringBuffer sb = new StringBuffer();
        sb.append(RangeValueSet.RANGE_VALUESET_START);
        sb.append((lowerBound == null ? "*" : getInputFormat().format(lowerBound))); //$NON-NLS-1$
        sb.append(RangeValueSet.RANGE_VALUESET_POINTS);
        sb.append((upperBound == null ? "*" : getInputFormat().format(upperBound))); //$NON-NLS-1$
        if (step != null) {
            sb.append(RangeValueSet.RANGE_STEP_SEPERATOR);
            sb.append(getInputFormat().format(step));
        }
        sb.append(RangeValueSet.RANGE_VALUESET_END);
        return sb.toString();
    }

    private String formatUnrestrictedValueSet() {
        return org.faktorips.devtools.core.model.valueset.Messages.ValueSetFormat_unrestricted;
    }

    private ValueDatatype getValueDatatype() {
        try {
            return valueSetOwner.findValueDatatype(valueSetOwner.getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    @Override
    protected void verifyInternal(VerifyEvent e, String resultingText) {
        // do nothing
    }

    @Override
    protected void initFormat(Locale locale) {
        // do nothing
    }

}
