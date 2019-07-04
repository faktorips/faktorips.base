/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.enums;

/**
 * Each <tt>IEnumType</tt> that contains values (that means it is not abstract and it does define
 * its values in the model) needs to specify exactly one <tt>IEnumLiteralNameAttribute</tt>.
 * <p>
 * The value of this attribute specifies the name of the enumeration literal that will be used in
 * the generated source code.
 * <p>
 * The <tt>IEnumLiteralNameAttribute</tt> can refer to another <tt>IEnumAttribute</tt> being its
 * provider for a default value.
 * <p>
 * <tt>IEnumLiteralNameAttribute</tt>s are always of data type <tt>String</tt> and <tt>unique</tt>.
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.4
 */
public interface IEnumLiteralNameAttribute extends IEnumAttribute {

    /** The XML tag for this <tt>IpsObjectPart</tt>. */
    public static final String XML_TAG = "EnumLiteralNameAttribute"; //$NON-NLS-1$

    /** Name of the <tt>defaultValueProviderAttribute</tt> property. */
    public static final String PROPERTY_DEFAULT_VALUE_PROVIDER_ATTRIBUTE = "defaultValueProviderAttribute"; //$NON-NLS-1$

    /** The default name for a <tt>IEnumLiteralNameAttribute</tt>. */
    public static final String DEFAULT_NAME = "LITERAL_NAME"; //$NON-NLS-1$

    /** Prefix for all message codes of this class. */
    public static final String MSGCODE_PREFIX = "ENUMLITERALNAMEATTRIBUTE-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the chosen default value provider attribute does not
     * exist.
     */
    public static final String MSGCODE_ENUM_LITERAL_NAME_ATTRIBUTE_DEFAULT_VALUE_PROVIDER_ATTRIBUTE_DOES_NOT_EXIST = MSGCODE_PREFIX
            + "EnumLiteralNameAttributeDefaultValueProviderAttributeDoesNotExist"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the chosen default value provider attribute is not
     * of data type <tt>String</tt>.
     */
    public static final String MSGCODE_ENUM_LITERAL_NAME_ATTRIBUTE_DEFAULT_VALUE_PROVIDER_ATTRIBUTE_NOT_OF_DATATYPE_STRING = MSGCODE_PREFIX
            + "EnumLiteralNameAttributeDefaultValueProviderAttributeNotOfDatatypeString"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this <tt>IEnumLiteralNameAttribute</tt> is not
     * required by the <tt>IEnumType</tt> it belongs to and therefore should be deleted.
     */
    public static final String MSGCODE_ENUM_LITERAL_NAME_ATTRIBUTE_NOT_NEEDED = MSGCODE_PREFIX
            + "EnumLiteralNameAttributeAttributeNotNeeded"; //$NON-NLS-1$

    /**
     * Sets the default value provider attribute.
     * 
     * @param defaultValueProviderAttributeName The unqualified name of the <tt>IEnumAttribute</tt>
     *            that shall be used as default value provider attribute for the enumeration
     *            literals.
     * 
     * @throws NullPointerException If <tt>defaultValueProviderAttributeName</tt> is <tt>null</tt>.
     */
    public void setDefaultValueProviderAttribute(String defaultValueProviderAttributeName);

    /**
     * Returns the name of the <tt>IEnumAttribute</tt> currently being used as default value
     * provider attribute for enumeration literals.
     * <p>
     * Returns an empty <tt>String</tt> if there is no default value provider attribute and never
     * returns <tt>null</tt>.
     */
    public String getDefaultValueProviderAttribute();

}
