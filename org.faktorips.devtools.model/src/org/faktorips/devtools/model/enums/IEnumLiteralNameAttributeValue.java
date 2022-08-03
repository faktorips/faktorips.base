/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.enums;

import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;

/**
 * An <code>IEnumLiteralNameAttributeValue</code> represents a value for a
 * <code>IEnumLiteralNameAttribute</code>.
 * 
 * @author Alexander Weickmann
 * 
 * @since 3.0
 */
public interface IEnumLiteralNameAttributeValue extends IEnumAttributeValue {

    /** The XML tag for this {@link IIpsObjectPart}. */
    String XML_TAG = "EnumLiteralNameAttributeValue"; //$NON-NLS-1$

    /** Prefix for all message codes of this class. */
    String MSGCODE_PREFIX = "ENUMLITERALNAMEATTRIBUTEVALUE-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the literal name value is not a valid Java
     * identifier.
     */
    String MSGCODE_ENUM_LITERAL_NAME_ATTRIBUTE_VALUE_IS_NO_VALID_JAVA_IDENTIFIER = MSGCODE_PREFIX
            + "EnumLiteralNameAttributeValueIsNoValidJavaIdentifier"; //$NON-NLS-1$

}
