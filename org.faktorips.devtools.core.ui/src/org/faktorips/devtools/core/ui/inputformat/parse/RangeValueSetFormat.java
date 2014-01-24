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

import java.util.Locale;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.events.VerifyEvent;
import org.faktorips.devtools.core.internal.model.valueset.RangeValueSet;
import org.faktorips.devtools.core.model.valueset.IRangeValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSetOwner;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

/**
 * Class to parse and format an {@link IRangeValueSet}.
 * 
 */
public class RangeValueSetFormat extends AbstractValueSetFormat {

    private static final String REGEX_SEPERATOR = "(\\s*/\\s*)"; //$NON-NLS-1$
    private static final String REGEX_POINTS = "(\\s*\\.\\.+\\s*)"; //$NON-NLS-1$
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
        sb.append((lowerBound == null ? "<null>" : getInputFormat().format(lowerBound))); //$NON-NLS-1$
        sb.append(RangeValueSet.RANGE_VALUESET_POINTS);
        sb.append((upperBound == null ? "<null>" : getInputFormat().format(upperBound))); //$NON-NLS-1$
        if (step != null) {
            sb.append(RangeValueSet.RANGE_STEP_SEPERATOR);
            sb.append(getInputFormat().format(step));
        }
        sb.append(RangeValueSet.RANGE_VALUESET_END);
        return sb.toString();
    }

    @Override
    protected IValueSet parseInternal(String stringToBeParsed) {
        String stringWithoutBrackets = stringToBeParsed.replaceAll(REGEX_BRACKETS, StringUtils.EMPTY);
        String[] splitByPoints = stringWithoutBrackets.split(REGEX_POINTS, 2);
        String lowerBound = parseValue(splitByPoints[0]);
        if (splitByPoints.length == 2) {
            String[] splitSeperator = splitByPoints[1].split(REGEX_SEPERATOR, 2);
            String upperBound = parseValue(splitSeperator[0]);
            String step = getStep(splitSeperator);
            if (!isEqualContentRange(lowerBound, upperBound, step)) {
                return createNewRangeValueSetContainingNull(lowerBound, upperBound, step);
            }
        }
        return getValueSet();
    }

    private String getStep(String[] splitSeperator) {
        String step;
        if (splitSeperator.length == 2) {
            step = splitSeperator[1];
        } else {
            step = null;
        }
        return step;
    }

    @Override
    protected String parseValue(String value) {
        if ("*".equals(value) || StringUtils.isEmpty(value)) { //$NON-NLS-1$
            return null;
        } else {
            return super.parseValue(value);
        }
    }

    private boolean isEqualContentRange(String lowerBound, String upperBound, String step) {
        if (getValueSet() instanceof IRangeValueSet) {
            IRangeValueSet range = (IRangeValueSet)getValueSet();
            return ObjectUtils.equals(range.getLowerBound(), lowerBound)
                    && ObjectUtils.equals(range.getUpperBound(), upperBound)
                    && ObjectUtils.equals(step, range.getStep());
        }
        return false;
    }

    private IValueSet createNewRangeValueSetContainingNull(String lowerBound, String upperBound, String step) {
        IValueSet valueSet = getValueSet();
        IRangeValueSet range = new RangeValueSet(getValueSetOwner(), getNextPartIdOfValueSetOwner(), lowerBound,
                upperBound, step);
        setIsContainingNull(valueSet, range);
        return range;
    }

    private void setIsContainingNull(IValueSet valueSet, IRangeValueSet range) {
        if (valueSet instanceof IRangeValueSet) {
            IRangeValueSet oldRange = (IRangeValueSet)valueSet;
            range.setContainsNull(oldRange.isContainingNull());
        }
    }

    public IValueSet getUnlimitedRangeSet() {
        IValueSet valueSet = getValueSet();
        if (valueSet.isRange() && isUnlimitedRange((IRangeValueSet)valueSet)) {
            return valueSet;
        } else {
            return createNewRangeValueSetContainingNull(null, null, null);
        }
    }

    private boolean isUnlimitedRange(IRangeValueSet valueSet) {
        return valueSet.getLowerBound() == null && valueSet.getUpperBound() == null;
    }

    @Override
    protected void verifyInternal(VerifyEvent e, String resultingText) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void initFormat(Locale locale) {
        // TODO Auto-generated method stub

    }
}
