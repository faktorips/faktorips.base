/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.valueset;

import static org.faktorips.devtools.model.util.DatatypeUtil.isNonNull;
import static org.faktorips.devtools.model.util.DatatypeUtil.isNullValue;

import java.text.MessageFormat;
import java.util.Objects;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.faktorips.datatype.NumericDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.IIpsModelExtensions;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.internal.ipsobject.DescriptionHelper;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.valueset.IEnumValueSet;
import org.faktorips.devtools.model.valueset.IRangeValueSet;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.devtools.model.valueset.IValueSetOwner;
import org.faktorips.devtools.model.valueset.ValueSetType;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A value set that describes a range with a lower and an upper bound, e.g. 100-200. Lower and upper
 * bound are part of the range. If lower bound or upper bound contain an empty string, the range is
 * unbounded. The range has an optional step attribute to define that only the values where
 * <code>((value-lower) mod step)== 0</code> holds true are included. E.g. 100-200 with step 10
 * defines the values 100, 110, 120, ... 200.
 * 
 * @author Jan Ortmann
 */
public class RangeValueSet extends ValueSet implements IRangeValueSet {

    public static final String XML_TAG_RANGE = ValueToXmlHelper.XML_TAG_RANGE;

    private String lowerBound;
    private String upperBound;
    private String step;

    /**
     * Flag that indicates whether this range contains <code>null</code> or not.
     */
    private boolean containsNull = false;

    /**
     * Flag that indicates whether this range is empty. An empty range contains no values.
     * 
     * @since 20.6
     */
    private boolean empty = false;

    /**
     * Creates an unbounded range with no step.
     */
    public RangeValueSet(IValueSetOwner parent, String partId) {
        this(parent, partId, null, null, null);
    }

    /**
     * Creates a range with the given bounds and step.
     */
    public RangeValueSet(IValueSetOwner parent, String partId, String lower, String upper, String step) {
        this(parent, partId, lower, upper, step, false);
    }

    /**
     * Creates a range with the given bounds, step and continasNull setting.
     */
    public RangeValueSet(IValueSetOwner parent, String partId, String lower, String upper, String step,
            boolean containsNull) {
        super(ValueSetType.RANGE, parent, partId);
        lowerBound = lower;
        upperBound = upper;
        this.step = step;
        this.containsNull = containsNull;
    }

    /**
     * Creates an empty range that allows no values.
     */
    public static RangeValueSet empty(IValueSetOwner parent, String partId) {
        RangeValueSet emptyRangeValueSet = new RangeValueSet(parent, partId);
        emptyRangeValueSet.empty = true;
        emptyRangeValueSet.containsNull = false;
        return emptyRangeValueSet;
    }

    /**
     * Sets the lower bound. An empty string or {@code null} means that the range is unbounded.
     */
    @Override
    public void setLowerBound(String lowerBound) {
        String oldBound = this.lowerBound;
        this.lowerBound = lowerBound;
        valueChanged(oldBound, lowerBound);
        if (lowerBound != null && !lowerBound.isEmpty()) {
            setEmpty(false);
        }
    }

    /**
     * Sets the step. An empty string or {@code null} means that no step exists and all possible
     * values in the range are valid.
     */
    @Override
    public void setStep(String step) {
        String oldStep = this.step;
        this.step = step;
        valueChanged(oldStep, step);
    }

    /**
     * Sets the upper bound. An empty string or {@code null} means that the range is unbounded.
     */
    @Override
    public void setUpperBound(String upperBound) {
        String oldBound = this.upperBound;
        this.upperBound = upperBound;
        valueChanged(oldBound, upperBound);
        if (upperBound != null && !upperBound.isEmpty()) {
            setEmpty(false);
        }
    }

    /**
     * Returns the lower bound of the range
     */
    @Override
    public String getLowerBound() {
        return lowerBound;
    }

    /**
     * Returns the upper bound of the range
     */
    @Override
    public String getUpperBound() {
        return upperBound;
    }

    /**
     * Returns the step of the range
     */
    @Override
    public String getStep() {
        return step;
    }

    @Override
    public boolean containsValue(String value, IIpsProject ipsProject) throws CoreRuntimeException {
        ValueDatatype datatype = findValueDatatype(ipsProject);
        if (!(datatype instanceof NumericDatatype)) {
            return false;
        }
        if (!isValid(ipsProject)) {
            return false;
        }
        return checkValueInRange(value, datatype);
    }

