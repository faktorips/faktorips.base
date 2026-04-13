/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.valueset;

import static org.faktorips.values.ObjectUtil.isNull;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.faktorips.values.NullObject;

/**
 * Default implementation of the <code>Range</code> interface. Implementations of this range that
 * support incremental steps must provide public factory methods instead of public constructors.
 * Within the factory method the checkIfStepFitsIntoBounds() method has to be called on the created
 * object to ensure that the step increment and the bounds of the range are consistent.
 * 
 * @author Jan Ortmann, Peter Erzberger
 * @author Daniel Hohenberger conversion to Java5
 */
public class DefaultRange<T extends Comparable<? super T>> implements Range<T> {

    private static final long serialVersionUID = -2886828952622682291L;

    private final T lowerBound;
    private final T upperBound;
    private final T step;
    private final boolean containsNull;
    private final boolean empty;
    private final boolean lowerBoundOpen;
    private final boolean upperBoundOpen;

    /**
     * Creates a new empty {@link DefaultRange} instance that doesn't contain any values.
     */
    public DefaultRange() {
        lowerBound = null;
        upperBound = null;
        step = null;
        containsNull = false;
        empty = true;
        lowerBoundOpen = false;
        upperBoundOpen = false;
    }

    /**
     * Creates a new continuous {@link DefaultRange} instance that doesn't contain null.
     * 
     * @param lower bound of the range
     * @param upper bound of the range
     */
    public DefaultRange(T lower, T upper) {
        this(lower, upper, null, false);
    }

    /**
     * Creates a new continuous {@link DefaultRange} instance. The third parameter defines if the
     * range contains null or not. Null can mean the native java {@code null} or a null
     * representation value specific to the datatype the range implementation is for.
     */
    public DefaultRange(T lower, T upper, boolean containsNull) {
        this(lower, upper, null, containsNull);
    }

    /**
     * Creates a new {@link DefaultRange} instance that doesn't contain null. Both bounds default to
     * closed (inclusive).
     *
     * @param lower bound of the range
     * @param upper bound of the range
     * @param step the unit that defines the discrete values that are allowed to be within this
     *            range. The value can be {@code null} indicating that it is a continuous range. It
     *            has to fulfill the condition: the value of the expression <em>abs(upperBound -
     *            lowerBound) / step</em> needs to be an integer
     * @throws IllegalArgumentException if the condition <em>abs(upperBound - lowerBound) /
     *             step</em> is not met. The condition is not applied if one or both of the bounds
     *             are {@code null}
     */
    public DefaultRange(T lower, T upper, T step) {
        this(lower, upper, step, false);
    }

    /**
     * Creates a new {@link DefaultRange} instance. Both bounds default to closed (inclusive).
     *
     * @param lower bound of the range
     * @param upper bound of the range
     * @param step the unit that defines the discrete values that are allowed to be within this
     *            range. The value can be {@code null} indicating that it is a continuous range. It
     *            has to fulfill the condition: the value of the expression <em>abs(upperBound -
     *            lowerBound) / step</em> needs to be an integer
     * @param containsNull whether this range contains null
     * @throws IllegalArgumentException if the condition <em>abs(upperBound - lowerBound) /
     *             step</em> is not met. The condition is not applied if one or both of the bounds
     *             are {@code null}
     */
    public DefaultRange(T lower, T upper, T step, boolean containsNull) {
        this(lower, upper, step, containsNull, false, false);
    }

    /**
     * Creates a new {@link DefaultRange} instance with open/closed bound configuration.
     *
     * @param lower bound of the range
     * @param upper bound of the range
     * @param step the unit that defines the discrete values that are allowed to be within this
     *            range. The value can be {@code null} indicating that it is a continuous range
     * @param containsNull whether this range contains null. Null can mean the native java
     *            {@code null} or a null representation value specific to the datatype the range
     *            implementation is for.
     * @param lowerBoundOpen whether the lower bound is open (exclusive)
     * @param upperBoundOpen whether the upper bound is open (exclusive)
     *
     * @since 26.7
     */
    public DefaultRange(T lower, T upper, T step, boolean containsNull,
            boolean lowerBoundOpen, boolean upperBoundOpen) {
        lowerBound = lower;
        upperBound = upper;
        this.step = step;
        this.containsNull = containsNull;
        empty = false;
        this.lowerBoundOpen = lowerBoundOpen;
        this.upperBoundOpen = upperBoundOpen;
        checkIfStepFitsIntoBounds();
    }

