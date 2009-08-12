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

import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * An enum value represents a complete set of enum attribute values for an enum type. Enum values
 * are always contained in an enum value container.
 * 
 * @see org.faktorips.devtools.core.model.enums.IEnumType
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public interface IEnumValue extends IIpsObjectPart {

    /** The XML tag for this IPS object part. */
    public final static String XML_TAG = "EnumValue"; //$NON-NLS-1$

    /** Prefix for all message codes of this class. */
    public final static String MSGCODE_PREFIX = "ENUMVALUE-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that there are not as many enum attribute values as enum
     * attributes in the enum type.
     */
    public final static String MSGCODE_ENUM_VALUE_NUMBER_ATTRIBUTE_VALUES_DOES_NOT_CORRESPOND_TO_NUMBER_ATTRIBUTES = MSGCODE_PREFIX
            + "EnumValueNumberAttributeValuesDoesNotCorrespondToNumberAttributes"; //$NON-NLS-1$

    /** Returns a list containing all enum attribute values. */
    public List<IEnumAttributeValue> getEnumAttributeValues();

    /**
     * Creates a new enum attribute value and returns a reference to it.
     * 
     * @throws CoreException If the enum type this enum value is based upon cannot be found.
     */
    public IEnumAttributeValue newEnumAttributeValue() throws CoreException;

    /**
     * Returns the enum value container this enum value is being stored in.
     * <p>
     * This is a shortcut for: <tt>(IEnumValueContainer)this.getParent();</tt>
     */
    public IEnumValueContainer getEnumValueContainer();

    /**
     * Searches and returns the enum attribute value that refers to the given enum attribute of the
     * enum type this enum value refers to.
     * <p>
     * Returns <tt>null</tt> if none can be found, if the given enum attribute does not exist in the
     * referenced enum type or if the provided enum attribute is <tt>null</tt>.
     * 
     * @param ipsProject The ips project which ips object path is used for the search of the
     *            referenced enum type. This is not necessarily the project this enum attribute is
     *            part of.
     * @param enumAttribute The enum attribute to obtain the enum attribute value for.
     * 
     * @throws CoreException If an error occurs while searching the given ips project for the
     *             referenced enum type.
     * @throws NullPointerException If <tt>ipsProject</tt> is <tt>null</tt>.
     */
    public IEnumAttributeValue findEnumAttributeValue(IIpsProject ipsProject, IEnumAttribute enumAttribute)
            throws CoreException;

    /** Returns how many enum attribute values this enum value is currently containing. */
    public int getEnumAttributeValuesCount();

    /**
     * Moves the given enum attribute value up or down by 1 in the containing list and returns its
     * new index.
     * <p>
     * If the enum attribute value is already the first / last one then nothing will be done.
     * 
     * @param enumAttributeValue The enum attribute value that is to be moved.
     * @param up Flag indicating whether to move upwards (<tt>true</tt>) or downwards (
     *            <tt>false</tt>).
     * 
     * @throws NullPointerException If <tt>enumAttributeValue</tt> is <tt>null</tt>.
     * @throws NoSuchElementException If the given enum attribute value is not a part of this enum
     *             value.
     */
    public int moveEnumAttributeValue(IEnumAttributeValue enumAttributeValue, boolean up);

    /**
     * Sets the value of the <tt>IEnumAttributeValue</tt> that refers to the given
     * <tt>IEnumAttribute</tt>.
     * <p>
     * This version of <tt>setEnumAttributeValue</tt> offers best performance.
     * 
     * @see #setEnumAttributeValue(String, String)
     * @see #setEnumAttributeValue(int, String)
     * 
     * @param enumAttribute The enum attribute for that the value shall be set.
     * @param value The new value. May also be <tt>null</tt>.
     * 
     * @throws CoreException If an error occurs while searching for the <tt>IEnumAttributeValue</tt>
     *             that refers to the given enum attribute.
     * @throws NullPointerException If <tt>enumAttribute</tt> is <tt>null</tt>.
     */
    public void setEnumAttributeValue(IEnumAttribute enumAttribute, String value) throws CoreException;

    /**
     * Sets the value of the <tt>IEnumAttributeValue</tt> that refers to the <tt>IEnumAttribute</tt>
     * identified by the given name.
     * 
     * @param enumAttributeName The name of the enum attribute for that the value shall be set.
     * @param value The new value. May also be <tt>null</tt>.
     * 
     * @see #setEnumAttributeValue(IEnumAttribute, String)
     * @see #setEnumAttributeValue(int, String)
     * 
     * @throws CoreException If an error occurs while searching for the enum attribute identified by
     *             the given name or while searching for the <tt>IEnumAttributeValue</tt> that
     *             refers to this enum attribute.
     * @throws NullPointerException If <tt>enumAttributeName</tt> is <tt>null</tt>.
     * @throws NoSuchElementException If there is no enum attribute with the given name in the
     *             parent enum type.
     */
    public void setEnumAttributeValue(String enumAttributeName, String value) throws CoreException;

    /**
     * Sets the value of the <tt>IEnumAttributeValue</tt> identified by the given index.
     * <p>
     * <strong>Attention:</strong> Use this operation only if you must because the ordering of the
     * <tt>IEnumAttributeValue</tt>s changes often.
     * 
     * @see #setEnumAttributeValue(IEnumAttribute, String)
     * @see #setEnumAttributeValue(String, String)
     * 
     * @param enumAttributeValueIndex The index of the enum attribute value which value shall be
     *            set.
     * @param value The new value. May also be <tt>null</tt>.
     * 
     * @throws IndexOutOfBoundsException If the given index is out of bounds.
     */
    public void setEnumAttributeValue(int enumAttributeValueIndex, String value);

    /**
     * Returns a list containing all <tt>IEnumAttributeValue</tt>s that refer to the given unique
     * <tt>IEnumAttribute</tt>s.
     * <p>
     * Returns an empty list if none could be found (either none exist or it was not possible to
     * find the referenced enum type). Never returns <tt>null</tt>.
     * 
     * @param uniqueEnumAttributes A list containing all <tt>IEnumAttribute</tt>s for which the
     *            <tt>IEnumAttributeValue</tt>s shall be returned.
     * @param ipsProject The ips project which ips object path is used for the search of the
     *            referenced enum type. This is not necessarily the project this enum attribute is
     *            part of.
     * 
     * @throws CoreException If an error occurs while searching for the referenced
     *             <tt>IEnumType</tt>.
     * @throws NullPointerException If any parameter is <tt>null</tt>.
     */
    public List<IEnumAttributeValue> findUniqueEnumAttributeValues(List<IEnumAttribute> uniqueEnumAttributes,
            IIpsProject ipsProject) throws CoreException;

    /**
     * Returns the index of the given enum attribute value in this enum value.
     * 
     * @param enumAttributeValue The enum attribute value to obtain its index for.
     * 
     * @throws NullPointerException If <tt>enumAttributeValue</tt> is <tt>null</tt>.
     * @throws NoSuchElementException If the given enum attribute value can not be found in this
     *             enum value.
     */
    public int getIndexOfEnumAttributeValue(IEnumAttributeValue enumAttributeValue);

}
