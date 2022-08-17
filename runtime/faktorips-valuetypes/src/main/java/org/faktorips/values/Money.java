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

import java.io.Serializable;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.util.Currency;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.faktorips.values.xml.MoneyXmlAdapter;

/**
 * Value class representing an amount of money in any currency.
 * <p>
 * The class provides a special value for handling null. The value is available via
 * <code>Money.NULL</code> constant. The Money class provides a special isNull() method to check if
 * a money instance is the special case null or not. The null object makes the implementation of
 * calculation routines much simpler as you don't have to check, if a money amount is null or not.
 * <p>
 * Example: Given the expression a+b
 * <p>
 * You can write this using Money as: <code>a.add(b)</code>. If a or b is the null value, also the
 * result is the null value. And this is the correct behavior. If you don't know for example how
 * much a is, you can't tell what a+b is. If you would use for example Double objects you would have
 * to write code that checks if a or b is null which is very tedious and error prone.
 * <p>
 * The class does not provide any methods that work with <code>double</code>, <code>float</code>,
 * <code>java.lang.Double</code> or <code>java.lang.Float</code> because of the loss of precision.
 * If you want to have exact numbers (and when it comes to money in most cases you will) you should
 * avoid these datatypes.
 * 
 * @author Jan Ortmann
 */
@XmlJavaTypeAdapter(MoneyXmlAdapter.class)
public class Money implements Comparable<Money>, NullObjectSupport, Serializable {

    /**
     * Constant representing the <code>null</code> value.
     */
    public static final Money NULL = new MoneyNull();

    /**
     * Currency EUR
     */
    public static final Currency EUR = Currency.getInstance("EUR");

    /**
     * Currency USD
     */
    public static final Currency USD = Currency.getInstance("USD");

    private static final long serialVersionUID = 5639586670329581901L;

    private static final int[] POWER_10 = { 1, 10, 100, 1000, 10000, 100000 };

    /**
     * the money's amount is stored internally as long; for example: The internal amount for 3.50EUR
     * is 350.
     */
    private final long internalAmount;

    private final Currency currency;

    /**
     * Creates a new money object.
     */
    Money(long intAmount, Currency currency) {
        internalAmount = intAmount;
        this.currency = currency;
    }

    /**
     * Returns a money object with the currency euro and an amount defined by the given euros and
     * cents.
     */
    public static final Money euro(long euros, int cents) {
        return Money.valueOf(euros, cents, EUR);
    }

    /**
     * Returns a money object with the currency euro and an amount defined by the given euros and 0
     * cents.
     */
    public static final Money euro(long euros) {
        return Money.valueOf(euros, 0, EUR);
    }

    /**
     * Returns a money object with the currency us dollar and an amount defined by the given
     * usdollars and cents.
     */
    public static final Money usd(long usdollars, int cents) {
        return Money.valueOf(usdollars, cents, USD);
    }

    /**
     * Returns a money object with the currency us dollar and an amount defined by the given
     * usdollars and 0 cents.
     */
    public static final Money usd(long usdollars) {
        return Money.valueOf(usdollars, 0, USD);
    }

    /**
     * Returns a {@link Collector} that sums a {@link Stream} of money objects.
     */
    public static final Collector<Money, ?, Optional<Money>> sum() {
        return Collectors.reducing(Money::add);
    }

    /**
     * Returns a {@link Collector} that sums a {@link Stream} of money objects.
     *
     * @param currency {@link Currency} of the money objects. This parameter is needed to return a
     *            money object with the correct currency even if the Stream is empty.
     */
    public static final Collector<Money, ?, Money> sum(Currency currency) {
        return Collectors.reducing(new Money(0, currency), Money::add);
    }

    /**
     * Returns a {@link Collector} that sums a {@link Stream} of money objects with the currency
     * euro.
     */
    public static final Collector<Money, ?, Money> sumEuro() {
        return sum(EUR);
    }