    /**
     * A subclass must override this method if it supports incremental steps. This method calculates
     * the number of values held by this range according to the step size. When this method is
     * called from {@link #getValues(boolean)} or {@link #size()} it is guaranteed that the lower
     * and upper bound are not {@code null}.
     * <p>
     * This method is only called for ranges with closed (inclusive) bounds. For ranges with open
     * bounds, {@code computeDiscreteSizeForOpenBounds()} is used instead.
     *
     * @return the number of values hold by this range
     *
     * @throws RuntimeException if the number of values in this range is larger than
     *             {@link Integer#MAX_VALUE}
     */
    protected int sizeForDiscreteValuesExcludingNull() {
        if (lowerBound != null && upperBound != null && Objects.equals(lowerBound, upperBound)) {
            return 1;
        }
        throw new RuntimeException("Needs to be implemented if the range supports incremental steps.");
    }

    /**
     * Calculates the number of discrete values for the full closed range using floor division. This
     * is used by {@link #computeDiscreteSizeForOpenBounds()} where the step may not divide evenly
     * into the range because the effective range is reduced by open bounds.
     * <p>
     * Subclasses that override {@link #sizeForDiscreteValuesExcludingNull()} should also override
     * this method.
     *
     * @return the number of values using floor division
     */
    protected int sizeForDiscreteValuesWithFloor() {
        if (lowerBound != null && upperBound != null && Objects.equals(lowerBound, upperBound)) {
            return 1;
        }
        throw new RuntimeException("Needs to be implemented if the range supports incremental steps.");
    }

    /**
     * A subclass must override this method if it supports incremental steps. This method checks if
     * the provided value actually fits in the range taking the step size into account.
     * 
     * @param value the value to check. The provided value is never {@code null} or the null
     *            representation
     * @param bound one of the bounds of this range. If the lower bound is not {@code null} it is
     *            provided otherwise if the upper bound is not {@code null} it is provided. This
     *            method is not called if both bounds are {@code null}.
     * @return true if the provided value fits into the range
     */
    protected boolean checkIfValueCompliesToStepIncrement(T value, T bound) {
        throw new RuntimeException("Needs to be implemented if the range supports incremental steps.");
    }

    /**
     * A subclass must override this method if it supports incremental steps. This method calculates
     * the next value starting from the provided value.
     * 
     * @param currentValue the value to use to calculate the next value
     * @return the next value
     */
    protected T getNextValue(T currentValue) {
        throw new RuntimeException("Needs to be implemented if the range supports incremental steps.");
    }

    /**
     * A subclass must override this method if it supports incremental steps. This method returns
     * {@code null} or the null representation value of the datatype of this range.
     */
    protected T getNullValue() {
        throw new RuntimeException("Needs to be implemented if the range supports incremental steps.");
    }

    /**
     * This method needs to be called in factory methods that create a new instance of a subclass of
     * this range if the range is instantiated with a step size different from {@code null}.
     */
    protected final void checkIfStepFitsIntoBounds() {
        if (isStepNull() || isLowerBoundNull() || isUpperBoundNull()) {
            return;
        }
        // For open bounds the classical "abs(upper-lower)/step must be integer" check does not
        // apply because the effective first/last values depend on which bounds are excluded.
        // Discrete value enumeration via forEachDiscreteValue handles this correctly at runtime.
        if (!isLowerBoundOpen() && !isUpperBoundOpen()) {
            if (!checkIfValueCompliesToStepIncrement(getLowerBound(), getUpperBound())) {
                throw new IllegalArgumentException(
                        "The step doesn't fit into the specified bounds. The step has to comply to the condition: the value of the expression 'abs(upperBound - lowerBound) / step' needs to be an integer.");
            }
        }
    }

