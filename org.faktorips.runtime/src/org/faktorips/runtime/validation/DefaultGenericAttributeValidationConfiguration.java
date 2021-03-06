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

import static java.util.Objects.requireNonNull;

import java.util.Locale;
import java.util.ResourceBundle;

import org.faktorips.runtime.IMarker;
import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.runtime.model.IpsModel;
import org.faktorips.runtime.model.type.PolicyAttribute;
import org.faktorips.values.ObjectUtil;
import org.faktorips.valueset.Range;
import org.faktorips.valueset.ValueSet;

/**
 * Default implementation of {@link IGenericAttributeValidationConfiguration} that uses a
 * {@link Locale} specific {@link ResourceBundle} to load messages.
 * <p>
 * Messages are created with one replacement parameter that is set to the attribute's localized
 * {@link PolicyAttribute#getLabel(Locale) label} and with a message code created from an error
 * specific
 * prefix({@value #ERROR_MANDATORY_MSG_CODE_PREFIX}/{@value #ERROR_IRRELEVANT_MSG_CODE_PREFIX}/{@value #ERROR_INVALID_MSG_CODE_PREFIX}),
 * the name of the policy type and the attribute name.
 * <p>
 * To add {@link IMarker markers} or other information to messages, you can override this class'
 * methods and use {@link org.faktorips.runtime.Message.Builder} to modify the messages returned
 * from them.
 */
public class DefaultGenericAttributeValidationConfiguration implements IGenericAttributeValidationConfiguration {

    /**
     * Message code prefix indicating a missing value for a mandatory attribute.
     */
    public static final String ERROR_MANDATORY_MSG_CODE_PREFIX = "InvalidAttribute.Mandatory";

    /**
     * Message code prefix indicating a value is set for an irrelevant attribute.
     */
    public static final String ERROR_IRRELEVANT_MSG_CODE_PREFIX = "InvalidAttribute.Irrelevant";

    /**
     * Message code prefix indicating a missing value outside of an attribute's allowed value set.
     */
    public static final String ERROR_INVALID_MSG_CODE_PREFIX = "InvalidAttribute.Invalid";

    private static final String MSG_KEY_VALUE_IN_RANGE = "ValueInRange";
    private static final String MSG_KEY_VALUE_IN_RANGE_LOWER = "ValueInRangeLower";
    private static final String MSG_KEY_VALUE_IN_RANGE_UPPER = "ValueInRangeUpper";
    private static final String MSG_KEY_VALUE_IN_RANGE_STEPS = "ValueInRangeSteps";

    private static final String RESOURCE_BUNDLE_NAME = DefaultGenericAttributeValidationConfiguration.class.getName();

    private final Locale locale;
    private final ResourceBundle messages;

    public DefaultGenericAttributeValidationConfiguration(Locale locale) {
        this(ResourceBundle.getBundle(RESOURCE_BUNDLE_NAME, locale), locale);
    }

    public DefaultGenericAttributeValidationConfiguration(ResourceBundle messages, Locale locale) {
        this.messages = requireNonNull(messages, "messages must not be null");
        this.locale = requireNonNull(locale, "locale must not be null");
    }

    /**
     * Returns the {@link Locale} this configuration was created for.
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Returns the {@link ResourceBundle} for this configuration.
     */
    public ResourceBundle getMessages() {
        return messages;
    }

    @Override
    public boolean shouldValidate(PolicyAttribute policyAttribute, IModelObject modelObject) {
        return true;
    }

    /**
     * Creates an error message with a message code created from the given prefix via
     * {@link #createMsgCode(String, IModelObject, PolicyAttribute)} and an invalid object property
     * for the given attribute.
     */
    protected Message createErrorMessage(PolicyAttribute policyAttribute,
            IModelObject modelObject,
            String msgCodePrefix,
            String message) {
        String msgCode = createMsgCode(msgCodePrefix, modelObject, policyAttribute);
        return Message.error(message)
                .code(msgCode)
                .invalidObjectWithProperties(modelObject, policyAttribute.getName())
                .create();
    }

