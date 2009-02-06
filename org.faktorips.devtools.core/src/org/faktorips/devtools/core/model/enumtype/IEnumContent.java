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

package org.faktorips.devtools.core.model.enumtype;

/**
 * <p>
 * This object type is used when the values for a Faktor-IPS enumeration shall not be defined
 * directly in the enum type itself but separate from it by the product side.
 * </p>
 * <p>
 * An enum content always refers to a specific enum type which defines the structure of the
 * enumeration.
 * </p>
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public interface IEnumContent extends IEnumValueContainer {

    /** The xml tag for this ips object. */
    public final static String XML_TAG = "EnumContent"; //$NON-NLS-1$

    /** Name of the enumType property. */
    public final static String PROPERTY_ENUM_TYPE = "enumType"; //$NON-NLS-1$

    /** Prefix for all message codes of this class. */
    public final static String MSGCODE_PREFIX = "ENUMCONTENT-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the enum type this enum content is built upon is not
     * specified.
     */
    public final static String MSGCODE_ENUM_CONTENT_ENUM_TYPE_MISSING = MSGCODE_PREFIX + "EnumContentEnumTypeMissing"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the enum type this enum content is built upon does
     * not exist.
     */
    public final static String MSGCODE_ENUM_CONTENT_ENUM_TYPE_DOES_NOT_EXIST = MSGCODE_PREFIX
            + "EnumContentEnumTypeDoesNotExist"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the enum type this enum content is built upon is
     * abstract.
     */
    public final static String MSGCODE_ENUM_CONTENT_ENUM_TYPE_IS_ABSTRACT = MSGCODE_PREFIX
            + "EnumContentEnumTypeIsAbstract"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the values of the enum type this enum content refers
     * to are defined in the model instead inside of a separate enum content object.
     */
    public final static String MSGCODE_ENUM_CONTENT_VALUES_ARE_PART_OF_MODEL = MSGCODE_PREFIX
            + "EnumContentValuesArePartOfModel"; //$NON-NLS-1$

    /**
     * Sets the enum type this enum content is based upon.
     * 
     * @param enumType The qualified name of the enum type this enum content shall be based upon.
     * 
     * @throws NullPointerException If enumType is <code>null</code>.
     */
    public void setEnumType(String enumType);

    /**
     * Returns the enum type this enum content is based upon.
     * 
     * @return A string representing the qualified name of the enum type this enum content is based
     *         upon.
     */
    public String getEnumType();

}
