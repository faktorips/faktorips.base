/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.NumericDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IRangeValueSet;
import org.faktorips.devtools.core.model.IValueSet;
import org.faktorips.devtools.core.model.Messages;
import org.faktorips.devtools.core.model.ValueSetType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.runtime.ObjectProperty;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A value set that desribes a range with a lower and an upper bound, e.g. 100-200. Lower and upper bound
 * are part of the range. If lower bound or upper bound contain an empty string, the range is unbounded.
 * The range has an optional step attribute to define that only the values where <code>((value-lower) mod step)== 0</code>
 * holds true. E.g. 100-200 with step 10 defines the values 100, 110, 120, ... 200.
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
    	this(parent, partId, "", "", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
     * @throws NullPointerException  if lowerBound is <code>null</code>.
     */
    public void setLowerBound(String lowerBound) {
        ArgumentCheck.notNull(lowerBound);
        String oldBound = this.lowerBound;
        this.lowerBound = lowerBound;
        
        valueChanged(oldBound, lowerBound);
    }

    /**
     * Sets the step. An empty string means that no step exists and all possible
     * values in the range are valid.
     * 
     * @throws NullPointerException  if step is <code>null</code>.
     */
    public void setStep(String step) {
        ArgumentCheck.notNull(step);
        String oldStep = this.step;
        this.step = step;
        valueChanged(oldStep, step);
    }

    /**
     * Sets the upper bound. An empty string means that the range is unbounded.
     * 
     * @throws NullPointerException  if upperBound is <code>null</code>.
     */
    public void setUpperBound(String upperBound) {
        ArgumentCheck.notNull(upperBound);
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
    public boolean containsValue(String value) {
    	return containsValue(value, null, null, null);
    }

    /**
     * {@inheritDoc}
     */
    public boolean containsValue(String value, MessageList list, Object invalidObject, String invalidProperty) {
    	ValueDatatype datatype = getValueDatatype();
    	
    	if (datatype == null) {
    		if (list != null) {
    			list.add(new Message(MSGCODE_UNKNOWN_DATATYPE, Messages.RangeValueSet_msgDatatypeUnknown, Message.WARNING, invalidObject, invalidProperty));
    		}
    		return false;
    	}

    	if (!datatype.supportsCompare()) {
    		String msg = NLS.bind(Messages.RangeValueSet_msgDatatypeNotComparable, datatype.getQualifiedName());
    		list.add(new Message(MSGCODE_DATATYPE_NOT_COMPARABLE, msg, Message.ERROR, invalidObject, invalidProperty));
    		return false;
    	}
    	
    	try {
            if (datatype.isNull(value)) {
            	return containsNull;
            }
            
            String lower = getLowerBound();
            String upper = getUpperBound();
            if ((!lower.equals("") && datatype.compare(lower, value) > 0) //$NON-NLS-1$
                    || (!upper.equals("") && datatype.compare(upper, value) < 0)) { //$NON-NLS-1$
            	if (list != null) {
                    String text = NLS.bind(Messages.Range_msgValueNotInRange, new Object[] {lower, upper, step});
                    addMsg(list, MSGCODE_VALUE_NOT_CONTAINED, text + '.', invalidObject, invalidProperty);
            	}
                return false;
            }
        } catch (IllegalArgumentException e) {
            return false;
        }
        
        NumericDatatype numDatatype = getAndValidateNumericDatatype(datatype, list);
        if (!StringUtils.isEmpty(getStep()) && numDatatype != null && !numDatatype.divisibleWithoutRemainder(value, getStep())) {
        	String msg = NLS.bind(Messages.RangeValueSet_msgStepViolation, value, getStep());
        	if (list != null) {
        		addMsg(list, MSGCODE_STEP_VIOLATION, msg, invalidObject, invalidProperty);
        	}
        	return false;
        	
        }
        return true;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean containsValueSet(IValueSet subset, MessageList list,
			Object invalidObject, String invalidProperty) {

    	ValueDatatype datatype = getValueDatatype();
    	ValueDatatype subDatatype = ((ValueSet)subset).getValueDatatype();
    	if (datatype == null || subDatatype == null) {
    		if (list != null) {
    			list.add(new Message(MSGCODE_UNKNOWN_DATATYPE, Messages.RangeValueSet_msgDatatypeUnknown, Message.WARNING, invalidObject, invalidProperty));
    		}
    		return false;
    	}

    	if (!(subset instanceof RangeValueSet)) {
    		if (list != null) {
    			addMsg(list, MSGCODE_TYPE_OF_VALUESET_NOT_MATCHING,
						Messages.Range_msgTypeOfValuesetNotMatching, invalidObject,
						invalidProperty);
    		}
    		return false;
    	}

    	IRangeValueSet subRange = (IRangeValueSet)subset;
    	boolean isSubset = true;
    	if (!getStep().equals("")) { //$NON-NLS-1$
    		if (subRange.getStep().equals("")) { //$NON-NLS-1$
    			if (list != null) {
    				String msg = Messages.Range_msgNoStepDefinedInSubset;
    				addMsg(list, MSGCODE_NO_STEP_DEFINED_IN_SUBSET, msg,
							invalidObject, getProperty(invalidProperty, PROPERTY_STEP));
    				isSubset = false;
    			}
    		} else {
    			String step = getStep();
    			String subStep = subRange.getStep();
    			
    			validateParsable(datatype, step, list, invalidObject, invalidProperty);
    			validateParsable(datatype, subStep, list, invalidObject, invalidProperty);
    		}
    	}
    	
    	String lower = getLowerBound();
    	String subLower = subRange.getLowerBound();
    	validateParsable(datatype, lower, list, invalidObject, invalidProperty);
    	validateParsable(datatype, subLower, list, invalidObject, invalidProperty);
    	if (lower != null && subLower != null && !datatype.isNull(lower) && !datatype.isNull(subLower) && datatype.compare(lower, subLower) > 0) {
    		if (list != null) {
    			String msg = NLS.bind(Messages.Range_msgLowerBoundViolation, getLowerBound(), subRange.getLowerBound());
    			addMsg(list, MSGCODE_LOWER_BOUND_VIOLATION, msg, invalidObject, getProperty(invalidProperty, PROPERTY_LOWERBOUND));
    		}
    		isSubset = false;
    	}
    	
    	String upper = getUpperBound();
    	String subUpper = subRange.getUpperBound();
    	validateParsable(datatype, upper, list, invalidObject, invalidProperty);
    	validateParsable(datatype, subUpper, list, invalidObject, invalidProperty);
    	if (upper != null && subUpper != null && !datatype.isNull(upper) && !datatype.isNull(subUpper)  && datatype.compare(upper, subUpper) < 0) {
    		if (list != null) {
    			String msg = NLS.bind(Messages.Range_msgUpperBoundViolation, getUpperBound(), subRange.getUpperBound());
    			addMsg(list, MSGCODE_UPPER_BOUND_VIOLATION, msg, invalidObject, getProperty(invalidProperty, PROPERTY_UPPERBOUND));
    		}
    		isSubset = false;
    	}
    	
    	if (subRange.getContainsNull() && !getContainsNull()) {
			String msg = NLS.bind(Messages.RangeValueSet_msgNullNotContained, IpsPlugin.getDefault().getIpsPreferences().getNullPresentation());
			addMsg(list, MSGCODE_NOT_SUBSET, msg, invalidObject, getProperty(invalidProperty, PROPERTY_CONTAINS_NULL));
    		isSubset = false;
    	}
    	
    	String step = getStep();
    	String subStep = subRange.getStep();
    	
		NumericDatatype numDatatype = getAndValidateNumericDatatype(datatype, list);

		if (StringUtils.isEmpty(subStep) && !StringUtils.isEmpty(step)) {
    		isSubset = false;
    	}
    	else if (!StringUtils.isEmpty(subStep) && !StringUtils.isEmpty(step) && numDatatype != null) {
    		if (!numDatatype.divisibleWithoutRemainder(subStep, step)) {
    			if (list != null) {
    				String msg = NLS.bind(Messages.RangeValueSet_msgStepMismatch, subRange.toShortString(), toShortString());
    				ObjectProperty[] props = new ObjectProperty[2];
    				props[0] = new ObjectProperty(this, PROPERTY_STEP);
    				props[1] = new ObjectProperty(subRange, PROPERTY_STEP);
    				list.add(new Message(MSGCODE_STEP_MISMATCH, msg, Message.ERROR, props));
    			}
    			isSubset = false;
    		}
    		if (!numDatatype.divisibleWithoutRemainder(subLower, step)) {
    			if (list != null) {
    				String msg = NLS.bind(Messages.RangeValueSet_msgLowerboundMismatch, subRange.toShortString(), toShortString());
    				ObjectProperty[] props = new ObjectProperty[2];
    				props[0] = new ObjectProperty(this, PROPERTY_STEP);
    				props[1] = new ObjectProperty(subRange, PROPERTY_LOWERBOUND);
    				list.add(new Message(MSGCODE_LOWERBOUND_MISMATCH, msg, Message.ERROR, props));
    			}
    			
    			isSubset = false;
    		}
    		if (!numDatatype.divisibleWithoutRemainder(subUpper, step)) {
    			if (list != null) {
    				String msg = NLS.bind(Messages.RangeValueSet_msgUpperboundMismatch, subRange.toShortString(), toShortString());
    				ObjectProperty[] props = new ObjectProperty[2];
    				props[0] = new ObjectProperty(this, PROPERTY_STEP);
    				props[1] = new ObjectProperty(subRange, PROPERTY_UPPERBOUND);
    				list.add(new Message(MSGCODE_UPPERBOUND_MISMATCH, msg, Message.ERROR, props));
    			}

    			isSubset = false;
    		}
    	}

		return isSubset;
	}
    
    private String getProperty(String original, String alternative) {
    	if (original == null) {
    		return alternative;
    	}
    	return original;
    }

    /**
     * {@inheritDoc}
     */
	public boolean containsValueSet(IValueSet subset) {
		return containsValueSet(subset, null, null, null);
	}

    /**
     * {@inheritDoc}
     */
    public void validate(MessageList list) {
    	ValueDatatype datatype = getValueDatatype();
        if (datatype==null) {
            String text = Messages.Range_msgUnknownDatatype;
            list.add(new Message(MSGCODE_UNKNOWN_DATATYPE, text, Message.WARNING, this, 
                    new String[]{PROPERTY_LOWERBOUND, PROPERTY_UPPERBOUND, PROPERTY_STEP}));
            return;
        }

        if (datatype.isPrimitive() && getContainsNull()) {
        	String text = Messages.RangeValueSet_msgNullNotSupported;
        	list.add(new Message(MSGCODE_NULL_NOT_SUPPORTED, text, Message.ERROR, this, PROPERTY_CONTAINS_NULL));
        }

        validateParsable(datatype, getStep(), list, this, PROPERTY_STEP);
        validateParsable(datatype, getLowerBound(), list, this, PROPERTY_LOWERBOUND);
        validateParsable(datatype, getUpperBound(), list, this, PROPERTY_UPPERBOUND);
        
        String lowerValue = getLowerBound();
        String upperValue = getUpperBound();
        if (list.getSeverity()==Message.ERROR) {
            return;
        }
        if (!datatype.isNull(lowerValue) && !datatype.isNull(upperValue)) {
        	// range is not unbounded on one side
        	if (datatype.compare(lowerValue, upperValue) > 0) {
        		String text = Messages.Range_msgLowerboundGreaterUpperbound;
        		list.add(new Message(MSGCODE_LBOUND_GREATER_UBOUND, text, Message.ERROR, this, 
        				new String[] { PROPERTY_LOWERBOUND, PROPERTY_UPPERBOUND}));
        		return;
        	}
        }

        NumericDatatype numDatatype = getAndValidateNumericDatatype(datatype, list);
        if (numDatatype != null && !StringUtils.isEmpty(upperValue) && !StringUtils.isEmpty(lowerValue) && !StringUtils.isEmpty(getStep())) {
        	String range = numDatatype.subtract(upperValue, lowerValue);
        	if (!numDatatype.divisibleWithoutRemainder(range, step)) {
        		String msg = NLS.bind(Messages.RangeValueSet_msgStepRangeMismatch, new String[] {lowerValue, upperValue, getStep()});
        		list.add(new Message(MSGCODE_STEP_RANGE_MISMATCH, msg, Message.ERROR, this, new String[] { PROPERTY_LOWERBOUND, PROPERTY_UPPERBOUND, PROPERTY_STEP}));
        	}
        }
    }
    
    private NumericDatatype getAndValidateNumericDatatype(ValueDatatype datatype, MessageList list) {
    	if (datatype instanceof NumericDatatype) {
    		return (NumericDatatype)datatype;
    	}
    	if (list==null) {
    		return null; // wg. null handling in containsValueSet() // flyspray #439
    	}
        IAttribute attr = null;
        if (getParent() instanceof IConfigElement) {
        	try {
				attr = ((IConfigElement)getParent()).findPcTypeAttribute();
			} catch (CoreException e) {
				IpsPlugin.log(e);
			}
        }
        else if (getParent() instanceof IAttribute) {
        	attr = (IAttribute)getParent();
        }

        String text = Messages.RangeValueSet_msgDatatypeNotNumeric;
    	
        Object obj = attr==null?(Object)this:attr;
    	list.add(new Message(MSGCODE_NOT_NUMERIC_DATATYPE, text, Message.ERROR, obj, IAttribute.PROPERTY_DATATYPE));
    	
    	return null;
    }

    private void validateParsable(ValueDatatype datatype, String value, MessageList list, Object invalidObject, String property) {
        if (!datatype.isParsable(value)) {
        	String msg = NLS.bind(Messages.Range_msgPropertyValueNotParsable, new Object[] {property, value, datatype.getName()});
            list.add(new Message(MSGCODE_VALUE_NOT_PARSABLE, msg, Message.ERROR, invalidObject, property));
        }
    }

    /**
     * {@inheritDoc}
     */
    public ValueSetType getValueSetType() {
        return ValueSetType.RANGE;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
    	return super.toString() + ":" + toShortString(); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public String toShortString() {
        if (StringUtils.isNotEmpty(step)) {
            return "[" + lowerBound + ";" + upperBound + "] by " + step; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        return "[" + lowerBound + ";" + upperBound + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    /**
	 * {@inheritDoc}
	 */
	public IIpsObjectPart newPart(Class partType) {
		throw new IllegalArgumentException("Unknown part type" + partType); //$NON-NLS-1$
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected void initPropertiesFromXml(Element element, Integer id) {
		super.initPropertiesFromXml(element, id);
		Element el = DescriptionHelper.getFirstNoneDescriptionElement(element);
		lowerBound = el.getAttribute(PROPERTY_LOWERBOUND);
		upperBound = el.getAttribute(PROPERTY_UPPERBOUND);
		step = el.getAttribute(PROPERTY_STEP);
		containsNull = Boolean.valueOf(el.getAttribute(PROPERTY_CONTAINS_NULL)).booleanValue();
	}

	/**
	 * {@inheritDoc}
	 */
	protected void propertiesToXml(Element element) {
		super.propertiesToXml(element);
		Document doc = element.getOwnerDocument();
        Element tagElement = doc.createElement(XML_TAG);
        tagElement.setAttribute(PROPERTY_LOWERBOUND, lowerBound);
        tagElement.setAttribute(PROPERTY_UPPERBOUND, upperBound);
        tagElement.setAttribute(PROPERTY_STEP, step);
        tagElement.setAttribute(PROPERTY_CONTAINS_NULL, Boolean.toString(containsNull));
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
		
		return retValue;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setValuesOf(IValueSet target) {
		if (!(target instanceof RangeValueSet)) {
			throw new IllegalArgumentException("The given value set is not a range value set"); //$NON-NLS-1$
		}
		RangeValueSet set = (RangeValueSet)target;
		lowerBound = set.lowerBound;
		upperBound = set.upperBound;
		step = set.step;
		containsNull = set.containsNull;
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
