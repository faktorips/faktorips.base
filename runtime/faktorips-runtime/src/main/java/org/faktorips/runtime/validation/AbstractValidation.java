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

import java.util.ResourceBundle;

import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IModelObjectVisitor;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.model.IpsModel;
import org.faktorips.runtime.model.type.PolicyAttribute;
import org.faktorips.runtime.model.type.PolicyCmptType;

/**
 * Abstract base class for validation implementations.
 */
public abstract class AbstractValidation implements IModelObjectVisitor {

    protected static final String RESOURCE_BUNDLE_NAME = AbstractValidation.class.getName();

    private final MessageList messageList;
    private final ResourceBundle resourceBundle;

    protected AbstractValidation(ValidationConfiguration config, MessageList messageList) {
        this.messageList = messageList;
        resourceBundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_NAME, config.getLocale());
    }

    /**
     * Gets the resource bundle.
     *
     * @return the resource bundle
     */
    protected ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    /**
     * Adds a validation message to the message list.
     *
     * @param message the message to add
     */
    protected void addMessage(Message message) {
        messageList.add(message);
    }

    @Override
    public boolean visit(IModelObject modelObject) {
        PolicyCmptType policyType = IpsModel.getPolicyCmptType(modelObject.getClass());
        if (policyType != null) {
            for (PolicyAttribute attribute : policyType.getAttributes()) {
                validateAttribute(attribute, modelObject);
            }
        }
        return true;
    }

    protected abstract void validateAttribute(PolicyAttribute attribute, IModelObject modelObject);
}