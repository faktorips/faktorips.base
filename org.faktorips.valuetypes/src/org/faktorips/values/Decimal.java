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
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.faktorips.values.xml.DecimalXmlAdapter;

/**
 * Value class that represents a decimal number. This class is similar to Java's BigDecimal with two
 * differences:
 * <ul>
 * <li>The equals() Method ignores the scale as most people expect 1.0 and 1 to be the same
 * number.</li>
 * <li>The class has a special subclass representing <code>null</code></li>
 * </ul>
 * 
 * @see java.math.BigDecimal
 * @see org.faktorips.values.DecimalNull
 * @see Decimal#NULL
 */
@XmlJavaTypeAdapter(DecimalXmlAdapter.class)
public class Decimal extends Number implements Comparable<Decimal>, NullObjectSupport {

    /**
     * Special case for the null value.
     */
    public static final Decimal NULL = new DecimalNull();

    /**
     * Special case for zero.
     */
    public static final Decimal ZERO = valueOf(0, 0);

    private static final long serialVersionUID = -642726667937769164L;

    private final BigDecimal value;

    Decimal(BigDecimal value) {
        this.value = value;
    }

    /**
     * Returns a {@link Collector} that sums a {@link Stream} of Decimals.
     */
    public static final Collector<Decimal, ?, Decimal> sum() {
        return Collectors.reducing(Decimal.ZERO, Decimal::add);
    }

    /**
     * Returns the sum of the values. If either the values array or one of the values is
     * <code>null</code> the method returns {@link Decimal#NULL}.
     */
    public static final Decimal sum(Decimal... values) {
        if (values == null) {
            return Decimal.NULL;
        }
        Decimal sum = Decimal.valueOf(0, 0);
        for (Decimal value2 : values) {
            sum = sum.add(value2);
        }
        return sum;
    }

    /**
     * Returns the Decimal value defined in the String <code>s</code>. Returns {@link Decimal#NULL}
     * if <code>s</code> is either <code>null</code> or an empty string or "DecimalNull".
     * 
     * @throws NumberFormatException if <code>s</code> is not a valid representation of a Decimal.
     */
    public static final Decimal valueOf(String s) {
        if (s == null || "".equals(s) || DecimalNull.STRING_REPRESENTATION.equals(s)) {
            return NULL;
        }
        return new Decimal(new BigDecimal(s));
    }

    public static final Decimal valueOf(BigDecimal value) {
        if (value == null) {
            return NULL;
        }
        return new Decimal(value);
    }

    /**
     * Getting a decimal from a double value.
     * 
     * @param value the double value you want a decimal for
     * @return the decimal representing the double value
     * @since 3.3
     */
    public static final Decimal valueOf(Double value) {
        if (value == null) {
            return NULL;
        }
        return Decimal.valueOf(BigDecimal.valueOf(value));
    }

    public static final Decimal valueOf(Integer value) {
        if (value == null) {
            return NULL;
        }
        return Decimal.valueOf(value.longValue(), 0);
    }

    public static final Decimal valueOf(long unscaledValue, int scale) {
        return new Decimal(BigDecimal.valueOf(unscaledValue, scale));
    }

    @Override
    public boolean isNull() {
        return false;
    }

    @Override
    public boolean isNotNull() {
        return true;
    }

    /**
     * Returns the <em>scale</em> of this BigDecimal. (The scale is the number of digits to the
     * right of the decimal point.)
     * 
     * @return the scale of this Decimal.
     */
    public int scale() {
        return value.scale();
    }

    /**
     * Returns a Decimal whose value is the absolute value of this Decimal, and whose scale is
     * <code>this.scale()</code>.
     * 
     * @return <code>abs(this)</code>
     */
    public Decimal abs() {
        return (signum() < 0 ? negate() : this);
    }

