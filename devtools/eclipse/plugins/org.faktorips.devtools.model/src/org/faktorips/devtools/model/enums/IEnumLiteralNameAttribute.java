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

/**
 * Each <code>IEnumType</code> that contains values (that means it is not abstract and it does
 * define its values in the model) needs to specify exactly one
 * <code>IEnumLiteralNameAttribute</code>.
 * <p>
 * The value of this attribute specifies the name of the enumeration literal that will be used in
 * the generated source code.
 * <p>
 * The <code>IEnumLiteralNameAttribute</code> can refer to another <code>IEnumAttribute</code> being
 * its provider for a default value.
 * <p>
 * <code>IEnumLiteralNameAttribute</code>s are always of data type <code>String</code> and
 * <code>unique</code>.
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.4
 */
public interface IEnumLiteralNameAttribute extends IEnumAttribute {

    /** The XML tag for this <code>IpsObjectPart</code>. */
    String XML_TAG = "EnumLiteralNameAttribute"; //$NON-NLS-1$

    /** Name of the <code>defaultValueProviderAttribute</code> property. */
    String PROPERTY_DEFAULT_VALUE_PROVIDER_ATTRIBUTE = "defaultValueProviderAttribute"; //$NON-NLS-1$

    /** The default name for a <code>IEnumLiteralNameAttribute</code>. */
    String DEFAULT_NAME = "LITERAL_NAME"; //$NON-NLS-1$

    /** Prefix for all message codes of this class. */
    String MSGCODE_PREFIX = "ENUMLITERALNAMEATTRIBUTE-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the chosen default value provider attribute does not
     * exist.
     */
    String MSGCODE_ENUM_LITERAL_NAME_ATTRIBUTE_DEFAULT_VALUE_PROVIDER_ATTRIBUTE_DOES_NOT_EXIST = MSGCODE_PREFIX
            + "EnumLiteralNameAttributeDefaultValueProviderAttributeDoesNotExist"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the chosen default value provider attribute is not
     * of data type <code>String</code>.
     */
    String MSGCODE_ENUM_LITERAL_NAME_ATTRIBUTE_DEFAULT_VALUE_PROVIDER_ATTRIBUTE_NOT_OF_DATATYPE_STRING = MSGCODE_PREFIX
            + "EnumLiteralNameAttributeDefaultValueProviderAttributeNotOfDatatypeString"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this <code>IEnumLiteralNameAttribute</code> is not
     * required by the <code>IEnumType</code> it belongs to and therefore should be deleted.
     */
    String MSGCODE_ENUM_LITERAL_NAME_ATTRIBUTE_NOT_NEEDED = MSGCODE_PREFIX
            + "EnumLiteralNameAttributeAttributeNotNeeded"; //$NON-NLS-1$

    /**
     * Sets the default value provider attribute.
     * 
     * @param defaultValueProviderAttributeName The unqualified name of the
     *            <code>IEnumAttribute</code> that shall be used as default value provider attribute
     *            for the enumeration literals.
     * 
     * @throws NullPointerException If <code>defaultValueProviderAttributeName</code> is
     *             <code>null</code>.
     */
    void setDefaultValueProviderAttribute(String defaultValueProviderAttributeName);

    /**
     * Returns the name of the <code>IEnumAttribute</code> currently being used as default value
     * provider attribute for enumeration literals.
     * <p>
     * Returns an empty <code>String</code> if there is no default value provider attribute and
     * never returns <code>null</code>.
     */
    String getDefaultValueProviderAttribute();

}
