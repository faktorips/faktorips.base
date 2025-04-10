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

import org.faktorips.runtime.IMarker;
import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.model.type.PolicyAttribute;

/**
 * Validates string length constraints for model object attributes.
 */
public class StringLengthValidation extends AbstractValidation {
    public static final String MSGCODE_STRING_TOO_LONG = "STRING_TOO_LONG";
    public static final String MSGKEY_STRING_TOO_LONG = "InvalidAttribute.StringTooLong";

    private final StringLengthValidationConfiguration configuration;

    /**
     * Creates a new string length validation with the specified configuration.
     *
     * @param configuration the configuration specifying string length constraints and locale
     * @param messageList the list to add validation messages to
     */
    public StringLengthValidation(StringLengthValidationConfiguration configuration, MessageList messageList) {
        super(configuration, messageList);
        this.configuration = configuration;
    }

    @Override
    protected void validateAttribute(PolicyAttribute attribute, IModelObject modelObject) {
        Object value = attribute.getValue(modelObject);
        if (value instanceof String stringValue) {
            validateStringLength(stringValue, attribute, modelObject);
        }
    }

    private void validateStringLength(String value, PolicyAttribute attribute, IModelObject modelObject) {
        int byteLength = value.getBytes(configuration.getStringEncoding()).length;
        int maxLength = configuration.getMaxStringByteLength();

        if (byteLength > maxLength) {
            String msg = String.format(getResourceBundle().getString(MSGKEY_STRING_TOO_LONG),
                    attribute.getLabel(configuration.getLocale()),
                    maxLength);
            Message message = Message.error(msg)
                    .code(MSGCODE_STRING_TOO_LONG)
                    .invalidObjectWithProperties(modelObject, attribute.getName())
                    .create();
            addMessageWithMarker(message, configuration);
        }
    }

    private void addMessageWithMarker(Message message, StringLengthValidationConfiguration configuration) {
        IMarker marker = configuration.getTechnicalConstraintViolatedMarker();

        Message messageToAdd = (marker != null)
                ? new Message.Builder(message).markers(marker).create()
                : message;

        addMessage(messageToAdd);
    }
}