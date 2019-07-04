/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.values;

import java.math.BigDecimal;

/**
 * NullObject for Decimal.
 * <p>
 * Overrides all Decimal methods with appropriate NullObject behaviour, e.g. add() called on a null
 * value always returns an instance of DecimalNull.
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
        return NULL;
    }

    /**
     * Returns the special case DECIMAL.NULL.
     */
    @Override
    public Decimal add(Integer i) {
        return NULL;
    }

    /**
     * Returns the special case DECIMAL.NULL.
     */
    @Override
    public Decimal add(int i) {
        return NULL;
    }

    /**
     * Returns the special case Decimal.NULL.
     */
    @Override
    public Decimal max(Decimal value) {
        return NULL;
    }

    /**
     * Returns the special case Decimal.NULL.
     */
    @Override
    public Decimal min(Decimal value) {
        return NULL;
    }

    /**
     * Returns the special case DECIMAL.NULL.
     */
    @Override
    public Decimal subtract(Decimal d) {
        return NULL;
    }

    /**
     * Returns the special case DECIMAL.NULL.
     */
    @Override
    public Decimal subtract(int i) {
        return NULL;
    }

    /**
     * Returns the special case DECIMAL.NULL.
     */
    @Override
    public Decimal multiply(Decimal d) {
        return NULL;
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
        return NULL;
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
        return NULL;
    }

    /**
     * Returns the special case DECIMAL.NULL.
     */
    @Override
    public Decimal divide(Decimal d, int scale, int roundingMode) {
        return NULL;
    }

    /**
     * Returns the special case DECIMAL.NULL.
     */
    @Override
    public Decimal divide(int value, int scale, int roundingMode) {
        return NULL;
    }

    /**
     * Returns the special case Decimal.NULL.
     */
    @Override
    public Decimal divide(long value, int scale, int roundingMode) {
        return NULL;
    }

    /**
     * Returns the special case Decimal.NULL.
     */
    @Override
    public Decimal setScale(int scale, int roundingMode) {
        return NULL;
    }

    /**
     * Returns the special case Decimal.NULL.
     */
    @Override
    public Decimal round(int precision, int roundingMode) {
        return NULL;
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
        if (d == null) {
            throw newNullPointerException();
        }
        if (d.isNull()) {
            return 0;
        }
        return -1;
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
        return NULL;
    }

    /**
     * Returns the special case Decimal.NULL.
     */
    @Override
    public Decimal negate() {
        return NULL;
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
        if (this == o) {
            return true;
        }
        if (!(o instanceof Decimal)) {
            return false;
        }
        return ((Decimal)o).isNull();
    }

    private NullPointerException newNullPointerException() throws NullPointerException {
        return new NullPointerException("Method not applicable to null.");
    }

}
