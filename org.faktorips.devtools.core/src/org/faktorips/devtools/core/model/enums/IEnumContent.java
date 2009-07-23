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

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.IIpsMetaObject;

/**
 * This ips object type is used when the values for a Faktor-IPS enum shall not be defined directly
 * in the enum type itself but separate from it as product content.
 * <p>
 * An enum content always refers to a specific enum type which defines the structure of the
 * enumeration.
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public interface IEnumContent extends IIpsMetaObject, IEnumValueContainer {

    /** The xml tag for this ips object. */
    public final static String XML_TAG = "EnumContent"; //$NON-NLS-1$

    /** Name of the <code>enumType</code> property. */
    public final static String PROPERTY_ENUM_TYPE = "enumType"; //$NON-NLS-1$

    /** Name of the <code>referencedEnumAttributesCount</code> property. */
    public final static String PROPERTY_REFERENCED_ENUM_ATTRIBUTES_COUNT = "referencedEnumAttributesCount"; //$NON-NLS-1$

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
     * to are defined in the type itself instead inside of a separate enum content object.
     */
    public final static String MSGCODE_ENUM_CONTENT_VALUES_ARE_PART_OF_TYPE = MSGCODE_PREFIX
            + "EnumContentValuesArePartOfType"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the number of referenced enum attributes does not
     * correspond to the number of enum attributes defined in the referenced enum type.
     */
    public final static String MSGCODE_ENUM_CONTENT_REFERENCED_ENUM_ATTRIBUTES_COUNT_INVALID = MSGCODE_PREFIX
            + "EnumContentReferencedEnumAttributesCountInvalid"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the package fragment this enum content is stored in
     * is not correct due to the specification in the referenced enum type.
     */
    public final static String MSGCODE_ENUM_CONTENT_NAME_NOT_CORRECT = MSGCODE_PREFIX
            + "EnumContentNameNotCorrect"; //$NON-NLS-1$

    /**
     * Sets the enum type this enum content is based upon.
     * <p>
     * If the new enum type can be found then the number of referenced enum attributes will be
     * updated to match the number of enum attributes of the new enum type.
     * 
     * @param enumType The qualified name of the enum type this enum content shall be based upon.
     * 
     * @throws CoreException If an error occurs while searching for the new enum type.
     * @throws NullPointerException If <code>enumType</code> is <code>null</code>.
     */
    public void setEnumType(String enumType) throws CoreException;

    /**
     * Returns the number of enum attributes to be referenced by this enum content.
     */
    public int getReferencedEnumAttributesCount();

    /**
     * Returns the qualified name of the enum type this enum content is based upon.
     */
    public String getEnumType();

}
