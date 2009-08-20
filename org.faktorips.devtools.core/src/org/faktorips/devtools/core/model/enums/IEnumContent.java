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
 * This IPS object type is used when the values for a Faktor-IPS enumeration shall not be defined
 * directly in the <tt>IEnumType</tt> itself but separate from it as product content.
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
    public final static String XML_TAG = "EnumContent"; //$NON-NLS-1$

    /** Name of the <tt>enumType</tt> property. */
    public final static String PROPERTY_ENUM_TYPE = "enumType"; //$NON-NLS-1$

    /** Name of the <tt>referencedEnumAttributesCount</tt> property. */
    public final static String PROPERTY_REFERENCED_ENUM_ATTRIBUTES_COUNT = "referencedEnumAttributesCount"; //$NON-NLS-1$

    /** Prefix for all message codes of this class. */
    public final static String MSGCODE_PREFIX = "ENUMCONTENT-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the <tt>IEnumType</tt> this <tt>IEnumContent</tt> is
     * built upon is not specified.
     */
    public final static String MSGCODE_ENUM_CONTENT_ENUM_TYPE_MISSING = MSGCODE_PREFIX + "EnumContentEnumTypeMissing"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the <tt>IEnumType</tt> this <tt>IEnumContent</tt> is
     * built upon does not exist.
     */
    public final static String MSGCODE_ENUM_CONTENT_ENUM_TYPE_DOES_NOT_EXIST = MSGCODE_PREFIX
            + "EnumContentEnumTypeDoesNotExist"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the <tt>IEnumType</tt> this <tt>IEnumContent</tt> is
     * built upon is abstract.
     */
    public final static String MSGCODE_ENUM_CONTENT_ENUM_TYPE_IS_ABSTRACT = MSGCODE_PREFIX
            + "EnumContentEnumTypeIsAbstract"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the values of the <tt>IEnumType</tt> this
     * <tt>IEnumContent</tt> refers to are defined in the type itself instead of inside a separate
     * <tt>IEnumContent</tt>.
     */
    public final static String MSGCODE_ENUM_CONTENT_VALUES_ARE_PART_OF_TYPE = MSGCODE_PREFIX
            + "EnumContentValuesArePartOfType"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the number of referenced <tt>IEnumAttribute</tt>s
     * does not correspond to the number of <tt>IEnumAttribute</tt>s defined in the referenced
     * <tt>IEnumType</tt>.
     */
    public final static String MSGCODE_ENUM_CONTENT_REFERENCED_ENUM_ATTRIBUTES_COUNT_INVALID = MSGCODE_PREFIX
            + "EnumContentReferencedEnumAttributesCountInvalid"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the package fragment this <tt>IEnumContent</tt> is
     * stored in is not correct due to the specification in the referenced <tt>IEnumType</tt>.
     */
    public final static String MSGCODE_ENUM_CONTENT_NAME_NOT_CORRECT = MSGCODE_PREFIX + "EnumContentNameNotCorrect"; //$NON-NLS-1$

    /**
     * Sets the <tt>IEnumType</tt> this <tt>IEnumContent</tt> is based upon.
     * <p>
     * If the new <tt>IEnumType</tt> can be found then the number of referenced
     * <tt>IEnumAttribute</tt>s will be updated to match the number of <tt>IEnumAttribute</tt>s of
     * the new <tt>IEnumType</tt>.
     * 
     * @param enumType The qualified name of the <tt>IEnumType</tt> this <tt>IEnumContent</tt> shall
     *            be based upon.
     * 
     * @throws CoreException If an error occurs while searching for the new <tt>IEnumType</tt>.
     * @throws NullPointerException If <tt>enumType</tt> is <tt>null</tt>.
     */
    public void setEnumType(String enumType) throws CoreException;

    /**
     * Returns the number of <tt>IEnumAttribute</tt>s that are to be referenced by this
     * <tt>IEnumContent</tt>.
     */
    public int getReferencedEnumAttributesCount();

    /**
     * Returns the qualified name of the <tt>IEnumType</tt> this <tt>IEnumContent</tt> is based
     * upon.
     */
    public String getEnumType();

}