    @Override
    public T getLowerBound() {
        return lowerBound;
    }

    @Override
    public T getUpperBound() {
        return upperBound;
    }

    @Override
    public T getStep() {
        return step;
    }

    @Override
    public boolean isLowerBoundOpen() {
        return lowerBoundOpen;
    }

    @Override
    public boolean isUpperBoundOpen() {
        return upperBoundOpen;
    }

    @Override
    public boolean isEmpty() {
        if (empty) {
            return true;
        }
        if (isLowerBoundNull() || isUpperBoundNull()) {
            return false;
        }
        int comparison = lowerBound.compareTo(upperBound);
        if (comparison > 0) {
            return true;
        }
        return comparison == 0 && (isLowerBoundOpen() || isUpperBoundOpen());
    }

    @Override
    public boolean isRange() {
        return true;
    }

    /**
     * {@inheritDoc}
     * 
     * Subclasses that support discrete values need to override
     * {@link #sizeForDiscreteValuesExcludingNull()} which is called by this method for discrete
     * ranges. By default {@link #sizeForDiscreteValuesExcludingNull()} throws a
     * {@link RuntimeException} indicating that it needs to be overridden.
     */
    @Override
    public int size() {
        if (isEmpty()) {
            return 0;
        }
        if (isLowerBoundNull() || isUpperBoundNull()) {
            return Integer.MAX_VALUE;
        }

        if (lowerBoundEqualsUpperBound()) {
            return 1;
        }
        if (isDiscrete()) {
            int size;
            if (isLowerBoundOpen() || isUpperBoundOpen()) {
                size = computeDiscreteSizeForOpenBounds();
            } else {
                size = sizeForDiscreteValuesExcludingNull();
            }
            if (containsNull()) {
                return size + 1;
            }
            return size;
        }
        return Integer.MAX_VALUE;
    }

    /**
     * Two Ranges are equals if lower, upper bound and step are equal.
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object obj) {
        if ((obj == null) || !obj.getClass().equals(getClass())) {
            return false;
        }
        Range<T> otherRange = (Range<T>)obj;
        return (isEmpty() && otherRange.isEmpty())
                || ((isEmpty() == otherRange.isEmpty()) && fieldsEqual(otherRange));
    }

    private boolean fieldsEqual(Range<T> otherRange) {
        boolean boundsEqual = equals(lowerBound, otherRange.getLowerBound())
                && equals(upperBound, otherRange.getUpperBound())
                && equals(step, otherRange.getStep());
        return boundsEqual
                && equals(containsNull, otherRange.containsNull())
                && isLowerBoundOpen() == otherRange.isLowerBoundOpen()
                && isUpperBoundOpen() == otherRange.isUpperBoundOpen();
    }

    /**
     * Compares the two objects for equality considering the case that the parameters can be
     * {@code null}. If both parameters are {@code null} this method returns {@code true}.
     */
    private static boolean equals(Object first, Object second) {
        if (first == second) {
            return true;
        }
        if ((first == null) || (second == null)) {
            return false;
        }
        return first.equals(second);
    }

    @Override
    public int hashCode() {
        if (isEmpty()) {
            return 19;
        } else {
            int result = 17;
            result = (lowerBound == null) ? result : result * 37 + lowerBound.hashCode();
            result = (upperBound == null) ? result : result * 37 + upperBound.hashCode();
            result = (step == null) ? result : result * 37 + step.hashCode();
            result = result * 37 + (containsNull() ? 1 : 0);
            result = result * 37 + (isLowerBoundOpen() ? 1 : 0);
            return result * 37 + (isUpperBoundOpen() ? 1 : 0);
        }
    }

    /**
     * Returns the range's String representation. Format is: [lowerBound-upperBound, step], e.g.
     * [5-10, 1] or (5-10] for open lower bound.
     */
    @Override
    public String toString() {
        if (isEmpty()) {
            return "[]";
        }
        String open = isLowerBoundOpen() ? "(" : "[";
        String close = isUpperBoundOpen() ? ")" : "]";
        String stepStr = (step != null && !(step instanceof NullObject)) ? ", " + step : "";
        return open + lowerBound + "-" + upperBound + stepStr + close;
    }

