package org.faktorips.datatype.classtypes;

import org.faktorips.datatype.ValueClassDatatype;
import org.faktorips.datatype.Decimal;

/**
 * Datatype for <code>Decimal</code>.
 * 
 * @author Jan Ortmann
 */
public class DecimalDatatype extends ValueClassDatatype {

	public DecimalDatatype() {
		super(Decimal.class);
	}
	
	public DecimalDatatype(String name) {
	    super(Decimal.class, name);
	}

	/**
	 * Overridden Method.
	 * @see org.faktorips.datatype.ValueDatatype#getValue(java.lang.String)
	 */
	public Object getValue(String s) {
		return Decimal.valueOf(s);
	}

}
