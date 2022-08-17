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

import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.model.internal.valueset.RangeValueSet;
import org.faktorips.devtools.model.valueset.IRangeValueSet;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.devtools.model.valueset.IValueSetOwner;
import org.faktorips.devtools.model.valueset.ValueSetType;

/**
 * Class to parse and format an {@link IRangeValueSet}.
 * 
 */
public class RangeValueSetFormat extends AbstractValueSetFormat {

    private static final String UNLIMITED_BOUND = "*"; //$NON-NLS-1$
    private static final String REGEX_STEP_SEPERATOR = "(\\s*/\\s*)"; //$NON-NLS-1$
    private static final String REGEX_BOUND_SEPERATOR = "(\\s*\\.\\.+\\s*)"; //$NON-NLS-1$
    private static final String REGEX_BRACKETS = "(^\\s*\\[?)|(\\]?\\s*$)"; //$NON-NLS-1$
    private static final String REGEX_EMPTY_BRACKETS = "^\\s*\\[\\s*\\]\\s*$"; //$NON-NLS-1$

    public RangeValueSetFormat(IValueSetOwner valueSetOwner, IpsUIPlugin uiPlugin) {
        super(valueSetOwner, uiPlugin);
    }

    @Override
    protected String getNullPresentationInValueSet() {
        return UNLIMITED_BOUND;
    }

    @Override
    public String formatInternal(IValueSet value) {
        if (value.isRange()) {
            return formatRangeValueSet(value);
        } else {
            return StringUtils.EMPTY;
        }
    }

    private String formatRangeValueSet(IValueSet value) {
        IRangeValueSet range = (IRangeValueSet)value;
        StringBuilder sb = new StringBuilder();
        sb.append(RangeValueSet.RANGE_VALUESET_START);
        if (!range.isEmpty()) {
            String lowerBound = range.getLowerBound();
            String upperBound = range.getUpperBound();
            String step = range.getStep();
            sb.append((lowerBound == null ? UNLIMITED_BOUND : getInputFormat().format(lowerBound)));
            sb.append(RangeValueSet.RANGE_VALUESET_POINTS);
            sb.append((upperBound == null ? UNLIMITED_BOUND : getInputFormat().format(upperBound)));
            if (step != null) {
                sb.append(RangeValueSet.RANGE_STEP_SEPERATOR);
                sb.append(getInputFormat().format(step));
            }
        }
        sb.append(RangeValueSet.RANGE_VALUESET_END);
        if (!range.isEmpty() && range.isContainsNull()) {
            sb.append(' ' + getNullPresentation());
        }
        return sb.toString();
    }

    @Override
    protected IValueSet parseInternal(String stringToBeParsed) {
        if (StringUtils.isEmpty(stringToBeParsed)) {
            return getUnlimitedRangeSet();
        } else {
            return parseNonEmptyString(stringToBeParsed);
        }
    }

    private IValueSet parseNonEmptyString(String stringToBeParsed) {
        boolean containsNull = stringToBeParsed.endsWith(getNullPresentation());
        String inputWihoutNullPresentation = StringUtils.removeEnd(stringToBeParsed, getNullPresentation());
        return parseValueSet(inputWihoutNullPresentation, containsNull);
    }

    private String getNullPresentation() {
        return NLS.bind(Messages.RangeValueSetFormat_includingNull,
                IpsPlugin.getDefault().getIpsPreferences().getNullPresentation());
    }

    private IValueSet parseValueSet(String stringToBeParsed, boolean containsNull) {
        String stringWithoutBrackets = stringToBeParsed.replaceAll(REGEX_BRACKETS, StringUtils.EMPTY);
        if (StringUtils.isBlank(stringWithoutBrackets)) {
            return RangeValueSet.empty(getValueSetOwner(), getNextPartId());
        }
        String[] splitedBounds = stringWithoutBrackets.split(REGEX_BOUND_SEPERATOR, 2);
        if (splitedBounds.length == 2) {
            String lowerBound = parseValue(splitedBounds[0]);
            String[] splitedUpperBoundAndStep = splitedBounds[1].split(REGEX_STEP_SEPERATOR, 2);
            String upperBound = parseValue(splitedUpperBoundAndStep[0]);
            String step = getStep(splitedUpperBoundAndStep);
            if (!isEqualContentRange(lowerBound, upperBound, step, containsNull)) {
                return createNewRangeValueSet(lowerBound, upperBound, step, containsNull);
            }
        }
        return getValueSet();
    }

    private String getStep(String[] splitSeperator) {
        if (splitSeperator.length == 2) {
            return parseValue(splitSeperator[1]);
        } else {
            return null;
        }
    }

    @Override
    protected String parseValue(String value) {
        if (StringUtils.isEmpty(value)) {
            return null;
        } else {
            return super.parseValue(value);
        }
    }

    private boolean isEqualContentRange(String lowerBound, String upperBound, String step, boolean containsNull) {
        if (getValueSet().isRange()) {
            IRangeValueSet range = (IRangeValueSet)getValueSet();
            return Objects.equals(range.getLowerBound(), lowerBound)
                    && Objects.equals(range.getUpperBound(), upperBound)
                    && Objects.equals(step, range.getStep()) && (containsNull == getValueSet().isContainsNull());
        }
        return false;
    }

    private IValueSet createNewRangeValueSet(String lowerBound, String upperBound, String step, boolean containsNull) {
        return new RangeValueSet(getValueSetOwner(), getNextPartId(), lowerBound, upperBound, step,
                containsNull);
    }

    @Override
    public boolean isResponsibleFor(String stringTobeParsed) {
        return (isRange(stringTobeParsed) && isAllowedValueSetType(ValueSetType.RANGE))
                || isOnlyAllowedValueSetType(ValueSetType.RANGE);
    }

    private boolean isRange(String stringToBeParsed) {
        return stringToBeParsed.matches("(.*" + REGEX_BOUND_SEPERATOR + ".*)|(" + REGEX_EMPTY_BRACKETS + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    private IValueSet getUnlimitedRangeSet() {
        IValueSet valueSet = getValueSet();
        if (valueSet.isRange() && isUnlimitedRange((IRangeValueSet)valueSet)) {
            return valueSet;
        } else {
            return createNewRangeValueSet(null, null, null, false);
        }
    }

    private boolean isUnlimitedRange(IRangeValueSet valueSet) {
        return valueSet.getLowerBound() == null && valueSet.getUpperBound() == null;
    }

}