    /**
     * Returns a Decimal whose value is <code>(-this)</code>, and whose scale is
     * <code>this.scale()</code>.
     * 
     * @return <code>-this</code>
     */
    public Decimal negate() {
        return new Decimal(value.negate());
    }

    /**
     * Returns the signum function of this Decimal.
     * 
     * @return -1, 0 or 1 as the value of this Decimal is negative, zero or positive.
     */
    public int signum() {
        return value.signum();
    }

    /**
     * Returns a Decimal whose value is <code>(this + d)</code>, and whose scale is
     * <code>max(this.scale(), d.scale())</code>.
     * 
     * @param d value to be added to this Decimal.
     * @return this + d
     */
    public Decimal add(Decimal d) {
        if (d == null || d.isNull()) {
            return Decimal.NULL;
        }
        return new Decimal(value.add(d.value));
    }

    /**
     * Returns the maximum of the two values. If the two values are equals <code>this</code> will be
     * returned.
     */
    public Decimal max(Decimal value) {
        if (greaterThanOrEqual(value)) {
            return this;
        }
        return value;
    }

    /**
     * Returns the minimum of the two values. If the two values are equals <code>this</code> will be
     * returned.
     */
    public Decimal min(Decimal value) {
        if (lessThanOrEqual(value)) {
            return this;
        }
        return value;
    }

    /**
     * Returns a Decimal whose value is <code>(this + i)</code>, and whose scale is
     * <code>this.scale()</code>.
     * 
     * @param i Integer value to be added to this Decimal.
     * @return this + i
     */
    public Decimal add(Integer i) {
        if (i == null) {
            return Decimal.NULL;
        }
        return new Decimal(value.add(BigDecimal.valueOf(i.longValue())));
    }

    /**
     * Returns a Decimal whose value is <code>(this + i)</code>, and whose scale is
     * <code>this.scale()</code>.
     * 
     * @param i int value to be added to this Decimal.
     * @return this + i
     */
    public Decimal add(int i) {
        return new Decimal(value.add(BigDecimal.valueOf(i)));
    }

    /**
     * Returns a Decimal whose value is <code>(this - d)</code>, and whose scale is
     * <code>max(this.scale(), d.scale())</code>.
     * 
     * @param d value to be subtracted from this Decimal.
     * @return this - d
     */
    public Decimal subtract(Decimal d) {
        if (d == null || d.isNull()) {
            return Decimal.NULL;
        }
        return new Decimal(value.subtract(d.value));
    }

    /**
     * Returns a Decimal whose value is <code>(this - i)</code>, and whose scale is
     * <code>this.scale()</code>.
     * 
     * @param i value to be subtracted from this Decimal.
     * @return this - i
     */
    public Decimal subtract(int i) {
        return new Decimal(value.subtract(BigDecimal.valueOf(i)));
    }

    /**
     * Returns a Decimal whose value is <code>(this * d)</code>, and whose scale is
     * <code>(this.scale() + d.scale())</code>.
     * <p>
     * Returns {@link Decimal#NULL} if either this is {@link Decimal#NULL} or d is <code>null</code>
     * or d is {@link Decimal#NULL}.
     * 
     * @param d value to be multiplied with this Decimal.
     * 
     * @return this * val or null if d is null or Decimal.NULL.
     */
    public Decimal multiply(Decimal d) {
        if (d == null || d.isNull()) {
            return Decimal.NULL;
        }
        return new Decimal(value.multiply(d.value));
    }

    /**
     * Returns a Money whose value is <code>(this * m.getAmount())</code> and that has the same
     * currency as the given currency.
     * <p>
     * Returns <code>Money.NULL</code> if either this is {@link Decimal#NULL} or m is
     * <code>null</code> or m is <code>Money.NULL</code>.
     * 
     * @deprecated since 21.6. Use {@link Decimal#multiply(Money, RoundingMode)} instead.
     * 
     * @param m Money value to be multiplied with this Decimal.
     * @param roundingMode rounding mode that is applied according to the definition in BigDecimal.
     * 
     * @return see above
     * 
     * @see BigDecimal
     */
    @Deprecated
    public Money multiply(Money m, int roundingMode) {
        return multiply(m, RoundingMode.valueOf(roundingMode));
    }

