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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.BiPredicate;

import org.faktorips.runtime.IMarker;
import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.Message.Builder;
import org.faktorips.runtime.ObjectProperty;
import org.faktorips.runtime.model.type.PolicyAttribute;
import org.faktorips.values.Decimal;

/**
 * Default {@link IDatabaseLengthValidationConfiguration configuration} for the
 * {@link DatabaseLengthValidation}.
 * <p>
 * It offers a number of {@code with~} methods for a fluent configuration.
 */
public class DefaultDatabaseLengthValidationConfiguration implements IDatabaseLengthValidationConfiguration {
    public static final String MSGCODE_STRING_TOO_LONG = "STRING_TOO_LONG";
    public static final String MSGKEY_STRING_TOO_LONG = "InvalidAttribute.StringTooLong";
    public static final String MSGCODE_NUMBER_EXCEEDS_PRECISION = "NUMBER_EXCEEDS_PRECISION";
    public static final String MSGKEY_NUMBER_EXCEEDS_PRECISION = "InvalidAttribute.NumberExceedsPrecision";
    public static final String MSGCODE_NUMBER_EXCEEDS_SCALE = "NUMBER_EXCEEDS_SCALE";
    public static final String MSGKEY_NUMBER_EXCEEDS_SCALE = "InvalidAttribute.NumberExceedsScale";
    public static final String RESOURCE_BUNDLE_NAME = DefaultDatabaseLengthValidationConfiguration.class.getName();

    private final Locale locale;
    private final ResourceBundle resourceBundle;

    private final Map<Class<? extends Number>, NumericConstraint> numericConstraints = new HashMap<>();
    private IMarker technicalConstraintViolationMarker;
    private StringLengthConstraint stringLengthConstraint;
    private BiPredicate<PolicyAttribute, IModelObject> shouldValidate;

    /**
     * Creates a new configuration using the given {@link Locale} and {@link ResourceBundle} to
     * create validation messages.
     *
     * @param locale the locale to be used
     * @param resourceBundle a resource bundle; it should contain messages for the validation
     *            messages in the given locale
     */
    public DefaultDatabaseLengthValidationConfiguration(Locale locale, ResourceBundle resourceBundle) {
        this.locale = locale;
        this.resourceBundle = resourceBundle;
    }