    /**
     * The value is in the range in one of the following cases:
     * <ul>
     * <li>The value is null (according to definition of the datatype) and the range contains the
     * null value</li>
     * <li>The range is abstract and hence all values are allowed (except null if
     * {@link #isContainsNull()} is false)</li>
     * <li>The value lies between the upper and the lower value</li>
     * </ul>
     */
    private boolean checkValueInRange(String value, ValueDatatype datatype) {
        try {
            if (isEmpty()) {
                return false;
            }
            if (isNullValue(datatype, value)) {
                return isContainsNull();
            }
            if (isAbstract()) {
                return true;
            }
            if ((!isNullValue(datatype, getLowerBound()) && datatype.compare(getLowerBound(), value) > 0)
                    || (!isNullValue(datatype, getUpperBound()) && datatype.compare(getUpperBound(), value) < 0)) {
                return false;
            }
            return isValueMatchStep(value, datatype);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * if the lower bound is set, the value to check is not the real value but the value reduced by
     * the lower bound! In a range from 1-5, Step 2 the values 1, 3 and 5 are valid, not 2 and 4.
     */
    private boolean isValueMatchStep(String value, ValueDatatype datatype) {
        String diff = value;
        NumericDatatype numDatatype = (NumericDatatype)datatype;
        if (!isNullValue(datatype, getStep())) {
            diff = numDatatype.subtract(value, getLowerBound());
            return numDatatype.divisibleWithoutRemainder(diff, getStep());
        } else {
            return true;
        }
    }

    @Override
    public boolean containsValueSet(IValueSet subset) {
        IIpsProject contextProject = subset.getIpsProject();
        ValueDatatype datatype = findValueDatatype(contextProject);
        ValueDatatype subDatatype = subset.findValueDatatype(contextProject);
        if (!Objects.equals(datatype, subDatatype)) {
            return false;
        }
        if (!checkValidRanges(subset, contextProject)) {
            return false;
        }

        if (subset.isEnum()) {
            return checkIsRangeSubset((IEnumValueSet)subset, (NumericDatatype)datatype);
        } else {
            return checkIsRangeSubset((IRangeValueSet)subset, (NumericDatatype)datatype);
        }
    }

    private boolean checkValidRanges(IValueSet subset, IIpsProject contextProject) {
        if (!isValid(contextProject)) {
            return false;
        }
        if (!(subset.isRange() || subset.isEnum()) || !subset.isValid(contextProject)) {
            return false;
        }
        return true;
    }

    /**
     * Another range is a subset of this range if the following conditions match:
     * <ul>
     * <li>An abstract valueset is considered containing all values and thus all non-abstract
     * rangeValueSets.</li></li>If this range is not abstract, an other abstract range cannot be a
     * subset of this range</li>
     * <li>The other range is no subset if it contains null but this range does not</li>
     * <li>If both ranges are not abstract, the other range is a subset if every value that is
     * allowed in the other range is also allowed in this range, according to lower bound, upper
     * bound and step</li>
     * </ul>
     */
    private boolean checkIsRangeSubset(IRangeValueSet subRange, NumericDatatype datatype) {
        if (subRange.isEmpty()) {
            return true;
        }
        if (isEmpty()) {
            return false;
        }
        if (!isContainsNull() && subRange.isContainsNull()) {
            return false;
        }
        if (isAbstract()) {
            return true;
        }
        if (subRange.isAbstract()) {
            return false;
        }

        String lower = getLowerBound();
        String subLower = subRange.getLowerBound();
        if (isMatchLowerBound(datatype, lower, subLower)) {
            return false;
        }

        String upper = getUpperBound();
        String subUpper = subRange.getUpperBound();
        if (isMatchUpperBound(upper, subUpper, datatype)) {
            return false;
        }

        return isSubrangeMatchStep(subRange, datatype);
    }

    private boolean checkIsRangeSubset(IEnumValueSet subRange, NumericDatatype datatype) {
        if (!isContainsNull() && subRange.isContainsNull()) {
            return false;
        }
        if (isAbstract()) {
            return true;
        }
        if (subRange.isAbstract()) {
            return false;
        }

        for (String value : subRange.getValues()) {
            if (!checkValueInRange(value, datatype)) {
                return false;
            }
        }
        return true;
    }

    private boolean isMatchLowerBound(NumericDatatype datatype, String lower, String subLower) {
        return !isNullValue(datatype, lower)
                && (isNullValue(datatype, subLower) || datatype.compare(lower, subLower) > 0);
    }

    private boolean isMatchUpperBound(String upper, String subUpper, NumericDatatype datatype) {
        return !isNullValue(datatype, upper)
                && (isNullValue(datatype, subUpper) || datatype.compare(upper, subUpper) < 0);
    }

    private boolean isSubrangeMatchStep(IRangeValueSet other, NumericDatatype datatype) {
        String subStep = other.getStep();
        if (isNullValue(datatype, step)) {
            // every valid sub-step is allowed
            return true;
        } else if (isNullValue(datatype, subStep)) {
            // null is no valid sub-step because this step is not null
            return false;
        } else if (!datatype.divisibleWithoutRemainder(subStep, step)) {
            // sub-step must be divisor of step
            return false;
        } else {
            return isSubBoundsMatchingStep(other, datatype);
        }
    }

    private boolean isSubBoundsMatchingStep(IRangeValueSet other, NumericDatatype datatype) {
        String lower = getLowerBound();
        String subLower = other.getLowerBound();
        String upper = getUpperBound();
        String subUpper = other.getUpperBound();
        String diffLower = datatype.subtract(subLower, lower);
        if (!datatype.divisibleWithoutRemainder(diffLower, step)) {
            return false;
        }
        if (isNonNull(datatype, upper, subUpper)) {
            String diffUpper = datatype.subtract(upper, subUpper);
            if (!datatype.divisibleWithoutRemainder(diffUpper, step)) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected RangeValueSetValidator createValidator(IValueSetOwner owner, ValueDatatype datatype) {
        return new RangeValueSetValidator(this, owner, datatype);
    }

    @Override
    public void validateThis(MessageList list, IIpsProject ipsProject) throws CoreRuntimeException {
        super.validateThis(list, ipsProject);
        RangeValueSetValidator validator = createValidator(getValueSetOwner(), findValueDatatype(ipsProject));
        list.add(validator.validate());
    }

    @Override
    public ValueSetType getValueSetType() {
        return ValueSetType.RANGE;
    }

    @Override
    public int compareTo(IValueSet o) {
        if (o.isRange()) {
            IRangeValueSet otherRangeValueSet = (IRangeValueSet)o;
            boolean thisContainsOther = this.containsValueSet(o);
            boolean otherContainsThis = o.containsValueSet(this);
            if (thisContainsOther && otherContainsThis) {
                return 0;
            } else if (thisContainsOther) {
                return 1;
            } else if (otherContainsThis) {
                return -1;
            } else {
                return compareDifferentRanges(otherRangeValueSet);
            }
        } else {
            return compareDifferentValueSets(o);
        }

    }

    /**
     * Compare two ranges that are known to be different. It is important that these two ranges will
     * never be treated as equal and that the comparison is symmetric.
     * 
     * For better performance we ignore the datatype comparison - the equality is already proved
     * using datatype compare.
     */
    private int compareDifferentRanges(IRangeValueSet otherRangeValueSet) {
        int compareLow = ObjectUtils.compare(getLowerBound(), otherRangeValueSet.getLowerBound());
        if (compareLow != 0) {
            return compareLow;
        }
        int compareUp = ObjectUtils.compare(getUpperBound(), otherRangeValueSet.getUpperBound());
        if (compareUp != 0) {
            return compareUp;
        }
        return ObjectUtils.compare(getStep(), otherRangeValueSet.getStep());
    }

    @Override
    public String toString() {
        if (isAbstract()) {
            return super.toString() + "(abstract)"; //$NON-NLS-1$
        }
        return super.toString() + ":" + toShortString(); //$NON-NLS-1$
    }

    @Override
    public String toShortString() {
        return getCanonicalString();
    }

    @Override
    public String getCanonicalString() {
        StringBuilder sb = new StringBuilder();
        sb.append(RANGE_VALUESET_START);
        if (!isEmpty()) {
            sb.append((lowerBound == null ? Messages.RangeValueSet_unlimited : lowerBound));
            sb.append(RANGE_VALUESET_POINTS);
            sb.append((upperBound == null ? Messages.RangeValueSet_unlimited : upperBound));
            if (step != null) {
                sb.append(RANGE_STEP_SEPERATOR);
                sb.append(step);
            }
        }
        sb.append(RANGE_VALUESET_END);
        if (isContainsNull()) {
            sb.append(" (").append(MessageFormat.format(Messages.ValueSet_includingNull, //$NON-NLS-1$
                    IIpsModelExtensions.get().getModelPreferences().getNullPresentation())).append(")"); //$NON-NLS-1$
        }
        return sb.toString();
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        Element el = DescriptionHelper.getFirstNoneDescriptionElement(element);
        if (el.hasAttribute(PROPERTY_LOWERBOUND)) {
            // old format prior to 1.0.0.rc2
            lowerBound = el.getAttribute(PROPERTY_LOWERBOUND);
            if ("".equals(lowerBound)) { //$NON-NLS-1$
                lowerBound = null;
            }
            upperBound = el.getAttribute(PROPERTY_UPPERBOUND);
            if ("".equals(upperBound)) { //$NON-NLS-1$
                upperBound = null;
            }
            step = el.getAttribute(PROPERTY_STEP);
            if ("".equals(step)) { //$NON-NLS-1$
                step = null;
            }
        } else {
            // new format since 1.0.0.rc2
            lowerBound = ValueToXmlHelper.getValueFromElement(el, StringUtils.capitalize(PROPERTY_LOWERBOUND));
            upperBound = ValueToXmlHelper.getValueFromElement(el, StringUtils.capitalize(PROPERTY_UPPERBOUND));
            step = ValueToXmlHelper.getValueFromElement(el, StringUtils.capitalize(PROPERTY_STEP));
        }
        containsNull = ValueToXmlHelper.isAttributeTrue(el, PROPERTY_CONTAINS_NULL);
        empty = ValueToXmlHelper.isAttributeTrue(el, PROPERTY_EMPTY);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        Document doc = element.getOwnerDocument();
        Element tagElement = doc.createElement(XML_TAG_RANGE);
        tagElement.setAttribute(PROPERTY_CONTAINS_NULL, Boolean.toString(isContainsNull()));
        ValueToXmlHelper.addValueToElement(lowerBound, tagElement, StringUtils.capitalize(PROPERTY_LOWERBOUND));
        ValueToXmlHelper.addValueToElement(upperBound, tagElement, StringUtils.capitalize(PROPERTY_UPPERBOUND));
        ValueToXmlHelper.addValueToElement(step, tagElement, StringUtils.capitalize(PROPERTY_STEP));
        if (isEmpty()) {
            tagElement.setAttribute(PROPERTY_EMPTY, Boolean.toString(isEmpty()));
        }
        element.appendChild(tagElement);
    }

    @Override
    public IValueSet copy(IValueSetOwner parent, String id) {
        RangeValueSet retValue = new RangeValueSet(parent, id);
        retValue.lowerBound = lowerBound;
        retValue.upperBound = upperBound;
        retValue.step = step;
        retValue.containsNull = isContainsNull();
        retValue.empty = empty;
        return retValue;
    }

    @Override
    public void copyPropertiesFrom(IValueSet source) {
        RangeValueSet set = (RangeValueSet)source;
        lowerBound = set.lowerBound;
        upperBound = set.upperBound;
        step = set.step;
        containsNull = set.isContainsNull();
        empty = set.empty;
        objectHasChanged();
    }

    @Override
    public boolean isContainsNull() {
        return containsNull && isContainingNullAllowed(getIpsProject());
    }

    @Override
    public void setContainsNull(boolean containsNull) {
        boolean old = this.isContainsNull();
        this.containsNull = containsNull;
        valueChanged(old, containsNull, PROPERTY_CONTAINS_NULL);
    }

    @Override
    public boolean isEmpty() {
        return empty;
    }

    @Override
    public void setEmpty(boolean empty) {
        boolean oldEmpty = this.empty;
        this.empty = empty;
        valueChanged(oldEmpty, empty);
        if (empty) {
            setContainsNull(false);
            setLowerBound(null);
            setUpperBound(null);
            setStep(null);
        }
    }
}