    /**
     * Returns a Money whose value is <code>(this * m.getAmount())</code> and that has the same
     * currency as the given currency.
     * <p>
     * Returns <code>Money.NULL</code> if either this is {@link Decimal#NULL} or m is
     * <code>null</code> or m is <code>Money.NULL</code>.
     * 
     * @param m Money value to be multiplied with this Decimal.
     * @param roundingMode rounding mode that is applied.
     * 
     * @see BigDecimal
     */
    public Money multiply(Money m, RoundingMode roundingMode) {
        if (m == null) {
            return Money.NULL;
        }
        return m.multiply(this, roundingMode);
    }

    /**
     * Returns a Decimal whose value is <code>(this * i)</code>, and whose scale is
     * <code>this.scale()</code>.
     * <p>
     * Returns {@link Decimal#NULL} if either this is {@link Decimal#NULL} or d is
     * <code>null</code>.
     * 
     * @param i value to be multiplied with this Decimal.
     * 
     * @return this * i or null if this is Decimal.NULL.
     */
    public Decimal multiply(Integer i) {
        if (i == null) {
            return Decimal.NULL;
        }
        return new Decimal(value.multiply(BigDecimal.valueOf(i.longValue())));
    }

    /**
     * Returns a Decimal whose value is <code>(this * i)</code>, and whose scale is
     * <code>this.scale()</code>.
     * 
     * @param i value to be multiplied with this Decimal.
     * 
     * @return this * i or null if this is Decimal.NULL.
     */
    public Decimal multiply(int i) {
        return new Decimal(value.multiply(BigDecimal.valueOf(i)));
    }

    /**
     * Returns a Decimal whose value is <code>(this * l)</code>, and whose scale is
     * <code>this.scale()</code>.
     * 
     * @param l value to be multiplied with this Decimal.
     * 
     * @return this * l or Decimal.NULL if this is Decimal.NULL.
     */
    public Decimal multiply(long l) {
        return new Decimal(value.multiply(BigDecimal.valueOf(l)));
    }

    /**
     * Returns a Decimal whose value is <code>(this / d)</code>, and whose scale is as specified. If
     * rounding must be performed to generate a result with the specified scale, the specified
     * rounding mode is applied. If this decimal is the Decimal.NULL object, then Decimal.NULL is
     * returned.
     * 
     * @deprecated since 21.6. Use {@link #divide(int, int, RoundingMode)} instead.
     * 
     * @param value value by which this Decimal is to be divided
     * @param scale scale of the Decimal quotient to be returned
     * @param roundingMode rounding mode to apply
     * 
     * @return <code>this / d</code>
     * @throws ArithmeticException <code>value</code> is zero, <code>scale</code> is negative, or
     *             <code>roundingMode==ROUND_UNNECESSARY</code> and the specified scale is
     *             insufficient to represent the result of the division exactly.
     * @throws IllegalArgumentException if roundingMode cannot be converted to {@link RoundingMode}.
     */
    @Deprecated
    public Decimal divide(int value, int scale, int roundingMode) {
        return divide(Decimal.valueOf(value, 0), scale, RoundingMode.valueOf(roundingMode));
    }

