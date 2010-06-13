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

package org.faktorips.values;

import java.math.BigDecimal;

/**
 * NullObject for Decimal.
 * <p>
 * Overrides all Decimal methods with appropriate NullObject behaviour, e.g. add() called on a null
 * value always returns an instance of DecimalNull.
 * <p>
 * The class is package private as the null behaviour is completly defined in {@link Decimal}.
 */
public class DecimalNull extends Decimal implements NullObject {

    private static final long serialVersionUID = -662857878963625638L;
    static final String STRING_REPRESENTATION = "DecimalNull";

    DecimalNull() {
        super(null);
    }

    @Override
    public boolean isNull() {
        return true;
    }

    @Override
    public boolean isNotNull() {
        return false;
    }

    @Override
    public int scale() {
        throw newNullPointerException();
    }

    @Override
    public int signum() {
        throw newNullPointerException();
    }

    /**
     * Returns the special case DECIMAL.NULL.
     */
    @Override
    public Decimal add(Decimal d) {
        return this;
    }

    /**
     * Returns the special case DECIMAL.NULL.
     */
    @Override
    public Decimal add(Integer i) {
        return this;
    }

    /**
     * Returns the special case DECIMAL.NULL.
     */
    @Override
    public Decimal add(int i) {
        return this;
    }

    /**
     * Returns the special case Decimal.NULL.
     */
    @Override
    public Decimal max(Decimal value) {
        return Decimal.NULL;
    }

    /**
     * Returns the special case Decimal.NULL.
     */
    @Override
    public Decimal min(Decimal value) {
        return Decimal.NULL;
    }

    /**
     * Returns the special case DECIMAL.NULL.
     */
    @Override
    public Decimal subtract(Decimal d) {
        return this;
    }

    /**
     * Returns the special case DECIMAL.NULL.
     */
    @Override
    public Decimal subtract(int i) {
        return this;
    }

    /**
     * Returns the special case DECIMAL.NULL.
     */
    @Override
    public Decimal multiply(Decimal d) {
        return this;
    }

    /**
     * Returns the special case Decimal.NULL.
     */
    @Override
    public Money multiply(Money m, int roundingMode) {
        return Money.NULL;
    }

    /**
     * Returns the special case DECIMAL.NULL.
     */
    @Override
    public Decimal multiply(Integer i) {
        return this;
    }

    /**
     * Returns the special case DECIMAL.NULL.
     */
    @Override
    public Decimal multiply(int i) {
        return Decimal.NULL;
    }

    /**
     * Returns the special case DECIMAL.NULL.
     */
    @Override
    public Decimal multiply(long l) {
        return this;
    }

    /**
     * Returns the special case DECIMAL.NULL.
     */
    @Override
    public Decimal divide(Decimal d, int scale, int roundingMode) {
        return this;
    }

    /**
     * Returns the special case DECIMAL.NULL.
     */
    @Override
    public Decimal divide(int value, int scale, int roundingMode) {
        return this;
    }

    /**
     * Returns the special case Decimal.NULL.
     */
    @Override
    public Decimal divide(long value, int scale, int roundingMode) {
        return this;
    }

    /**
     * Returns the special case Decimal.NULL.
     */
    @Override
    public Decimal setScale(int scale, int roundingMode) {
        return this;
    }

    /**
     * Returns the special case Decimal.NULL.
     */
    @Override
    public Decimal round(int precision, int roundingMode) {
        return this;
    }

    @Override
    public double doubleValue() {
        throw newNullPointerException();
    }

    @Override
    public float floatValue() {
        throw newNullPointerException();
    }

    @Override
    public int intValue() {
        throw newNullPointerException();
    }

    @Override
    public long longValue() {
        throw newNullPointerException();
    }

    /**
     * Returns <code>null</code>.
     */
    @Override
    public BigDecimal bigDecimalValue() {
        return null;
    }

    @Override
    public byte byteValue() {
        throw newNullPointerException();
    }

    @Override
    public short shortValue() {
        throw newNullPointerException();
    }

    @Override
    public int compareTo(Decimal d) {
        throw newNullPointerException();
    }

    @Override
    public boolean equalsIgnoreScale(Decimal d) {
        throw newNullPointerException();
    }

    @Override
    public boolean greaterThan(Decimal d) {
        throw newNullPointerException();
    }

    @Override
    public boolean greaterThanOrEqual(Decimal d) {
        throw newNullPointerException();
    }

    @Override
    public boolean lessThan(Decimal d) {
        throw newNullPointerException();
    }

    @Override
    public boolean lessThanOrEqual(Decimal d) {
        throw newNullPointerException();
    }

    @Override
    public boolean notEqualsIgnoreScale(Decimal d) {
        throw newNullPointerException();
    }

    /**
     * Returns the special case Decimal.NULL.
     */
    @Override
    public Decimal abs() {
        return this;
    }

    /**
     * Returns the special case Decimal.NULL.
     */
    @Override
    public Decimal negate() {
        return this;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public String toString() {
        return STRING_REPRESENTATION;
    }

    /**
     * Returns <code>true</code> if the given object is also Decimal.NULL, otherwise false.
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Decimal)) {
            return false;
        }
        return ((Decimal)o).isNull();
    }

    private NullPointerException newNullPointerException() throws NullPointerException {
        return new NullPointerException("Method not applicable to null.");
    }

}