    /**
     * Creates a message code from the pattern
     * &lt;prefix&gt;.&lt;policyCmptTypeName&gt;.&lt;policyAttributeName&gt;.
     */
    protected String createMsgCode(String msgCodePrefix, IModelObject modelObject, PolicyAttribute policyAttribute) {
        return String.format("%s.%s.%s", msgCodePrefix, IpsModel.getPolicyCmptType(modelObject).getName(),
                policyAttribute.getName());
    }

    /**
     * {@link String#format(String, Object...) Formats the String} obtained from this
     * configuration's {@link ResourceBundle} with the given key using the replacement parameters.
     *
     * @param msgKey the key to find the format String from the {@link ResourceBundle}.
     * @param replacementParameters the parameters to be passed to
     *            {@link String#format(String, Object...)}
     */
    protected String format(String msgKey, Object... replacementParameters) {
        return String.format(messages.getString(msgKey), replacementParameters);
    }

    /**
     * Returns the label for the given attribute to be used in an error message.
     *
     * @implNote The default implementation uses the {@link PolicyAttribute#getLabel(Locale)
     *           attribute's label} in the locale provided to this configuration's constructor and
     *           returns it in double quotes.
     * @implSpec Implementers may use the given model object to further qualify the field.
     * @param policyAttribute the model type reference for the validated attribute
     * @param modelObject the model object instance on which the attribute was validated
     */
    protected String getLabelFor(PolicyAttribute policyAttribute, IModelObject modelObject) {
        return '"' + policyAttribute.getLabel(locale) + '"';
    }

    @Override
    public Message createMessageForMissingMandatoryValue(PolicyAttribute policyAttribute, IModelObject modelObject) {
        return createErrorMessage(policyAttribute, modelObject, ERROR_MANDATORY_MSG_CODE_PREFIX,
                format(ERROR_MANDATORY_MSG_CODE_PREFIX, getLabelFor(policyAttribute, modelObject)));
    }

    @Override
    public Message createMessageForValuePresentForIgnoredAttribute(PolicyAttribute policyAttribute,
            IModelObject modelObject) {
        return createErrorMessage(policyAttribute, modelObject, ERROR_IRRELEVANT_MSG_CODE_PREFIX,
                format(ERROR_IRRELEVANT_MSG_CODE_PREFIX, getLabelFor(policyAttribute, modelObject)));
    }

    @Override
    public Message createMessageForValueNotInAllowedValueSet(PolicyAttribute policyAttribute,
            IModelObject modelObject) {
        StringBuilder sb = new StringBuilder(
                format(ERROR_INVALID_MSG_CODE_PREFIX, getLabelFor(policyAttribute, modelObject)));
        ValueSet<?> valueSet = policyAttribute.getValueSet(modelObject);
        if (valueSet.isRange()) {
            sb.append(' ');
            Range<?> range = (Range<?>)valueSet;
            Comparable<?> lowerBound = range.getLowerBound();
            Comparable<?> upperBound = range.getUpperBound();
            Comparable<?> step = range.getStep();
            String stepLabel = ObjectUtil.isNull(step) ? IpsStringUtils.EMPTY
                    : format(MSG_KEY_VALUE_IN_RANGE_STEPS, step);
            if (ObjectUtil.isNull(lowerBound)) {
                sb.append(format(MSG_KEY_VALUE_IN_RANGE_UPPER, upperBound, stepLabel));
            } else if (ObjectUtil.isNull(upperBound)) {
                sb.append(format(MSG_KEY_VALUE_IN_RANGE_LOWER, lowerBound, stepLabel));
            } else {
                sb.append(format(MSG_KEY_VALUE_IN_RANGE, lowerBound, upperBound, stepLabel));
            }
        }
        return createErrorMessage(policyAttribute, modelObject, ERROR_INVALID_MSG_CODE_PREFIX, sb.toString());
    }

}
