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
 * A validation context is provided to the validate() method generated by Faktor-IPS. By means of
 * the validation context the caller can provide additional information to the validate method like
 * for example the business context in which the validation is to execute.
 * 
 * @author Peter Erzberger
 */
public interface IValidationContext {

    /**
     * Returns the Locale that is to use for the creation of validation messages.
     */
    public Locale getLocale();

    /**
     * Returns the value for property with the specified name.
     */
    public Object getValue(String propertyName);
}