    private boolean isLowerBoundNull() {
        return isNullValue(lowerBound);
    }

    private boolean isUpperBoundNull() {
        return isNullValue(upperBound);
    }

    private boolean isStepNull() {
        return isNullValue(step);
    }

    @Override
    public boolean contains(T value) {
        if (isNullValue(value)) {
            return containsNull();
        }
        if (empty) {
            return false;
        }

        boolean withinBounds = isAboveLowerBound(value) && isBelowUpperBound(value);

        if (withinBounds) {
            if (!isStepNull()) {
                if (!isLowerBoundNull()) {
                    return checkIfValueCompliesToStepIncrement(value, getLowerBound());
                }
                if (!isUpperBoundNull()) {
                    return checkIfValueCompliesToStepIncrement(value, getUpperBound());
                }
            }
            return true;
        }
        return false;
    }

    private boolean isNullValue(T value) {
        return isNull(value);
    }

    private boolean isAboveLowerBound(T value) {
        if (isLowerBoundNull()) {
            return true;
        }
        int cmp = value.compareTo(lowerBound);
        return isLowerBoundOpen() ? cmp > 0 : cmp >= 0;
    }

    private boolean isBelowUpperBound(T value) {
        if (isUpperBoundNull()) {
            return true;
        }
        int cmp = value.compareTo(upperBound);
        return isUpperBoundOpen() ? cmp < 0 : cmp <= 0;
    }

    /**
     * {@inheritDoc}
     * <p>
     * In case of ranges this method returns <code>true</code> if one of the following conditions is
     * <code>true</code>
     * <ul>
     * <li>the range is empty</li>
     * <li>lower bound is equal to upper bound</li>
     * <li>a step is specified (step is not {@code null})</li>
     * </ul>
     * 
     * Even if the datatype might be discrete in such a way that the range is discrete in theory,
     * this method only returns {@code true} if a step is explicitly defined. For example an
     * {@link IntegerRange} is not discrete if the step is {@code null} even though a step of 1
     * could be assumed. The exception to this rule is when the upper bound and the lower bound are
     * equal. Then the step does not have to be defined explicitly.
     */
    @Override
    public boolean isDiscrete() {
        return isEmpty() || lowerBoundEqualsUpperBound() || !isStepNull();
    }

    // a value of null for both bounds is not considered equal, as it represents negative/positive
    // infinity, respectively
    private boolean lowerBoundEqualsUpperBound() {
        boolean noBoundNull = !isLowerBoundNull() && !isUpperBoundNull();
        return noBoundNull && getLowerBound().compareTo(getUpperBound()) == 0;
    }

    private int computeDiscreteSizeForOpenBounds() {
        int closedSize = sizeForDiscreteValuesWithFloor();
        int adjustment = 0;
        if (isLowerBoundOpen()) {
            adjustment++;
        }
        if (isUpperBoundOpen() && checkIfValueCompliesToStepIncrement(getUpperBound(), getLowerBound())) {
            adjustment++;
        }
        return Math.max(0, closedSize - adjustment);
    }

    @Override
    public boolean containsNull() {
        return containsNull;
    }

    @Override
    public Set<T> getValues(boolean excludeNull) {
        if (!isDiscrete()) {
            throw new IllegalStateException("This method cannot be called for ranges that are not discrete.");
        }

        if (isLowerBoundNull() || isUpperBoundNull()) {
            throw new IllegalStateException("This method cannot be called for unlimited ranges.");
        }

        if (isEmpty()) {
            return Set.of();
        }

        Set<T> values = new LinkedHashSet<>();

        if (containsNull() && !excludeNull) {
            values.add(getNullValue());
        }
        forEachDiscreteValue(values::add);
        return values;
    }