    /**
     * Returns a {@link Collector} that sums a {@link Stream} of money objects with the currency
     * usdollars.
     */
    public static final Collector<Money, ?, Money> sumUsd() {
        return sum(USD);
    }

    /**
     * Returns the money value represented by the given string. Returns <code>Money.NULL</code>, if
     * the value is either <code>null</code> or the empty string or "MoneyNull".
     * 
     * @throws IllegalArgumentException if the String does not represent a money amount, e.g. if the
     *             currency symbol is invalid.
     */
    // CSOFF: IllegalCatch
    // CSOFF: CyclomaticComplexity
    public static final Money valueOf(String value) {
        if (value == null || "".equals(value) || MoneyNull.STRING_REPRESENTATION.equals(value)) {
            return Money.NULL;
        }
        String initialValue = value;
        try {
            String trimmedValue = value.trim();
            Currency currency = Currency.getInstance(trimmedValue.substring(trimmedValue.length() - 3));
            String amountValue = trimmedValue.substring(0, trimmedValue.length() - 3).trim();
            long majorUnits;
            int minorUnits = 0;
            int dotPos = amountValue.indexOf('.');
            int fractionDigitsIn = 0;
            if (dotPos == -1) {
                majorUnits = Long.parseLong(amountValue);
            } else {
                majorUnits = Long.parseLong(amountValue.substring(0, dotPos));
                if (dotPos < amountValue.length() - 1) {
                    String minorUnitsString = amountValue.substring(dotPos + 1);
                    if (minorUnitsString.length() > 0) {
                        minorUnits = Integer.parseInt(minorUnitsString);
                        fractionDigitsIn = minorUnitsString.length();
                    }
                }
            }

            int fractionDigits = currency.getDefaultFractionDigits();
            if (fractionDigits < fractionDigitsIn) {
                throw new IllegalArgumentException("Too much fraction digits (is " + minorUnits + ")");
            }

            // we can get leading zeros in the fraction part of the value, e.g "09".
            // this is different to "9" as fraction part, for example. The leading zero is lost
            // by converting the string to an int above, but the number of fractionDigits
            // of the input is saved in fractionDigitsIn.
            // we have to care about the correct amount in minorUnits, and that is done here:
            while (fractionDigits > 0 && fractionDigits > fractionDigitsIn && minorUnits > 0) {
                minorUnits *= 10;
                fractionDigitsIn++;
            }

            if (amountValue.charAt(0) == '-') {
                return Money.valueOf(majorUnits, minorUnits * -1, currency);
            }
            return Money.valueOf(majorUnits, minorUnits, currency);
        } catch (Exception e) {
            throw (IllegalArgumentException)new IllegalArgumentException("Can't parse " + initialValue).initCause(e);
        }
    }

    // CSON: IllegalCatch
    // CSON: CyclomaticComplexity

    /**
     * Returns a money value in the indicated currency and with the indicated number of major units
     * (e.g. Euro or US Dollar) and minor units (e.g. Cents).
     */
    public static final Money valueOf(long majorUnits, int minorUnits, Currency currency) {
        // the first check is for currencies like Yen which does not have minor units at all
        // the second check verify that the count of digits of the minor units is smaller than the
        // default fraction digits
        if (minorUnits == currency.getDefaultFractionDigits()
                || Math.log10(Math.abs(minorUnits)) <= currency.getDefaultFractionDigits()) {
            long intAmount = majorUnits * POWER_10[currency.getDefaultFractionDigits()] + minorUnits;
            return new Money(intAmount, currency);
        }
        throw new IllegalArgumentException("Too much fraction digits (is " + minorUnits + ")");
    }

