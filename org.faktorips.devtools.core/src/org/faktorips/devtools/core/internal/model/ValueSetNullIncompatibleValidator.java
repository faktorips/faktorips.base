/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model;

import org.faktorips.devtools.core.internal.model.productcmpt.ConfigElement;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.util.message.MessageList;

/**
 * Validates whether a value set is compatible with another value set (preset) in terms of
 * containing <code>null</code>. Adds an error message if the preset value set does not contain
 * <code>null</code>, but the current value set does. The message is bound to the current value set
 * and the property {@link IValueSet#PROPERTY_CONTAINS_NULL}. Used in multiple contexts, e.g. :
 * <ul>
 * <li>
 * overwriting attributes (product- and policy side). The preset value set is the value set of the
 * overwritten attribute.</li>
 * <li>{@link ConfigElement configuration elements} that specialize the value set of a policy
 * attribute. The preset value set is the policy attribute's value set.</li>
 * </ul>
 */
public class ValueSetNullIncompatibleValidator implements IMetaModelValidator {
    /**
     * Validation message code to indicate that a value set is not compatible with another because
     * it contains <code>null</code>.
     */
    public static final String MSGCODE_INCOMPAIBLE_VALUESET = "ValueSetNullIncompatibleValidator_" + "NullIncompatible"; //$NON-NLS-1$ //$NON-NLS-2$

    private final IValueSet presetValueset;
    private final IValueSet currentValueset;

    public ValueSetNullIncompatibleValidator(IValueSet presetValueset, IValueSet currentValueset) {
        this.presetValueset = presetValueset;
        this.currentValueset = currentValueset;

    }

    @Override
    public MessageList validateIfPossible() {
        MessageList messageList = new MessageList();
        if (canValidate()) {
            validateAndAppendMessages(messageList);
        }
        return messageList;
    }

    @Override
    public boolean canValidate() {
        return currentValueset != null && presetValueset != null;
    }

    @Override
    public void validateAndAppendMessages(MessageList messageList) {
        if (isNullIncompatible()) {
            messageList.newError(MSGCODE_INCOMPAIBLE_VALUESET,
                    Messages.ValueSetNullIncompatibleValidator_Msg_NullNotAllowed, currentValueset,
                    IValueSet.PROPERTY_CONTAINS_NULL);
        }

    }

    private boolean isNullIncompatible() {
        return currentValueset.isContainsNull() && !presetValueset.isContainsNull();
    }

}
