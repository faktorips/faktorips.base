package org.faktorips.datatype.classtypes;

import org.apache.commons.lang.StringUtils;
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
        if (StringUtils.isEmpty(s)) {
            return null;
        }
		return Double.valueOf(s);
    }

}
