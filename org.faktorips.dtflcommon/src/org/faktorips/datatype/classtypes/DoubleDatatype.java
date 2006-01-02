package org.faktorips.datatype.classtypes;

import org.faktorips.datatype.ValueClassDatatype;

/**
 * 
 * @author Jan Ortmann
 */
public class DoubleDatatype extends ValueClassDatatype {

    /**
     * @param clazz
     */
    public DoubleDatatype() {
        super(Double.class);
    }

    /**
     * @param clazz
     * @param name
     */
    public DoubleDatatype(String name) {
        super(Double.class, name);
    }

    /**
     * Overridden Method.
     *
     * @see org.faktorips.datatype.ValueDatatype#getValue(java.lang.String)
     */
    public Object getValue(String s) {
        if (s.equals("")) {
            return Double.valueOf("0");
        }
		return Double.valueOf(s);
    }

}
