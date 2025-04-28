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

import java.nio.charset.Charset;
import java.util.Locale;

import org.faktorips.runtime.IMarker;
import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.model.type.PolicyAttribute;

import edu.umd.cs.findbugs.annotations.CheckForNull;

/**
 * Configuration for the {@link DatabaseLengthValidation}.
 */
public interface IDatabaseLengthValidationConfiguration {

    /** {@return the locale to be used for validation messages} */
    Locale getLocale();

    /** {@return the marker to be used for validation messages} */
    @CheckForNull
    IMarker getTechnicalConstraintViolationMarker();

    /** {@return the constraint to be used to validate String attributes} */
    @CheckForNull
    StringLengthConstraint getStringLengthConstraint();

    /**
     * {@return the constraint to be used to validate numeric attributes of the given type}
     *
     * @param numericDatatype a (wrapper) class used for numeric values - primitive types should not
     *            be used and are validated with the same settings as their wrapper types.
     */
    @CheckForNull
    NumericConstraint getNumericConstraint(Class<? extends Number> numericDatatype);

    /**
     * Decides whether the given {@link PolicyAttribute} should be validated for the given
     * {@link IModelObject}.
     *
     * @implSpec Implementers should use this to prevent validation of fields that allow values
     *               exceeding the configured limits, for example because they are only transitive
     *               and not persisted.
     *
     * @param policyAttribute the model type reference for the validated attribute
     * @param modelObject the model object instance on which the attribute may be validated
     * @return whether validation for the given combination of {@link PolicyAttribute} and
     *             {@link IModelObject} should run
     */
    default boolean shouldValidate(PolicyAttribute policyAttribute, IModelObject modelObject) {
        return true;
    }

    /**
     * Creates a message to indicate that the given attribute's value on the given model object
     * exceeds the configured {@link #getStringLengthConstraint() StringLengthConstraint}.
     *
     * @param attribute the model type reference for the validated attribute
     * @param modelObject the model object instance on which the attribute was validated
     * @param actualLength the actual value's length in bytes according to the {@link Charset} used
     *            in the constraint
     * @param lengthLimit the exceeded limit, set in the constraint
     */
    Message createMessageForStringLengthViolation(PolicyAttribute attribute,
            IModelObject modelObject,
            int actualLength,
            int lengthLimit);

    /**
     * Creates a message to indicate that the given attribute's value on the given model object
     * exceeds the configured {@link #getNumericConstraint(Class) NumericConstraint}`s
     * {@link NumericConstraint#precision() precision}.
     *
     * @param attribute the model type reference for the validated attribute
     * @param modelObject the model object instance on which the attribute was validated
     * @param actualPrecision the actual value's length in digits
     * @param precisionLimit the exceeded limit, set in the constraint
     */
    Message createMessageForPrecisionViolation(PolicyAttribute attribute,
            IModelObject modelObject,
            Class<? extends Number> numericDatatype,
            int actualPrecision,
            int precisionLimit);

    /**
     * Creates a message to indicate that the given attribute's value on the given model object
     * exceeds the configured {@link #getNumericConstraint(Class) NumericConstraint}`s
     * {@link NumericConstraint#scale() scale}.
     *
     * @param attribute the model type reference for the validated attribute
     * @param modelObject the model object instance on which the attribute was validated
     * @param actualScale the actual value's amount of decimal places
     * @param scaleLimit the exceeded limit, set in the constraint
     */
    Message createMessageForScaleViolation(PolicyAttribute attribute,
            IModelObject modelObject,
            Class<? extends Number> numericDatatype,
            int actualScale,
            int scaleLimit);
}
