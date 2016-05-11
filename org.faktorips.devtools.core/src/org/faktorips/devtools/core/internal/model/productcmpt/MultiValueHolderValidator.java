/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.productcmpt;

import static org.faktorips.devtools.core.internal.model.productcmpt.MultiValueHolder.MSGCODE_CONTAINS_DUPLICATE_VALUE;
import static org.faktorips.devtools.core.internal.model.productcmpt.MultiValueHolder.MSGCODE_CONTAINS_INVALID_VALUE;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IValueHolder;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;

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
    public MessageList validate() throws CoreException {
        MessageList messages = new MessageList();
        List<SingleValueHolder> values = valueHolder.getValue();
        if (values == null) {
            return messages;
        }
        Set<SingleValueHolder> duplicateValueHolders = getDuplicateValueHolders(values);
        for (SingleValueHolder duplicateValueHolder : duplicateValueHolders) {
            messages.newError(MSGCODE_CONTAINS_DUPLICATE_VALUE, Messages.MultiValueHolder_DuplicateValueMessageText,
                    duplicateValueHolder, IValueHolder.PROPERTY_VALUE);
        }
        for (SingleValueHolder singleValueHolder : values) {
            messages.add(singleValueHolder.newValidator(parent, ipsProject).validate());
        }
        if (messages.containsErrorMsg()) {
            messages.newError(MSGCODE_CONTAINS_INVALID_VALUE,
                    Messages.MultiValueHolder_AtLeastOneInvalidValueMessageText, new ObjectProperty[] {
                    new ObjectProperty(parent, IAttributeValue.PROPERTY_VALUE_HOLDER),
                    new ObjectProperty(valueHolder, IValueHolder.PROPERTY_VALUE) });
        }
        return messages;
    }

    private Set<SingleValueHolder> getDuplicateValueHolders(List<SingleValueHolder> values) {
        Set<SingleValueHolder> duplicates = new HashSet<SingleValueHolder>();
        Set<SingleValueHolder> processedValues = new HashSet<SingleValueHolder>();
        for (SingleValueHolder element : values) {
            if (processedValues.contains(element)) {
                duplicates.add(element);
            } else {
                processedValues.add(element);
            }
        }
        return duplicates;
    }

}
