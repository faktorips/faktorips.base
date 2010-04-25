/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.datatype.classtypes;

import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;
import org.faktorips.datatype.NumericDatatype;
import org.faktorips.datatype.ValueClassDatatype;

/**
 * Data type for {@link BigDecimal}.
 * 
 * @author Jan Ortmann
 */
public class BigDecimalDatatype extends ValueClassDatatype implements NumericDatatype {

    /**
     * @param clazz
     */
    public BigDecimalDatatype() {
        super(BigDecimal.class);
    }

    @Override
    public Object getValue(String value) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        return new BigDecimal(value);
    }

    public boolean supportsCompare() {
        return true;
    }

    public boolean divisibleWithoutRemainder(String dividend, String divisor) {
        if (dividend == null || divisor == null) {
            throw new NullPointerException("dividend and divisor both can not be null.");
        }
        BigDecimal a = (BigDecimal)getValue(dividend);
        BigDecimal b = (BigDecimal)getValue(divisor);
        try {
            a.divide(b, 0, BigDecimal.ROUND_UNNECESSARY);
        } catch (ArithmeticException e) {
            return false;
        }
        return true;

    }

    public boolean hasDecimalPlaces() {
        return true;
    }

    public String subtract(String minuend, String subtrahend) {
        if (minuend == null || subtrahend == null) {
            throw new NullPointerException("Minuend and subtrahend both can not be null.");
        }
        return ((BigDecimal)getValue(minuend)).subtract((BigDecimal)getValue(subtrahend)).toString();

    }
}
