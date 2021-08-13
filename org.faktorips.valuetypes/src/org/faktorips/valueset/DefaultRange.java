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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.faktorips.values.NullObject;
import org.faktorips.values.ObjectUtil;

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

    private static final long serialVersionUID = -2886828952622682290L;

    private final T lowerBound;
    private final T upperBound;
    private final T step;
    private final boolean containsNull;
    private final boolean empty;

    /**
     * Creates a new empty {@link DefaultRange} instance that doesn't contain any values.
     */
    public DefaultRange() {
        lowerBound = null;
        upperBound = null;
        step = null;
        containsNull = false;
        empty = true;
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
     * Creates a new {@link DefaultRange} instance that doesn't contain null. The third parameter
     * defines whether the range contains null or not. Null can mean the native java {@code null} or
     * a null representation value specific to the datatype the range implementation is for.
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
     * Creates a new {@link DefaultRange} instance that doesn't contain null.
     * 
     * @param lower bound of the range
     * @param upper bound of the range
     * @param step the unit that defines the discrete values that are allowed to be within this
     *            range. The value can be {@code null} indicating that it is a continuous range. It
     *            has to fulfill the condition: the value of the expression <em>abs(upperBound -
     *            lowerBound) / step</em> needs to be an integer
     * @throws IllegalArgumentException if the condition <em>abs(upperBound - lowerBound) /
     *             step</em> is not met. The condition is not applied if one is or both of the
     *             bounds are {@code null}
     */
    public DefaultRange(T lower, T upper, T step, boolean containsNull) {
        lowerBound = lower;
        upperBound = upper;
        this.step = step;
        this.containsNull = containsNull;
        empty = false;
        checkIfStepFitsIntoBounds();
    }

    /**
     * A subclass must override this method if it supports incremental steps. This method calculates
     * the number of values held by this range according to the step size. When this method is
     * called from {@link #getValues(boolean)} or {@link #size()} it is guaranteed that the lower
     * and upper bound are not {@code null}.
     * 
     * @return the number of values hold by this range
     * 
     * @throws RuntimeException if the number of values in this range is larger than
     *             {@link Integer#MAX_VALUE}
     */
    protected int sizeForDiscreteValuesExcludingNull() {
        if (lowerBound != null && upperBound != null && lowerBound == upperBound) {
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
        if (isStepNull()) {
            return;
        }
        if (isLowerBoundNull() || isUpperBoundNull()) {
            return;
        }
        if (!checkIfValueCompliesToStepIncrement(getLowerBound(), getUpperBound())) {
            throw new IllegalArgumentException(
                    "The step doesn't fit into the specified bounds. The step has to comply to "
                            + "the condition: the value of the expression 'abs(upperBound - lowerBound) / "
                            + "step' needs to be an integer.");
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
    public boolean isEmpty() {
        if (empty) {
            return true;
        }
        if (isLowerBoundNull() || isUpperBoundNull()) {
            return false;
        }
        return (lowerBound.compareTo(upperBound) > 0);
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
     * 
     * @throws RuntimeException if the number of values in this range is larger than
     *             {@link Integer#MAX_VALUE}
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
            int size = sizeForDiscreteValuesExcludingNull();
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
        if (obj == null) {
            return false;
        }
        if (!obj.getClass().equals(getClass())) {
            return false;
        }
        Range<T> otherRange = (Range<T>)obj;
        return (isEmpty() && otherRange.isEmpty())
                || (equals(lowerBound, otherRange.getLowerBound())
                        && equals(upperBound, otherRange.getUpperBound())
                        && equals(step, otherRange.getStep()));
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
            result = result * 37 + lowerBound.hashCode();
            result = result * 37 + upperBound.hashCode();
            result = (step == null) ? result : result * 37 + step.hashCode();
            return result;
        }
    }

    /**
     * Returns the range's String representation. Format is: lowerBound-upperBound, step, e.g. 5-10,
     * 1
     */
    @Override
    public String toString() {
        return isEmpty() ? "[]"
                : (lowerBound + "-" + upperBound + (step != null && !(step instanceof NullObject) ? ", " + step : ""));
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

        boolean withinBounds = (isLowerBoundNull() || value.compareTo(lowerBound) >= 0)
                && (isUpperBoundNull() || value.compareTo(upperBound) <= 0);

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
        return ObjectUtil.isNull(value);
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

    @Override
    public boolean containsNull() {
        return containsNull;
    }

    @Override
    public Set<T> getValues(boolean excludeNull) {
        if (!isDiscrete()) {
            throw new IllegalStateException("This method cannot be called for ranges that are not discrete.");
        }

        if (size() == Integer.MAX_VALUE) {
            throw new IllegalStateException("This method cannot be called for unlimited ranges.");
        }

        if (isEmpty()) {
            return Collections.emptySet();
        }

        int numberOfEntries = sizeForDiscreteValuesExcludingNull();

        Set<T> values = null;

        if (containsNull() && !excludeNull) {
            values = new HashSet<>(numberOfEntries + 1);
            values.add(getNullValue());
        } else {
            values = new HashSet<>(numberOfEntries);
        }

        T nextValue = getLowerBound();
        values.add(nextValue);
        for (int i = 1; i < numberOfEntries; i++) {
            nextValue = getNextValue(nextValue);
            values.add(nextValue);
        }
        return values;
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
        if (isEmpty() || getLowerBound() != null || getUpperBound() != null || getStep() != null) {
            return false;
        }
        if (excludeNull) {
            return true;
        }
        return containsNull();
    }

}
