package org.faktorips.datatype.classtypes;

import org.faktorips.datatype.ValueClassDatatype;

/**
 * Datatype for <code>Long</code>.
 * 
 * @author Jan Ortmann
 */
public class LongDatatype extends ValueClassDatatype {

	public LongDatatype() {
		super(Long.class);
	}

	public LongDatatype(String name) {
		super(Long.class, name);
	}
	
	/**
	 * Overridden Method.
	 * @see org.faktorips.datatype.ValueDatatype#getValue(java.lang.String)
	 */
	public Object getValue(String s) {
        if (s.equals("")) {
            return Long.valueOf("0");
        }
		return Long.valueOf(s);
	}

}
