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

import java.lang.invoke.MethodType;
import java.math.BigDecimal;

import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IVisitorSupport;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.model.IpsModel;
import org.faktorips.runtime.model.type.PolicyAttribute;
import org.faktorips.runtime.model.type.PolicyCmptType;
import org.faktorips.values.Decimal;

/**
 * Utility class for length validation.
 * <p>
 * This class performs a generic validation of {@link String} lengths, and precision (and scale) for
 * numeric values based on the limits specified. It does <strong>not</strong> validate against any
 * constraints that may be defined via JPA annotations.
 *
 * @see IDatabaseLengthValidationConfiguration IDatabaseLengthValidationConfiguration for
 *          configuration of the validation
 * @see DefaultDatabaseLengthValidationConfiguration DefaultDatabaseLengthValidationConfiguration
 *          for a default configuration implementation
 *
 * @since 25.7
 */
public final class DatabaseLengthValidation {

    private final IDatabaseLengthValidationConfiguration config;

    private DatabaseLengthValidation(IDatabaseLengthValidationConfiguration config) {
        this.config = config;
    }

    public static DatabaseLengthValidation with(IDatabaseLengthValidationConfiguration config) {
        return new DatabaseLengthValidation(config);
    }

    /**
     * Validates that all string attributes in the model object and all its child objects do not
     * exceed the specified maximum byte length, and all numeric attributes in the model object do
     * not exceed the given precision and scale.
     *
     * @param modelObject the model object to validate
     * @return a list of validation messages, empty if no attribute exceeds its constraints
     * @see #validateOnly(IModelObject) validateOnly(IModelObject) for validation of a single model
     *          object
     */
    public MessageList validateWithChildren(IModelObject modelObject) {
        MessageList messages = new MessageList();
        IVisitorSupport.orGenericVisitorSupport(modelObject).accept(o -> {
            messages.add(validateOnly(o));
            return true;
        });
        return messages;
    }

    /**
     * Validates that all string attributes in the model object do not exceed the specified maximum
     * byte length, and all numeric attributes in the model object do not exceed the given precision
     * and scale.
     *
     * @param modelObject the model object to validate
     * @return a list of validation messages, empty if no attribute exceeds its constraints
     * @see #validateWithChildren(IModelObject) validateWithChildren(IModelObject) for recursive
     *          validation of a model object tree
     */
    public MessageList validateOnly(IModelObject modelObject) {
        MessageList messages = new MessageList();
        PolicyCmptType policyType = IpsModel.getPolicyCmptType(modelObject.getClass());
        if (policyType != null) {
            for (PolicyAttribute attribute : policyType.getAttributes()) {
                if (config.shouldValidate(attribute, modelObject)) {
                    validateAttribute(attribute, modelObject, messages);
                }
            }
        }
        return messages;
    }

    @SuppressWarnings("unchecked")
    private void validateAttribute(PolicyAttribute attribute, IModelObject modelObject, MessageList messages) {
        Class<?> datatype = attribute.getDatatype();
        if (datatype.isPrimitive()) {
            datatype = MethodType.methodType(datatype).wrap().returnType();
        }
        if (String.class.isAssignableFrom(datatype)) {
            validateString((String)attribute.getValue(modelObject), attribute, modelObject, messages);
        }
        if (Number.class.isAssignableFrom(datatype)) {
            validateNumeric((Class<? extends Number>)datatype, (Number)attribute.getValue(modelObject), attribute,
                    modelObject, messages);
        }
    }

    private void validateString(String value,
            PolicyAttribute attribute,
            IModelObject modelObject,
            MessageList messages) {
        StringLengthConstraint stringLengthConstraint = config.getStringLengthConstraint();
        if (stringLengthConstraint != null) {
            int byteLength = value.getBytes(stringLengthConstraint.stringEncoding()).length;
            int maxLength = stringLengthConstraint.maxStringByteLength();
            if (byteLength > maxLength) {
                messages.add(
                        config.createMessageForStringLengthViolation(attribute, modelObject, byteLength, maxLength));
            }
        }
    }

    private void validateNumeric(Class<? extends Number> datatype,
            Number value,
            PolicyAttribute attribute,
            IModelObject modelObject,
            MessageList messages) {
        var numericConstraint = config.getNumericConstraint(datatype);
        if (numericConstraint != null) {
            var bigDecimalValue = toBigDecimal(value);
            if (bigDecimalValue == null) {
                return;
            }

            int actualPrecision = bigDecimalValue.precision();
            int actualScale = bigDecimalValue.scale();

            if (actualPrecision > numericConstraint.precision()) {
                messages.add(
                        config.createMessageForPrecisionViolation(attribute, modelObject, datatype, actualPrecision,
                                numericConstraint.precision()));
            }
            if (actualScale > numericConstraint.scale()) {
                messages.add(config.createMessageForScaleViolation(attribute, modelObject, datatype, actualScale,
                        numericConstraint.scale()));
            }
        }
    }

    private BigDecimal toBigDecimal(Number value) {
        return switch (value) {
            case null -> null;
            case BigDecimal bd -> bd;
            case Decimal decimal -> new BigDecimal(decimal.toString());
            case Short s -> BigDecimal.valueOf(s);
            case Integer i -> BigDecimal.valueOf(i);
            case Long l -> BigDecimal.valueOf(l);
            case Float f -> new BigDecimal(f.toString());
            case Double d -> BigDecimal.valueOf(d);
            default -> null;
        };
    }
}
