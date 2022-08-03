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

import java.util.List;

import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.IIpsMetaObject;
import org.faktorips.devtools.model.IPartReference;
import org.faktorips.devtools.model.ipsobject.IDeprecation;

/**
 * An <code>IEnumContent</code> is used when the values for a Faktor-IPS enumeration shall not be
 * defined directly in the <code>IEnumType</code> itself but separate from it as product content.
 * <p>
 * An <code>IEnumContent</code> always refers to a specific <code>IEnumType</code> which defines the
 * structure of the enumeration.
 * 
 * @see IEnumType
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public interface IEnumContent extends IIpsMetaObject, IEnumValueContainer {

    /** The XML tag for this IPS object. */
    String XML_TAG = "EnumContent"; //$NON-NLS-1$

    /** Name of the <code>enumType</code> property. */
    String PROPERTY_ENUM_TYPE = "enumType"; //$NON-NLS-1$

    /** Prefix for all message codes of this class. */
    String MSGCODE_PREFIX = "ENUMCONTENT-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the <code>IEnumType</code> this
     * <code>IEnumContent</code> is built upon is not specified.
     */
    String MSGCODE_ENUM_CONTENT_ENUM_TYPE_MISSING = MSGCODE_PREFIX + "EnumContentEnumTypeMissing"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the <code>IEnumType</code> this
     * <code>IEnumContent</code> is built upon does not exist.
     */
    String MSGCODE_ENUM_CONTENT_ENUM_TYPE_DOES_NOT_EXIST = MSGCODE_PREFIX
            + "EnumContentEnumTypeDoesNotExist"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the <code>IEnumType</code> this
     * <code>IEnumContent</code> is built upon is abstract.
     */
    String MSGCODE_ENUM_CONTENT_ENUM_TYPE_IS_ABSTRACT = MSGCODE_PREFIX
            + "EnumContentEnumTypeIsAbstract"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the values of the <code>IEnumType</code> this
     * <code>IEnumContent</code> refers to are defined in the type itself instead of inside a
     * separate <code>IEnumContent</code>.
     */
    String MSGCODE_ENUM_CONTENT_VALUES_ARE_PART_OF_TYPE = MSGCODE_PREFIX
            + "EnumContentValuesArePartOfType"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the number of referenced
     * <code>IEnumAttribute</code>s does not correspond to the number of
     * <code>IEnumAttribute</code>s defined in the referenced <code>IEnumType</code>.
     */
    String MSGCODE_ENUM_CONTENT_REFERENCED_ENUM_ATTRIBUTES_COUNT_INVALID = MSGCODE_PREFIX
            + "EnumContentReferencedEnumAttributesCountInvalid"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the names of the referenced
     * <code>IEnumAttribute</code>s as stored in this <code>IEnumContent</code> do not match the
     * names of the <code>IEnumAttribute</code> s as defined in the base <code>IEnumType</code>.
     */
    String MSGCODE_ENUM_CONTENT_REFERENCED_ENUM_ATTRIBUTE_NAMES_INVALID = MSGCODE_PREFIX
            + "EnumContentReferencedEnumAttributeNamesInvalid"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the ordering of the referenced
     * <code>IEnumAttribute</code>s as stored in this <code>IEnumContent</code> does not match the
     * ordering of the <code>IEnumAttribute</code>s as defined in the base <code>IEnumType</code>.
     */
    String MSGCODE_ENUM_CONTENT_REFERENCED_ENUM_ATTRIBUTE_ORDERING_INVALID = MSGCODE_PREFIX
            + "EnumContentReferencedEnumAttributeOrderingInvalid"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the package fragment this <code>IEnumContent</code>
     * is stored in is not correct due to the specification in the referenced
     * <code>IEnumType</code>.
     */
    String MSGCODE_ENUM_CONTENT_NAME_NOT_CORRECT = MSGCODE_PREFIX + "EnumContentNameNotCorrect"; //$NON-NLS-1$

    /**
     * Validation message code that indicates that the enum type is deprecated. A
     * replacement/migration strategy should be documented in its
     * {@link IDeprecation#getDescriptions() deprecation descriptions}.
     */
    String MSGCODE_DEPRECATED_ENUM_TYPE = MSGCODE_PREFIX + "DeprecatedEnumType"; //$NON-NLS-1$

    /**
     * Sets the <code>IEnumType</code> this <code>IEnumContent</code> is based upon.
     * <p>
     * If the new <code>IEnumType</code> can be found then the <code>IPartReference</code>s will be
     * updated to match the <code>IEnumAttribute</code>s of the new <code>IEnumType</code>.
     * 
     * @param enumType The qualified name of the <code>IEnumType</code> this
     *            <code>IEnumContent</code> shall be based upon.
     * 
     * @throws IpsException If an error occurs while searching for the new <code>IEnumType</code>.
     * @throws NullPointerException If <code>enumType</code> is <code>null</code>.
     */
    void setEnumType(String enumType) throws IpsException;

    /**
     * Returns the qualified name of the <code>IEnumType</code> this <code>IEnumContent</code> is
     * based upon.
     */
    String getEnumType();

    /**
     * Returns <code>true</code> if this <code>IEnumContent</code> is inconsistent with the model
     * and needs to be fixed by the user, <code>false</code> otherwise.
     * 
     * @throws IpsException If an error occurs during the validation.
     */
    boolean isFixToModelRequired() throws IpsException;

    /**
     * Returns a list containing all <code>IPartReference</code>s that belong to this
     * <code>IEnumContent</code>.
     * <p>
     * Returns an empty list if there are none, never returns <code>null</code>.
     */
    List<IPartReference> getEnumAttributeReferences();

    /**
     * Returns the <code>IPartReference</code> with the given name or <code>null</code> if there is
     * no such reference in this <code>IEnumContent</code>.
     * 
     * @param name The name of the <code>IPartReference</code> to obtain.
     * 
     * @throws NullPointerException If <code>name</code> is <code>null</code>.
     */
    IPartReference getEnumAttributeReference(String name);

    /**
     * Returns the number of <code>IEnumAttribute</code>s that are currently referenced by this
     * <code>IEnumContent</code>.
     */
    int getEnumAttributeReferencesCount();

}