    /**
     * Returns a Decimal whose value is <code>(this / d)</code>, and whose scale is as specified. If
     * rounding must be performed to generate a result with the specified scale, the specified
     * rounding mode is applied. If this decimal is the Decimal.NULL object, then Decimal.NULL is
     * returned.
     * 
     * @param value value by which this Decimal is to be divided
     * @param scale scale of the Decimal quotient to be returned
     * @param roundingMode rounding mode to apply
     * 
     * @return <code>this / d</code>
     * @throws ArithmeticException <code>value</code> is zero, <code>scale</code> is negative, or
     *             <code>roundingMode==ROUND_UNNECESSARY</code> and the specified scale is
     *             insufficient to represent the result of the division exactly.
     * @throws IllegalArgumentException <code>roundingMode</code> does not represent a valid
     *             rounding mode.
     */
    public Decimal divide(int value, int scale, RoundingMode roundingMode) {
        return divide(Decimal.valueOf(value, 0), scale, roundingMode);
    }

    /**
     * Returns a Decimal whose value is <code>(this / d)</code>, and whose scale is as specified. If
     * rounding must be performed to generate a result with the specified scale, the specified
     * rounding mode is applied. If this decimal is the Decimal.NULL object, then Decimal.NULL is
     * returned.
     * 
     * @deprecated since 21.6. Use {{@link #divide(long, int, RoundingMode)} instead.
     * 
     * @param value value by which this Decimal is to be divided
     * @param scale scale of the Decimal quotient to be returned
     * @param roundingMode rounding mode to apply
     * 
     * @return <code>this / d</code>
     * 
     * @throws ArithmeticException <code>value</code> is zero, <code>scale</code> is negative, or
     *             <code>roundingMode==ROUND_UNNECESSARY</code> and the specified scale is
     *             insufficient to represent the result of the division exactly.
     * @throws IllegalArgumentException if roundingMode cannot be converted to {@link RoundingMode}.
     */
    @Deprecated
    public Decimal divide(long value, int scale, int roundingMode) {
        return divide(Decimal.valueOf(value, 0), scale, RoundingMode.valueOf(roundingMode));
    }

    /**
     * Returns a Decimal whose value is <code>(this / d)</code>, and whose scale is as specified. If
     * rounding must be performed to generate a result with the specified scale, the specified
     * rounding mode is applied. If this decimal is the Decimal.NULL object, then Decimal.NULL is
     * returned.
     * 
     * @param value value by which this Decimal is to be divided
     * @param scale scale of the Decimal quotient to be returned
     * @param roundingMode rounding mode to apply
     * 
     * @return <code>this / d</code>
     * 
     * @throws ArithmeticException <code>value</code> is zero, <code>scale</code> is negative, or
     *             <code>roundingMode==ROUND_UNNECESSARY</code> and the specified scale is
     *             insufficient to represent the result of the division exactly.
     */
    public Decimal divide(long value, int scale, RoundingMode roundingMode) {
        return divide(Decimal.valueOf(value, 0), scale, roundingMode);
    }

    /**
     * Returns a Decimal whose value is <code>(this / d)</code>, and whose scale is as specified. If
     * rounding must be performed to generate a result with the specified scale, the specified
     * rounding mode is applied. If either this decimal or d is the Decimal.NULL, then Decimal.NULL
     * is returned.
     * 
     * @deprecated since 21.6. Use {@link #divide(Decimal, int, RoundingMode)} instead.
     * 
     * @param d value by which this Decimal is to be divided.
     * @param scale scale of the Decimal quotient to be returned.
     * @param roundingMode rounding mode to apply.
     * 
     * @return <code>this / d</code>
     * @throws ArithmeticException <code>value</code> is zero, <code>scale</code> is negative, or
     *             <code>roundingMode==ROUND_UNNECESSARY</code> and the specified scale is
     *             insufficient to represent the result of the division exactly.
     * @throws IllegalArgumentException if <code>roundingMode</code> cannot be converted to
     *             {@link RoundingMode}.
     */
    @Deprecated
    public Decimal divide(Decimal d, int scale, int roundingMode) {
        return divide(d, scale, RoundingMode.valueOf(roundingMode));
    }