    /**
     * Returns a money value with the indicated value and currency.
     * 
     * @param value The money amount's value
     * @param currency The money amount's currency
     * @throws IllegalArgumentException if the value's scale is greater than the currency's default
     *             fraction digits, e.g. value is 10.324 (scale 3) and currency is Euro with 2
     *             digits.
     */
    public static final Money valueOf(Decimal value, Currency currency) {
        if (value == null || currency == null || value.isNull()) {
            return Money.NULL;
        }
        if (value.scale() == 0) {
            return Money.valueOf(value.longValue(), 0, currency);
        }
        if (value.scale() > currency.getDefaultFractionDigits()) {
            throw new IllegalArgumentException("Value " + value
                    + " has more digits after the decimal point than the curreny " + currency + " supports!");
        }
        if (value.scale() < currency.getDefaultFractionDigits()) {
            return new Money(
                    value.bigDecimalValue().setScale(currency.getDefaultFractionDigits()).unscaledValue().longValue(),
                    currency);
        }
        return new Money(value.bigDecimalValue().unscaledValue().longValue(), currency);
    }

    /**
     * Returns a money value with the indicated value and currency. If the value's scale is greater
     * than the currency's default fraction digits, the indicated rounding mode is applied to set
     * the value's scale to the currency's default fraction digits.
     * 
     * @deprecated since 21.6. Use {@link #valueOf(Decimal, Currency, RoundingMode)} instead.
     * 
     * @param value the money amount's value
     * @param currency the money amount's currency
     * @param roundingMode the rounding mode according to the definition in BigDecimal to be applied
     *            when the value's scale is greater than the currency's default fraction digits
     * 
     * @throws IllegalArgumentException if roundingMode cannot be converted to {@link RoundingMode}.
     */
    @Deprecated
    public static final Money valueOf(Decimal value, Currency currency, int roundingMode) {
        return valueOf(value, currency, RoundingMode.valueOf(roundingMode));
    }

    /**
     * Returns a money value with the indicated value and currency. If the value's scale is greater
     * than the currency's default fraction digits, the indicated rounding mode is applied to set
     * the value's scale to the currency's default fraction digits.
     * 
     * @param value the money amount's value
     * @param currency the money amount's currency
     * @param roundingMode the rounding mode to be applied when the value's scale is greater than
     *            the currency's default fraction digits
     */
    public static final Money valueOf(Decimal value, Currency currency, RoundingMode roundingMode) {
        if (value == null || currency == null || value.isNull()) {
            return Money.NULL;
        }
        Decimal scaledValue = value;
        if (value.scale() > currency.getDefaultFractionDigits()) {
            scaledValue = value.setScale(currency.getDefaultFractionDigits(), roundingMode);
        }
        return Money.valueOf(scaledValue.toString() + currency.toString());
    }

    /**
     * Returns the amount of money, e.g. for 13.45EUR the method returns 13.45. For the special case
     * MoneyNull the method returns <code>Decimal.NULL</code>.
     */
    public Decimal getAmount() {
        return Decimal.valueOf(internalAmount, currency.getDefaultFractionDigits());
    }

    /**
     * Returns the money value's currency. For the special case MoneyNull the method returns null.
     */
    public Currency getCurrency() {
        return currency;
    }

    /**
     * Returns true if this instance represents the special case null, otherwise false.
     */
    @Override
    public boolean isNull() {
        return false;
    }

    @Override
    public boolean isNotNull() {
        return true;
    }

    /**
     * Adds the money value to this money value.
     * 
     * @return A money value with the same currency this value object has and an amount that is the
     *             sum of this value's amount and the passed value's amount. Returns Money.NULL if
     *             either this object is the Money.NULL object, or the other value is the Money.NULL
     *             object.
     * 
     * @throws IllegalArgumentException if the two money values have different currencies
     * @throws NullPointerException if value is <code>null</code>
     */
    public Money add(Money value) {
        if (value.isNull()) {
            return Money.NULL;
        }
        if (!currency.equals(value.currency)) {
            throw new IllegalArgumentException(
                    "Can't add " + this + " and " + value + " because they have different currencies.");
        }
        return new Money(internalAmount + value.internalAmount, currency);
    }

