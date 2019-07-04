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

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.IIpsMetaObject;
import org.faktorips.devtools.core.model.IPartReference;

/**
 * An <tt>IEnumContent</tt> is used when the values for a Faktor-IPS enumeration shall not be
 * defined directly in the <tt>IEnumType</tt> itself but separate from it as product content.
 * <p>
 * An <tt>IEnumContent</tt> always refers to a specific <tt>IEnumType</tt> which defines the
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
    public static final String XML_TAG = "EnumContent"; //$NON-NLS-1$

    /** Name of the <tt>enumType</tt> property. */
    public static final String PROPERTY_ENUM_TYPE = "enumType"; //$NON-NLS-1$

    /** Prefix for all message codes of this class. */
    public static final String MSGCODE_PREFIX = "ENUMCONTENT-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the <tt>IEnumType</tt> this <tt>IEnumContent</tt> is
     * built upon is not specified.
     */
    public static final String MSGCODE_ENUM_CONTENT_ENUM_TYPE_MISSING = MSGCODE_PREFIX + "EnumContentEnumTypeMissing"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the <tt>IEnumType</tt> this <tt>IEnumContent</tt> is
     * built upon does not exist.
     */
    public static final String MSGCODE_ENUM_CONTENT_ENUM_TYPE_DOES_NOT_EXIST = MSGCODE_PREFIX
            + "EnumContentEnumTypeDoesNotExist"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the <tt>IEnumType</tt> this <tt>IEnumContent</tt> is
     * built upon is abstract.
     */
    public static final String MSGCODE_ENUM_CONTENT_ENUM_TYPE_IS_ABSTRACT = MSGCODE_PREFIX
            + "EnumContentEnumTypeIsAbstract"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the values of the <tt>IEnumType</tt> this
     * <tt>IEnumContent</tt> refers to are defined in the type itself instead of inside a separate
     * <tt>IEnumContent</tt>.
     */
    public static final String MSGCODE_ENUM_CONTENT_VALUES_ARE_PART_OF_TYPE = MSGCODE_PREFIX
            + "EnumContentValuesArePartOfType"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the number of referenced <tt>IEnumAttribute</tt>s
     * does not correspond to the number of <tt>IEnumAttribute</tt>s defined in the referenced
     * <tt>IEnumType</tt>.
     */
    public static final String MSGCODE_ENUM_CONTENT_REFERENCED_ENUM_ATTRIBUTES_COUNT_INVALID = MSGCODE_PREFIX
            + "EnumContentReferencedEnumAttributesCountInvalid"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the names of the referenced <tt>IEnumAttribute</tt>s
     * as stored in this <tt>IEnumContent</tt> do not match the names of the <tt>IEnumAttribute</tt>
     * s as defined in the base <tt>IEnumType</tt>.
     */
    public static final String MSGCODE_ENUM_CONTENT_REFERENCED_ENUM_ATTRIBUTE_NAMES_INVALID = MSGCODE_PREFIX
            + "EnumContentReferencedEnumAttributeNamesInvalid"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the ordering of the referenced
     * <tt>IEnumAttribute</tt>s as stored in this <tt>IEnumContent</tt> does not match the ordering
     * of the <tt>IEnumAttribute</tt>s as defined in the base <tt>IEnumType</tt>.
     */
    public static final String MSGCODE_ENUM_CONTENT_REFERENCED_ENUM_ATTRIBUTE_ORDERING_INVALID = MSGCODE_PREFIX
            + "EnumContentReferencedEnumAttributeOrderingInvalid"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the package fragment this <tt>IEnumContent</tt> is
     * stored in is not correct due to the specification in the referenced <tt>IEnumType</tt>.
     */
    public static final String MSGCODE_ENUM_CONTENT_NAME_NOT_CORRECT = MSGCODE_PREFIX + "EnumContentNameNotCorrect"; //$NON-NLS-1$

    /**
     * Sets the <tt>IEnumType</tt> this <tt>IEnumContent</tt> is based upon.
     * <p>
     * If the new <tt>IEnumType</tt> can be found then the <tt>IPartReference</tt>s will be updated
     * to match the <tt>IEnumAttribute</tt>s of the new <tt>IEnumType</tt>.
     * 
     * @param enumType The qualified name of the <tt>IEnumType</tt> this <tt>IEnumContent</tt> shall
     *            be based upon.
     * 
     * @throws CoreException If an error occurs while searching for the new <tt>IEnumType</tt>.
     * @throws NullPointerException If <tt>enumType</tt> is <tt>null</tt>.
     */
    public void setEnumType(String enumType) throws CoreException;

    /**
     * Returns the qualified name of the <tt>IEnumType</tt> this <tt>IEnumContent</tt> is based
     * upon.
     */
    public String getEnumType();

    /**
     * Returns <tt>true</tt> if this <tt>IEnumContent</tt> is inconsistent with the model and needs
     * to be fixed by the user, <tt>false</tt> otherwise.
     * 
     * @throws CoreException If an error occurs during the validation.
     */
    public boolean isFixToModelRequired() throws CoreException;

    /**
     * Returns a list containing all <tt>IPartReference</tt>s that belong to this
     * <tt>IEnumContent</tt>.
     * <p>
     * Returns an empty list if there are none, never returns <tt>null</tt>.
     */
    public List<IPartReference> getEnumAttributeReferences();

    /**
     * Returns the <tt>IPartReference</tt> with the given name or <tt>null</tt> if there is no such
     * reference in this <tt>IEnumContent</tt>.
     * 
     * @param name The name of the <tt>IPartReference</tt> to obtain.
     * 
     * @throws NullPointerException If <tt>name</tt> is <tt>null</tt>.
     */
    public IPartReference getEnumAttributeReference(String name);

    /**
     * Returns the number of <tt>IEnumAttribute</tt>s that are currently referenced by this
     * <tt>IEnumContent</tt>.
     */
    public int getEnumAttributeReferencesCount();

}
