/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.valueset;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.NumericDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.ipsobject.DescriptionHelper;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.valueset.IRangeValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A value set that desribes a range with a lower and an upper bound, e.g. 100-200. Lower and upper
 * bound are part of the range. If lower bound or upper bound contain an empty string, the range is
 * unbounded. The range has an optional step attribute to define that only the values where
 * <code>((value-lower) mod step)== 0</code> holds true. E.g. 100-200 with step 10 defines the
 * values 100, 110, 120, ... 200.
 * 
 * @author Jan Ortmann
 */
public class RangeValueSet extends ValueSet implements IRangeValueSet {

    public final static String XML_TAG = "Range"; //$NON-NLS-1$

    private String lowerBound;
    private String upperBound;
    private String step;

    /**
     * Flag that indicates whether this range contains <code>null</code> or not.
     */
    private boolean containsNull;

    /**
     * Creates an unbounded range with no step.
     */
    public RangeValueSet(IIpsObjectPart parent, int partId) {
        this(parent, partId, null, null, null);
    }

    /**
     * Creates a range with the given bounds and and step.
     */
    public RangeValueSet(IIpsObjectPart parent, int partId, String lower, String upper, String step) {
        super(ValueSetType.RANGE, parent, partId);
        lowerBound = lower;
        upperBound = upper;
        this.step = step;
    }

    /**
     * Sets the lower bound. An empty string means that the range is unbouned.
     * 
     * @throws NullPointerException if lowerBound is <code>null</code>.
     */
    public void setLowerBound(String lowerBound) {
        String oldBound = this.lowerBound;
        this.lowerBound = lowerBound;
        valueChanged(oldBound, lowerBound);
    }

    /**
     * Sets the step. An empty string means that no step exists and all possible values in the range
     * are valid.
     * 
     * @throws NullPointerException if step is <code>null</code>.
     */
    public void setStep(String step) {
        String oldStep = this.step;
        this.step = step;
        valueChanged(oldStep, step);
    }

    /**
     * Sets the upper bound. An empty string means that the range is unbounded.
     * 
     * @throws NullPointerException if upperBound is <code>null</code>.
     */
    public void setUpperBound(String upperBound) {
        String oldBound = this.upperBound;
        this.upperBound = upperBound;
        valueChanged(oldBound, upperBound);
    }

    /**
     * Returns the lower bound of the range
     */
    public String getLowerBound() {
        return lowerBound;
    }

    /**
     * Returns the upper bound of the range
     */
    public String getUpperBound() {
        return upperBound;
    }

    /**
     * Returns the step of the range
     */
    public String getStep() {
        return step;
    }

