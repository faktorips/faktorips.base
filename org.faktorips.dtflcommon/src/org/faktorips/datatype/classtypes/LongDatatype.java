/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.datatype.classtypes;

import org.apache.commons.lang.StringUtils;
import org.faktorips.datatype.NumericDatatype;
import org.faktorips.datatype.ValueClassDatatype;

/**
 * Datatype for <code>Long</code>.
 * 
 * @author Jan Ortmann
 */
public class LongDatatype extends ValueClassDatatype implements NumericDatatype {

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
        if (StringUtils.isEmpty(s)) {
            return null;
        }
		return Long.valueOf(s);
	}


    /**
     * {@inheritDoc}
     */
    public boolean supportsCompare() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public String subtract(String minuend, String subtrahend) {
        if (minuend == null || subtrahend == null) {
            throw new NullPointerException("Minuend and subtrahend both can not be null.");
        }

        long result = ((Long)getValue(minuend)).longValue() - ((Long)getValue(subtrahend)).longValue();
        return Long.toString(result);
    }

    /**
     * {@inheritDoc}
     */
    public boolean divisibleWithoutRemainder(String dividend, String divisor) {
        if (dividend == null || divisor == null) {
            throw new NullPointerException("dividend and divisor both can not be null.");
        }
        Long longA = (Long)getValue(dividend);
        Long longB = (Long)getValue(divisor);
        
        if (longA == null) {
            throw new NumberFormatException("The dividend '" + dividend + "' can not be parsed to a Long");
        }
        
        if (longB == null) {
            throw new NumberFormatException("The divisor '" + divisor + "' can not be parsed to a Long");
        }
        
        long a = longA.longValue();
        long b = longB.longValue();

        return Math.IEEEremainder(a, b) == 0;
    }
}