    private void forEachDiscreteValue(Consumer<T> action) {
        if (lowerBoundEqualsUpperBound()) {
            if (!isLowerBoundOpen() && !isUpperBoundOpen()) {
                action.accept(getLowerBound());
            }
            return;
        }
        T firstValue = isLowerBoundOpen() ? getNextValue(getLowerBound()) : getLowerBound();
        for (T v = firstValue; isBelowUpperBound(v); v = getNextValue(v)) {
            action.accept(v);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Additionally a {@code DefaultRange} is considered restricted if either {@link #lowerBound},
     * {@link #upperBound} or {@link #step} is not {@code null}.
     * 
     */
    @Override
    public boolean isUnrestricted(boolean excludeNull) {
        boolean hasAnyBoundOrStep = !isNull(getLowerBound()) || !isNull(getUpperBound()) || !isNull(getStep());
        boolean hasEffectiveOpenBound = (isLowerBoundOpen() && !isLowerBoundNull())
                || (isUpperBoundOpen() && !isUpperBoundNull());
        if (isEmpty() || hasAnyBoundOrStep || hasEffectiveOpenBound) {
            return false;
        }
        if (excludeNull) {
            return true;
        }
        return containsNull();
    }

    @Override
    public boolean isSubsetOf(ValueSet<T> otherValueSet) {
        if (otherValueSet.isUnrestricted(!containsNull) || isEmpty()) {
            return otherValueSet.containsNull() || !containsNull();
        }
        if (!otherValueSet.containsNull() && containsNull()) {
            return false;
        }
        Optional<Class<T>> datatype = getDatatype();
        Optional<Class<T>> otherDatatype = otherValueSet.getDatatype();
        if (!datatype.isPresent() && !otherDatatype.isPresent()) {
            return true;
        }
        return otherDatatype
                .flatMap(d -> datatype.filter(Predicate.isEqual(d)))
                .map(d -> otherValueSet instanceof DefaultRange ? isSubRangeOf((DefaultRange<T>)otherValueSet) : false)
                .orElse(false);
    }

    // CSOFF: CyclomaticComplexity
    private boolean isSubRangeOf(DefaultRange<T> otherRange) {
        if (lowerBoundInRange(otherRange) && upperBoundInRange(otherRange)) {
            if (!isNull(otherRange.step)) {
                if (isNull(step) || !divisibleWithoutRest(step, otherRange.step)) {
                    return false;
                }

                if (!isNull(lowerBound) && !isNull(otherRange.lowerBound)
                        && !otherRange.checkIfValueCompliesToStepIncrement(lowerBound, otherRange.lowerBound)) {
                    return false;
                }
                if (!isNull(upperBound) && !isNull(otherRange.upperBound)
                        && !otherRange.checkIfValueCompliesToStepIncrement(upperBound, otherRange.upperBound)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    // CSON: CyclomaticComplexity

    private boolean upperBoundInRange(DefaultRange<T> otherRange) {
        if (isNull(otherRange.upperBound)) {
            return true;
        }
        if (isNull(upperBound)) {
            return false;
        }
        int cmp = otherRange.upperBound.compareTo(upperBound);
        if (cmp > 0) {
            return true;
        }
        // equal bounds: this open is always within other, but this closed requires other closed
        return cmp == 0 && (isUpperBoundOpen() || !otherRange.isUpperBoundOpen());
    }

    private boolean lowerBoundInRange(DefaultRange<T> otherRange) {
        if (isNull(otherRange.lowerBound)) {
            return true;
        }
        if (isNull(lowerBound)) {
            return false;
        }
        int cmp = otherRange.lowerBound.compareTo(lowerBound);
        if (cmp < 0) {
            return true;
        }
        // equal bounds: this open is always within other, but this closed requires other closed
        return cmp == 0 && (isLowerBoundOpen() || !otherRange.isLowerBoundOpen());
    }

    /**
     * A subclass must override this method if it supports incremental steps. This method checks
     * whether the provided dividend is divisible by the given divisor without rest.
     * 
     * @param dividend the value being divided
     * @param divisor the value by which the dividend is to be divided
     * @return whether the provided dividend can be divided by the the divisor without rest
     * 
     * @since 24.1
     */
    protected boolean divisibleWithoutRest(T dividend, T divisor) {
        return false;
    }

    @Override
    public Optional<Class<T>> getDatatype() {
        return Optional.empty();
    }

}
