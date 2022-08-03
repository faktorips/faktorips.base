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
import org.faktorips.runtime.model.type.PolicyAttribute;
import org.faktorips.values.ObjectUtil;
import org.faktorips.valueset.Range;
import org.faktorips.valueset.ValueSet;

import edu.umd.cs.findbugs.annotations.CheckForNull;

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
 * <p>
 * An {@link IMarker} for {@link IMarker#isRequiredInformationMissing() missing required
 * information}, that will be used in the default implementation of
 * {@link #createMessageForMissingMandatoryValue(PolicyAttribute, IModelObject, Class)} can be
 * provided in the constructor.
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
    private final IMarker missingMandatoryValueMarker;

    public DefaultGenericAttributeValidationConfiguration(Locale locale) {
        this(locale, null);
    }

    public DefaultGenericAttributeValidationConfiguration(Locale locale,
            @CheckForNull IMarker requiredInformationMissingMarker) {
        this(ResourceBundle.getBundle(RESOURCE_BUNDLE_NAME, locale), locale, requiredInformationMissingMarker);
    }

    public DefaultGenericAttributeValidationConfiguration(ResourceBundle messages, Locale locale) {
        this(messages, locale, null);
    }

    public DefaultGenericAttributeValidationConfiguration(ResourceBundle messages, Locale locale,
            @CheckForNull IMarker requiredInformationMissingMarker) {
        this.messages = requireNonNull(messages, "messages must not be null");
        this.locale = requireNonNull(locale, "locale must not be null");
        missingMandatoryValueMarker = requiredInformationMissingMarker;
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

    /**
     * Returns the {@link IMarker} used to
     * {@link #createMessageForMissingMandatoryValue(PolicyAttribute, IModelObject, Class) create a
     * message for a missing mandatory value}.
     */
    public IMarker getMissingMandatoryValueMarker() {
        return missingMandatoryValueMarker;
    }

    @Override
    public boolean shouldValidate(PolicyAttribute policyAttribute, IModelObject modelObject) {
        return true;
    }

    /**
     * Creates an error message with a message code created from the given prefix via
     * {@link #createMsgCode(GenericRelevanceValidation.Error, PolicyAttribute, Class)} and an
     * invalid object property for the given attribute.
     *
     * @implSpec calling
     *               {@link #builderForErrorMessage(PolicyAttribute, IModelObject, org.faktorips.runtime.validation.GenericRelevanceValidation.Error, Class, String)}
     *               is preferred if you want to adapt the message.
     */
    protected Message createErrorMessage(PolicyAttribute policyAttribute,
            IModelObject modelObject,
            GenericRelevanceValidation.Error error,
            Class<? extends IModelObject> definingModelObjectClass,
            String message) {
        return builderForErrorMessage(policyAttribute, modelObject, error, definingModelObjectClass, message)
                .create();
    }

    /**
     * Creates an error message builder with a message code created from the given prefix via
     * {@link #createMsgCode(GenericRelevanceValidation.Error, PolicyAttribute, Class)} and an
     * invalid object property for the given attribute.
     */
    protected Message.Builder builderForErrorMessage(PolicyAttribute policyAttribute,
            IModelObject modelObject,
            GenericRelevanceValidation.Error error,
            Class<? extends IModelObject> definingModelObjectClass,
            String message) {
        String msgCode = createMsgCode(error, policyAttribute, definingModelObjectClass);
        return Message.error(message)
                .code(msgCode)
                .invalidObjectWithProperties(modelObject, policyAttribute.getName());
    }

    /**
     * Creates a message code from the pattern
     * &lt;errorCode&gt;.&lt;definingModelObjectClassName&gt;.&lt;policyAttributeName&gt;.
     */
    protected String createMsgCode(GenericRelevanceValidation.Error error,
            PolicyAttribute policyAttribute,
            Class<? extends IModelObject> definingModelObjectClass) {
        return error.getDefaultMessageCode(definingModelObjectClass, policyAttribute.getName());
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
     *               attribute's label} in the locale provided to this configuration's constructor
     *               and returns it in double quotes.
     * @implSpec Implementers may use the given model object to further qualify the field.
     * @param policyAttribute the model type reference for the validated attribute
     * @param modelObject the model object instance on which the attribute was validated
     */
    protected String getLabelFor(PolicyAttribute policyAttribute, IModelObject modelObject) {
        return '"' + policyAttribute.getLabel(locale) + '"';
    }

    @Override
    public Message createMessageForMissingMandatoryValue(PolicyAttribute policyAttribute,
            IModelObject modelObject,
            Class<? extends IModelObject> definingModelObjectClass) {
        Message message = createErrorMessage(policyAttribute, modelObject,
                GenericRelevanceValidation.Error.MandatoryValueMissing,
                definingModelObjectClass,
                format(ERROR_MANDATORY_MSG_CODE_PREFIX, getLabelFor(policyAttribute, modelObject)));
        IMarker marker = getMissingMandatoryValueMarker();
        if (marker != null) {
            message = new Message.Builder(message).markers(marker).create();
        }
        return message;
    }

    @Override
    public Message createMessageForValuePresentForIrrelevantAttribute(PolicyAttribute policyAttribute,
            IModelObject modelObject,
            Class<? extends IModelObject> definingModelObjectClass) {
        return createErrorMessage(policyAttribute, modelObject, GenericRelevanceValidation.Error.IrrelevantValuePresent,
                definingModelObjectClass,
                format(ERROR_IRRELEVANT_MSG_CODE_PREFIX, getLabelFor(policyAttribute, modelObject)));
    }

    @Override
    public Message createMessageForValueNotInAllowedValueSet(PolicyAttribute policyAttribute,
            IModelObject modelObject,
            Class<? extends IModelObject> definingModelObjectClass) {
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
        return createErrorMessage(policyAttribute, modelObject, GenericRelevanceValidation.Error.ValueNotInValueSet,
                definingModelObjectClass, sb.toString());
    }

}