    /**
     * Returns a Decimal whose value is <code>(this / d)</code>, and whose scale is as specified. If
     * rounding must be performed to generate a result with the specified scale, the specified
     * rounding mode is applied. If either this decimal or d is the Decimal.NULL, then Decimal.NULL
     * is returned.
     * 
     * @param d value by which this Decimal is to be divided.
     * @param scale scale of the Decimal quotient to be returned.
     * @param roundingMode rounding mode to apply.
     * 
     * @return <code>this / d</code>
     * 
     * @throws ArithmeticException <code>value</code> is zero, <code>scale</code> is negative, or
     *             <code>roundingMode==ROUND_UNNECESSARY</code> and the specified scale is
     *             insufficient to represent the result of the division exactly.
     */
    public Decimal divide(Decimal d, int scale, RoundingMode roundingMode) {
        if (isNull() || d.isNull()) {
            return Decimal.NULL;
        }
        return new Decimal(value.divide(d.value, scale, roundingMode));
    }

    /**
     * Returns a Decimal whose scale is the specified value, and whose unscaled value is determined
     * by multiplying or dividing this Decimal's unscaled value by the appropriate power of ten to
     * maintain its overall value. If the scale is reduced by the operation, the unscaled value must
     * be divided (rather than multiplied), and the value may be changed; in this case, the
     * specified rounding mode is applied to the division.
     * <p>
     * Note that since Decimal objects are immutable, calls of this method do <em>not</em> result in
     * the original object being modified, contrary to the usual convention of having methods named
     * <code>set<em>X</em></code> mutate field <code><em>X</em></code>. Instead,
     * <code>setScale</code> returns an object with the proper scale; the returned object may or may
     * not be newly allocated.
     * 
     * @deprecated since 21.6. Use {@link #setScale(int, RoundingMode)} instead.
     * 
     * @param scale scale of the Decimal value to be returned.
     * @param roundingMode The rounding mode to apply.
     * @return a Decimal whose scale is the specified value, and whose unscaled value is determined
     *         by multiplying or dividing this Decimal's unscaled value by the appropriate power of
     *         ten to maintain its overall value.
     * @throws ArithmeticException <code>scale</code> is negative, or
     *             <code>roundingMode==ROUND_UNNECESSARY</code> and the specified scaling operation
     *             would require rounding.
     * @throws IllegalArgumentException if <code>roundingMode</code> cannot be converted to
     *             {@link RoundingMode}.
     * 
     * @see java.math.BigDecimal#ROUND_UP
     * @see java.math.BigDecimal#ROUND_DOWN
     * @see java.math.BigDecimal#ROUND_CEILING
     * @see java.math.BigDecimal#ROUND_FLOOR
     * @see java.math.BigDecimal#ROUND_HALF_UP
     * @see java.math.BigDecimal#ROUND_HALF_DOWN
     * @see java.math.BigDecimal#ROUND_HALF_EVEN
     * @see java.math.BigDecimal#ROUND_UNNECESSARY
     */
    @Deprecated
    public Decimal setScale(int scale, int roundingMode) {
        return setScale(scale, RoundingMode.valueOf(roundingMode));
    }

    /**
     * Returns a Decimal whose scale is the specified value, and whose unscaled value is determined
     * by multiplying or dividing this Decimal's unscaled value by the appropriate power of ten to
     * maintain its overall value. If the scale is reduced by the operation, the unscaled value must
     * be divided (rather than multiplied), and the value may be changed; in this case, the
     * specified rounding mode is applied to the division.
     * <p>
     * Note that since Decimal objects are immutable, calls of this method do <em>not</em> result in
     * the original object being modified, contrary to the usual convention of having methods named
     * <code>set<em>X</em></code> mutate field <code><em>X</em></code>. Instead,
     * <code>setScale</code> returns an object with the proper scale; the returned object may or may
     * not be newly allocated.
     * 
     * @param scale scale of the Decimal value to be returned.
     * 
     * @param roundingMode The rounding mode to apply.
     * @return a Decimal whose scale is the specified value, and whose unscaled value is determined
     *         by multiplying or dividing this Decimal's unscaled value by the appropriate power of
     *         ten to maintain its overall value.
     * @throws ArithmeticException <code>scale</code> is negative, or
     *             <code>roundingMode==ROUND_UNNECESSARY</code> and the specified scaling operation
     *             would require rounding.
     * 
     * @see java.math.RoundingMode#UP
     * @see java.math.RoundingMode#DOWN
     * @see java.math.RoundingMode#CEILING
     * @see java.math.RoundingMode#FLOOR
     * @see java.math.RoundingMode#HALF_UP
     * @see java.math.RoundingMode#HALF_DOWN
     * @see java.math.RoundingMode#HALF_EVEN
     * @see java.math.RoundingMode#UNNECESSARY
     */
    public Decimal setScale(int scale, RoundingMode roundingMode) {
        return new Decimal(value.setScale(scale, roundingMode));
    }