    /**
     * {@inheritDoc}
     */
    public boolean containsValue(String value,
            MessageList list,
            Object invalidObject,
            String invalidProperty,
            IIpsProject ipsProject) throws CoreException {
        if (list == null) {
            throw new NullPointerException("MessageList required."); //$NON-NLS-1$
        }

        ValueDatatype datatype = findValueDatatype(ipsProject);

        if (datatype == null) {
            addMsg(list, Message.WARNING, MSGCODE_UNKNOWN_DATATYPE, Messages.RangeValueSet_msgDatatypeUnknown,
                    invalidObject, invalidProperty);
            return false;
        }

        if (!datatype.supportsCompare()) {
            String msg = NLS.bind(Messages.RangeValueSet_msgDatatypeNotComparable, datatype.getQualifiedName());
            addMsg(list, MSGCODE_DATATYPE_NOT_COMPARABLE, msg, invalidObject, invalidProperty);
            return false;
        }

        if (!datatype.isParsable(step)) {
            String msg = NLS.bind(Messages.RangeValueSet_msgStepNotParsable, step, datatype.getQualifiedName());
            addMsg(list, MSGCODE_STEP_NOT_PARSABLE, msg, invalidObject, invalidProperty);
            return false;
        }

        try {

            if (datatype.isNull(value)) {
                return containsNull;
            }
            /*
             * An abstract valueset is considered containing all values. See #isAbstract()
             */
            if (isAbstract()) {
                return true;
            }

            String lower = getLowerBound();
            String upper = getUpperBound();
            if ((lower != null && datatype.compare(lower, value) > 0)
                    || (upper != null && datatype.compare(upper, value) < 0)) {
                String text = NLS.bind(Messages.Range_msgValueNotInRange, new Object[] { lower, upper, step });
                addMsg(list, MSGCODE_VALUE_NOT_CONTAINED, text + '.', invalidObject, invalidProperty);
                return false;
            }
        } catch (IllegalArgumentException e) {
            return false;
        }

        NumericDatatype numDatatype = getAndValidateNumericDatatype(datatype, list);

        String diff = value;

        // if the lower bound is set, the value to check is not the real value but
        // the value reduced by the lower bound! In a range from 1-5, Step 2 the
        // values 1, 3 and 5 are valid, not 2 and 4.
        if (!StringUtils.isEmpty(getLowerBound())) {
            diff = numDatatype.subtract(value, getLowerBound());
        }

        if (!StringUtils.isEmpty(getStep()) && numDatatype != null
                && !numDatatype.divisibleWithoutRemainder(diff, getStep())) {
            String msg = NLS.bind(Messages.RangeValueSet_msgStepViolation, value, getStep());
            addMsg(list, MSGCODE_STEP_VIOLATION, msg, invalidObject, invalidProperty);
            return false;

        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean containsValueSet(IValueSet subset, MessageList list, Object invalidObject, String invalidProperty) {
        if (list == null) {
            throw new NullPointerException("MessageList required"); //$NON-NLS-1$
        }

        ValueDatatype datatype = getValueDatatype();
        ValueDatatype subDatatype = ((ValueSet)subset).getValueDatatype();
        if (datatype == null || subDatatype == null) {
            addMsg(list, Message.WARNING, MSGCODE_UNKNOWN_DATATYPE, Messages.RangeValueSet_msgDatatypeUnknown,
                    invalidObject, invalidProperty);
            return false;
        }

        if (!(subset instanceof RangeValueSet)) {
            addMsg(list, MSGCODE_TYPE_OF_VALUESET_NOT_MATCHING, Messages.Range_msgTypeOfValuesetNotMatching,
                    invalidObject, invalidProperty);
            return false;
        }

        /*
         * An abstract valueset is considered containing all values and thus all non-abstract
         * rangeValueSets. See #isAbstract()
         */
        if (isAbstract()) {
            return true;
        }
        if (subset.isAbstract()) {
            return false;
        }

        IRangeValueSet subRange = (IRangeValueSet)subset;
        boolean isSubset = true;
        if (step != null) {
            if (subRange.getStep() == null) {
                String msg = Messages.Range_msgNoStepDefinedInSubset;
                addMsg(list, MSGCODE_NO_STEP_DEFINED_IN_SUBSET, msg, invalidObject, getProperty(invalidProperty,
                        PROPERTY_STEP));
                isSubset = false;
            } else {
                String step = getStep();
                String subStep = subRange.getStep();

                validateParsable(datatype, step, list, invalidObject, invalidProperty);
                validateParsable(datatype, subStep, list, invalidObject, invalidProperty);
            }
        }

        String lower = getLowerBound();
        String subLower = subRange.getLowerBound();
        if (validateParsable(datatype, lower, list, invalidObject, invalidProperty)
                && validateParsable(datatype, subLower, list, invalidObject, invalidProperty)) {
            if (!datatype.isNull(lower) && !datatype.isNull(subLower) && datatype.compare(lower, subLower) > 0) {
                String msg = NLS.bind(Messages.Range_msgLowerBoundViolation, getLowerBound(), subRange.getLowerBound());
                addMsg(list, MSGCODE_LOWER_BOUND_VIOLATION, msg, invalidObject, getProperty(invalidProperty,
                        PROPERTY_LOWERBOUND));
                isSubset = false;
            }
        }

        String upper = getUpperBound();
        String subUpper = subRange.getUpperBound();
        if (validateParsable(datatype, upper, list, invalidObject, invalidProperty)
                && validateParsable(datatype, subUpper, list, invalidObject, invalidProperty)) {
            if (!datatype.isNull(upper) && !datatype.isNull(subUpper) && datatype.compare(upper, subUpper) < 0) {
                String msg = NLS.bind(Messages.Range_msgUpperBoundViolation, getUpperBound(), subRange.getUpperBound());
                addMsg(list, MSGCODE_UPPER_BOUND_VIOLATION, msg, invalidObject, getProperty(invalidProperty,
                        PROPERTY_UPPERBOUND));
                isSubset = false;
            }
        }

        if (lower != null && subRange.getLowerBound() == null) {
            String[] bindings = { subRange.toShortString(), toShortString(), getLowerBound() };
            String msg = NLS.bind(Messages.RangeValueSet_msgLowerboundViolation, bindings);
            addMsg(list, MSGCODE_LOWER_BOUND_VIOLATION, msg, invalidObject, getProperty(invalidProperty,
                    PROPERTY_LOWERBOUND));
            isSubset = false;
        }

        if (upper != null && subRange.getUpperBound() == null) {
            String[] bindings = { subRange.toShortString(), toShortString(), getUpperBound() };
            String msg = NLS.bind(Messages.RangeValueSet_msgUpperboundViolation, bindings);
            addMsg(list, MSGCODE_UPPER_BOUND_VIOLATION, msg, invalidObject, getProperty(invalidProperty,
                    PROPERTY_UPPERBOUND));
            isSubset = false;
        }

        if (subRange.getContainsNull() && !getContainsNull()) {
            String msg = NLS.bind(Messages.RangeValueSet_msgNullNotContained, IpsPlugin.getDefault()
                    .getIpsPreferences().getNullPresentation());
            addMsg(list, MSGCODE_NOT_SUBSET, msg, invalidObject, getProperty(invalidProperty, PROPERTY_CONTAINS_NULL));
            isSubset = false;
        }

        NumericDatatype numDatatype = getAndValidateNumericDatatype(datatype, list);

        if (!matchStep(subRange, numDatatype, list, invalidObject, invalidProperty)) {
            isSubset = false;
        }

        return isSubset;
    }

    private boolean matchStep(IRangeValueSet other,
            NumericDatatype datatype,
            MessageList list,
            Object invalidObject,
            String invalidProperty) {

        boolean match = true;
        String subStep = other.getStep();

        if (subStep == null && step != null) {
            return false;
        }

        if (datatype == null) {
            return true; // no datatype, so we can not decide if matching or not - return true in
            // this case.
        }

        if (isSetAndParsable(subStep, datatype) && isSetAndParsable(step, datatype)) {
            // both steps are set and the substep is valid
            if (!datatype.divisibleWithoutRemainder(subStep, step)) {
                String msg = NLS.bind(Messages.RangeValueSet_msgStepMismatch, other.toShortString(), toShortString());
                addMsg(list, MSGCODE_STEP_MISMATCH, msg, invalidObject, getProperty(invalidProperty, PROPERTY_STEP));
                match = false;
            }

            String lower = getLowerBound();
            String subLower = other.getLowerBound();
            String upper = getUpperBound();
            String subUpper = other.getUpperBound();

            if (isSetAndParsable(lower, datatype) && isSetAndParsable(subLower, datatype)) {
                // this valueset has a lower bound, so we have to check against the difference of
                // the both lower bounds
                String diff = datatype.subtract(subLower, lower);
                if (!datatype.divisibleWithoutRemainder(diff, step)) {
                    String msg = NLS.bind(Messages.RangeValueSet_msgLowerboundMismatch, diff, step);
                    addMsg(list, MSGCODE_LOWERBOUND_MISMATCH, msg, invalidObject, getProperty(invalidProperty,
                            PROPERTY_LOWERBOUND));

                    match = false;
                }
            }

            if (isSetAndParsable(upper, datatype) && isSetAndParsable(subUpper, datatype)) {
                // this valueset has an upper bound, so we have to check against the difference of
                // the both upper bounds
                String diff = datatype.subtract(upper, subUpper);
                if (!datatype.divisibleWithoutRemainder(diff, step)) {
                    String msg = NLS.bind(Messages.RangeValueSet_msgUpperboundMismatch, diff, step);
                    addMsg(list, MSGCODE_UPPERBOUND_MISMATCH, msg, invalidObject, getProperty(invalidProperty,
                            PROPERTY_UPPERBOUND));

                    match = false;
                }
            }

            if (isSetAndParsable(subLower, datatype) && isSetAndParsable(subUpper, datatype)) {
                // both the upper and the lower bound of the sub-valueset are set, so we have to
                // validate that the difference
                // of both is divisible without remainder by the given step.
                String diff = datatype.subtract(subUpper, subLower);
                if (!datatype.divisibleWithoutRemainder(diff, subStep)
                        && list.getMessageByCode(MSGCODE_STEP_RANGE_MISMATCH) == null) {
                    String[] props = { subLower, subUpper, subStep };
                    String msg = NLS.bind(Messages.RangeValueSet_msgStepRangeMismatch, props);
                    addMsg(list, MSGCODE_STEP_RANGE_MISMATCH, msg, invalidObject, getProperty(invalidProperty,
                            PROPERTY_STEP));
                    match = false;
                }
            }
        }

        return match;
    }

    private boolean isSetAndParsable(String value, NumericDatatype datatype) {
        return datatype.isParsable(value) && !datatype.isNull(value);
    }

    /**
     * {@inheritDoc}
     */
    public boolean containsValueSet(IValueSet subset) {
        MessageList dummy = new MessageList();
        return containsValueSet(subset, dummy, null, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        ValueDatatype datatype = getValueDatatype();
        if (datatype == null) {
            String text = Messages.Range_msgUnknownDatatype;
            list.add(new Message(MSGCODE_UNKNOWN_DATATYPE, text, Message.WARNING, this, new String[] {
                    PROPERTY_LOWERBOUND, PROPERTY_UPPERBOUND, PROPERTY_STEP }));
            return;
        }
        if (datatype.isPrimitive()) {
            if (getContainsNull()) {
                String text = Messages.RangeValueSet_msgNullNotSupported;
                list.add(new Message(MSGCODE_NULL_NOT_SUPPORTED, text, Message.ERROR, this, PROPERTY_CONTAINS_NULL));
            }
            // even if the basic datatype is a primitve, null is allowed for upper, lower bound and
            // step.
            datatype = datatype.getWrapperType();
        }

        validateParsable(datatype, getLowerBound(), list, this, PROPERTY_LOWERBOUND);
        validateParsable(datatype, getUpperBound(), list, this, PROPERTY_UPPERBOUND);
        boolean stepParsable = validateParsable(datatype, getStep(), list, this, PROPERTY_STEP);

        String lowerValue = getLowerBound();
        String upperValue = getUpperBound();
        if (list.getSeverity() == Message.ERROR) {
            return;
        }
        if (!datatype.isNull(lowerValue) && !datatype.isNull(upperValue)) {
            // range is not unbounded on one side
            if (datatype.compare(lowerValue, upperValue) > 0) {
                String text = Messages.Range_msgLowerboundGreaterUpperbound;
                list.add(new Message(MSGCODE_LBOUND_GREATER_UBOUND, text, Message.ERROR, this, new String[] {
                        PROPERTY_LOWERBOUND, PROPERTY_UPPERBOUND }));
                return;
            }
        }

        NumericDatatype numDatatype = getAndValidateNumericDatatype(datatype, list);
        if (stepParsable && numDatatype != null && !StringUtils.isEmpty(upperValue) && !StringUtils.isEmpty(lowerValue)
                && !StringUtils.isEmpty(getStep())) {
            String range = numDatatype.subtract(upperValue, lowerValue);
            if (!numDatatype.divisibleWithoutRemainder(range, step)) {
                String msg = NLS.bind(Messages.RangeValueSet_msgStepRangeMismatch, new String[] { lowerValue,
                        upperValue, getStep() });
                list.add(new Message(MSGCODE_STEP_RANGE_MISMATCH, msg, Message.ERROR, this, new String[] {
                        PROPERTY_LOWERBOUND, PROPERTY_UPPERBOUND, PROPERTY_STEP }));
            }
        }
    }

    private NumericDatatype getAndValidateNumericDatatype(ValueDatatype datatype, MessageList list) {
        if (datatype instanceof NumericDatatype) {
            return (NumericDatatype)datatype;
        }

        String text = Messages.RangeValueSet_msgDatatypeNotNumeric;
        list.add(new Message(MSGCODE_NOT_NUMERIC_DATATYPE, text, Message.ERROR, this));

        return null;
    }

    private boolean validateParsable(ValueDatatype datatype,
            String value,
            MessageList list,
            Object invalidObject,
            String property) {

        if (!datatype.isParsable(value)) {
            String msg = NLS.bind(Messages.Range_msgPropertyValueNotParsable, new Object[] { property, value,
                    datatype.getName() });
            addMsg(list, MSGCODE_VALUE_NOT_PARSABLE, msg, invalidObject, property);
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ValueSetType getValueSetType() {
        return ValueSetType.RANGE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        if (isAbstract()) {
            return super.toString() + "(abstract)";
        }
        return super.toString() + ":" + toShortString(); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public String toShortString() {
        StringBuffer sb = new StringBuffer();
        sb.append('[');
        sb.append((lowerBound == null ? "unlimited" : lowerBound)); //$NON-NLS-1$
        sb.append('-');
        sb.append((upperBound == null ? "unlimited" : upperBound)); //$NON-NLS-1$
        sb.append(']');
        if (step != null) {
            sb.append(Messages.RangeValueSet_0);
            sb.append(step);
        }
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initPropertiesFromXml(Element element, Integer id) {
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
            // new format sind 1.0.0.rc2
            lowerBound = ValueToXmlHelper.getValueFromElement(el, StringUtils.capitalize(PROPERTY_LOWERBOUND));
            upperBound = ValueToXmlHelper.getValueFromElement(el, StringUtils.capitalize(PROPERTY_UPPERBOUND));
            step = ValueToXmlHelper.getValueFromElement(el, StringUtils.capitalize(PROPERTY_STEP));
        }

        containsNull = Boolean.valueOf(el.getAttribute(PROPERTY_CONTAINS_NULL)).booleanValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        Document doc = element.getOwnerDocument();
        Element tagElement = doc.createElement(XML_TAG);
        tagElement.setAttribute(PROPERTY_CONTAINS_NULL, Boolean.toString(containsNull));
        ValueToXmlHelper.addValueToElement(lowerBound, tagElement, StringUtils.capitalize(PROPERTY_LOWERBOUND));
        ValueToXmlHelper.addValueToElement(upperBound, tagElement, StringUtils.capitalize(PROPERTY_UPPERBOUND));
        ValueToXmlHelper.addValueToElement(step, tagElement, StringUtils.capitalize(PROPERTY_STEP));
        element.appendChild(tagElement);
    }

    /**
     * {@inheritDoc}
     */
    public IValueSet copy(IIpsObjectPart parent, int id) {
        RangeValueSet retValue = new RangeValueSet(parent, id);

        retValue.lowerBound = lowerBound;
        retValue.upperBound = upperBound;
        retValue.step = step;
        retValue.containsNull = containsNull;

        return retValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void copyPropertiesFrom(IValueSet source) {
        RangeValueSet set = (RangeValueSet)source;
        lowerBound = set.lowerBound;
        upperBound = set.upperBound;
        step = set.step;
        containsNull = set.containsNull;
        objectHasChanged();
    }

    /**
     * {@inheritDoc}
     */
    public boolean getContainsNull() {
        return containsNull;
    }

    /**
     * {@inheritDoc}
     */
    public void setContainsNull(boolean containsNull) {
        boolean old = this.containsNull;
        this.containsNull = containsNull;
        valueChanged(old, containsNull);
    }

}