    /**
     * Subtracts the indicated money value from this money value.
     * 
     * @return A money value with the same currency this money object has and an amount that is this
     *             value's amount minus the passed value's amount. Returns Money.NULL if either this
     *             object is the Money.NULL object, or the other value is the Money.NULL object.
     * 
     * @throws IllegalArgumentException if the two money values have different currencies
     * @throws NullPointerException if value is <code>null</code>
     */
    public Money subtract(Money value) {
        if (value.isNull()) {
            return Money.NULL;
        }
        if (!currency.equals(value.currency)) {
            throw new IllegalArgumentException(
                    "Can't subtract " + value + " from " + this + " because they have different currencies.");
        }
        return new Money(internalAmount - value.internalAmount, currency);
    }

    /**
     * Multiplies this money amount with the given integer faktor.
     * 
     * @param factor The int factor this money amount is multiplied with.
     * 
     * @return new money amount with the same currency and a value of <code>this * factor</code>
     */
    public Money multiply(int factor) {
        return new Money(internalAmount * factor, currency);
    }

    /**
     * Multiplies this money amount with the given integer faktor.
     * 
     * @param factor The int factor this money amount is multiplied with.
     * 
     * @return new money amount with the same currency and a value of <code>this * factor</code>.
     *             Returns <code>Money.NULL</code> if either this is <code>Money.NULL</code> or
     *             factor is <code>null</code>.
     */
    public Money multiply(Integer factor) {
        if (factor == null) {
            return NULL;
        }
        return new Money(internalAmount * factor.intValue(), currency);
    }

    /**
     * Multiplies this money amount with the given long faktor.
     * 
     * @param factor The long factor this money amount is multiplied with.
     * 
     * @return new money amount with the same currency and a value of <code>this * factor</code>
     */
    public Money multiply(long factor) {
        return new Money(internalAmount * factor, currency);
    }

    /**
     * Multiplies this money amount with the given decimal value. If rounding is required to set the
     * scale to the currencie's number of fractional digits the given rounding mode is used.
     * 
     * @deprecated since 21.6. Use {@link #multiply(Decimal, RoundingMode)} instead.
     * 
     * @param d The decimal value this money amount is multiplied with.
     * @param roundingMode One of the rounding modes defined in <code>BigDecimal</code>
     * 
     * @return new money amount with the same currency and a value of <code>this * d</code>
     * 
     * @throws NullPointerException if d is <code>null</code>.
     * @throws IllegalArgumentException if roundingMode cannot be converted to {@link RoundingMode}.
     */
    @Deprecated
    public Money multiply(Decimal d, int roundingMode) {
        return multiply(d, RoundingMode.valueOf(roundingMode));
    }

    /**
     * Multiplies this money amount with the given decimal value. If rounding is required to set the
     * scale to the currencie's number of fractional digits the given rounding mode is used.
     * 
     * @param d The decimal value this money amount is multiplied with.
     * @param roundingMode The rounding mode.
     * 
     * @return new money amount with the same currency and a value of <code>this * d</code>
     * 
     * @throws NullPointerException if d is <code>null</code>.
     */
    public Money multiply(Decimal d, RoundingMode roundingMode) {
        if (d.isNull()) {
            return Money.NULL;
        }
        long newInternalAmount = d.multiply(internalAmount).setScale(0, roundingMode).longValue();
        return new Money(newInternalAmount, currency);
    }

    /**
     * Divides this money amount by the given divisor. If rounding is required to set the scale to
     * the currencie's number of fractional digits the given rounding mode is used.
     * 
     * @deprecated since 21.6. Use {@link #divide(int, RoundingMode)} instead.
     * 
     * @param d The divisor.
     * @param roundingMode One of the rounding modes defined in <code>BigDecimal</code>
     * 
     * @return new money amount with the same currency and a value of <code>this / d</code> rounded
     *             to the fractional digits defined by the money's currency using the given rounding
     *             mode.
     *             <p>
     *             Returns <code>Money.null</code> if this is the Money.NULL object.
     * 
     * @throws IllegalArgumentException if roundingMode cannot be converted to {@link RoundingMode}.
     */
    @Deprecated
    public Money divide(int d, int roundingMode) {
        return divide(d, RoundingMode.valueOf(roundingMode));
    }

