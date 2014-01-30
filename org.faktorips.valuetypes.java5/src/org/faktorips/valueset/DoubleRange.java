/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.valueset;

/**
 * A Range class where upper and lower bounds are Doubles.
 * 
 * @author Jan Ortmann
 * @author Daniel Hohenberger conversion to Java5
 */
public class DoubleRange extends DefaultRange<Double> {

    private static final long serialVersionUID = 3093772484960108819L;
    private static final String ASTERISK_STAR = "*";

    public DoubleRange(Double lower, Double upper, boolean containsNull) {
        super(lower, upper, containsNull);
    }

    public DoubleRange(Double lower, Double upper) {
        this(lower, upper, false);
    }

    /**
     * Creates an DoubleRange based on the indicated Strings. The Strings are parsed with the
     * Double.valueOf() method. An asterisk (*) is interpreted as the maximum/minimum available
     * Double value.
     * 
     * @param containsNull defines if null is part of the range or not
     */
    public static DoubleRange valueOf(String lower, String upper, boolean containsNull) {
        Double min = null;
        if (lower != null) {
            if (!ASTERISK_STAR.equals(lower)) {
                min = Double.valueOf(lower);
            }
        }

        Double max = null;
        if (upper != null) {
            if (!ASTERISK_STAR.equals(upper)) {
                max = Double.valueOf(upper);
            }
        }
        return new DoubleRange(min, max, containsNull);
    }

    /**
     * Creates an DoubleRange based on the indicated Double values.
     * 
     * @param containsNull defines if null is part of the range or not
     */
    public static DoubleRange valueOf(Double lower, Double upper, boolean containsNull) {
        return new DoubleRange(lower, upper, containsNull);
    }

}
