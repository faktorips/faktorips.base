/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal;

import org.faktorips.devtools.model.internal.productcmpt.ConfiguredValueSet;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.runtime.MessageList;

/**
 * Validates whether a value set is compatible with another value set (preset) in terms of
 * containing <code>null</code>. Adds an error message if the preset value set does not contain
 * <code>null</code>, but the current value set does. The message is bound to the current value set
 * and the property {@link IValueSet#PROPERTY_CONTAINS_NULL}. Used in multiple contexts, e.g. :
 * <ul>
 * <li>overwriting attributes (product- and policy side). The preset value set is the value set of
 * the overwritten attribute.</li>
 * <li>{@link ConfiguredValueSet configuration elements} that specialize the value set of a policy
 * attribute. The preset value set is the policy attribute's value set.</li>
 * </ul>
 */
public class ValueSetNullIncompatibleValidator implements IMetaModelValidator {
    /**
     * Validation message code to indicate that a value set is not compatible with another because
     * it contains <code>null</code>.
     */
    public static final String MSGCODE_INCOMPATIBLE_VALUESET = "ValueSetNullIncompatibleValidator_" //$NON-NLS-1$
            + "NullIncompatible"; //$NON-NLS-1$

    private final IValueSet presentValueset;
    private final IValueSet currentValueset;

    public ValueSetNullIncompatibleValidator(IValueSet presetValueset, IValueSet currentValueset) {
        presentValueset = presetValueset;
        this.currentValueset = currentValueset;

    }

    @Override
    public MessageList validateIfPossible() {
        MessageList messageList = new MessageList();
        validateAndAppendMessages(messageList);
        return messageList;
    }

    @Override
    public boolean canValidate() {
        return currentValueset != null && presentValueset != null;
    }

    @Override
    public void validateAndAppendMessages(MessageList messageList) {
        if (canValidate()) {
            validateInternal(messageList);
        }
    }

    private void validateInternal(MessageList messageList) {
        if (isNullIncompatible()) {
            messageList.newError(MSGCODE_INCOMPATIBLE_VALUESET,
                    Messages.ValueSetNullIncompatibleValidator_Msg_NullNotAllowed, currentValueset,
                    IValueSet.PROPERTY_CONTAINS_NULL);
        }
    }

    private boolean isNullIncompatible() {
        return currentValueset.isContainsNull() && !presentValueset.isContainsNull();
    }

}
