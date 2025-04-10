/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.validation;

/**
 * Represents constraints for numeric values.
 */
public class NumericConstraint {
    private final int precision;
    private final int scale;

    /**
     * Creates a new numeric constraint with the specified precision and scale.
     *
     * @param precision the maximum total number of digits allowed
     * @param scale the maximum number of decimal places allowed
     */
    public NumericConstraint(int precision, int scale) {
        this.precision = precision;
        this.scale = scale;
    }

    /**
     * Creates a new numeric constraint with the specified precision.
     *
     * @param precision the maximum total number of digits allowed
     */
    public NumericConstraint(int precision) {
        this(precision, 0);
    }

    public int getPrecision() {
        return precision;
    }

    public int getScale() {
        return scale;
    }
}
