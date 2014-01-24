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

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.valueset.RangeValueSet;
import org.faktorips.devtools.core.model.valueset.IRangeValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSetOwner;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.inputformat.IInputFormat;

/**
 * Class to parse and format an {@link IRangeValueSet}.
 * 
 */
public class RangeValueSetParser extends ValueSetParser {

    public RangeValueSetParser(IValueSetOwner valueSetOwner, IpsUIPlugin uiPlugin) {
        super(valueSetOwner, uiPlugin);
    }

    public String formatRangeValueSet(IRangeValueSet valueSet) {
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

    @Override
    public IValueSet parseValueSet(String stringTobeParsed) {
        List<String> parsedValues = getParsedStringOfRangeValueSet(stringTobeParsed);
        if (parsedValues.size() > 1) {
            if (!isEqualContentRange(parsedValues)) {
                return createNewRangeValueSetContainingNull(parsedValues);
            }
        }
        return getValueSet();
    }

    private List<String> getParsedStringOfRangeValueSet(String stringToBeParsed) {
        String stringWithoutBrackets = stringToBeParsed.replaceAll("(^\\[?)|(\\]?$)", ""); //$NON-NLS-1$ //$NON-NLS-2$
        String[] splitByPoints = stringWithoutBrackets.split("(\\s*\\.\\.+\\s*)", 2); //$NON-NLS-1$
        ArrayList<String> parsedValueList = new ArrayList<String>();
        parsedValueList.add(splitByPoints[0]);
        if (splitByPoints.length == 2) {
            String[] splitSeperator = splitByPoints[1].split("(\\s*/\\s*)", 2); //$NON-NLS-1$
            List<String> list = Arrays.asList(splitSeperator);
            parsedValueList.addAll(list);
        }
        return parseValues(parsedValueList);
    }

    private boolean isEqualContentRange(List<String> parsedValues) {
        if (getValueSet() instanceof IRangeValueSet) {
            IRangeValueSet range = (IRangeValueSet)getValueSet();
            if (isEqualBounds(parsedValues, range)) {
                return true;
            }
        }
        return false;
    }

    private boolean isEqualBounds(List<String> parsedValues, IRangeValueSet range) {
        if (ObjectUtils.equals(range.getLowerBound(), parsedValues.get(0))
                && ObjectUtils.equals(range.getUpperBound(), parsedValues.get(1))) {
            return checkStep(parsedValues, range);
        }
        return false;
    }

    private boolean checkStep(List<String> parsedValues, IRangeValueSet range) {
        if (isBoundsWithoutSteps(parsedValues, range) || parsedValues.size() == 3
                && ObjectUtils.equals(range.getStep(), parsedValues.get(2))) {
            return true;
        }
        return false;
    }

    private boolean isBoundsWithoutSteps(List<String> parsedValues, IRangeValueSet range) {
        return parsedValues.size() == 2 && StringUtils.isEmpty(range.getStep());
    }

    private IValueSet createNewRangeValueSetContainingNull(List<String> parsedValues) {
        IValueSet valueSet = getValueSet();
        IRangeValueSet range = createNewRangeValues(parsedValues);
        if (valueSet instanceof IRangeValueSet) {
            IRangeValueSet oldRange = (IRangeValueSet)valueSet;
            range.setContainsNull(oldRange.isContainingNull());
        }
        return range;
    }

    private IRangeValueSet createNewRangeValues(List<String> parsedValues) {
        if (parsedValues.size() == 0) {
            return new RangeValueSet(getValueSetOwner(), getNextPartIdOfValueSetOwner(), null, null, null);
        }
        if (parsedValues.size() == 2) {
            return new RangeValueSet(getValueSetOwner(), getNextPartIdOfValueSetOwner(), parsedValues.get(0),
                    parsedValues.get(1), null);
        }
        return new RangeValueSet(getValueSetOwner(), getNextPartIdOfValueSetOwner(), parsedValues.get(0),
                parsedValues.get(1), parsedValues.get(2));
    }

    private List<String> parseValues(List<String> listToparse) {
        List<String> parseValues = new ArrayList<String>();
        IInputFormat<String> inputFormat = getInputFormat();
        for (String value : listToparse) {
            parseValues.add(inputFormat.parse(value.trim()));
        }
        return parseValues;
    }

    public IValueSet getUnlimitedRangeSet() {
        IValueSet valueSet = getValueSet();
        if (valueSet.isRange() && isUnlimitedRange((IRangeValueSet)valueSet)) {
            return valueSet;
        } else {
            return createNewRangeValueSetContainingNull(new ArrayList<String>());
        }
    }

    private boolean isUnlimitedRange(IRangeValueSet valueSet) {
        return valueSet.getLowerBound() == null && valueSet.getUpperBound() == null;
    }

    @Override
    public boolean isResponsibleFor(String stringTobeParsed) {
        return (isRange(stringTobeParsed) && isRangeValueSetAllowed()) || isOnlyRangeAllowed();
    }

    private boolean isRange(String stringToBeParsed) {
        return stringToBeParsed.startsWith(IRangeValueSet.RANGE_VALUESET_START)
                && stringToBeParsed.endsWith(IRangeValueSet.RANGE_VALUESET_END);
    }

    private boolean isRangeValueSetAllowed() {
        return isAllowedValueSetType(ValueSetType.RANGE);
    }

    public boolean isOnlyRangeAllowed() {
        try {
            List<ValueSetType> allowedValueSetTypes = getValueSetOwner().getAllowedValueSetTypes(
                    getValueSetOwner().getIpsProject());
            return allowedValueSetTypes.size() == 1 && allowedValueSetTypes.get(0).equals(ValueSetType.RANGE);
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }
}
