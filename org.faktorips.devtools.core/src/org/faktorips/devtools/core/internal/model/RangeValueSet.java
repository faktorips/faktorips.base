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
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IRangeValueSet;
import org.faktorips.devtools.core.model.IValueSet;
import org.faktorips.devtools.core.model.Messages;
import org.faktorips.devtools.core.model.ValueSetType;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.XmlUtil;
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
    public boolean containsValue(String value, ValueDatatype datatype) {
    	return containsValue(value, datatype, null, null, null);
    }

    /**
     * {@inheritDoc}
     */
    public boolean containsValue(String value, ValueDatatype datatype, MessageList list, Object invalidObject, String invalidProperty) {
        try {
            Comparable lower = (Comparable)datatype.getValue(getLowerBound());
            Comparable upper = (Comparable)datatype.getValue(getUpperBound());
            Comparable objectvalue = (Comparable)datatype.getValue(value);
            if (objectvalue == null) {
            	return true;
            }
            if ((!getLowerBound().equals("") && ((Comparable)lower).compareTo(objectvalue) > 0) //$NON-NLS-1$
                    || (!getUpperBound().equals("") && ((Comparable)upper).compareTo(objectvalue) < 0)) { //$NON-NLS-1$
            	if (list != null) {
                    String text = NLS.bind(Messages.Range_msgValueNotInRange, new Object[] {lowerBound, upperBound, step});
                    addMsg(list, MSGCODE_VALUE_NOT_CONTAINED, text + '.', invalidObject, invalidProperty);
            	}
                return false;
            }
        } catch (IllegalArgumentException e) {
            return false;
        }
        // TODO Implement an test when step is set to a non zero value. Not until values and 
        // step can calculate...
        return true;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean containsValueSet(IValueSet subset, ValueDatatype datatype,
			MessageList list, Object invalidObject, String invalidProperty) {
    	
    	if (subset instanceof AllValuesValueSet) {
    		// if the subset is an all values valueset, it is allways contained in this valueset.
    		return true;
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
    			
    			Comparable step = parse(getStep(), datatype, list, invalidObject, invalidProperty);
    			Comparable subStep = parse(subRange.getStep(), datatype, list, invalidObject, invalidProperty);
    			
    			// TODO if subStep is an integer multiple of step (if possible for the datatype...)
    			// the step and subStep can be non-equal, anyway the subSet can be a real subset of this
    			// range. This is only possible to test if the values have to implement another
    			// interface then Comparable...
    			
    			if (step.compareTo(subStep) != 0) {
    				if (list != null) {
    					String msg = NLS.bind(Messages.Range_msgStepMismatch, getStep(), subRange.getStep());
    					addMsg(list, MSGCODE_STEP_MISMATCH, msg, invalidObject, getProperty(invalidProperty, PROPERTY_STEP));
    				}
    				isSubset = false;
    			}
    		}
    	}
    	
    	Comparable lower = parse(getLowerBound(), datatype, list, invalidObject, invalidProperty);
    	Comparable subLower = parse(subRange.getLowerBound(), datatype, list, invalidObject, invalidProperty);
    	if (lower.compareTo(subLower) > 0) {
    		if (list != null) {
    			String msg = NLS.bind(Messages.Range_msgLowerBoundViolation, getLowerBound(), subRange.getLowerBound());
    			addMsg(list, MSGCODE_LOWER_BOUND_VIOLATION, msg, invalidObject, getProperty(invalidProperty, PROPERTY_LOWERBOUND));
    		}
    		isSubset = false;
    	}
    	
    	Comparable upper = parse(getUpperBound(), datatype, list, invalidObject, invalidProperty);
    	Comparable subUpper = parse(subRange.getUpperBound(), datatype, list, invalidObject, invalidProperty);
    	if (upper.compareTo(subUpper) < 0) {
    		if (list != null) {
    			String msg = NLS.bind(Messages.Range_msgUpperBoundViolation, getUpperBound(), subRange.getUpperBound());
    			addMsg(list, MSGCODE_UPPER_BOUND_VIOLATION, msg, invalidObject, getProperty(invalidProperty, PROPERTY_UPPERBOUND));
    		}
    		isSubset = false;
    	}

    	// TODO if step != "", the lower and upper bound of the subset must be divisible without remainder
    	// by this step. Not until values and step can calculate...
    	
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
	public boolean containsValueSet(IValueSet subset, ValueDatatype datatype) {
		return containsValueSet(subset, datatype, null, null, null);
	}

    /**
     * Returns the parsed value as comparable. If an error occurs during parse (e.g. the value can not be 
     * parsed by the datatype), the provided error message is filled with two values:
     * <ul>
     *   <li>{0} is replaced with the value</li>
     *   <li>{1} is replaced with the name of the datatype</li>
     * </ul>
     * For more details on message substitution see org.eclipse.osgi.util.NLS.
     * 
     * @param value The value to parse
     * @param datatype The datatype to use for parsing
     * @param list The list to append any messages to or null if this feature is not needed.
     * @return
     */
    private Comparable parse(String value, ValueDatatype datatype, MessageList list, Object invalidObject, String invalidProperty) {
    	Comparable retValue = null;
        try {
        	retValue = (Comparable)datatype.getValue(value);
		} 
        catch (IllegalArgumentException e) {
			if (list != null) {
				String msg = NLS.bind(Messages.Range_msgValueNotParsable, value, datatype.getName());
				addMsg(list, MSGCODE_VALUE_NOT_PARSABLE, msg, invalidObject, invalidProperty);
			}
		}
		catch (ClassCastException e) {
			if (list != null) {
				String msg = NLS.bind(Messages.Range_msgValueNotComparable, datatype.getName());
				addMsg(list, MSGCODE_NOT_COMPARABLE, msg, invalidObject, invalidProperty);
			}
		}
		return retValue;
    }
    
    /**
     * {@inheritDoc}
     */
    public void validate(ValueDatatype datatype, MessageList list) {
        if (datatype==null) {
            String text = Messages.Range_msgUnknownDatatype;
            list.add(new Message(MSGCODE_UNKNOWN_DATATYPE, text, Message.WARNING, this, 
                    new String[]{PROPERTY_LOWERBOUND, PROPERTY_UPPERBOUND, PROPERTY_STEP}));
            return;
        }
        Comparable lowerValue = (Comparable)getValue(datatype, lowerBound, list, PROPERTY_LOWERBOUND);
        Comparable upperValue = (Comparable)getValue(datatype, upperBound, list, PROPERTY_UPPERBOUND);
        getValue(datatype, step, list, PROPERTY_STEP);
        if (list.getSeverity()==Message.ERROR) {
            return;
        }
        if (datatype.isNull(lowerValue) || datatype.isNull(upperValue)) {
            return; // range is unbounded on one side
        }
        if (lowerValue.compareTo(upperValue)>0) {
            String text = Messages.Range_msgLowerboundGreaterUpperbound;
            list.add(new Message(MSGCODE_LBOUND_GREATER_UBOUND, text, Message.WARNING, this, 
                    new String[] { PROPERTY_LOWERBOUND, PROPERTY_UPPERBOUND}));
            return;
        }
    }

    private Object getValue(ValueDatatype datatype, String value, MessageList list, String property) {
        try {
            return datatype.getValue(value);
        } catch (IllegalArgumentException e) {
        	String msg = NLS.bind(Messages.Range_msgPropertyValueNotParsable, new Object[] {property, value, datatype.getName()});
            list.add(new Message(MSGCODE_VALUE_NOT_PARSABLE, msg, Message.ERROR, this, property));
            return null;
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
    	return super.toString() + ":" + toShortString(null); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public String toShortString(Datatype type) {
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
		Element el = XmlUtil.getFirstElement(element);
		lowerBound = el.getAttribute(PROPERTY_LOWERBOUND);
		upperBound = el.getAttribute(PROPERTY_UPPERBOUND);
		step = el.getAttribute(PROPERTY_STEP);
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
	}
}
