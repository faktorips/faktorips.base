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
     * Name of the xml element used in the xml conversion.
     */
    public final static String XML_TAG = "ValueSet";
    
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
        throw new RuntimeException("Unknown subclass element " + subclassEl.getNodeName());
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
     * Returns <code>true</code> if this is value set is an enumeration, otherwise <code>false</code>.
     */
    public final boolean isEnum() {
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
    public abstract boolean contains(String value, ValueDatatype datatype);

    /**
     * Returns a message if the value set doesn't contain the indicated value, otherwise <code>null</code>.
     * 
     * @param value The value to check.
     * @param datatype The datatype to parse the string values to 'real' values.
     * 
     * @throws NullPointerException if datatype is <code>null</code>. 
     */
    public abstract Message containsValue(String value, ValueDatatype datatype);
    
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

}
