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

import org.faktorips.runtime.IMarker;
import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.model.type.PolicyAttribute;
import org.faktorips.values.Decimal;

/**
 * Validates precision and scale constraints for numeric attributes in model objects.
 */
public class NumericValidation extends AbstractValidation {
    public static final String MSGCODE_NUMBER_EXCEEDS_PRECISION_OR_SCALE = "NUMBER_EXCEEDS_PRECISION_OR_SCALE";
    public static final String MSGKEY_NUMBER_EXCEEDS_PRECISION_OR_SCALE = "InvalidAttribute.NumberExceedsPrecisionOrScale";

    public static final String MSGCODE_INTEGERTYPE_EXCEEDS_PRECISION = "INTEGERTYPE_EXCEEDS_PRECISION";
    public static final String MSGKEY_INTEGERTYPE_EXCEEDS_PRECISION = "InvalidAttribute.IntegerTypeExceedsPrecision";

    private final NumericValidationConfiguration configuration;

    /**
     * Creates a new numeric validation with the specified configuration.
     *
     * @param configuration the configuration specifying numeric constraint
     * @param messageList the list to add validation messages to
     */
    public NumericValidation(NumericValidationConfiguration configuration, MessageList messageList) {
        super(configuration, messageList);
        this.configuration = configuration;
    }

    @Override
    protected void validateAttribute(PolicyAttribute attribute, IModelObject modelObject) {
        Object value = attribute.getValue(modelObject);
        if (value == null) {
            return;
        }

        Class<?> datatype = attribute.getDatatype();
        if (configuration.hasConstraintFor(datatype)) {
            validateNumeric(value, datatype, attribute, modelObject);
        }
    }

    private void validateNumeric(Object value,
            Class<?> datatype,
            PolicyAttribute attribute,
            IModelObject modelObject) {

        NumericConstraint constraint = configuration.getConstraint(datatype);

        var bigDecimalValue = toBigDecimal(value);
        if (bigDecimalValue == null) {
            return;
        }

        int actualPrecision = bigDecimalValue.precision();
        int actualScale = bigDecimalValue.scale();

        if (isIntegerType(value)) {
            validateIntegerTypePrecision(actualPrecision, constraint, attribute, modelObject);
        } else {
            validateDecimalTypePrecisionAndScale(actualPrecision, actualScale, constraint, attribute, modelObject);
        }
    }

    private boolean isIntegerType(Object value) {
        return value instanceof Byte || value instanceof Short
                || value instanceof Integer || value instanceof Long;
    }

    private void validateDecimalTypePrecisionAndScale(int actualPrecision,
            int actualScale,
            NumericConstraint constraint,
            PolicyAttribute attribute,
            IModelObject modelObject) {

        if (actualPrecision > constraint.getPrecision() || actualScale > constraint.getScale()) {
            addDecimalTypeErrorMessage(attribute, modelObject, constraint);
        }
    }

    private void validateIntegerTypePrecision(int actualPrecision,
            NumericConstraint constraint,
            PolicyAttribute attribute,
            IModelObject modelObject) {

        if (actualPrecision > constraint.getPrecision()) {
            addIntegerTypeErrorMessage(attribute, modelObject, constraint);
        }
    }

    private BigDecimal toBigDecimal(Object value) {
        return switch (value) {
            case null -> null;
            case BigDecimal bd -> bd;
            case Decimal decimal -> new BigDecimal(decimal.toString());
            case Byte b -> BigDecimal.valueOf(b);
            case Short s -> BigDecimal.valueOf(s);
            case Integer i -> BigDecimal.valueOf(i);
            case Long l -> BigDecimal.valueOf(l);
            case Float f -> new BigDecimal(f.toString());
            case Double d -> BigDecimal.valueOf(d);
            default -> null;
        };
    }

    private void addIntegerTypeErrorMessage(PolicyAttribute attribute,
            IModelObject modelObject,
            NumericConstraint constraint) {

        String msg = String.format(
                getResourceBundle().getString(MSGKEY_INTEGERTYPE_EXCEEDS_PRECISION),
                attribute.getLabel(configuration.getLocale()),
                constraint.getPrecision(),
                constraint.getScale());

        Message message = Message.error(msg)
                .code(MSGCODE_INTEGERTYPE_EXCEEDS_PRECISION)
                .invalidObjectWithProperties(modelObject, attribute.getName())
                .create();

        addMessage(message);
    }

    private void addDecimalTypeErrorMessage(PolicyAttribute attribute,
            IModelObject modelObject,
            NumericConstraint constraint) {

        String msg = String.format(
                getResourceBundle().getString(MSGKEY_NUMBER_EXCEEDS_PRECISION_OR_SCALE),
                attribute.getLabel(configuration.getLocale()),
                constraint.getPrecision(),
                constraint.getScale());

        Message message = Message.error(msg)
                .code(MSGCODE_NUMBER_EXCEEDS_PRECISION_OR_SCALE)
                .invalidObjectWithProperties(modelObject, attribute.getName())
                .create();

        addMessageWithMarker(message, configuration);
    }

    private void addMessageWithMarker(Message message, NumericValidationConfiguration config) {
        IMarker marker = config.getTechnicalConstraintViolatedMarker();

        Message messageToAdd = (marker != null)
                ? new Message.Builder(message).markers(marker).create()
                : message;

        addMessage(messageToAdd);
    }
}