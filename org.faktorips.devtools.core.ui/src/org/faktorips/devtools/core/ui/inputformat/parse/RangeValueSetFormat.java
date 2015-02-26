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

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.valueset.RangeValueSet;
import org.faktorips.devtools.core.model.valueset.IRangeValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSetOwner;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

/**
 * Class to parse and format an {@link IRangeValueSet}.
 * 
 */
public class RangeValueSetFormat extends AbstractValueSetFormat {

    private static final String UNLIMITED_BOUND = "*"; //$NON-NLS-1$
    private static final String REGEX_STEP_SEPERATOR = "(\\s*/\\s*)"; //$NON-NLS-1$
    private static final String REGEX_BOUND_SEPERATOR = "(\\s*\\.\\.+\\s*)"; //$NON-NLS-1$
    private static final String REGEX_BRACKETS = "(^\\[?)|(\\]?$)"; //$NON-NLS-1$

    public RangeValueSetFormat(IValueSetOwner valueSetOwner, IpsUIPlugin uiPlugin) {
        super(valueSetOwner, uiPlugin);
    }

    @Override
    public String formatInternal(IValueSet value) {
        if (value instanceof IRangeValueSet) {
            return formatRangeValueSet(value);
        } else {
            return StringUtils.EMPTY;
        }
    }

    private String formatRangeValueSet(IValueSet value) {
        IRangeValueSet range = (IRangeValueSet)value;
        String lowerBound = range.getLowerBound();
        String upperBound = range.getUpperBound();
        String step = range.getStep();
        StringBuffer sb = new StringBuffer();
        sb.append(RangeValueSet.RANGE_VALUESET_START);
        sb.append((lowerBound == null ? UNLIMITED_BOUND : getInputFormat().format(lowerBound)));
        sb.append(RangeValueSet.RANGE_VALUESET_POINTS);
        sb.append((upperBound == null ? UNLIMITED_BOUND : getInputFormat().format(upperBound)));
        if (step != null) {
            sb.append(RangeValueSet.RANGE_STEP_SEPERATOR);
            sb.append(getInputFormat().format(step));
        }
        sb.append(RangeValueSet.RANGE_VALUESET_END);
        if (getValueSet().isContainsNull()) {
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
        String stringWithoutBrackets = stringToBeParsed.replaceAll(REGEX_BRACKETS, StringUtils.EMPTY);
        String[] splitedBounds = stringWithoutBrackets.split(REGEX_BOUND_SEPERATOR, 2);
        String lowerBound = parseValue(splitedBounds[0]);
        if (splitedBounds.length == 2) {
            String[] splitedUpperBoundAndStepAndNullPresentation = splitedBounds[1].split(REGEX_STEP_SEPERATOR, 2);
            String upperBound = parseValue(splitedUpperBoundAndStepAndNullPresentation[0]);
            String step = getStep(splitedUpperBoundAndStepAndNullPresentation);
            boolean containsNull = isContainsNull(getNulRepresentation(splitedUpperBoundAndStepAndNullPresentation));
            if (!isEqualContentRange(lowerBound, upperBound, step, containsNull)) {
                return createNewRangeValueSet(lowerBound, upperBound, step, containsNull);
            }
        }
        return getValueSet();
    }

    private boolean isContainsNull(String nullPresentation) {
        return StringUtils.isNotBlank(nullPresentation) ? getNullPresentation().equals(
                nullPresentation.trim()) : false;
    }

    private String getNullPresentation() {
        return NLS.bind(Messages.RangeValueSetFormat_includingNull, IpsPlugin.getDefault().getIpsPreferences()
                .getNullPresentation());
    }

    private String getNulRepresentation(String[] splitSeperator) {
        if (splitSeperator.length == 2) {
            String nullRepresentation[] = splitSeperator[1].split("]"); //$NON-NLS-1$
            if (nullRepresentation.length == 2) {
                return nullRepresentation[1];
            }
        }
        return null;
    }

    private String getStep(String[] splitSeperator) {
        if (splitSeperator.length == 2) {
            String step[] = splitSeperator[1].split("]"); //$NON-NLS-1$
            return parseValue(step[0]);
        } else {
            return null;
        }
    }

    @Override
    protected String parseValue(String value) {
        if (UNLIMITED_BOUND.equals(value) || StringUtils.isEmpty(value)) {
            return null;
        } else {
            return super.parseValue(value);
        }
    }

    private boolean isEqualContentRange(String lowerBound, String upperBound, String step, boolean containsNull) {
        if (getValueSet() instanceof IRangeValueSet) {
            IRangeValueSet range = (IRangeValueSet)getValueSet();
            return ObjectUtils.equals(range.getLowerBound(), lowerBound)
                    && ObjectUtils.equals(range.getUpperBound(), upperBound)
                    && ObjectUtils.equals(step, range.getStep()) && (containsNull == getValueSet().isContainsNull());
        }
        return false;
    }

    private IValueSet createNewRangeValueSet(String lowerBound, String upperBound, String step, boolean isContainsNull) {
        return new RangeValueSet(getValueSetOwner(), getNextPartId(), lowerBound, upperBound, step, isContainsNull);
    }

    @Override
    public boolean isResponsibleFor(String stringTobeParsed) {
        return (isRange(stringTobeParsed) && isAllowedValueSetType(ValueSetType.RANGE))
                || isOnlyAllowedValueSetType(ValueSetType.RANGE);
    }

    private boolean isRange(String stringToBeParsed) {
        return stringToBeParsed.matches(".*" + REGEX_BOUND_SEPERATOR + ".*"); //$NON-NLS-1$ //$NON-NLS-2$
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
