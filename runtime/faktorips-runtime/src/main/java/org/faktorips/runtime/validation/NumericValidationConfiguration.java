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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Configuration for numeric precision and scale validation.
 */
public class NumericValidationConfiguration extends ValidationConfiguration {
    private final Map<Class<?>, NumericConstraint> numericConstraints = new HashMap<>();

    public NumericValidationConfiguration(Locale locale) {
        super(locale);
    }

    /**
     * Adds a precision and a scale constraint for a numeric type.
     * <p>
     * If a constraint already exists for the specified type, it will be replaced.
     *
     * @param numericType the class of the numeric type (e.g., BigDecimal.class)
     * @param precision the maximum total number of digits allowed
     * @param scale the maximum number of decimal places allowed
     * @return this configuration instance for method chaining
     */
    public NumericValidationConfiguration addConstraint(Class<?> numericType, int precision, int scale) {
        numericConstraints.put(numericType, new NumericConstraint(precision, scale));
        return this;
    }

    /**
     * Adds a precision constraint for a numeric type.
     * <p>
     * If a constraint already exists for the specified type, it will be replaced.
     *
     * @param numericType the class of the numeric type (e.g., BigDecimal.class)
     * @param precision the maximum total number of digits allowed
     * @return this configuration instance for method chaining
     */
    public NumericValidationConfiguration addConstraint(Class<?> numericType, int precision) {
        numericConstraints.put(numericType, new NumericConstraint(precision));
        return this;
    }

    public NumericConstraint getConstraint(Class<?> numericType) {
        return numericConstraints.get(numericType);
    }

    /**
     * Checks if a constraint is defined for the specified numeric type.
     *
     * @param numericType the class of the numeric type to check
     * @return true if a constraint exists for the type, false otherwise
     */
    public boolean hasConstraintFor(Class<?> numericType) {
        return numericConstraints.containsKey(numericType);
    }
}
