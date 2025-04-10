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

import java.nio.charset.Charset;
import java.util.Locale;

/**
 * Configuration for string length validation
 */
public class StringLengthValidationConfiguration extends ValidationConfiguration {
    private final Charset stringEncoding;
    private final int maxStringByteLength;

    /**
     * Creates a new string length validation configuration.
     *
     * @param locale the locale to use for validation messages
     * @param stringEncoding the character encoding to use for calculating string byte length
     * @param maxStringByteLength the maximum allowed byte length for string values
     */
    public StringLengthValidationConfiguration(Locale locale, Charset stringEncoding, int maxStringByteLength) {
        super(locale);
        this.stringEncoding = stringEncoding;
        this.maxStringByteLength = maxStringByteLength;
    }

    public Charset getStringEncoding() {
        return stringEncoding;
    }

    public int getMaxStringByteLength() {
        return maxStringByteLength;
    }
}
