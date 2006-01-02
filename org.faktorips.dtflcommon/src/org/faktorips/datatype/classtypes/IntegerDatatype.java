package org.faktorips.datatype.classtypes;

import org.faktorips.datatype.ValueClassDatatype;

/**
 * Datatype for <code>Integer</code>.
 * 
 * @author Jan Ortmann
 */
public class IntegerDatatype extends ValueClassDatatype {

	public IntegerDatatype() {
		super(Integer.class);
	}

	public IntegerDatatype(String name) {
		super(Integer.class, name);
	}
	
	/**
	 * Overridden Method.
	 * @see org.faktorips.datatype.ValueDatatype#getValue(java.lang.String)
	 */
	public Object getValue(String s) {
        if (s.equals("")) {
            return Integer.valueOf("0");
        }
		return Integer.valueOf(s);
	}

}
