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

package org.faktorips.datatype;

/**
 * Datatype for the primitive <code>long</code>.
 */
public class PrimitiveLongDatatype extends AbstractPrimitiveDatatype implements NumericDatatype {

    /** 
     * {@inheritDoc}
     */
    public String getName() {
        return "long";
    }

    /**
     * {@inheritDoc}
     */
    public String getQualifiedName() {
        return "long";
    }

    /**
     * {@inheritDoc}
     */
    public ValueDatatype getWrapperType() {
        return Datatype.LONG;
    }
    
    /** 
     * {@inheritDoc}
     */
    public String getJavaClassName() {
        return "long";
    }

    /**
     * {@inheritDoc}
     */
    public String getDefaultValue() {
        return "0";
    }

    /** 
     * {@inheritDoc}
     */
    public Object getValue(String value) {
        return Long.valueOf(value);
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
        long a = ((Long)getValue(dividend)).longValue();
        long b = ((Long)getValue(divisor)).longValue();
        return b == 0 ? false : a % b == 0;
    }

}
