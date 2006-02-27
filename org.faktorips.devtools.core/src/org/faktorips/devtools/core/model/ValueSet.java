package org.faktorips.devtools.core.model;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.util.XmlUtil;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A ValueSet is the specification of a set of values. It is asumed that all values in a ValueSet are of the same
 * datatype.
 * <p>
 * Values in the set are represented by strings so that we don't have to deal with type conversion
 * when the datatype changes. E.g. If an attributes datatype is changed by the user from Decimal to Money,
 * lower bound and upper bound from a range value set become invalid (if they were valid before) but the 
 * string values remain. The user can switch back the datatype to Decimal and the range is valid again. 
 * This works also when the attribute's datatype is unkown.
 * 
 * @author Andy Roesch
 * @author Jan Ortmann
 */
public abstract class ValueSet {
    
    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "VALUESET-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the value is not contained in the value set.
     */
    public final static String MSGCODE_VALUE_NOT_CONTAINED = MSGCODE_PREFIX + "ValueNotContained"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the value-subset is not of the correct type.
     */
    public final static String MSGCODE_TYPE_OF_VALUESET_NOT_MATCHING = MSGCODE_PREFIX + "TypeOfValueSetNotMatching"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the value could not be parsed.
     */
    public final static String MSGCODE_VALUE_NOT_PARSABLE = MSGCODE_PREFIX + "ValueNotParsable"; //$NON-NLS-1$
    
    /**
     * Validation message code to indicate that the lower bound of the subset is less than the lower
     * bound of this value set. 
     */
    public final static String MSGCODE_NOT_COMPARABLE = MSGCODE_PREFIX + "NotComparable"; //$NON-NLS-1$
    
    /**
     * Validation message code to indicate that the lower bound of the subset is less than the lower
     * bound of this value set. 
     */
    public final static String MSGCODE_UNKNOWN_DATATYPE = MSGCODE_PREFIX + "UnknownDatatype"; //$NON-NLS-1$
    
    /**
     * Name of the xml element used in the xml conversion.
     */
    public final static String XML_TAG = "ValueSet"; //$NON-NLS-1$
    
    /**
     * A value set instance representing all values.
     */
    public final static ValueSet ALL_VALUES = new AllValuesValueSet();

    /**
     * Returns the value set represented by the indicated value set element.
     * 
     * @param valueSetElement The ValueSet xml element.
     */
    public final static ValueSet createFromXml(Element valueSetElement) {
        Element subclassEl = XmlUtil.getFirstElement(valueSetElement);
        if (subclassEl.getNodeName().equals(Range.XML_TAG)) {
            return Range.createRangeFromXml(subclassEl);
        }
        if (subclassEl.getNodeName().equals(EnumValueSet.XML_TAG)) {
            return EnumValueSet.createEnumFromXml(subclassEl);
        }
        if (subclassEl.getNodeName().equals(AllValuesValueSet.XML_TAG)) {
            return ValueSet.ALL_VALUES;
        }
        throw new RuntimeException("Unknown subclass element " + subclassEl.getNodeName()); //$NON-NLS-1$
    }
    
    /**
     * Return a copy of the value set.
     */
    public abstract ValueSet copy();
    
    /**
     * Returns the type of the value set.
     */
    public abstract ValueSetType getValueSetType();
    
    /**
     * Returns <code>true</code> if this is the value set containg all values, otherwise <code>false</code>.
     */
    public final boolean isAllValues() {
        return getValueSetType()==ValueSetType.ALL_VALUES;
    }
    
    /**
     * Returns <code>true</code> if this is value set is a range, otherwise <code>false</code>.
     */
    public final boolean isRange() {
        return getValueSetType()==ValueSetType.RANGE;
    }
    
    /**
     * Returns <code>true</code> if this is an EnumValueSet you can safely cast it to EnumValueSet,
     * otherwise <code>false</code>.
     */
    public final boolean isEnumValueSet() {
        return getValueSetType()==ValueSetType.ENUM;
    }

