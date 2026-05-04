/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal;

/**
 * Represents a range. This class is used during the process of reading data of a range from an xml
 * representation into memory and create <code>org.faktorips.valueset.Range</code> implementation
 * instances.
 *
 * @author Peter Erzberger
 */
public class Range {

    private final String lower;
    private final String upper;
    private final String step;
    private final boolean containsNull;
    private final boolean empty;
    private final boolean lowerBoundOpen;
    private final boolean upperBoundOpen;

    /**
     * Creates a new Range.
     */
    public Range(String lower, String upper, String step, boolean containsNull) {
        this(lower, upper, step, containsNull, false, false);
    }

    /**
     * Creates a new Range with open/closed bound configuration.
     *
     * @since 26.7
     */
    public Range(String lower, String upper, String step, boolean containsNull,
            boolean lowerBoundOpen, boolean upperBoundOpen) {
        this.lower = lower;
        this.upper = upper;
        this.step = step;
        this.containsNull = containsNull;
        this.lowerBoundOpen = lowerBoundOpen;
        this.upperBoundOpen = upperBoundOpen;
        empty = false;
    }

    public Range() {
        lower = null;
        upper = null;
        step = null;
        containsNull = false;
        lowerBoundOpen = false;
        upperBoundOpen = false;
        empty = true;
    }

    /**
     * @return whether this range contains {@code null}
     */
    public boolean containsNull() {
        return containsNull;
    }

    /**
     * @return whether this {@link Range} is empty;
     */
    public boolean isEmpty() {
        return empty;
    }

    /**
     * @return Returns the lower bound of this range
     */
    public String getLower() {
        return lower;
    }

    /**
     * @return Returns the upper bound of this range
     */
    public String getUpper() {
        return upper;
    }

    /**
     * @return Returns the step of this range
     */
    public String getStep() {
        return step;
    }

    /**
     * @return whether the lower bound is open (exclusive)
     *
     * @since 26.7
     */
    public boolean isLowerBoundOpen() {
        return lowerBoundOpen;
    }

    /**
     * @return whether the upper bound is open (exclusive)
     *
     * @since 26.7
     */
    public boolean isUpperBoundOpen() {
        return upperBoundOpen;
    }
}