    /**
     * Returns a decimal that is rounded with the given rounding mode and precision and has (in
     * contrast to the {@link #setScale(int, int)} method) the same scale as this decimal.
     * 
     * @deprecated since 21.6. Use {@link #round(int, RoundingMode)} instead.
     * 
     * @param precision The number of digits the value is rounded to.
     * @param roundingMode The rounding mode to apply
     * 
     * @throws ArithmeticException if <code>precision</code> is negative.
     * @throws IllegalArgumentException if <code>roundingMode</code> cannot be converted to
     *             {@link RoundingMode}.
     * 
     * @see #setScale(int, int)
     * @see java.math.BigDecimal#ROUND_UP
     * @see java.math.BigDecimal#ROUND_DOWN
     * @see java.math.BigDecimal#ROUND_CEILING
     * @see java.math.BigDecimal#ROUND_FLOOR
     * @see java.math.BigDecimal#ROUND_HALF_UP
     * @see java.math.BigDecimal#ROUND_HALF_DOWN
     * @see java.math.BigDecimal#ROUND_HALF_EVEN
     * @see java.math.BigDecimal#ROUND_UNNECESSARY
     */
    @Deprecated
    public Decimal round(int precision, int roundingMode) {
        return round(precision, RoundingMode.valueOf(roundingMode));
    }

    /**
     * Returns a decimal that is rounded with the given rounding mode and precision and has (in
     * contrast to the {@link #setScale(int, RoundingMode)} method) the same scale as this decimal.
     * 
     * @param precision The number of digits the value is rounded to.
     * @param roundingMode The rounding mode to apply
     * 
     * @throws ArithmeticException if <code>precision</code> is negative.
     * 
     * @see java.math.RoundingMode#UP
     * @see java.math.RoundingMode#DOWN
     * @see java.math.RoundingMode#CEILING
     * @see java.math.RoundingMode#FLOOR
     * @see java.math.RoundingMode#HALF_UP
     * @see java.math.RoundingMode#HALF_DOWN
     * @see java.math.RoundingMode#HALF_EVEN
     * @see java.math.RoundingMode#UNNECESSARY
     */
    public Decimal round(int precision, RoundingMode roundingMode) {
        return setScale(precision, roundingMode).setScale(scale(), RoundingMode.UNNECESSARY);
    }

    @Override
    public int intValue() {
        return value.intValue();
    }

    @Override
    public long longValue() {
        return value.longValue();
    }

    @Override
    public float floatValue() {
        return value.floatValue();
    }

    @Override
    public double doubleValue() {
        return value.doubleValue();
    }

    /**
     * Returns the value of the specified number as a <code>BigDecimal</code>.
     */
    public BigDecimal bigDecimalValue() {
        return value;
    }

