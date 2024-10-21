/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime;

import org.faktorips.valueset.IntegerRange;

/**
 * A Range class for cardinalities. A {@link CardinalityRange} is defined by a lower and an upper
 * bound as well as a default value.
 *
 * @author Stefan Widmaier
 */
public class CardinalityRange extends IntegerRange {

    /**
     * A cardinality that describes the optional range from 0 to 1 with default 0.
     */
    public static final CardinalityRange OPTIONAL = new CardinalityRange(0, 1, 0);

    /**
     * A cardinality that describes the obligatory range 1 to 1 with default 1.
     */
    public static final CardinalityRange MANDATORY = new CardinalityRange(1, 1, 1);

    /**
     * A cardinality that describes the full range from 0 to * with default 0.
     */
    public static final CardinalityRange FULL_RANGE = new CardinalityRange(0, Integer.MAX_VALUE, 0);

    /**
     * A cardinality that describes the empty range. It is used to mark associations as excluded in
     * product variants.
     */
    public static final CardinalityRange EXCLUDED = new CardinalityRange() {
        private static final long serialVersionUID = 1L;
        // Product variants use an upper bound of 0 to remove links defined in the varied
        // product component.

        @Override
        public Integer getLowerBound() {
            // For backwards compatibility with code iterating from lower to upper bound, the empty
            // cardinality range should not report null but 0 as its lower bound.
            return 0;
        }

        @Override
        public Integer getUpperBound() {
            // For backwards compatibility with code checking for an upper bound of 0, the empty
            // cardinality range should not report null but 0 as its upper bound.
            return 0;
        }
    };

    /**
     * <code>serialVersionUID</code> for {@link CardinalityRange}s
     */
    private static final long serialVersionUID = -6655684714703290189L;

    private final Integer defaultCardinality;

    /**
     * Constructs a {@link CardinalityRange} with the given lower and upper bounds as well as the
     * given default value. The default value must be in between the lower and upper bound or equal
     * to one of them (though not Integer.MAX_VALUE).
     *
     * @param lower the lowest possible cardinality
     * @param upper the highest possible cardinality
     * @param def the default cardinality, must be in between or equal to one of the bounds
     * @throws IllegalArgumentException if the default value is not in the range.
     */
    @SuppressWarnings("deprecation")
    public CardinalityRange(Integer lower, Integer upper, Integer def) {
        // constructs an IntegerRage with step=1 that does not contain null
        super(lower, upper);
        defaultCardinality = def;
        if (lower > def || def > upper || def == Integer.MAX_VALUE) {
            throw new IllegalArgumentException(
                    "The default cardinality is out of range. The default value must be in between the lower and upper bound or equal to one of them (though not Integer.MAX_VALUE).");
        }
    }

    /**
     * Creates a new empty {@link CardinalityRange}. These are only used to mark associations as
     * excluded in product variants.
     */
    @SuppressWarnings("deprecation")
    private CardinalityRange() {
        super();
        defaultCardinality = 0;
    }

    /**
     * {@inheritDoc}
     *
     * A {@link CardinalityRange} is considered empty when its {@linkplain #getUpperBound() upper
     * bound} is 0.
     */
    @Override
    public boolean isEmpty() {
        return super.isEmpty() || Integer.valueOf(0).equals(getUpperBound());
    }

    public Integer getDefaultCardinality() {
        return defaultCardinality;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof CardinalityRange range) {
            boolean upperLowerAndStepEqual = super.equals(obj);
            return upperLowerAndStepEqual
                    && getDefaultCardinality().equals(range.getDefaultCardinality());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int upperLowerAndStepHashCode = super.hashCode();
        return upperLowerAndStepHashCode * 37 + getDefaultCardinality();
    }

    @Override
    public String toString() {
        return getLowerBound() + ".." + getUpperBound() + ", default:" + getDefaultCardinality();
    }
}
