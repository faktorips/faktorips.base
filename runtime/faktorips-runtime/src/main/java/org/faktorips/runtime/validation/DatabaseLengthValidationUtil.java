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

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.runtime.MessageList;
import org.faktorips.values.Decimal;

/**
 * Utility class for length validation.
 * <p>
 * This class performs a generic validation of string lengths, precision and scale based on the
 * limits specified. It does <strong>not</strong> validate against any constraints that may be
 * defined via JPA annotations
 */
public final class DatabaseLengthValidationUtil {

    private DatabaseLengthValidationUtil() {
    }

    /**
     * Validates that all string attributes in the model object do not exceed the specified maximum
     * byte length, and all numeric attributes in the model object do not exceed the given precision
     * and scale.
     *
     * @param messageList the message list to add validation messages to
     * @param context the validation context
     * @param modelObject the model object to validate
     * @param maxStringByteLength the maximum allowed byte length for strings
     * @param precision the maximum total number of digits allowed
     * @param scale the maximum number of decimal places allowed
     */
    public static void validate(MessageList messageList,
            IValidationContext context,
            IModelObject modelObject,
            int maxStringByteLength,
            int precision,
            int scale) {

        validateStringLength(messageList, context, modelObject, maxStringByteLength);
        validateAllNumeric(messageList, context, modelObject, precision, scale);
    }

    /**
     * Validates that all string attributes in the model object do not exceed the specified maximum
     * byte length.
     *
     * @param messageList the message list to add validation messages to
     * @param context the validation context
     * @param modelObject the model object to validate
     * @param maxStringByteLength the maximum allowed byte length for strings
     */
    public static void validateStringLength(MessageList messageList,
            IValidationContext context,
            IModelObject modelObject,
            int maxStringByteLength) {

        var config = new StringLengthValidationConfiguration(
                context.getLocale(), StandardCharsets.UTF_8, maxStringByteLength);

        var validation = new StringLengthValidation(config, messageList);
        validation.visit(modelObject);
    }

    /**
     * Validates that all numeric attributes in the model object do not exceed the specified
     * precision and scale.
     *
     * @param messageList the message list to add validation messages to
     * @param context the validation context
     * @param modelObject the model object to validate
     * @param precision the maximum total number of digits allowed
     * @param scale the maximum number of decimal places allowed
     */
    public static void validateAllNumeric(MessageList messageList,
            IValidationContext context,
            IModelObject modelObject,
            int precision,
            int scale) {

        var config = new NumericValidationConfiguration(context.getLocale())
                .addConstraint(Decimal.class, precision, scale);
        config.addConstraint(BigDecimal.class, precision, scale);
        config.addConstraint(Float.class, precision, scale);
        config.addConstraint(Double.class, precision, scale);

        config.addConstraint(Byte.class, precision);
        config.addConstraint(Short.class, precision);
        config.addConstraint(Integer.class, precision);
        config.addConstraint(Long.class, precision);

        var validation = new NumericValidation(config, messageList);
        validation.visit(modelObject);
    }

    /**
     * Validates that all attributes in the model object with the given class do not exceed the
     * specified precision and scale.
     *
     * @param messageList the message list to add validation messages to
     * @param context the validation context
     * @param modelObject the model object to validate
     * @param precision the maximum total number of digits allowed
     * @param scale the maximum number of decimal places allowed
     */
    public static void validateAttributes(MessageList messageList,
            IValidationContext context,
            IModelObject modelObject,
            Class<?> clazz,
            int precision,
            int scale) {

        var config = new NumericValidationConfiguration(context.getLocale())
                .addConstraint(clazz, precision, scale);

        var validation = new NumericValidation(config, messageList);
        validation.visit(modelObject);
    }
}