    /**
     * Creates a new configuration using the given {@link Locale} using the default
     * {@link ResourceBundle} for {@link #RESOURCE_BUNDLE_NAME}.
     *
     * @param locale the locale to be used
     */
    public DefaultDatabaseLengthValidationConfiguration(Locale locale) {
        this(locale, ResourceBundle.getBundle(RESOURCE_BUNDLE_NAME, locale));
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    /** {@return the used resource bundle} */
    protected ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    /**
     * {@return this configuration, updated to use the given marker on validation messages}
     *
     * @param technicalConstraintViolationMarker a marker to be used on all validation messages
     *            created by this configuration
     */
    public DefaultDatabaseLengthValidationConfiguration withTechnicalConstraintViolationMarker(
            IMarker technicalConstraintViolationMarker) {
        this.technicalConstraintViolationMarker = technicalConstraintViolationMarker;
        return this;
    }

    @Override
    public IMarker getTechnicalConstraintViolationMarker() {
        return technicalConstraintViolationMarker;
    }

    /**
     * {@return this configuration, updated to use the given constraint to check String values}
     *
     * @param stringLengthConstraint a {@link StringLengthConstraint} to be used to check String
     *            values
     */
    public DefaultDatabaseLengthValidationConfiguration withStringLengthConstraint(
            StringLengthConstraint stringLengthConstraint) {
        this.stringLengthConstraint = stringLengthConstraint;
        return this;
    }

    /**
     * {@return this configuration, updated to use a new constraint created from the given encoding
     * and length to check String values}
     *
     * @param stringEncoding a {@link Charset} to be used to convert Strings into their Byte
     *            representation
     * @param maxStringByteLength the maximum number of Bytes a String representation may have
     *
     * @see StringLengthConstraint
     */
    public DefaultDatabaseLengthValidationConfiguration withStringLengthConstraint(Charset stringEncoding,
            int maxStringByteLength) {
        return withStringLengthConstraint(new StringLengthConstraint(stringEncoding, maxStringByteLength));
    }

    /**
     * {@return this configuration, updated to use a new constraint created from the given length to
     * check String values in UTF-8}
     *
     * @param maxStringByteLength the maximum number of Bytes a String representation in
     *            {@link StandardCharsets#UTF_8} may have
     *
     * @see StringLengthConstraint
     */
    public DefaultDatabaseLengthValidationConfiguration withStringLengthConstraint(int maxStringByteLength) {
        return withStringLengthConstraint(new StringLengthConstraint(StandardCharsets.UTF_8, maxStringByteLength));
    }

    @Override
    public StringLengthConstraint getStringLengthConstraint() {
        return stringLengthConstraint;
    }

    /**
     * {@return this configuration, updated to use the given constraint to check numeric values of
     * the given data type}
     *
     * @param numericDatatype a (wrapper) class used for numeric values - primitive types should not
     *            be used and are validated with the same settings as their wrapper types.
     * @param numericConstraint a {@link NumericConstraint} to be used to check numeric values
     */
    public DefaultDatabaseLengthValidationConfiguration withNumericConstraint(Class<? extends Number> numericDatatype,
            NumericConstraint numericConstraint) {
        numericConstraints.put(numericDatatype, numericConstraint);
        return this;
    }

    /**
     * {@return this configuration, updated to use a new constraint created from the given precision
     * and scale to check numeric values of the given data type}
     *
     * @param numericDatatype a (wrapper) class used for numeric values - primitive types should not
     *            be used and are validated with the same settings as their wrapper types.
     * @param precision the maximum total number of digits usable to represent a number of the given
     *            type
     * @param scale the maximum number of decimal places usable to represent a number of the given
     *            type
     *
     * @see NumericConstraint
     */
    public DefaultDatabaseLengthValidationConfiguration withNumericConstraint(Class<? extends Number> numericDatatype,
            int precision,
            int scale) {
        return withNumericConstraint(numericDatatype, new NumericConstraint(precision, scale));
    }

    /**
     * {@return this configuration, updated to use a new constraint created from the given precision
     * (and no scale) to check numeric values of the given data type}
     *
     * @param numericDatatype a (wrapper) class used for numeric values - primitive types should not
     *            be used and are validated with the same settings as their wrapper types.
     * @param precision the maximum total number of digits usable to represent a number of the given
     *            type
     *
     * @see NumericConstraint
     */
    public DefaultDatabaseLengthValidationConfiguration withNumericConstraint(Class<? extends Number> numericDatatype,
            int precision) {
        return withNumericConstraint(numericDatatype, new NumericConstraint(precision));
    }

    /**
     * {@return this configuration, updated to use a new constraint created from the given precision
     * and scale to check numeric values of any numeric data type}
     *
     * @param precision the maximum total number of digits usable to represent a number of the given
     *            type
     * @param scale the maximum number of decimal places usable to represent a number of the given
     *            type
     *
     * @see NumericConstraint
     */
    public DefaultDatabaseLengthValidationConfiguration withNumericConstraintForAllNumbers(int precision, int scale) {
        NumericConstraint numericConstraint = new NumericConstraint(precision, scale);
        return withNumericConstraint(Decimal.class, numericConstraint)
                .withNumericConstraint(BigDecimal.class, numericConstraint)
                .withNumericConstraint(Float.class, numericConstraint)
                .withNumericConstraint(Double.class, numericConstraint)
                .withNumericConstraint(Short.class, numericConstraint)
                .withNumericConstraint(Integer.class, numericConstraint)
                .withNumericConstraint(Long.class, numericConstraint);
    }

    @Override
    public NumericConstraint getNumericConstraint(Class<? extends Number> numericDatatype) {
        return numericConstraints.get(numericDatatype);
    }

    @Override
    public boolean shouldValidate(PolicyAttribute policyAttribute, IModelObject modelObject) {
        return shouldValidate == null || shouldValidate.test(policyAttribute, modelObject);
    }

    /**
     * {@return this configuration, updated to use the given predicate to determine which attributes
     * to validate}
     *
     * @param shouldValidate a predicate, returning {@code true} for attributes that should be
     *            validated
     *
     * @see #shouldValidate(PolicyAttribute, IModelObject)
     */
    public DefaultDatabaseLengthValidationConfiguration withAttributeFilter(
            BiPredicate<PolicyAttribute, IModelObject> shouldValidate) {
        this.shouldValidate = shouldValidate;
        return this;
    }

    @Override
    public Message createMessageForStringLengthViolation(PolicyAttribute attribute,
            IModelObject modelObject,
            int actualLength,
            int lengthLimit) {
        return createMessage(MSGKEY_STRING_TOO_LONG, MSGCODE_STRING_TOO_LONG, modelObject, attribute,
                actualLength, lengthLimit);
    }

    @Override
    public Message createMessageForPrecisionViolation(PolicyAttribute attribute,
            IModelObject modelObject,
            Class<? extends Number> numericDatatype,
            int actualPrecision,
            int precisionLimit) {
        return createMessage(MSGKEY_NUMBER_EXCEEDS_PRECISION, MSGCODE_NUMBER_EXCEEDS_PRECISION, modelObject, attribute,
                actualPrecision, precisionLimit);
    }

    @Override
    public Message createMessageForScaleViolation(PolicyAttribute attribute,
            IModelObject modelObject,
            Class<? extends Number> numericDatatype,
            int actualScale,
            int scaleLimit) {
        return createMessage(MSGKEY_NUMBER_EXCEEDS_SCALE, MSGCODE_NUMBER_EXCEEDS_SCALE, modelObject, attribute,
                actualScale, scaleLimit);
    }

    /**
     * {@return a message, using the given parameters as follows:}
     *
     * @param msgKey is used to retrieve the message text from the {@link #getResourceBundle()
     *            ResourceBundle}
     * @param msgCode is used for {@link Message#getCode()}
     * @param modelObject is used to determine the invalid {@link ObjectProperty#getObject()}
     * @param attribute is used to determine the invalid {@link ObjectProperty#getProperty()}
     * @param actualValue the invalid value
     * @param maxValue the limit that was exceeded
     */
    protected Message createMessage(
            String msgKey,
            String msgCode,
            IModelObject modelObject,
            PolicyAttribute attribute,
            int actualValue,
            int maxValue) {
        String msgText = String.format(getResourceBundle().getString(msgKey),
                attribute.getLabel(getLocale()),
                actualValue,
                maxValue);
        Builder messageBuilder = Message.error(msgText)
                .code(msgCode)
                .invalidObjectWithProperties(modelObject, attribute.getName());
        addMarker(messageBuilder);
        return messageBuilder.create();
    }

    /**
     * Adds the {@link #getTechnicalConstraintViolationMarker()} to the given {@link Builder
     * Message.Builder}.
     *
     * @param messageBuilder a message builder, usually used inside
     *            {@link #createMessage(String, String, IModelObject, PolicyAttribute, int, int)}
     */
    protected void addMarker(Builder messageBuilder) {
        if (getTechnicalConstraintViolationMarker() != null) {
            messageBuilder.markers(getTechnicalConstraintViolationMarker());
        }
    }

}
