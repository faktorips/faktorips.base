/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt;

import static org.faktorips.devtools.model.internal.productcmpt.MultiValueHolder.MSGCODE_CONTAINS_DUPLICATE_VALUE;
import static org.faktorips.devtools.model.internal.productcmpt.MultiValueHolder.MSGCODE_CONTAINS_INVALID_VALUE;
import static org.faktorips.devtools.model.productcmpt.IAttributeValue.MSGCODE_MANDATORY_VALUE_NOT_DEFINED;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.faktorips.devtools.model.internal.value.InternationalStringValue;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.ISingleValueHolder;
import org.faktorips.devtools.model.productcmpt.IValueHolder;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.ObjectProperty;
import org.faktorips.runtime.internal.IpsStringUtils;

/** {@code IValueHolderValidator} implementation for {@link MultiValueHolder}s. */
public class MultiValueHolderValidator implements IValueHolderValidator {

    private final MultiValueHolder valueHolder;
    private final IAttributeValue parent;
    private final IIpsProject ipsProject;

    public MultiValueHolderValidator(MultiValueHolder valueHolder, IAttributeValue parent, IIpsProject ipsProject) {
        super();
        this.valueHolder = valueHolder;
        this.parent = parent;
        this.ipsProject = ipsProject;
    }

    @Override
    public MessageList validate() {
        MessageList messages = new MessageList();
        List<ISingleValueHolder> values = valueHolder.getValue();
        if (values == null) {
            return messages;
        }
        Set<ISingleValueHolder> duplicateValueHolders = getDuplicateValueHolders(values);
        for (ISingleValueHolder duplicateValueHolder : duplicateValueHolders) {
            messages.newError(MSGCODE_CONTAINS_DUPLICATE_VALUE, Messages.MultiValueHolder_DuplicateValueMessageText,
                    duplicateValueHolder, IValueHolder.PROPERTY_VALUE);
        }
        for (ISingleValueHolder singleValueHolder : values) {
            messages.add(((SingleValueHolder)singleValueHolder).newValidator(parent, ipsProject).validate());
        }
        if (messages.containsErrorMsg()) {
            messages.newError(MSGCODE_CONTAINS_INVALID_VALUE,
                    Messages.MultiValueHolder_AtLeastOneInvalidValueMessageText,
                    new ObjectProperty(parent, IAttributeValue.PROPERTY_VALUE_HOLDER),
                    new ObjectProperty(valueHolder, IValueHolder.PROPERTY_VALUE));
        }
        validateEmptyMultiValue(messages, values);
        return messages;
    }

    private Set<ISingleValueHolder> getDuplicateValueHolders(List<ISingleValueHolder> values) {
        Set<ISingleValueHolder> duplicates = new HashSet<>();
        Set<ISingleValueHolder> processedValues = new HashSet<>();
        for (ISingleValueHolder element : values) {
            if (processedValues.contains(element)) {
                duplicates.add(element);
            } else {
                processedValues.add(element);
            }
        }
        return duplicates;
    }

    private boolean containsNullValue(List<ISingleValueHolder> values) {
        for (ISingleValueHolder holder : values) {
            if (holder.isNullValue()
                    || IpsStringUtils.isEmpty(holder.getStringValue())
                    || (holder.getValue() instanceof InternationalStringValue internalionalStringValue
                            && internalionalStringValue.getContent().values().stream()
                                    .anyMatch(e -> IpsStringUtils.isBlank(e.getValue())))) {
                return true;
            }
        }
        return false;
    }

    private void validateEmptyMultiValue(MessageList messages, List<ISingleValueHolder> values) {
        if (parent.findAttribute(ipsProject) != null) {
            IValueSet valueSet = parent.findAttribute(ipsProject).getValueSet();
            if (valueSet != null) {
                if (values.isEmpty() && (!valueSet.isContainsNull())) {
                    String text = MessageFormat.format(Messages.AttributeValue_MultiValueMustNotBeEmpty,
                            parent.getName().toString());
                    messages.newError(MSGCODE_MANDATORY_VALUE_NOT_DEFINED, text,
                            new ObjectProperty(parent, IAttributeValue.PROPERTY_VALUE_HOLDER),
                            new ObjectProperty(valueHolder, IValueHolder.PROPERTY_VALUE));
                }

                if (!(values.isEmpty()) && (containsNullValue(values))) {
                    String text = MessageFormat.format(Messages.AttributeValue_MultiValueMustNotContainNull,
                            parent.getName().toString());
                    messages.newError(MSGCODE_CONTAINS_INVALID_VALUE, text,
                            new ObjectProperty(parent, IAttributeValue.PROPERTY_VALUE_HOLDER),
                            new ObjectProperty(valueHolder, IValueHolder.PROPERTY_VALUE));
                }

            }
        }
    }

}