    /**
     * Returns <code>true</code> if the value set contains the indicated value, otherwise <code>false</code>.
     * 
     * @param value The value to check.
     * @param datatype The datatype to parse the string values to 'real' values.
     * 
     * @throws NullPointerException if datatype is <code>null</code>. 
     */
    public abstract boolean containsValue(String value, ValueDatatype datatype);

    /**
     * Returns <code>true</code> it the value set contains the indicated value, otherwise <code>false</code>.
     * A message is stored in the message list, if the value set doesn't contain the indicated value.
     * 
     * @param value The value to check.
     * @param datatype The datatype to parse the string values to 'real' values.
     * @param list The list to add messages, if any. Can be null if no messages are needed.
     * @param invalidObject The object the message refers to. Ignored if <code>list</code>
     *                      is <code>null</code>. Can be <code>null</code> itself. 
     * @param invalidProperty The property of the object the message refers to. Ignored if 
     *                        <code>list</code> or <code>invalidObject</code> is <code>null</code>.
     *                        Can be <code>null</code> itself.
     * @throws NullPointerException if datatype is <code>null</code>. 
     */
    public abstract boolean containsValue(String value, ValueDatatype datatype,
			MessageList list, Object invalidObject, String invalidProperty);
    
    /**
     * Returns <code>true</code> if this valueset contains the other valueset, otherwise <code>false</code>.
     * 
     * @param subset The valueset to check.
     * @param datatype The datatype to parse the valueset's values to 'real' values.
     * 
     * @throws NullPointerException if subset or datatype is <code>null</code>. 
     */
    public abstract boolean containsValueSet(ValueSet subset, ValueDatatype datatype);
    
    /**
     * Returns <code>true</code> if this valueset contains the other valueset, otherwise <code>false</code>.
     * 
     * @param subset The valueset to check.
     * @param datatype The datatype to parse the valueset's values to 'real' values.
     * @param list The list to which a message is added in case the given valueset is not a subset of this valueset. 
     * @param invalidObject The object the message refers to. Ignored if <code>list</code>
     *                      is <code>null</code>. Can be <code>null</code> itself. 
     * @param invalidProperty The property of the object the message refers to. Ignored if 
     *                        <code>list</code> or <code>invalidObject</code> is <code>null</code>.
     *                        Can be <code>null</code> itself.
     * 
     * @throws NullPointerException if subset or datatype is <code>null</code>. 
     */
    public abstract boolean containsValueSet(ValueSet subset, ValueDatatype datatype, 
    		MessageList list, Object invalidObject, String invalidProperty);

    /**
     * Validates the value set.
     * 
     * @param datatype The value datatype to parse the set's string values to 'real' values.
     * @param list Message collection paramter.
     */
    public abstract void validate(ValueDatatype datatype, MessageList list);

    /**
     * Creates an xml element representing the value set.
     */
    public final Element toXml(Document doc) {
        Element element = doc.createElement(XML_TAG);
        element.appendChild(createSubclassElement(doc));
        return element;
    }
    
    protected abstract Element createSubclassElement(Document doc);

    /**
     * Creates a new message with severity ERROR and adds the new message to the given message list.
     * 
     * @param list The message list to add the new message to 
     * @param id The message code
     * @param text The message text
     * @param invalidObject The object this message is for. Can be null if no relation to an object exists.
     * @param invalidProperty The name of the property the message is created for. Can be null.
     */
    protected void addMsg(MessageList list, String id, String text, Object invalidObject, String invalidProperty) {
    	Message msg;
    	int severity = Message.ERROR;
    	
    	if (invalidObject == null) {
    		msg = new Message(id, text, severity);
    	}
    	else if (invalidProperty == null) {
    		msg = new Message(id, text, severity, invalidObject);
    	}
    	else {
    		msg = new Message(id, text, severity, invalidObject, invalidProperty);
    	}
    	
    	list.add(msg);
    }
}
