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

import org.faktorips.runtime.MessageList;

/**
 * Common interface for classes that validate objects of the FIPS meta model.
 */
public interface IMetaModelValidator {

    /**
     * Returns a {@link MessageList} containing all validation messages this validator creates.
     * Returns an empty {@link MessageList} if no errors could be found or if no validation can be
     * performed ( {@link #canValidate()} returns <code>false</code> in that case).
     */
    public MessageList validateIfPossible();

    /**
     * Returns <code>true</code> if this validator has sufficient information to perform its
     * validation task. <code>false</code> if it cannot perform a validation. No messages will be
     * created in the latter case.
     */
    public boolean canValidate();

    /**
     * Convenience method to perform the validation (if possible) and append all created messages to
     * the given {@link MessageList}.
     */
    public void validateAndAppendMessages(MessageList messageList);

}
