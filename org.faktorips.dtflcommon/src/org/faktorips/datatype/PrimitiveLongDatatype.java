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
 * Datatype for the primitive <code>long</code>.
 */
public class PrimitiveLongDatatype extends AbstractPrimitiveDatatype implements NumericDatatype {

    public String getName() {
        return "long"; //$NON-NLS-1$
    }

    public String getQualifiedName() {
        return "long"; //$NON-NLS-1$
    }

    public ValueDatatype getWrapperType() {
        return Datatype.LONG;
    }

    public String getJavaClassName() {
        return "long"; //$NON-NLS-1$
    }

    public String getDefaultValue() {
        return "0"; //$NON-NLS-1$
    }

    @Override
    public Object getValue(String value) {
        return Long.valueOf(value);
    }

    public boolean supportsCompare() {
        return true;
    }

    public String subtract(String minuend, String subtrahend) {
        if (minuend == null || subtrahend == null) {
            throw new NullPointerException("Minuend and subtrahend both can not be null."); //$NON-NLS-1$
        }
        long result = ((Long)getValue(minuend)).longValue() - ((Long)getValue(subtrahend)).longValue();
        return Long.toString(result);
    }

    public boolean divisibleWithoutRemainder(String dividend, String divisor) {
        if (dividend == null || divisor == null) {
            throw new NullPointerException("dividend and divisor both can not be null."); //$NON-NLS-1$
        }
        long a = ((Long)getValue(dividend)).longValue();
        long b = ((Long)getValue(divisor)).longValue();
        return b == 0 ? false : a % b == 0;
    }

    public boolean hasDecimalPlaces() {
        return false;
    }

}