    /**
     * Divides this money amount by the given divisor. If rounding is required to set the scale to
     * the currencie's number of fractional digits the given rounding mode is used.
     * 
     * @param d The divisor.
     * @param roundingMode The rounding mode.
     * 
     * @return new money amount with the same currency and a value of <code>this / d</code> rounded
     *             to the fractional digits defined by the money's currency using the given rounding
     *             mode.
     *             <p>
     *             Returns <code>Money.null</code> if this is the Money.NULL object.
     */
    public Money divide(int d, RoundingMode roundingMode) {
        long newAmount = getAmount().divide(d, currency.getDefaultFractionDigits(), roundingMode)
                .multiply(POWER_10[currency.getDefaultFractionDigits()]).longValue();
        return new Money(newAmount, currency);
    }

    /**
     * Divides this money amount by the given divisor. If rounding is required to set the scale to
     * the currencie's number of fractional digits the given rounding mode is used.
     * 
     * @deprecated since 21.6. Use {@link #divide(long, RoundingMode)} instead.
     * 
     * @param d The divisor.
     * @param roundingMode One of the rounding modes defined in <code>BigDecimal</code>
     * 
     * @return new money amount with the same currency and a value of <code>this / d</code> rounded
     *             to the fractional digits defined by the money's currency using the given rounding
     *             mode.
     *             <p>
     *             Returns <code>Money.null</code> if this is the Money.NULL object.
     * 
     * @throws IllegalArgumentException if roundingMode cannot be converted to {@link RoundingMode}.
     */
    @Deprecated
    public Money divide(long d, int roundingMode) {
        return divide(d, RoundingMode.valueOf(roundingMode));
    }

    /**
     * Divides this money amount by the given divisor. If rounding is required to set the scale to
     * the currencie's number of fractional digits the given rounding mode is used.
     * 
     * @param d The divisor.
     * @param roundingMode The rounding mode.
     * 
     * @return new money amount with the same currency and a value of <code>this / d</code> rounded
     *             to the fractional digits defined by the money's currency using the given rounding
     *             mode.
     *             <p>
     *             Returns <code>Money.null</code> if this is the Money.NULL object.
     */
    public Money divide(long d, RoundingMode roundingMode) {
        long newAmount = getAmount().divide(d, currency.getDefaultFractionDigits(), roundingMode)
                .multiply(POWER_10[currency.getDefaultFractionDigits()]).longValue();
        return new Money(newAmount, currency);
    }

    /**
     * Divides this money amount by the given divisor. If rounding is required to set the scale to
     * the currencie's number of fractional digits the given rounding mode is used.
     * 
     * @deprecated since 21.6. Use {@link #divide(Decimal, RoundingMode)} instead.
     * 
     * @param d The divisor.
     * @param roundingMode One of the rounding modes defined in <code>BigDecimal</code>
     * 
     * @return new money amount with the same currency and a value of <code>this / d</code> rounded
     *             to the fractional digits defined by the money's currency using the given rounding
     *             mode.
     *             <p>
     *             Returns <code>Money.null</code> if this is the Money.NULL object or the divisor
     *             is the <code>Decimal.NULL</code> object.
     * 
     * @throws NullPointerException if d is <code>null</code>.
     * @throws IllegalArgumentException if roundingMode cannot be converted to {@link RoundingMode}.
     */
    @Deprecated
    public Money divide(Decimal d, int roundingMode) {
        return divide(d, RoundingMode.valueOf(roundingMode));
    }

    /**
     * Divides this money amount by the given divisor. If rounding is required to set the scale to
     * the currencie's number of fractional digits the given rounding mode is used.
     * 
     * @param d The divisor.
     * @param roundingMode The rounding mode.
     * 
     * @return new money amount with the same currency and a value of <code>this / d</code> rounded
     *             to the fractional digits defined by the money's currency using the given rounding
     *             mode.
     *             <p>
     *             Returns <code>Money.null</code> if this is the Money.NULL object or the divisor
     *             is the <code>Decimal.NULL</code> object.
     * 
     * @throws NullPointerException if d is <code>null</code>.
     */
    public Money divide(Decimal d, RoundingMode roundingMode) {
        if (d.isNull()) {
            return Money.NULL;
        }
        long newAmount = getAmount().divide(d, currency.getDefaultFractionDigits(), roundingMode)
                .multiply(POWER_10[currency.getDefaultFractionDigits()]).longValue();
        return new Money(newAmount, currency);
    }

