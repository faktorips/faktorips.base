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

import org.faktorips.datatype.NumericDatatype;
import org.faktorips.datatype.ValueClassDatatype;
import org.faktorips.values.Decimal;

/**
 * Datatype for <code>Decimal</code>.
 * 
 * @author Jan Ortmann
 */
public class DecimalDatatype extends ValueClassDatatype implements NumericDatatype {

    public DecimalDatatype() {
        super(Decimal.class);
    }

    public DecimalDatatype(String name) {
        super(Decimal.class, name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue(String s) {
        return Decimal.valueOf(s);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNullObject() {
        return true;
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
        return ((Decimal)getValue(minuend)).subtract((Decimal)getValue(subtrahend)).toString();
    }

    /**
     * {@inheritDoc}
     */
    public boolean divisibleWithoutRemainder(String dividend, String divisor) {
        if (dividend == null || divisor == null) {
            throw new NullPointerException("dividend and divisor both can not be null.");
        }
        Decimal a = Decimal.valueOf(dividend);
        Decimal b = Decimal.valueOf(divisor);
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
}
