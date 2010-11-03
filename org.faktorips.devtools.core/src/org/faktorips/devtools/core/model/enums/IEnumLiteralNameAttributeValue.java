/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
    public final static String XML_TAG = "EnumLiteralNameAttributeValue"; //$NON-NLS-1$

    /** Prefix for all message codes of this class. */
    public final static String MSGCODE_PREFIX = "ENUMLITERALNAMEATTRIBUTEVALUE-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the literal name value is a number or begins with a
     * number.
     */
    public final static String MSGCODE_ENUM_LITERAL_NAME_ATTRIBUTE_VALUE_IS_NUMBER = MSGCODE_PREFIX
            + "EnumLiteralNameAttributeValueIsNumber"; //$NON-NLS-1$

}
