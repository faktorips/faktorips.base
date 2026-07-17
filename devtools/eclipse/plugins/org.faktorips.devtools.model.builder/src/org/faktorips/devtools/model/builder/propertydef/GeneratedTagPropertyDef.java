/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.propertydef;

import org.faktorips.devtools.model.builder.java.JavaBuilderSet;
import org.faktorips.devtools.model.internal.ipsproject.properties.IpsBuilderSetPropertyDef;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * Base {@link IpsBuilderSetPropertyDef} that implements, via the Template Method Pattern, the
 * cross-validation logic ensuring that the start-tag and end-tag builder properties are either
 * both set or both empty. Concrete subclasses {@link StartTag} and {@link EndTag} supply the name
 * of their sibling property and the ordering of the error-message arguments.
 */
public abstract class GeneratedTagPropertyDef extends IpsBuilderSetPropertyDef {

    public static final String MSGCODE_ONLY_ONE_TAG_SET = MSGCODE_PREFIX + "OnlyOneTagSet"; //$NON-NLS-1$

    /**
     * Returns the name of the sibling property (e.g. {@link StartTag} returns the end-tag property
     * name, {@link EndTag} returns the start-tag property name).
     */
    protected abstract String getSiblingPropertyName();

    /**
     * Returns the arguments for the validation error message, where {@code {0}} is always the
     * start-tag value and {@code {1}} is always the end-tag value.
     */
    protected abstract String[] messageArgs(String value, String siblingValue);

    @Override
    public Message validateValue(IIpsProject ipsProject, String value) {
        Message validationMessage = super.validateValue(ipsProject, value);
        if (validationMessage != null) {
            return validationMessage;
        }
        if (IpsStringUtils.isEmpty(value)) {
            return null;
        }
        String siblingValue = ipsProject.getIpsArtefactBuilderSet().getConfig()
                .getPropertyValueAsString(getSiblingPropertyName());
        if (IpsStringUtils.isEmpty(siblingValue)) {
            return Message.newError(MSGCODE_ONLY_ONE_TAG_SET,
                    Messages.bind(Messages.GeneratedTagPropertyDef_onlyOneTagSet, messageArgs(value, siblingValue)));
        }
        return null;
    }

    public static class StartTag extends GeneratedTagPropertyDef {

        @Override
        protected String getSiblingPropertyName() {
            return JavaBuilderSet.CONFIG_PROPERTY_GENERATED_END_TAG;
        }

        @Override
        protected String[] messageArgs(String value, String siblingValue) {
            return new String[] { value, siblingValue };
        }
    }

    public static class EndTag extends GeneratedTagPropertyDef {

        @Override
        protected String getSiblingPropertyName() {
            return JavaBuilderSet.CONFIG_PROPERTY_GENERATED_START_TAG;
        }

        @Override
        protected String[] messageArgs(String value, String siblingValue) {
            return new String[] { siblingValue, value };
        }
    }
}