    /**
     * Typesafe version of the compareTo method.
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(Decimal d) {
        if (d.isNull()) {
            return 1;
        }
        return value.compareTo(d.value);
    }

    /**
     * Returns true if the indicated Decimal is greater than this one. Two Decimals that are equal
     * in value but have a different scale (like 2.0 and 2.00) are considered equal by this method.
     * If <code>d</code> is <code>null</code> or {@link Decimal#NULL} the method returns false.
     */
    public boolean greaterThan(Decimal d) {
        if (d == null || d.isNull()) {
            return false;
        }
        return value.compareTo(d.value) > 0;
    }

    /**
     * Returns true if the indicated Decimal is greater or equal than this one. Two Decimals that
     * are equal in value but have a different scale (like 2.0 and 2.00) are considered equal by
     * this method. If <code>d</code> is <code>null</code> or {@link Decimal#NULL} the method
     * returns false.
     */
    public boolean greaterThanOrEqual(Decimal d) {
        if (d == null || d.isNull()) {
            return false;
        }
        return value.compareTo(d.value) >= 0;
    }

    /**
     * Returns true if the indicated Decimal is smaller than this one. Two Decimals that are equal
     * in value but have a different scale (like 2.0 and 2.00) are considered equal by this method.
     * If <code>d</code> is <code>null</code> or {@link Decimal#NULL} the method returns false.
     */
    public boolean lessThan(Decimal d) {
        if (d == null || d.isNull()) {
            return false;
        }
        return value.compareTo(d.value) < 0;
    }

    /**
     * Returns true if the indicated Decimal is smaller than or equal to this one. Two Decimals that
     * are equal in value but have a different scale (like 2.0 and 2.00) are considered equal by
     * this method. If <code>d</code> is <code>null</code> or {@link Decimal#NULL} the method
     * returns false.
     */
    public boolean lessThanOrEqual(Decimal d) {
        if (d == null || d.isNull()) {
            return false;
        }
        return value.compareTo(d.value) <= 0;
    }

    /**
     * Returns true if the indicated Decimal is equal to this one. Two Decimals that are equal in
     * value but have a different scale (like 2.0 and 2.00) are considered equal by this method in
     * contrast to the <code>equals(Object o)</code> method.
     * <p>
     * If <code>d</code> is <code>null</code> or {@link Decimal#NULL} the method returns false.
     * 
     * @deprecated since 21.6. equals(Object o) also ignores the scale so this method is superfluous
     */
    @Deprecated
    public boolean equalsIgnoreScale(Decimal d) {
        if (d == null || d.isNull()) {
            return false;
        }
        return value.compareTo(d.value) == 0;
    }

    /**
     * Returns true if the indicated Decimal is not equal to this one. Two Decimals that are equal
     * in value but have a different scale (like 2.0 and 2.00) are considered equal by this method
     * in contrast to the <code>equals(Object o)</code> method.
     * <p>
     * If <code>d</code> is <code>null</code> or {@link Decimal#NULL} the method returns false.
     * 
     * @deprecated since 21.6. equals(Object o) also ignores the scale so this method is superfluous
     */
    @Deprecated
    public boolean notEqualsIgnoreScale(Decimal d) {
        if (d == null || d.isNull()) {
            return false;
        }
        return value.compareTo(d.value) != 0;
    }

    /**
     * Compares this Decimal with the specified Object for equality. <strong> Note that unlike the
     * BigDecimal class this method ignores the value's scale. This decision was made as most people
     * consider 1.0 and 1.00 as equal. </strong>
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Decimal)) {
            return false;
        }
        Decimal other = (Decimal)o;
        if (other.isNull()) {
            return false;
        }
        return value.compareTo(other.value) == 0;
    }

    /**
     * Returns the hash code for the decimal value. <strong>Note that unlike the {@link BigDecimal}
     * class this class ignores the value's scale when comparing for equality. Thus this method
     * returns the same hash code for 1.0 and 1.00 for example.</strong>
     */
    @Override
    public int hashCode() {
        return value.stripTrailingZeros().hashCode();
    }

    @Override
    public String toString() {
        return value.toString();
    }

}
