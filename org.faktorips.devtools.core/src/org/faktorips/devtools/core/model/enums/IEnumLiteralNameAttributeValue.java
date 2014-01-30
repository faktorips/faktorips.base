/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.enums;

import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;

/**
 * An <tt>IEnumLiteralNameAttributeValue</tt> represents a value for a
 * <tt>IEnumLiteralNameAttribute</tt>.
 * 
 * @author Alexander Weickmann
 * 
 * @since 3.0
 */
public interface IEnumLiteralNameAttributeValue extends IEnumAttributeValue {

    /** The XML tag for this {@link IIpsObjectPart}. */
    public static final String XML_TAG = "EnumLiteralNameAttributeValue"; //$NON-NLS-1$

    /** Prefix for all message codes of this class. */
    public static final String MSGCODE_PREFIX = "ENUMLITERALNAMEATTRIBUTEVALUE-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the literal name value is not a valid Java
     * identifier.
     */
    public static final String MSGCODE_ENUM_LITERAL_NAME_ATTRIBUTE_VALUE_IS_NO_VALID_JAVA_IDENTIFIER = MSGCODE_PREFIX
            + "EnumLiteralNameAttributeValueIsNoValidJavaIdentifier"; //$NON-NLS-1$

}
