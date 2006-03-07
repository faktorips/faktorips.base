package org.faktorips.datatype.classtypes;

import org.faktorips.datatype.ValueClassDatatype;

/**
 * Datatype for <code>Boolean</code>.
 * 
 * @author Jan Ortmann
 */
public class BooleanDatatype extends ValueClassDatatype {

	public BooleanDatatype() {
		super(Boolean.class);
	}

	public BooleanDatatype(String name) {
		super(Boolean.class, name);
	}
	
	/**
	 * Overridden Method.
	 * @see org.faktorips.datatype.ValueDatatype#getValue(java.lang.String)
	 */
	public Object getValue(String s) {
        if (s == null) {
            return null;
        }
		return Boolean.valueOf(s);
	}

}
