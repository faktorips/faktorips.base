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
 * Datatype for the primitive <code>int</code>.
 */
public class PrimitiveIntegerDatatype extends AbstractPrimitiveDatatype implements NumericDatatype {

    /** 
     * Overridden method.
     * @see org.faktorips.datatype.Datatype#getName()
     */
    public String getName() {
        return "int";
    }

    /**
     * Overridden method.
     * @see org.faktorips.datatype.Datatype#getQualifiedName()
     */
    public String getQualifiedName() {
        return "int";
    }

    /**
     * Overridden method.
     * @see org.faktorips.datatype.Datatype#getWrapperType()
     */
    public Datatype getWrapperType() {
        return Datatype.INTEGER;
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.datatype.Datatype#getJavaClassName()
     */
    public String getJavaClassName() {
        return "int";
    }

    /** 
     * Overridden method.
     * @see org.faktorips.datatype.Datatype#getValue(java.lang.String)
     */
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
        return Math.IEEEremainder(a, b) == 0;
    }

}
