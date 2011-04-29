/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
     * <code>serialVersionUID</code> for {@link CardinalityRange}s
     */
    private static final long serialVersionUID = -6655684714703290189L;

    private Integer defaultCardinality;

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
    public CardinalityRange(Integer lower, Integer upper, Integer def) {
        // constructs an IntegerRage with step=1 that does not contain null
        super(lower, upper);
        defaultCardinality = def;
        if (lower > def || def > upper || def == Integer.MAX_VALUE) {
            throw new IllegalArgumentException(
                    "The default cardinality is out of range. The default value must be in between the lower and upper bound or equal to one of them (though not Integer.MAX_VALUE).");
        }
    }

    public Integer getDefaultCardinality() {
        return defaultCardinality;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof CardinalityRange) {
            boolean upperLowerAndStepEqual = super.equals(obj);
            return upperLowerAndStepEqual
                    && getDefaultCardinality().equals(((CardinalityRange)obj).getDefaultCardinality());
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
