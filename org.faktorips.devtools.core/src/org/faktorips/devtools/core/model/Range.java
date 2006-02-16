package org.faktorips.devtools.core.model;

import org.apache.commons.lang.StringUtils;
import org.faktorips.datatype.ValueDatatype;
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
public class Range extends ValueSet {

    final static String XML_TAG = "Range";

    public final static String PROPERTY_UPPERBOUND = "upperBound";
    public final static String PROPERTY_LOWERBOUND = "lowerBound";
    public final static String PROPERTY_STEP = "step";

    /**
     * Creates a Range based on the data in the XML element. If element is <code>null</code> the method
     * returns <code>null</code>.
     * 
     * @throws IllegalArgumentException if the element's name is not Range.
     */
    static final Range createRangeFromXml(Element element) {
        ArgumentCheck.nodeName(element, XML_TAG);
        String lb = element.getAttribute(PROPERTY_LOWERBOUND);
        String ub = element.getAttribute(PROPERTY_UPPERBOUND);
        String stp = element.getAttribute(PROPERTY_STEP);
        return new Range(lb, ub, stp);
    }

    private String lowerBound="";
    private String upperBound="";
    private String step="";

    /**
     * Creates an unbounded range with no step.
     */
    public Range() {
    }
    
    /**
     * Creates ar range with the given bounds and no step.
     */
    public Range(String lower, String upper) {
        lowerBound = lower;
        upperBound = upper;
        step = "";
    }

    /**
     * Creates a range with the given bounds and and step.
     */
    public Range(String lower, String upper, String step) {
        lowerBound = lower;
        upperBound = upper;
        this.step = step;
    }
    
    /**
     * Copy constructor.
     */
    public Range(Range range) {
        this(range.lowerBound, range.upperBound, range.step);
    }

    /**
     * Sets the lower bound. An empty string means that the range is unbouned.
     * 
     * @throws NullPointerException  if lowerBound is <code>null</code>.
     */
    public void setLowerBound(String lowerBound) {
        ArgumentCheck.notNull(lowerBound);
        this.lowerBound = lowerBound;
    }

    /**
     * Sets the step. An empty string means that no step exists.
     * 
     * @throws NullPointerException  if step is <code>null</code>.
     */
    public void setStep(String step) {
        ArgumentCheck.notNull(step);
        this.step = step;
    }

    /**
     * Sets the upper bound. An empty string means that the range is unbounded.
     * 
     * @throws NullPointerException  if upperBound is <code>null</code>.
     */
    public void setUpperBound(String upperBound) {
        ArgumentCheck.notNull(upperBound);
        this.upperBound = upperBound;
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
     * Overridden
     */
    public boolean contains(String value, ValueDatatype datatype) {
        try {
            Comparable lower = (Comparable)datatype.getValue(getLowerBound());
            Comparable upper = (Comparable)datatype.getValue(getUpperBound());
            Comparable objectvalue = (Comparable)datatype.getValue(value);
            if ((!getLowerBound().equals("") && ((Comparable)lower).compareTo(objectvalue) > 0)
                    || (!getUpperBound().equals("") && ((Comparable)upper).compareTo(objectvalue) < 0)) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        // TODO Implement an test when step is set to a non zero value
        return true;
    }

    /**
     * Overridden.
     */
    public Message containsValue(String value, ValueDatatype datatype) {
        if (!contains(value, datatype)) {
            String text = "The value is not in the range " + lowerBound + " - " + upperBound;
            if (StringUtils.isNotEmpty(step)) {
                text = text + ", step " + step;
            }
            return Message.newError("", text + '.');
        }
        return null;
    }
    
    /**
     * Overridden.
     */
    protected Element createSubclassElement(Document doc) {
        Element tagElement = doc.createElement(XML_TAG);
        tagElement.setAttribute(PROPERTY_LOWERBOUND, lowerBound);
        tagElement.setAttribute(PROPERTY_UPPERBOUND, upperBound);
        tagElement.setAttribute(PROPERTY_STEP, step);
        return tagElement;
    }
    
    /**
     * Overridden.
     */
    public ValueSet copy() {
        return new Range(this);
    }
    
    /**
     * Overridden.
     */
    public void validate(ValueDatatype datatype, MessageList list) {
        if (datatype==null) {
            String text = "Can't parse lower bound, upper bound and step as the datatype is unknown!";
            list.add(new Message("", text, Message.WARNING, this, 
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
            String text = "The lowerbound is greater than the upperbound!";
            list.add(new Message("", text, Message.WARNING, this, 
                    new String[] { PROPERTY_LOWERBOUND, PROPERTY_UPPERBOUND}));
            return;
        }
    }

    private Object getValue(ValueDatatype datatype, String value, MessageList list, String property) {
        try {
            return datatype.getValue(value);
        } catch (IllegalArgumentException e) {
            list.add(new Message("", "The " + property + " " + value + " is not a " + datatype.getName() + " .",
                    Message.ERROR, this, property));
            return null;
        }
    }

    /**
     * Overridden.
     */
    public ValueSetType getValueSetType() {
        return ValueSetType.RANGE;
    }

    /**
     * Overridden.
     */
    public String toString() {
        if (StringUtils.isNotEmpty(step)) {
            return "[" + lowerBound + ";" + upperBound + "] by " + step;
        }
        return "[" + lowerBound + ";" + upperBound + "]";
    }

}
