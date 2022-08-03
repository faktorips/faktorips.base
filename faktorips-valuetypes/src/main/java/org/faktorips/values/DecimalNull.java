/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.values;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * NullObject for Decimal.
 * <p>
 * Overrides all Decimal methods with appropriate NullObject behaviour, e.g. add() called on a null
 * value always returns an instance of DecimalNull.
 */
public class DecimalNull extends Decimal implements NullObject {

    static final String STRING_REPRESENTATION = "DecimalNull";
    private static final long serialVersionUID = -662857878963625638L;

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
        throw newUnsupportedOperationException();
    }

    @Override
    public int signum() {
        throw newUnsupportedOperationException();
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
     * 
     * @deprecated since 21.6. {@link DecimalNull#multiply(Money, RoundingMode)} should be used
     *                 instead.
     */
    @Override
    @Deprecated
    public Money multiply(Money m, int roundingMode) {
        return Money.NULL;
    }

    /**
     * Returns the special case Decimal.NULL.
     */
    @Override
    public Money multiply(Money m, RoundingMode roundingMode) {
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
     * 
     * @deprecated since 21.6. {@link DecimalNull#divide(Decimal, int, RoundingMode)} should be used
     *                 instead.
     */
    @Override
    @Deprecated
    public Decimal divide(Decimal d, int scale, int roundingMode) {
        return NULL;
    }

    /**
     * Returns the special case DECIMAL.NULL.
     */
    @Override
    public Decimal divide(Decimal d, int scale, RoundingMode roundingMode) {
        return NULL;
    }

    /**
     * Returns the special case DECIMAL.NULL.
     * 
     * @deprecated since 21.6. {@link DecimalNull#divide(int, int, RoundingMode)} should be used
     *                 instead.
     */
    @Override
    @Deprecated
    public Decimal divide(int value, int scale, int roundingMode) {
        return NULL;
    }

    /**
     * Returns the special case DECIMAL.NULL.
     */
    @Override
    public Decimal divide(int value, int scale, RoundingMode roundingMode) {
        return NULL;
    }

    /**
     * Returns the special case Decimal.NULL.
     * 
     * @deprecated since 21.6. {@link #divide(long, int, RoundingMode)} should be used instead.
     */
    @Override
    @Deprecated
    public Decimal divide(long value, int scale, int roundingMode) {
        return NULL;
    }

    /**
     * Returns the special case Decimal.NULL.
     */
    @Override
    public Decimal divide(long value, int scale, RoundingMode roundingMode) {
        return NULL;
    }

    /**
     * Returns the special case Decimal.NULL.
     * 
     * @deprecated since 21.6. {@link #setScale(int, RoundingMode)} should be used instead.
     */
    @Override
    @Deprecated
    public Decimal setScale(int scale, int roundingMode) {
        return NULL;
    }

    /**
     * Returns the special case Decimal.NULL.
     */
    @Override
    public Decimal setScale(int scale, RoundingMode roundingMode) {
        return NULL;
    }

    /**
     * Returns the special case Decimal.NULL.
     * 
     * @deprecated since 21.6. {@link #round(int, RoundingMode)} should be used instead.
     */
    @Override
    @Deprecated
    public Decimal round(int precision, int roundingMode) {
        return NULL;
    }

    /**
     * Returns the special case Decimal.NULL.
     */
    @Override
    public Decimal round(int precision, RoundingMode roundingMode) {
        return NULL;
    }

    @Override
    public double doubleValue() {
        throw newUnsupportedOperationException();
    }

    @Override
    public float floatValue() {
        throw newUnsupportedOperationException();
    }

    @Override
    public int intValue() {
        throw newUnsupportedOperationException();
    }

    @Override
    public long longValue() {
        throw newUnsupportedOperationException();
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
        throw newUnsupportedOperationException();
    }

    @Override
    public short shortValue() {
        throw newUnsupportedOperationException();
    }

    @Override
    public int compareTo(Decimal d) {
        throw newUnsupportedOperationException();
    }

    @Override
    public boolean equalsIgnoreScale(Decimal d) {
        throw newUnsupportedOperationException();
    }

    @Override
    public boolean greaterThan(Decimal d) {
        throw newUnsupportedOperationException();
    }

    @Override
    public boolean greaterThanOrEqual(Decimal d) {
        throw newUnsupportedOperationException();
    }

    @Override
    public boolean lessThan(Decimal d) {
        throw newUnsupportedOperationException();
    }

    @Override
    public boolean lessThanOrEqual(Decimal d) {
        throw newUnsupportedOperationException();
    }

    @Override
    public boolean notEqualsIgnoreScale(Decimal d) {
        throw newUnsupportedOperationException();
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

    private UnsupportedOperationException newUnsupportedOperationException() throws UnsupportedOperationException {
        return new UnsupportedOperationException("Method not applicable to Decimal.NULL.");
    }

}
