/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime;

import java.util.Locale;

/**
 * Base interface for all model objects.
 * 
 * @author Jan Ortmann
 */
public interface IModelObject {

    /**
     * Constant for the return values of validate methods (i.e. validateSelf() or ruleXYZ()).
     * Indicates that the validation should be stopped.
     */
    boolean STOP_VALIDATION = false;
    /**
     * Constant for the return values of validate methods (i.e. validateSelf() or ruleXYZ()).
     * Indicates that the validation should be continued.
     */
    boolean CONTINUE_VALIDATION = true;

    /**
     * Validates the model object and returns a list of messages. If no message is generated the
     * method returns an empty list. Note that also messages like warnings or informations can be
     * returned for valid objects.
     * 
     * @param context provides additional external information that might be necessary to execute
     *            the validation. E.g. the business context, the locale to provide locale specific
     *            message texts, user information. The parameter must not be <code>null</code>.
     * 
     * @throws NullPointerException if context is <code>null</code>.
     */
    MessageList validate(IValidationContext context);

    /**
     * Returns a description for this model object in the specified locale. The description can be
     * used for possible error messages.
     * 
     * @param locale the locale/language in which the description should be given
     * 
     * @return the description for the given locale/language or the result of the {@code toString}
     *             method if no custom implementation is provided
     */
    default String getDescription(Locale locale) {
        return toString();
    }
}
