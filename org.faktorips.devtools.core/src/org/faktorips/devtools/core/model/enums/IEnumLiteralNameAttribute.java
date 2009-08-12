/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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
 * The <tt>IEnumLiteralNameAttribute</tt> refers to another enum attribute being its provider for a
 * default value. The value will be transformed a little bit to fit the java conventions about
 * enumeration literals.
 * <p>
 * Example: Imagine an enumeration type called <i>Genders</i> with an enum attribute <i>label</i>.
 * For an enum value the value for the label might be <i>male</i>. If this enum attribute would be
 * chosen as the default literal provider the enumeration literal would be <i>MALE</i>.
 * <p>
 * <tt>IEnumLiteralNameAttribute</tt>s are always of datatype <tt>String</tt> and <tt>unique</tt>.
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
     * of datatype <tt>String</tt>.
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
     * Sets the default value provider attribute.
     * 
     * @param defaultValueProviderAttributeName The unqualified name of the enum attribute that
     *            shall be used as default value provider for the enumeration literals.
     * 
     * @throws NullPointerException If <tt>defaultValueProviderAttributeName</tt> is <tt>null</tt>.
     */
    public void setDefaultValueProviderAttribute(String defaultValueProviderAttributeName);

    /**
     * Returns the name of the enum attribute currently being used as default value provider for
     * enumeration literals.
     */
    public String getDefaultValueProviderAttribute();

}
