package org.faktorips.datatype.classtypes;

import org.apache.commons.lang.StringUtils;
import org.faktorips.datatype.ValueClassDatatype;
import org.faktorips.values.Money;

/**
 * Datatype for <code>Money</code>.
 * 
 * @author Jan Ortmann
 */
public class MoneyDatatype extends ValueClassDatatype {

	public MoneyDatatype() {
		super(Money.class);
	}

	public MoneyDatatype(String name) {
		super(Money.class, name);
	}
	
	/**
	 * Overridden Method.
	 * @see org.faktorips.datatype.ValueDatatype#getValue(java.lang.String)
	 */
	public Object getValue(String s) {
		return Money.valueOf(s);
	}

}
