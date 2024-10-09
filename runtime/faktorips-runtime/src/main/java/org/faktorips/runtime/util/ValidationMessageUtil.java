/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.util;

import java.util.Locale;
import java.util.ResourceBundle;

import org.faktorips.annotation.UtilityClass;

@UtilityClass
public class ValidationMessageUtil {

    private ValidationMessageUtil() {
        // do not instantiate
    }

    /**
     * Generates a validation error message localized using the provided {@link Locale} and
     * formatted with the provided arguments.
     *
     * @param locale The {@link Locale} for localizing the message.
     * @param resourceBundleName The name of the resource bundle containing the message template.
     * @param msgKey The key in the resource bundle to get the message format.
     * @param formatArgs A list of arguments to format the message.
     * @return the localized and formatted message
     */
    public static String generateValidationMessage(
            Locale locale,
            String resourceBundleName,
            String msgKey,
            Object... formatArgs) {

        ResourceBundle messages = ResourceBundle.getBundle(resourceBundleName, locale);
        return String.format(messages.getString(msgKey), formatArgs);
    }

}
