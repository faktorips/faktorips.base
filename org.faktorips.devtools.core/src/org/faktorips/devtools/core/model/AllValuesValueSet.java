package org.faktorips.devtools.core.model;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A value set containing all values.
 *
 * @author Andy Roesch
 */
public class AllValuesValueSet extends ValueSet {
    
    final static String XML_TAG = "AllValues";
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.model.ValueSet#createSubclassElement(org.w3c.dom.Document)
     */
    protected Element createSubclassElement(Document doc) {
        return doc.createElement(XML_TAG);
    }
    
    /**
     * Overridden IMethod.
     * @see org.faktorips.devtools.core.model.ValueSet#getValueSetType()
     */
    public ValueSetType getValueSetType() {
        return ValueSetType.ALL_VALUES;
    }

    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.model.ValueSet#validate(org.faktorips.datatype.ValueDatatype, org.faktorips.util.message.MessageList)
     */
    public void validate(ValueDatatype datatype, MessageList list) {
        // nothing to do
    }

    /**
     * Overridden IMethod.
     *
     * @see java.lang.Object#toString()
     */
    public String toString () {
        return "AllValuesValueSet";
    }

    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.model.ValueSet#contains(java.lang.String, org.faktorips.datatype.ValueDatatype)
     */
    public boolean contains(String value, ValueDatatype datatype) {
        return true;
    }

    /**
     * Overridden IMethod.
     */
    public Message containsValue(String value, ValueDatatype datatype) {
        return null;
    }
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.model.ValueSet#copys()
     */
    public ValueSet copy() {
        return ValueSet.ALL_VALUES;
    }
}
