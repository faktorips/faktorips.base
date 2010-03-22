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

package org.faktorips.datatype;

/**
 * Datatype for the primitive <code>int</code>.
 */
public class PrimitiveIntegerDatatype extends AbstractPrimitiveDatatype implements NumericDatatype {

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return "int";
    }

    /**
     * {@inheritDoc}
     */
    public String getQualifiedName() {
        return "int";
    }

    /**
     * {@inheritDoc}
     */
    public ValueDatatype getWrapperType() {
        return Datatype.INTEGER;
    }

    /**
     * {@inheritDoc}
     */
    public String getJavaClassName() {
        return "int";
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
    @Override
    public Object getValue(String value) {
        return Integer.valueOf(value);
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
        int result = ((Integer)getValue(minuend)).intValue() - ((Integer)getValue(subtrahend)).intValue();
        return Integer.toString(result);
    }

    /**
     * {@inheritDoc}
     */
    public boolean divisibleWithoutRemainder(String dividend, String divisor) {
        if (dividend == null || divisor == null) {
            throw new NullPointerException("dividend and divisor both can not be null.");
        }
        int a = ((Integer)getValue(dividend)).intValue();
        int b = ((Integer)getValue(divisor)).intValue();
        return b == 0 ? false : a % b == 0;
    }

    public boolean hasDecimalPlaces() {
        return false;
    }
}