    /**
     * Typesafe version of the compareTo method.
     * 
     * @throws IllegalArgumentException if this money and the other money object have different
     *             currencies.
     * @throws IllegalArgumentException if this either this money or the other money object are
     *             <code>Money.Null</code>
     * @throws NullPointerException if the other money object is <code>null</code>.
     */
    @Override
    public int compareTo(Money other) {
        // First check the currencies
        if (other.isNull()) {
            return 1;
        }
        if (!getCurrency().equals(other.getCurrency())) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "The currencies this:{0} and other:{1} are different. The objects are not comparable.",
                    getCurrency(),
                    other.getCurrency()));
        }
        return getAmount().compareTo(other.getAmount());
    }

    /**
     * Returns true if this Money is greater than the other money object. If either this money
     * object or other is <code>Money.NULL</code> the method returns false.
     * 
     * @throws NullPointerException if other is <code>null</code>.
     * @throws IllegalArgumentException if this money object and the other money object have
     *             different currencies.
     */
    public boolean greaterThan(Money other) {
        if (other.isNull()) {
            return false;
        }
        return compareTo(other) > 0;
    }

    /**
     * Returns true if this Money is greater than or equal to the other money object. If either this
     * money object or other is <code>Money.NULL</code> the method returns false.
     * 
     * @throws NullPointerException if other is <code>null</code>.
     * @throws IllegalArgumentException if this money object and the other money object have
     *             different currencies.
     */
    public boolean greaterThanOrEqual(Money other) {
        if (other.isNull()) {
            return false;
        }
        return compareTo(other) >= 0;
    }

    /**
     * Returns true if this Money is less than the other money object. If either this money object
     * or other is <code>Money.NULL</code> the method returns false.
     * 
     * @throws NullPointerException if other is <code>null</code>.
     * @throws IllegalArgumentException if this money object and the other money object have
     *             different currencies.
     */
    public boolean lessThan(Money other) {
        if (other.isNull()) {
            return false;
        }
        return compareTo(other) < 0;
    }

    /**
     * Returns true if the this Money is less than or equal to the other money object. Two Moneys
     * that are equal in value but have a different scale (like 2.0 and 2.00) are considered equal
     * by this method. If either this money object or other is <code>Money.NULL</code> the method
     * returns false.
     * 
     * @throws NullPointerException if other is <code>null</code>.
     * @throws IllegalArgumentException if this money object and the other money object have
     *             different currencies.
     */
    public boolean lessThanOrEqual(Money other) {
        if (other.isNull()) {
            return false;
        }
        return compareTo(other) <= 0;
    }

    /**
     * Returns the maximum of the two values. If the two values are equals <code>this</code> will be
     * returned.
     */
    public Money max(Money value) {
        if (greaterThanOrEqual(value)) {
            return this;
        }
        return value;
    }

    /**
     * Returns the minimum of the two values. If the two values are equals <code>this</code> will be
     * returned.
     */
    public Money min(Money value) {
        if (lessThanOrEqual(value)) {
            return this;
        }
        return value;
    }

    /**
     * Overridden Method.
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return (int)internalAmount;
    }

    /**
     * Overridden Method.
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Money)) {
            return false;
        }
        Money other = (Money)o;
        if (isNull() && other.isNull()) {
            return true;
        } else if (isNull() || other.isNull()) {
            return false;
        } else {
            return internalAmount == other.internalAmount && currency.equals(other.currency);
        }
    }

    /**
     * Overridden Method.
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "" + getAmount() + ' ' + currency.toString();
    }

}
