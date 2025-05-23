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
public record NumericConstraint(int precision, int scale) {

    /**
     * Creates a new numeric constraint with the specified precision.
     *
     * @param precision the maximum total number of digits allowed
     */
    public NumericConstraint(int precision) {
        this(precision, 0);
    }
}
