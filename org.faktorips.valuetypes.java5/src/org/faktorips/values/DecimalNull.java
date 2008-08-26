/***************************************************************************************************
 * Copyright (c) 2005-2008 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 * 
 **************************************************************************************************/

package org.faktorips.values;

import java.math.BigDecimal;

/**
 * NullObject for Decimal.
 * <p>
 * Overrides all Decimal methods with appropriate NullObject behaviour, e.g.
 * add() called on a null value always returns an instance of DecimalNull.
 * <p>
 * The class is package private as the null behaviour is completly defined
 * in {@link Decimal}.
 */
public class DecimalNull extends Decimal implements NullObject {

    private static final long serialVersionUID = -662857878963625638L;

    DecimalNull() {
        super(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isNull() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isNotNull() {
        return false;
    }

    /**
     * @throws NullPointerException
     */
    @Override
    public int scale() {
        throw newNullPointerException();
    }

    /**
     * @throws NullPointerException
     */
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
     * {@inheritDoc}
     */
    @Override
    public Decimal max(Decimal value){
        return Decimal.NULL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Decimal min(Decimal value){
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
     * Returns the special case Money.NULL.
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
     * {@inheritDoc}
     */
    @Override
    public Decimal divide(long value, int scale, int roundingMode) {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Decimal setScale(int scale, int roundingMode) {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Decimal round(int precision, int roundingMode) {
        return this;
    }

    /**
     * @throws NullPointerException
     */
    @Override
    public double doubleValue() {
        throw newNullPointerException();
    }

    /**
     * @throws NullPointerException
     */
    @Override
    public float floatValue() {
        throw newNullPointerException();
    }

    /**
     * @throws NullPointerException
     */
    @Override
    public int intValue() {
        throw newNullPointerException();
    }

    /**
     * @throws NullPointerException
     */
    @Override
    public long longValue() {
        throw newNullPointerException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal bigDecimalValue() {
        return null;
    }

    /**
     * @throws NullPointerException
     */
    @Override
    public byte byteValue() {
        throw newNullPointerException();
    }

    /**
     * @throws NullPointerException
     */
    @Override
    public short shortValue() {
        throw newNullPointerException();
    }

    /**
     * @throws NullPointerException
     */
    @Override
    public int compareTo(Decimal d) {
        throw newNullPointerException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equalsIgnoreScale(Decimal d) {
        throw newNullPointerException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean greaterThan(Decimal d) {
        throw newNullPointerException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean greaterThanOrEqual(Decimal d) {
        throw newNullPointerException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean lessThan(Decimal d) {
        throw newNullPointerException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean lessThanOrEqual(Decimal d) {
        throw newNullPointerException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean notEqualsIgnoreScale(Decimal d) {
        throw newNullPointerException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Decimal abs() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Decimal negate() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "DecimalNull";
    }

    /**
     * Returns <code>true</code> if the given object is also Decimal.NULL,
     * otherwise false.
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
