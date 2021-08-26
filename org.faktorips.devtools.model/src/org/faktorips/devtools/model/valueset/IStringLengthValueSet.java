/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.valueset;

import org.faktorips.datatype.ValueDatatype;

/**
 * ValueSet to restrict the length of a String attribute
 * 
 * @see ValueDatatype
 */
public interface IStringLengthValueSet extends IValueSet {

    public static final String MSGCODE_PREFIX = "STRINGLENGTH-"; //$NON-NLS-1$
    public static final String MSGCODE_NEGATIVE_VALUE = MSGCODE_PREFIX + "negativeValue"; //$NON-NLS-1$

    public static final String PROPERTY_MAXIMUMLENGTH = "maximumLength"; //$NON-NLS-1$

    String getMaximumLength();

    void setMaximumLength(String maximumLength);

    /**
     * Returns the maximum length of the String attribute parsed to an {@link Integer}.
     */
    Integer getParsedMaximumLength();
}
