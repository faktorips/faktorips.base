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

/**
 * Configuration for string length validation. The encoding is used to transform a String to its
 * Byte representation, which is then measured against the {@code maxStringByteLength}
 */
public record StringLengthConstraint(Charset stringEncoding, int maxStringByteLength) {

}
