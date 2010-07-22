/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
    public final static String XML_TAG = "EnumLiteralNameAttribute"; //$NON-NLS-1$

    /** Name of the <tt>defaultValueProviderAttribute</tt> property. */
    public final static String PROPERTY_DEFAULT_VALUE_PROVIDER_ATTRIBUTE = "defaultValueProviderAttribute"; //$NON-NLS-1$

    /** The default name for a <tt>IEnumLiteralNameAttribute</tt>. */
    public final static String DEFAULT_NAME = "LITERAL_NAME"; //$NON-NLS-1$

    /** Prefix for all message codes of this class. */
    public final static String MSGCODE_PREFIX = "ENUMLITERALNAMEATTRIBUTE-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the chosen default value provider attribute does not
     * exist.
     */
    public final static String MSGCODE_ENUM_LITERAL_NAME_ATTRIBUTE_DEFAULT_VALUE_PROVIDER_ATTRIBUTE_DOES_NOT_EXIST = MSGCODE_PREFIX
            + "EnumLiteralNameAttributeDefaultValueProviderAttributeDoesNotExist"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the chosen default value provider attribute is not
     * of data type <tt>String</tt>.
     */
    public final static String MSGCODE_ENUM_LITERAL_NAME_ATTRIBUTE_DEFAULT_VALUE_PROVIDER_ATTRIBUTE_NOT_OF_DATATYPE_STRING = MSGCODE_PREFIX
            + "EnumLiteralNameAttributeDefaultValueProviderAttributeNotOfDatatypeString"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the chosen default value provider attribute is not
     * unique.
     */
    public final static String MSGCODE_ENUM_LITERAL_NAME_ATTRIBUTE_DEFAULT_VALUE_PROVIDER_ATTRIBUTE_NOT_UNIQUE = MSGCODE_PREFIX
            + "EnumLiteralNameAttributeDefaultValueProviderAttributeNotUnique"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this <tt>IEnumLiteralNameAttribute</tt> is not
     * required by the <tt>IEnumType</tt> it belongs to and therefore should be deleted.
     */
    public final static String MSGCODE_ENUM_LITERAL_NAME_ATTRIBUTE_NOT_NEEDED = MSGCODE_PREFIX
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
